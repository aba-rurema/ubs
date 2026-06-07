package com.ubs.service;

import com.ubs.config.BillingProperties;
import com.ubs.dto.bill.BillGenerateRequest;
import com.ubs.dto.bill.BillResponse;
import com.ubs.entity.Bill;
import com.ubs.entity.BillStatus;
import com.ubs.entity.Customer;
import com.ubs.entity.Meter;
import com.ubs.entity.MeterReading;
import com.ubs.entity.MeterStatus;
import com.ubs.entity.MeterType;
import com.ubs.exception.BusinessRuleViolationException;
import com.ubs.exception.DuplicateResourceException;
import com.ubs.exception.ResourceNotFoundException;
import com.ubs.repository.BillRepository;
import com.ubs.repository.MeterReadingRepository;
import com.ubs.repository.PaymentRepository;
import com.ubs.security.CustomUserDetails;
import com.ubs.security.CustomerAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class BillService {

	private static final int MONEY_SCALE = 2;
	private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
	private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(MONEY_SCALE, ROUNDING);
	private static final List<BillStatus> OVERDUE_ELIGIBLE_STATUSES = List.of(
			BillStatus.APPROVED, BillStatus.PARTIALLY_PAID);

	private final BillRepository billRepository;
	private final MeterReadingRepository meterReadingRepository;
	private final PaymentRepository paymentRepository;
	private final BillingProperties billingProperties;
	private final TariffService tariffService;
	private final BillingNotificationService billingNotificationService;
	private final CustomerAccessService customerAccessService;

	public BillService(BillRepository billRepository,
					   MeterReadingRepository meterReadingRepository,
					   PaymentRepository paymentRepository,
					   BillingProperties billingProperties,
					   TariffService tariffService,
					   BillingNotificationService billingNotificationService,
					   CustomerAccessService customerAccessService) {
		this.billRepository = billRepository;
		this.meterReadingRepository = meterReadingRepository;
		this.paymentRepository = paymentRepository;
		this.billingProperties = billingProperties;
		this.tariffService = tariffService;
		this.billingNotificationService = billingNotificationService;
		this.customerAccessService = customerAccessService;
	}

	@Transactional
	public BillResponse generate(BillGenerateRequest request) {
		return generateFromReading(request.meterReadingId());
	}

	@Transactional
	public BillResponse generateFromReading(Long meterReadingId) {
		MeterReading reading = findReadingOrThrow(meterReadingId);
		Meter meter = reading.getMeter();
		Customer customer = meter.getCustomer();

		validateMeterIsActive(meter);
		validateBillNotExistsForReading(reading.getId());
		validateBillNotExistsForPeriod(meter.getId(), reading.getReadingMonth(), reading.getReadingYear());

		BillCalculation calculation = calculateBillAmounts(reading, customer);

		Bill bill = Bill.builder()
				.billNumber(generateBillNumber(reading.getReadingYear(), reading.getReadingMonth()))
				.customer(customer)
				.meter(meter)
				.meterReading(reading)
				.consumption(reading.getConsumption())
				.unitRate(calculation.unitRate())
				.fixedCharges(calculation.fixedCharges())
				.baseAmount(calculation.baseAmount())
				.vatAmount(calculation.vatAmount())
				.penaltyAmount(calculation.penaltyAmount())
				.totalAmount(calculation.totalAmount())
				.amountPaid(ZERO)
				.balance(calculation.totalAmount())
				.status(BillStatus.PENDING)
				.billingMonth(reading.getReadingMonth())
				.billingYear(reading.getReadingYear())
				.dueDate(LocalDate.now().plusDays(billingProperties.dueDays()))
				.build();

		return toResponse(billRepository.save(bill));
	}

	@Transactional
	public BillResponse approve(Long id) {
		Bill bill = findBillOrThrow(id);

		if (bill.getStatus() != BillStatus.PENDING) {
			throw new BusinessRuleViolationException(
					"Only bills with PENDING status can be approved. Current status: " + bill.getStatus());
		}

		bill.setStatus(BillStatus.APPROVED);
		Bill savedBill = billRepository.save(bill);
		billingNotificationService.notifyBillApproved(savedBill);
		if (savedBill.getPenaltyAmount().compareTo(ZERO) > 0) {
			String period = formatPeriod(savedBill.getBillingMonth(), savedBill.getBillingYear());
			billingNotificationService.notifyPenaltyApplied(
					savedBill,
					savedBill.getPenaltyAmount(),
					"on your new bill for " + period + " because you have other overdue bills");
		}

		return toResponse(savedBill);
	}

	@Transactional(readOnly = true)
	public BillResponse getById(Long id, CustomUserDetails principal) {
		Bill bill = findBillOrThrow(id);
		customerAccessService.validateBillAccess(principal, bill);
		return toResponse(bill);
	}

	@Transactional(readOnly = true)
	public Page<BillResponse> getAll(Pageable pageable) {
		return billRepository.findAll(pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<BillResponse> getMyBills(CustomUserDetails principal, Pageable pageable) {
		Long customerId = customerAccessService.requireLinkedCustomerId(principal);
		return billRepository.findByCustomerId(customerId, pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<BillResponse> getByCustomerId(Long customerId, CustomUserDetails principal, Pageable pageable) {
		customerAccessService.validateCustomerOwnership(principal, customerId);
		return billRepository.findByCustomerId(customerId, pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<BillResponse> getByMeterId(Long meterId, Pageable pageable) {
		return billRepository.findByMeterId(meterId, pageable).map(this::toResponse);
	}

	@Transactional
	public void delete(Long id) {
		Bill bill = findBillOrThrow(id);

		if (paymentRepository.existsByBillId(id)) {
			throw new BusinessRuleViolationException("Cannot delete a bill that has recorded payments");
		}

		billRepository.delete(bill);
	}

	@Transactional
	public void processOverdueBills() {
		List<Bill> overdueCandidates = billRepository.findBillsEligibleForOverdue(
				LocalDate.now(), OVERDUE_ELIGIBLE_STATUSES);

		for (Bill bill : overdueCandidates) {
			TariffService.TariffPricing pricing = tariffService.resolvePricing(bill.getMeter().getMeterType());
			BigDecimal penalty = bill.getBalance()
					.multiply(pricing.penaltyPercentage())
					.divide(BigDecimal.valueOf(100), MONEY_SCALE, ROUNDING);

			bill.setPenaltyAmount(bill.getPenaltyAmount().add(penalty));
			bill.setTotalAmount(bill.getTotalAmount().add(penalty));
			bill.setBalance(bill.getBalance().add(penalty));
			bill.setStatus(BillStatus.OVERDUE);

			Bill savedBill = billRepository.save(bill);
			if (penalty.compareTo(ZERO) > 0) {
				billingNotificationService.notifyPenaltyApplied(
						savedBill,
						penalty,
						"because bill " + savedBill.getBillNumber() + " passed the due date ("
								+ savedBill.getDueDate() + ")");
			}
		}
	}

	private String formatPeriod(int month, int year) {
		return String.format("%02d/%d", month, year);
	}

	private BillCalculation calculateBillAmounts(MeterReading reading, Customer customer) {
		BigDecimal consumption = reading.getConsumption();
		MeterType meterType = reading.getMeter().getMeterType();
		TariffService.TariffPricing pricing = tariffService.resolvePricing(meterType);

		BigDecimal consumptionCharge = consumption.multiply(pricing.unitRate()).setScale(MONEY_SCALE, ROUNDING);
		BigDecimal baseAmount = consumptionCharge.add(pricing.fixedCharges()).setScale(MONEY_SCALE, ROUNDING);
		BigDecimal vatAmount = baseAmount
				.multiply(pricing.vatPercentage())
				.divide(BigDecimal.valueOf(100), MONEY_SCALE, ROUNDING);
		BigDecimal penaltyAmount = calculateNewBillPenalty(customer, baseAmount, pricing.penaltyPercentage());
		BigDecimal totalAmount = baseAmount.add(vatAmount).add(penaltyAmount).setScale(MONEY_SCALE, ROUNDING);

		return new BillCalculation(
				pricing.unitRate(),
				pricing.fixedCharges(),
				baseAmount,
				vatAmount,
				penaltyAmount,
				totalAmount);
	}

	private BigDecimal calculateNewBillPenalty(Customer customer,
											   BigDecimal baseAmount,
											   BigDecimal penaltyPercentage) {
		List<Bill> overdueBills = billRepository.findOverdueBills(
				customer.getId(), LocalDate.now(), List.of(BillStatus.OVERDUE));

		if (overdueBills.isEmpty()) {
			return ZERO;
		}

		return baseAmount
				.multiply(penaltyPercentage)
				.divide(BigDecimal.valueOf(100), MONEY_SCALE, ROUNDING);
	}

	private void validateMeterIsActive(Meter meter) {
		if (meter.getStatus() != MeterStatus.ACTIVE) {
			throw new BusinessRuleViolationException(
					"Meter must be active to generate a bill. Current status: " + meter.getStatus());
		}
	}

	private void validateBillNotExistsForReading(Long meterReadingId) {
		if (billRepository.existsByMeterReadingId(meterReadingId)) {
			throw new DuplicateResourceException("A bill already exists for this meter reading");
		}
	}

	private void validateBillNotExistsForPeriod(Long meterId, int month, int year) {
		if (billRepository.existsByMeterIdAndBillingMonthAndBillingYear(meterId, month, year)) {
			throw new DuplicateResourceException(
					"A bill already exists for this meter in " + String.format("%02d/%d", month, year));
		}
	}

	private MeterReading findReadingOrThrow(Long id) {
		return meterReadingRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Meter reading not found with id: " + id));
	}

	private Bill findBillOrThrow(Long id) {
		return billRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
	}

	private String generateBillNumber(int year, int month) {
		long sequence = billRepository.countByBillingYearAndBillingMonth(year, month) + 1;
		return String.format("BILL-%d%02d-%05d", year, month, sequence);
	}

	private BillResponse toResponse(Bill bill) {
		Meter meter = bill.getMeter();
		Customer customer = bill.getCustomer();

		return new BillResponse(
				bill.getId(),
				bill.getBillNumber(),
				customer.getId(),
				customer.getFullNames(),
				meter.getId(),
				meter.getMeterNumber(),
				meter.getMeterType(),
				bill.getMeterReading().getId(),
				bill.getConsumption(),
				bill.getUnitRate(),
				bill.getFixedCharges(),
				bill.getBaseAmount(),
				bill.getVatAmount(),
				bill.getPenaltyAmount(),
				bill.getTotalAmount(),
				bill.getAmountPaid(),
				bill.getBalance(),
				bill.getStatus(),
				bill.getBillingMonth(),
				bill.getBillingYear(),
				bill.getDueDate(),
				bill.getCreatedAt(),
				bill.getUpdatedAt()
		);
	}

	private record BillCalculation(
			BigDecimal unitRate,
			BigDecimal fixedCharges,
			BigDecimal baseAmount,
			BigDecimal vatAmount,
			BigDecimal penaltyAmount,
			BigDecimal totalAmount
	) {
	}

}
