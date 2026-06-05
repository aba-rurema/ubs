package com.ubs.audit;

import com.ubs.entity.Role;
import com.ubs.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;
import java.util.stream.Collectors;

public final class AuditContext {

	private AuditContext() {
	}

	public static Long currentUserId() {
		CustomUserDetails user = currentUser();
		return user != null ? user.getId() : null;
	}

	public static String currentUsername() {
		CustomUserDetails user = currentUser();
		return user != null ? user.getUsername() : "system";
	}

	public static String currentRole() {
		CustomUserDetails user = currentUser();
		if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
			return null;
		}
		Set<Role> roles = user.getRoles();
		return roles.stream().map(Role::name).collect(Collectors.joining(","));
	}

	public static String currentIpAddress() {
		ServletRequestAttributes attributes =
				(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes == null) {
			return null;
		}
		HttpServletRequest request = attributes.getRequest();
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private static CustomUserDetails currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}
		Object principal = authentication.getPrincipal();
		if (principal instanceof CustomUserDetails userDetails) {
			return userDetails;
		}
		return null;
	}

}
