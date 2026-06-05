package com.ubs.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standard API error response")
public record ErrorResponse(
		@Schema(description = "Error timestamp", example = "2024-06-29T10:00:00Z")
		Instant timestamp,

		@Schema(description = "HTTP status code", example = "400")
		int status,

		@Schema(description = "Error category", example = "Validation Failed")
		String error,

		@Schema(description = "Detailed error message", example = "fullNames: Full names are required")
		String message,

		@Schema(description = "Request path", example = "/api/customers")
		String path
) {
}
