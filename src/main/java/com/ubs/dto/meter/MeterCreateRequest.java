package com.ubs.dto.meter;

import com.ubs.entity.MeterStatus;
import com.ubs.entity.MeterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MeterCreateRequest(
		@NotNull(message = "Customer ID is required")
		Long customerId,

		@NotBlank(message = "Meter number is required")
		@Size(min = 3, max = 50, message = "Meter number must be between 3 and 50 characters")
		@Pattern(regexp = "^[A-Za-z0-9-]+$", message = "Meter number may only contain letters, numbers, and hyphens")
		String meterNumber,

		@NotNull(message = "Meter type is required")
		MeterType meterType,

		@NotNull(message = "Installation date is required")
		@PastOrPresent(message = "Installation date cannot be in the future")
		LocalDate installationDate,

		@NotNull(message = "Status is required")
		MeterStatus status
) {
}
