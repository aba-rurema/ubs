package com.ubs.dto.tariff;

import com.ubs.entity.MeterType;

import java.math.BigDecimal;
import java.time.Instant;

public record TariffResponse(
		Long id,
		MeterType meterType,
		BigDecimal unitRate,
		BigDecimal fixedCharges,
		BigDecimal vatPercentage,
		BigDecimal penaltyPercentage,
		String description,
		boolean active,
		Instant createdAt,
		Instant updatedAt
) {
}
