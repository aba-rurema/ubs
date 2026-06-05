package com.ubs.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public record VerifyOtpRequest(
		@NotNull(message = "Session ID is required")
		Long sessionId,

		@NotBlank(message = "OTP code is required")
		@Pattern(regexp = "^\\d{6}$", message = "OTP must be a 6-digit code")
		String otpCode
) {
}
