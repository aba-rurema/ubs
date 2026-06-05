package com.ubs.security;

/**
 * Centralized Spring Security {@code @PreAuthorize} expressions for role-based authorization.
 */
public final class SecurityExpressions {

	private SecurityExpressions() {
	}

	public static final String MANAGE_USERS = "hasRole('ADMIN')";

	public static final String MANAGE_TARIFFS = "hasRole('ADMIN')";

	public static final String MANAGE_METER_READINGS = "hasAnyRole('ADMIN', 'OPERATOR')";

	public static final String VIEW_METER_READINGS = "hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')";

	public static final String MANAGE_BILLS = "hasAnyRole('ADMIN', 'FINANCE')";

	public static final String VIEW_ALL_BILLS = "hasAnyRole('ADMIN', 'FINANCE')";

	public static final String VIEW_OWN_BILLS = "hasRole('CUSTOMER')";

	public static final String MANAGE_PAYMENTS = "hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')";

	public static final String VIEW_ALL_PAYMENTS = "hasAnyRole('ADMIN', 'FINANCE')";

	public static final String VIEW_OWN_PAYMENTS = "hasRole('CUSTOMER')";

	public static final String MANAGE_CUSTOMERS = "hasRole('ADMIN')";

	public static final String VIEW_CUSTOMERS = "hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')";

	public static final String MANAGE_METERS = "hasRole('ADMIN')";

	public static final String VIEW_METERS = "hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')";

	public static final String MANAGE_NOTIFICATIONS = "hasAnyRole('ADMIN', 'FINANCE')";

	public static final String VIEW_ALL_NOTIFICATIONS = "hasAnyRole('ADMIN', 'FINANCE')";

	public static final String VIEW_NOTIFICATIONS = "hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')";

	public static final String VIEW_AUDIT_LOGS = "hasRole('ADMIN')";

}
