package com.ubs.dto.user;

import com.ubs.entity.Role;

import java.time.Instant;
import java.util.Set;

public record UserResponse(
		Long id,
		Long customerId,
		String username,
		String email,
		Set<Role> roles,
		boolean enabled,
		boolean accountNonLocked,
		Instant createdAt,
		Instant updatedAt
) {
}
