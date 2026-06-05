package com.ubs.service;

import com.ubs.dto.meterreading.MeterReadingCreateRequest;
import com.ubs.dto.meterreading.MeterReadingResponse;
import com.ubs.dto.meterreading.MeterReadingUpdateRequest;
import com.ubs.entity.Meter;
import com.ubs.entity.MeterReading;
import com.ubs.entity.MeterStatus;
import com.ubs.exception.BusinessRuleViolationException;
import com.ubs.exception.DuplicateResourceException;
import com.ubs.exception.ResourceNotFoundException;
import com.ubs.repository.MeterReadingRepository;
import com.ubs.repository.MeterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class MeterReadingService {

	private static final BigDecimal ZERO = BigDecimal.ZERO;

	private final MeterReadingRepository meterReadingRepository;
	private final MeterRepository meterRepository;
	private final BillService billService;

	public MeterReadingService(MeterReadingRepository meterReadingRepository,
							   MeterRepository meterRepository,
							   @org.springframework.context.annotation.Lazy BillService billService) {
		this.meterReadingRepository = meterReadingRepository;
		this.meterRepository = meterRepository;
		this.billService = billService;
	}

	@Transactional
	public MeterReadingResponse create(MeterReadingCreateRequest request) {
		Meter meter = findMeterOrThrow(request.meterId());
		validateMeterIsActive(meter);
		validateUniquePeriod(meter.getId(), request.readingMonth(), request.readingYear(), null);

		BigDecimal previousReading = resolvePreviousReading(meter.getId(), request.readingMonth(), request.readingYear());
		validateCurrentGreaterThanPrevious(request.currentReading(), previousReading);
		validateAgainstNextReading(meter.getId(), request.readingMonth(), request.readingYear(),
				request.currentReading(), null);

		MeterReading reading = MeterReading.builder()
				.meter(meter)
				.currentReading(request.currentReading())
				.previousReading(previousReading)
				.consumption(calculateConsumption(request.currentReading(), previousReading))
				.readingMonth(request.readingMonth())
				.readingYear(request.readingYear())
				.readingDate(request.readingDate())
				.build();

		MeterReading savedReading = meterReadingRepository.save(reading);
		billService.generateFromReading(savedReading.getId());

		return toResponse(savedReading);
	}

	@Transactional(readOnly = true)
	public MeterReadingResponse getById(Long id) {
		return toResponse(findReadingOrThrow(id));
	}

	@Transactional(readOnly = true)
	public Page<MeterReadingResponse> getAll(Pageable pageable) {
		return meterReadingRepository.findAll(pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<MeterReadingResponse> getByMeterId(Long meterId, Pageable pageable) {
		findMeterOrThrow(meterId);
		return meterReadingRepository.findByMeterId(meterId, pageable).map(this::toResponse);
	}

	@Transactional
	public MeterReadingResponse update(Long id, MeterReadingUpdateRequest request) {
		MeterReading reading = findReadingOrThrow(id);
		Meter meter = reading.getMeter();

		validateMeterIsActive(meter);
		validateUniquePeriod(meter.getId(), request.readingMonth(), request.readingYear(), id);

		BigDecimal previousReading = resolvePreviousReading(meter.getId(), request.readingMonth(), request.readingYear());
		validateCurrentGreaterThanPrevious(request.currentReading(), previousReading);
		validateAgainstNextReading(meter.getId(), request.readingMonth(), request.readingYear(),
				request.currentReading(), id);

		reading.setCurrentReading(request.currentReading());
		reading.setPreviousReading(previousReading);
		reading.setConsumption(calculateConsumption(request.currentReading(), previousReading));
		reading.setReadingMonth(request.readingMonth());
		reading.setReadingYear(request.readingYear());
		reading.setReadingDate(request.readingDate());

		return toResponse(meterReadingRepository.save(reading));
	}

	@Transactional
	public void delete(Long id) {
		MeterReading reading = findReadingOrThrow(id);
		meterReadingRepository.delete(reading);
	}

	private Meter findMeterOrThrow(Long meterId) {
		return meterRepository.findById(meterId)
				.orElseThrow(() -> new ResourceNotFoundException("Meter not found with id: " + meterId));
	}

	private MeterReading findReadingOrThrow(Long id) {
		return meterReadingRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Meter reading not found with id: " + id));
	}

	private void validateMeterIsActive(Meter meter) {
		if (meter.getStatus() != MeterStatus.ACTIVE) {
			throw new BusinessRuleViolationException(
					"Meter must be active to record a reading. Current status: " + meter.getStatus());
		}
	}

	private void validateUniquePeriod(Long meterId, int month, int year, Long excludeId) {
		boolean exists = excludeId == null
				? meterReadingRepository.existsByMeterIdAndReadingMonthAndReadingYear(meterId, month, year)
				: meterReadingRepository.existsByMeterIdAndReadingMonthAndReadingYearAndIdNot(meterId, month, year, excludeId);

		if (exists) {
			throw new DuplicateResourceException(
					"A reading already exists for this meter in " + formatPeriod(month, year));
		}
	}

	private BigDecimal resolvePreviousReading(Long meterId, int month, int year) {
		return meterReadingRepository.findLatestBeforePeriod(meterId, month, year)
				.map(MeterReading::getCurrentReading)
				.orElse(ZERO);
	}

	private void validateCurrentGreaterThanPrevious(BigDecimal currentReading, BigDecimal previousReading) {
		if (currentReading.compareTo(previousReading) <= 0) {
			throw new BusinessRuleViolationException(
					"Current reading (" + currentReading + ") must be greater than previous reading (" + previousReading + ")");
		}
	}

	private void validateAgainstNextReading(Long meterId,
											int month,
											int year,
											BigDecimal currentReading,
											Long excludeId) {
		meterReadingRepository.findEarliestAfterPeriod(meterId, month, year)
				.filter(next -> excludeId == null || !next.getId().equals(excludeId))
				.ifPresent(next -> {
					if (currentReading.compareTo(next.getCurrentReading()) >= 0) {
						throw new BusinessRuleViolationException(
								"Current reading (" + currentReading + ") must be less than the next period reading ("
										+ next.getCurrentReading() + ") for "
										+ formatPeriod(next.getReadingMonth(), next.getReadingYear()));
					}
				});
	}

	private BigDecimal calculateConsumption(BigDecimal currentReading, BigDecimal previousReading) {
		return currentReading.subtract(previousReading);
	}

	private String formatPeriod(int month, int year) {
		return String.format("%02d/%d", month, year);
	}

	private MeterReadingResponse toResponse(MeterReading reading) {
		Meter meter = reading.getMeter();
		return new MeterReadingResponse(
				reading.getId(),
				meter.getId(),
				meter.getMeterNumber(),
				meter.getMeterType(),
				meter.getCustomer().getId(),
				meter.getCustomer().getFullNames(),
				reading.getCurrentReading(),
				reading.getPreviousReading(),
				reading.getConsumption(),
				reading.getReadingMonth(),
				reading.getReadingYear(),
				reading.getReadingDate(),
				reading.getCreatedAt(),
				reading.getUpdatedAt()
		);
	}

}
