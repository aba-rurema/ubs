package com.ubs.config.openapi;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Groups API endpoints by domain for Swagger UI dropdown navigation.
 */
@Configuration
public class OpenApiGroupConfig {

	@Bean
	public GroupedOpenApi allApisGroup() {
		return baseGroup("00-all-apis", "/**");
	}

	@Bean
	public GroupedOpenApi authenticationGroup() {
		return baseGroup("01-authentication", "/auth/**");
	}

	@Bean
	public GroupedOpenApi usersGroup() {
		return baseGroup("02-users", "/users/**");
	}

	@Bean
	public GroupedOpenApi tariffsGroup() {
		return baseGroup("03-tariffs", "/tariffs/**");
	}

	@Bean
	public GroupedOpenApi customersGroup() {
		return baseGroup("04-customers", "/customers/**");
	}

	@Bean
	public GroupedOpenApi metersGroup() {
		return baseGroup("05-meters", "/meters/**");
	}

	@Bean
	public GroupedOpenApi meterReadingsGroup() {
		return baseGroup("06-meter-readings", "/meter-readings/**");
	}

	@Bean
	public GroupedOpenApi billsGroup() {
		return baseGroup("07-bills", "/bills/**");
	}

	@Bean
	public GroupedOpenApi paymentsGroup() {
		return baseGroup("08-payments", "/payments/**");
	}

	@Bean
	public GroupedOpenApi notificationsGroup() {
		return baseGroup("09-notifications", "/notifications/**");
	}

	@Bean
	public GroupedOpenApi auditLogsGroup() {
		return baseGroup("10-audit-logs", "/audit-logs/**");
	}

	private GroupedOpenApi baseGroup(String groupName, String pathPattern) {
		return GroupedOpenApi.builder()
				.group(groupName)
				.pathsToMatch(pathPattern)
				.displayName(formatDisplayName(groupName))
				.build();
	}

	private String formatDisplayName(String groupName) {
		return groupName.substring(3).replace('-', ' ');
	}

}
