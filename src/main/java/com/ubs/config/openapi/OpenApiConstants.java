package com.ubs.config.openapi;

/**
 * Shared OpenAPI constants used across configuration and controllers.
 */
public final class OpenApiConstants {

	public static final String BEARER_AUTH_SCHEME = "bearerAuth";

	public static final String BEARER_AUTH_HEADER = "Authorization";

	public static final String BEARER_AUTH_DESCRIPTION =
			"JWT access token. Obtain a token via POST /auth/login, then enter: Bearer {token}";

	private OpenApiConstants() {
	}

}
