package com.ubs.dto.auth;

import com.ubs.entity.OtpPurpose;


public record OtpChallengeResponse(
		Long sessionId,
		String message,
		String maskedEmail,
		long expiresInMinutes,
		OtpPurpose purpose
) {
}
