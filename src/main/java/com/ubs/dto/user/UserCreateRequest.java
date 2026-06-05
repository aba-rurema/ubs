package com.ubs.dto.user;

import com.ubs.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserCreateRequest(
		@NotBlank(message = "Username is required")
		@Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
		@Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username may only contain letters, numbers, dots, underscores, and hyphens")
		String username,

		@NotBlank(message = "Email is required")
		@Email(message = "Email must be valid")
		@Size(max = 100, message = "Email must not exceed 100 characters")
		String email,

		@NotBlank(message = "Password is required")
		@Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
		String password,

		@NotEmpty(message = "At least one role is required")
		Set<Role> roles
) {
}
