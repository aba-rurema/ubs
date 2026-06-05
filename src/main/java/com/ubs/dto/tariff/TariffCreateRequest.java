package com.ubs.dto.tariff;

import com.ubs.entity.MeterType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TariffCreateRequest(
		@NotNull(message = "Meter type is required")
		MeterType meterType,

		@NotNull(message = "Unit rate is required")
		@DecimalMin(value = "0.01", message = "Unit rate must be greater than zero")
		@Digits(integer = 10, fraction = 2, message = "Unit rate must have at most 10 integer digits and 2 decimal places")
		BigDecimal unitRate,

		@NotNull(message = "Fixed charges are required")
		@DecimalMin(value = "0.0", message = "Fixed charges cannot be negative")
		@Digits(integer = 10, fraction = 2, message = "Fixed charges must have at most 10 integer digits and 2 decimal places")
		BigDecimal fixedCharges,

		@NotNull(message = "VAT percentage is required")
		@DecimalMin(value = "0.0", message = "VAT percentage cannot be negative")
		@Digits(integer = 3, fraction = 2, message = "VAT percentage must have at most 3 integer digits and 2 decimal places")
		BigDecimal vatPercentage,

		@NotNull(message = "Penalty percentage is required")
		@DecimalMin(value = "0.0", message = "Penalty percentage cannot be negative")
		@Digits(integer = 3, fraction = 2, message = "Penalty percentage must have at most 3 integer digits and 2 decimal places")
		BigDecimal penaltyPercentage,

		@Size(max = 255, message = "Description must not exceed 255 characters")
		String description
) {
}
