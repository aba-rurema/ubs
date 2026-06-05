package com.ubs.config.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Registers example request/response payloads on OpenAPI schema definitions.
 */
@Configuration
public class OpenApiExampleCustomizer {

	private final ObjectMapper objectMapper;

	public OpenApiExampleCustomizer(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Bean
	public OpenApiCustomizer schemaExampleCustomizer() {
		return openApi -> {
			if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
				return;
			}

			Map<String, Schema> schemas = openApi.getComponents().getSchemas();

			setExample(schemas, "LoginRequest", OpenApiExamples.LOGIN_REQUEST);
			setExample(schemas, "RegisterRequest", OpenApiExamples.REGISTER_REQUEST);
			setExample(schemas, "AuthResponse", OpenApiExamples.AUTH_RESPONSE);
			setExample(schemas, "CustomerCreateRequest", OpenApiExamples.CUSTOMER_CREATE_REQUEST);
			setExample(schemas, "CustomerResponse", OpenApiExamples.CUSTOMER_RESPONSE);
			setExample(schemas, "MeterReadingCreateRequest", OpenApiExamples.METER_READING_CREATE_REQUEST);
			setExample(schemas, "BillGenerateRequest", OpenApiExamples.BILL_GENERATE_REQUEST);
			setExample(schemas, "BillResponse", OpenApiExamples.BILL_RESPONSE);
			setExample(schemas, "PaymentCreateRequest", OpenApiExamples.PAYMENT_CREATE_REQUEST);
			setExample(schemas, "PaymentResponse", OpenApiExamples.PAYMENT_RESPONSE);
			setExample(schemas, "ErrorResponse", OpenApiExamples.ERROR_RESPONSE);
		};
	}

	private void setExample(Map<String, Schema> schemas, String schemaName, String jsonExample) {
		Schema schema = schemas.get(schemaName);
		if (schema == null) {
			return;
		}

		try {
			Object example = objectMapper.readValue(jsonExample, Object.class);
			schema.setExample(example);
		} catch (Exception ignored) {
			schema.setExample(jsonExample);
		}
	}

}
