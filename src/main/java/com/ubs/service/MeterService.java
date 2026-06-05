package com.ubs.service;

import com.ubs.dto.meter.MeterCreateRequest;
import com.ubs.dto.meter.MeterResponse;
import com.ubs.dto.meter.MeterUpdateRequest;
import com.ubs.entity.Customer;
import com.ubs.entity.Meter;
import com.ubs.exception.DuplicateResourceException;
import com.ubs.exception.ResourceNotFoundException;
import com.ubs.repository.CustomerRepository;
import com.ubs.repository.MeterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class MeterService {

	private final MeterRepository meterRepository;
	private final CustomerRepository customerRepository;

	public MeterService(MeterRepository meterRepository, CustomerRepository customerRepository) {
		this.meterRepository = meterRepository;
		this.customerRepository = customerRepository;
	}

	@Transactional
	public MeterResponse create(MeterCreateRequest request) {
		Customer customer = findCustomerOrThrow(request.customerId());
		validateMeterNumberUnique(request.meterNumber(), null);

		Meter meter = Meter.builder()
				.customer(customer)
				.meterNumber(request.meterNumber().trim())
				.meterType(request.meterType())
				.installationDate(request.installationDate())
				.status(request.status())
				.build();

		return toResponse(meterRepository.save(meter));
	}

	@Transactional(readOnly = true)
	public MeterResponse getById(Long id) {
		return toResponse(findMeterOrThrow(id));
	}

	@Transactional(readOnly = true)
	public Page<MeterResponse> getAll(Pageable pageable) {
		return meterRepository.findAll(pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<MeterResponse> getByCustomerId(Long customerId, Pageable pageable) {
		findCustomerOrThrow(customerId);
		return meterRepository.findByCustomerId(customerId, pageable).map(this::toResponse);
	}

	@Transactional
	public MeterResponse update(Long id, MeterUpdateRequest request) {
		Meter meter = findMeterOrThrow(id);
		validateMeterNumberUnique(request.meterNumber(), id);

		meter.setMeterNumber(request.meterNumber().trim());
		meter.setMeterType(request.meterType());
		meter.setInstallationDate(request.installationDate());
		meter.setStatus(request.status());

		return toResponse(meterRepository.save(meter));
	}

	@Transactional
	public void delete(Long id) {
		Meter meter = findMeterOrThrow(id);
		meterRepository.delete(meter);
	}

	private Customer findCustomerOrThrow(Long customerId) {
		return customerRepository.findById(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
	}

	private Meter findMeterOrThrow(Long id) {
		return meterRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Meter not found with id: " + id));
	}

	private void validateMeterNumberUnique(String meterNumber, Long excludeId) {
		String normalizedMeterNumber = meterNumber.trim();
		boolean exists = excludeId == null
				? meterRepository.existsByMeterNumber(normalizedMeterNumber)
				: meterRepository.existsByMeterNumberAndIdNot(normalizedMeterNumber, excludeId);

		if (exists) {
			throw new DuplicateResourceException("Meter number is already registered: " + normalizedMeterNumber);
		}
	}

	private MeterResponse toResponse(Meter meter) {
		return new MeterResponse(
				meter.getId(),
				meter.getCustomer().getId(),
				meter.getCustomer().getFullNames(),
				meter.getMeterNumber(),
				meter.getMeterType(),
				meter.getInstallationDate(),
				meter.getStatus(),
				meter.getCreatedAt(),
				meter.getUpdatedAt()
		);
	}

}
