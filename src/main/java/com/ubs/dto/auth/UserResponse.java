package com.ubs.dto.auth;

import com.ubs.entity.Role;

import java.util.Set;

public record UserResponse(
		Long id,
		Long customerId,
		String username,
		String email,
		Set<Role> roles
) {
}
