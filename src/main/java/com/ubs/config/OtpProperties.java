package com.ubs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.otp")
public record OtpProperties(
		long expirationMinutes,
		int codeLength
) {
}
