package com.ubs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
		boolean enabled,
		String from,
		String fromName,
		String devRedirectTo,
		java.util.List<String> devRedirectDomains
) {
}
