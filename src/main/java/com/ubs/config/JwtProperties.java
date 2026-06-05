package com.ubs.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.jwt")
@Validated
public record JwtProperties(
		@NotBlank(message = "JWT secret must not be blank")
		@Size(min = 32, message = "JWT secret must be at least 32 characters")
		String secret,

		@Positive(message = "JWT expiration must be positive")
		long expirationMs,

		@Positive(message = "JWT refresh expiration must be positive")
		long refreshExpirationMs
) {
}
