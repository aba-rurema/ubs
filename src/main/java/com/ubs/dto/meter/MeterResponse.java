package com.ubs.dto.meter;

import com.ubs.entity.MeterStatus;
import com.ubs.entity.MeterType;

import java.time.Instant;
import java.time.LocalDate;

public record MeterResponse(
		Long id,
		Long customerId,
		String customerFullNames,
		String meterNumber,
		MeterType meterType,
		LocalDate installationDate,
		MeterStatus status,
		Instant createdAt,
		Instant updatedAt
) {
}
