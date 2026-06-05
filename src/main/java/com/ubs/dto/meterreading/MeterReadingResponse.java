package com.ubs.dto.meterreading;

import com.ubs.entity.MeterType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record MeterReadingResponse(
		Long id,
		Long meterId,
		String meterNumber,
		MeterType meterType,
		Long customerId,
		String customerFullNames,
		BigDecimal currentReading,
		BigDecimal previousReading,
		BigDecimal consumption,
		int readingMonth,
		int readingYear,
		LocalDate readingDate,
		Instant createdAt,
		Instant updatedAt
) {
}
