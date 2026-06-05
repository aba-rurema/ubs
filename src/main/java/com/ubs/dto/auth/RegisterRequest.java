package com.ubs.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
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

		@NotBlank(message = "Full names are required")
		@Size(min = 2, max = 150, message = "Full names must be between 2 and 150 characters")
		String fullNames,

		@NotBlank(message = "National ID is required")
		@Size(min = 5, max = 30, message = "National ID must be between 5 and 30 characters")
		@Pattern(regexp = "^[A-Za-z0-9-]+$", message = "National ID may only contain letters, numbers, and hyphens")
		String nationalId,

		@NotBlank(message = "Phone number is required")
		@Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number must be 7 to 15 digits, optionally prefixed with +")
		String phone,

		@NotBlank(message = "Address is required")
		@Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
		String address
) {
}
