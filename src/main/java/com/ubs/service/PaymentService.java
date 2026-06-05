package com.ubs.service;

import com.ubs.dto.payment.PaymentCreateRequest;
import com.ubs.dto.payment.PaymentResponse;
import com.ubs.entity.Bill;
import com.ubs.entity.BillStatus;
import com.ubs.entity.Payment;
import com.ubs.entity.PaymentType;
import com.ubs.exception.BusinessRuleViolationException;
import com.ubs.exception.ResourceNotFoundException;
import com.ubs.repository.BillRepository;
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
import java.util.Set;

@Service
public class PaymentService {

	private static final int MONEY_SCALE = 2;
	private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
	private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(MONEY_SCALE, ROUNDING);
	private static final Set<BillStatus> PAYABLE_STATUSES = Set.of(
			BillStatus.APPROVED, BillStatus.PARTIALLY_PAID, BillStatus.OVERDUE);

	private final PaymentRepository paymentRepository;
	private final BillRepository billRepository;
	private final BillingNotificationService billingNotificationService;
	private final CustomerAccessService customerAccessService;

	public PaymentService(PaymentRepository paymentRepository,
						  BillRepository billRepository,
						  BillingNotificationService billingNotificationService,
						  CustomerAccessService customerAccessService) {
		this.paymentRepository = paymentRepository;
		this.billRepository = billRepository;
		this.billingNotificationService = billingNotificationService;
		this.customerAccessService = customerAccessService;
	}

	@Transactional
	public PaymentResponse processPayment(PaymentCreateRequest request, CustomUserDetails principal) {
		Bill bill = findBillOrThrow(request.billId());
		customerAccessService.validateBillAccess(principal, bill);
		validateBillPayable(bill);

		BigDecimal balanceBefore = bill.getBalance();
		BigDecimal paymentAmount = resolvePaymentAmount(request, balanceBefore);
		validatePaymentAmount(paymentAmount, balanceBefore);

		PaymentType paymentType = determinePaymentType(paymentAmount, balanceBefore);
		BigDecimal balanceAfter = balanceBefore.subtract(paymentAmount).setScale(MONEY_SCALE, ROUNDING);

		Payment payment = Payment.builder()
				.paymentReference(generatePaymentReference(request.paymentDate()))
				.bill(bill)
				.amount(paymentAmount)
				.paymentMethod(request.paymentMethod())
				.paymentType(paymentType)
				.paymentDate(request.paymentDate())
				.balanceBefore(balanceBefore)
				.balanceAfter(balanceAfter)
				.notes(request.notes())
				.build();

		updateBillAfterPayment(bill, balanceAfter);

		Payment savedPayment = paymentRepository.save(payment);
		billRepository.save(bill);

		if (bill.getStatus() == BillStatus.PAID) {
			billingNotificationService.notifyPaymentConfirmed(bill, savedPayment);
		}

		return toResponse(savedPayment);
	}

	@Transactional(readOnly = true)
	public PaymentResponse getById(Long id, CustomUserDetails principal) {
		Payment payment = findPaymentOrThrow(id);
		customerAccessService.validateBillAccess(principal, payment.getBill());
		return toResponse(payment);
	}

	@Transactional(readOnly = true)
	public Page<PaymentResponse> getAll(Pageable pageable) {
		return paymentRepository.findAll(pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<PaymentResponse> getMyPayments(CustomUserDetails principal, Pageable pageable) {
		Long customerId = customerAccessService.requireLinkedCustomerId(principal);
		return paymentRepository.findByBillCustomerId(customerId, pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<PaymentResponse> getByBillId(Long billId, CustomUserDetails principal, Pageable pageable) {
		Bill bill = findBillOrThrow(billId);
		customerAccessService.validateBillAccess(principal, bill);
		return paymentRepository.findByBillId(billId, pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<PaymentResponse> getByCustomerId(Long customerId, CustomUserDetails principal, Pageable pageable) {
		customerAccessService.validateCustomerOwnership(principal, customerId);
		return paymentRepository.findByBillCustomerId(customerId, pageable).map(this::toResponse);
	}

	private BigDecimal resolvePaymentAmount(PaymentCreateRequest request, BigDecimal balanceBefore) {
		if (request.payFullBalance()) {
			return balanceBefore;
		}

		if (request.amount() == null) {
			throw new BusinessRuleViolationException(
					"Payment amount is required when payFullBalance is false");
		}

		return request.amount().setScale(MONEY_SCALE, ROUNDING);
	}

	private void validateBillPayable(Bill bill) {
		if (bill.getStatus() == BillStatus.PAID) {
			throw new BusinessRuleViolationException("Bill is already fully paid");
		}

		if (bill.getStatus() == BillStatus.PENDING) {
			throw new BusinessRuleViolationException("Bill must be approved before processing payments");
		}

		if (!PAYABLE_STATUSES.contains(bill.getStatus())) {
			throw new BusinessRuleViolationException("Bill is not payable in its current status: " + bill.getStatus());
		}

		if (bill.getBalance().compareTo(ZERO) <= 0) {
			throw new BusinessRuleViolationException("Bill has no outstanding balance");
		}
	}

	private void validatePaymentAmount(BigDecimal paymentAmount, BigDecimal balanceBefore) {
		if (paymentAmount.compareTo(ZERO) <= 0) {
			throw new BusinessRuleViolationException("Payment amount must be greater than zero");
		}

		if (paymentAmount.compareTo(balanceBefore) > 0) {
			throw new BusinessRuleViolationException(
					"Payment amount (" + paymentAmount + ") exceeds outstanding balance (" + balanceBefore + ")");
		}
	}

	private PaymentType determinePaymentType(BigDecimal paymentAmount, BigDecimal balanceBefore) {
		return paymentAmount.compareTo(balanceBefore) == 0 ? PaymentType.FULL : PaymentType.PARTIAL;
	}

	private void updateBillAfterPayment(Bill bill, BigDecimal balanceAfter) {
		BigDecimal paymentAmount = bill.getBalance().subtract(balanceAfter).setScale(MONEY_SCALE, ROUNDING);
		BigDecimal newAmountPaid = bill.getAmountPaid().add(paymentAmount).setScale(MONEY_SCALE, ROUNDING);

		bill.setAmountPaid(newAmountPaid);
		bill.setBalance(balanceAfter);

		if (balanceAfter.compareTo(ZERO) == 0) {
			bill.setStatus(BillStatus.PAID);
		} else {
			bill.setStatus(BillStatus.PARTIALLY_PAID);
		}
	}

	private Bill findBillOrThrow(Long billId) {
		return billRepository.findById(billId)
				.orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + billId));
	}

	private Payment findPaymentOrThrow(Long id) {
		return paymentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
	}

	private String generatePaymentReference(LocalDate paymentDate) {
		long sequence = paymentRepository.countByPaymentYear(paymentDate.getYear()) + 1;
		return String.format("PAY-%d-%06d", paymentDate.getYear(), sequence);
	}

	private PaymentResponse toResponse(Payment payment) {
		Bill bill = payment.getBill();

		return new PaymentResponse(
				payment.getId(),
				payment.getPaymentReference(),
				bill.getId(),
				bill.getBillNumber(),
				bill.getCustomer().getId(),
				bill.getCustomer().getFullNames(),
				payment.getAmount(),
				payment.getPaymentMethod(),
				payment.getPaymentType(),
				payment.getPaymentDate(),
				payment.getBalanceBefore(),
				payment.getBalanceAfter(),
				bill.getBalance(),
				bill.getStatus(),
				payment.getNotes(),
				payment.getCreatedAt()
		);
	}

}
