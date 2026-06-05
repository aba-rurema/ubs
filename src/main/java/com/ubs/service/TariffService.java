package com.ubs.service;

import com.ubs.config.BillingProperties;
import com.ubs.dto.tariff.TariffCreateRequest;
import com.ubs.dto.tariff.TariffResponse;
import com.ubs.dto.tariff.TariffUpdateRequest;
import com.ubs.entity.MeterType;
import com.ubs.entity.Tariff;
import com.ubs.exception.DuplicateResourceException;
import com.ubs.exception.ResourceNotFoundException;
import com.ubs.repository.TariffRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TariffService {

	private final TariffRepository tariffRepository;
	private final BillingProperties billingProperties;

	public TariffService(TariffRepository tariffRepository, BillingProperties billingProperties) {
		this.tariffRepository = tariffRepository;
		this.billingProperties = billingProperties;
	}

	@Transactional
	public TariffResponse create(TariffCreateRequest request) {
		if (tariffRepository.existsByMeterType(request.meterType())) {
			throw new DuplicateResourceException("Tariff already exists for meter type: " + request.meterType());
		}

		Tariff tariff = Tariff.builder()
				.meterType(request.meterType())
				.unitRate(request.unitRate())
				.fixedCharges(request.fixedCharges())
				.vatPercentage(request.vatPercentage())
				.penaltyPercentage(request.penaltyPercentage())
				.description(request.description())
				.active(true)
				.build();

		return toResponse(tariffRepository.save(tariff));
	}

	@Transactional
	public TariffResponse update(Long id, TariffUpdateRequest request) {
		Tariff tariff = findTariffOrThrow(id);

		tariff.setUnitRate(request.unitRate());
		tariff.setFixedCharges(request.fixedCharges());
		tariff.setVatPercentage(request.vatPercentage());
		tariff.setPenaltyPercentage(request.penaltyPercentage());
		tariff.setDescription(request.description());
		tariff.setActive(request.active());

		return toResponse(tariffRepository.save(tariff));
	}

	@Transactional(readOnly = true)
	public TariffResponse getById(Long id) {
		return toResponse(findTariffOrThrow(id));
	}

	@Transactional(readOnly = true)
	public TariffResponse getByMeterType(MeterType meterType) {
		return tariffRepository.findByMeterType(meterType)
				.map(this::toResponse)
				.orElseThrow(() -> new ResourceNotFoundException("Tariff not found for meter type: " + meterType));
	}

	@Transactional(readOnly = true)
	public Page<TariffResponse> getAll(Pageable pageable) {
		return tariffRepository.findAll(pageable).map(this::toResponse);
	}

	@Transactional
	public void delete(Long id) {
		Tariff tariff = findTariffOrThrow(id);
		tariffRepository.delete(tariff);
	}

	@Transactional(readOnly = true)
	public TariffPricing resolvePricing(MeterType meterType) {
		return tariffRepository.findByMeterType(meterType)
				.filter(Tariff::isActive)
				.map(t -> new TariffPricing(
						t.getUnitRate(),
						t.getFixedCharges(),
						t.getVatPercentage(),
						t.getPenaltyPercentage()))
				.orElse(new TariffPricing(
						billingProperties.getRateFor(meterType),
						BigDecimal.ZERO,
						billingProperties.vatRate(),
						billingProperties.penaltyRate()));
	}

	private Tariff findTariffOrThrow(Long id) {
		return tariffRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tariff not found with id: " + id));
	}

	private TariffResponse toResponse(Tariff tariff) {
		return new TariffResponse(
				tariff.getId(),
				tariff.getMeterType(),
				tariff.getUnitRate(),
				tariff.getFixedCharges(),
				tariff.getVatPercentage(),
				tariff.getPenaltyPercentage(),
				tariff.getDescription(),
				tariff.isActive(),
				tariff.getCreatedAt(),
				tariff.getUpdatedAt()
		);
	}

	public record TariffPricing(
			BigDecimal unitRate,
			BigDecimal fixedCharges,
			BigDecimal vatPercentage,
			BigDecimal penaltyPercentage
	) {
	}

}
