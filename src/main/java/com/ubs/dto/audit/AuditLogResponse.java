package com.ubs.dto.audit;

import com.ubs.entity.AuditAction;

import java.time.Instant;

public record AuditLogResponse(
		Long id,
		Long userId,
		String username,
		String role,
		AuditAction action,
		String entityType,
		Long entityId,
		String details,
		String ipAddress,
		Instant createdAt
) {
}
