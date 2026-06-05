package com.ubs.config.openapi;

import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

/**
 * Adds common 401/403 response descriptions to secured operations.
 */
@Configuration
public class OpenApiOperationCustomizer {

	@Bean
	public OperationCustomizer securedOperationResponsesCustomizer() {
		return (Operation operation, HandlerMethod handlerMethod) -> {
			if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
				operation.getResponses().addApiResponse("401",
						new io.swagger.v3.oas.models.responses.ApiResponse()
								.description("Unauthorized – missing or invalid JWT token"));
				operation.getResponses().addApiResponse("403",
						new io.swagger.v3.oas.models.responses.ApiResponse()
								.description("Forbidden – insufficient role permissions"));
			}
			return operation;
		};
	}

}
