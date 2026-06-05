package com.ubs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.password-reset")
public record PasswordResetProperties(
		long expirationMinutes,
		String baseUrl
) {
}
