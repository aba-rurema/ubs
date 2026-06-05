package com.ubs.config;

import com.ubs.config.openapi.OpenApiConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

	@Value("${server.servlet.context-path:/}")
	private String contextPath;

	@Value("${server.port:8080}")
	private int serverPort;

	@Bean
	public OpenAPI utilityBillingOpenAPI() {
		String normalizedContextPath = contextPath.endsWith("/")
				? contextPath.substring(0, contextPath.length() - 1)
				: contextPath;

		return new OpenAPI()
				.info(apiInfo())
				.servers(List.of(
						new Server()
								.url("http://localhost:" + serverPort + normalizedContextPath)
								.description("Local development server"),
						new Server()
								.url(normalizedContextPath)
								.description("Relative server (current host)")
				))
				.tags(apiTags())
				.addSecurityItem(new SecurityRequirement().addList(OpenApiConstants.BEARER_AUTH_SCHEME))
				.components(new Components()
						.addSecuritySchemes(OpenApiConstants.BEARER_AUTH_SCHEME, bearerSecurityScheme()));
	}

	private Info apiInfo() {
		return new Info()
				.title("Utility Billing System API")
				.description("""
						REST API for the Utility Billing System (UBS).

						## Overview
						Manage customers, meters, meter readings, bills, payments, tariffs, users, and notifications.

						## Authentication
						1. Register or login via **Authentication** endpoints (two-step with email OTP).
						2. Step 1: POST `/auth/login` or `/auth/register` — receive OTP by email.
						3. Step 2: POST `/auth/login/verify-otp` or `/auth/register/verify-otp` with the OTP.
						4. Click **Authorize** and enter: `Bearer {your-access-token}`
						5. Call protected endpoints with the JWT in the `Authorization` header.

						## Roles
						| Role | Permissions |
						|------|-------------|
						| **ROLE_ADMIN** | Manage users and tariffs |
						| **ROLE_OPERATOR** | Manage meter readings |
						| **ROLE_FINANCE** | Manage bills and payments |
						| **ROLE_CUSTOMER** | View bills and payments |

						## Typical workflow
						1. Register customer and assign meters
						2. Record monthly meter readings
						3. Generate and approve bills
						4. Process partial or full payments
						5. Notifications are created automatically (DB triggers) and via API
						""")
				.version("v1.0.0")
				.contact(new Contact()
						.name("UBS Support Team")
						.email("support@ubs.example.com")
						.url("https://ubs.example.com"))
				.license(new License()
						.name("Proprietary")
						.url("https://ubs.example.com/license"));
	}

	private List<Tag> apiTags() {
		return List.of(
				tag("Authentication", "Registration, login, password reset, change password, and profile"),
				tag("Users", "User account management (admin only)"),
				tag("Tariffs", "Utility unit rate configuration (admin only)"),
				tag("Customers", "Customer registration and profile management"),
				tag("Meters", "Meter registration linked to customers"),
				tag("Meter Readings", "Monthly meter reading capture and validation"),
				tag("Bills", "Bill generation, approval, and billing lifecycle"),
				tag("Payments", "Partial and full payment processing"),
				tag("Notifications", "Customer notification delivery via email and in-app tracking"),
				tag("Audit Logs", "System activity audit trail (admin only)")
		);
	}

	private Tag tag(String name, String description) {
		return new Tag().name(name).description(description);
	}

	private SecurityScheme bearerSecurityScheme() {
		return new SecurityScheme()
				.name(OpenApiConstants.BEARER_AUTH_SCHEME)
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT")
				.description(OpenApiConstants.BEARER_AUTH_DESCRIPTION);
	}

}
