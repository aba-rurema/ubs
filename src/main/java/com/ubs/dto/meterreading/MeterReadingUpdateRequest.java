package com.ubs.dto.meterreading;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MeterReadingUpdateRequest(
		@NotNull(message = "Current reading is required")
		@DecimalMin(value = "0.0", inclusive = false, message = "Current reading must be greater than zero")
		@Digits(integer = 9, fraction = 3, message = "Current reading must have at most 9 integer digits and 3 decimal places")
		BigDecimal currentReading,

		@NotNull(message = "Reading month is required")
		@Min(value = 1, message = "Reading month must be between 1 and 12")
		@Max(value = 12, message = "Reading month must be between 1 and 12")
		Integer readingMonth,

		@NotNull(message = "Reading year is required")
		@Min(value = 2000, message = "Reading year must be 2000 or later")
		Integer readingYear,

		@NotNull(message = "Reading date is required")
		LocalDate readingDate
) {
}
