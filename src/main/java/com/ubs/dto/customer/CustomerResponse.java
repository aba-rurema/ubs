package com.ubs.dto.customer;

import com.ubs.entity.CustomerStatus;

import java.time.Instant;

public record CustomerResponse(
		Long id,
		String fullNames,
		String nationalId,
		String email,
		String phone,
		String address,
		CustomerStatus status,
		Instant createdAt,
		Instant updatedAt
) {
}
