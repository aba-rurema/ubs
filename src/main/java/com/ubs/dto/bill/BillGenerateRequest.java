package com.ubs.dto.bill;

import jakarta.validation.constraints.NotNull;


public record BillGenerateRequest(
		@NotNull(message = "Meter reading ID is required")
		Long meterReadingId
) {
}
