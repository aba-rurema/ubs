package com.ubs.dto.user;

import com.ubs.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserUpdateRequest(
		@Email(message = "Email must be valid")
		@Size(max = 100, message = "Email must not exceed 100 characters")
		String email,

		@NotEmpty(message = "At least one role is required")
		Set<Role> roles,

		Boolean enabled,

		Boolean accountNonLocked
) {
}
