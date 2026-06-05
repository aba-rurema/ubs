package com.ubs.service;

import com.ubs.audit.AuditContext;
import com.ubs.dto.audit.AuditLogResponse;
import com.ubs.entity.AuditAction;
import com.ubs.entity.AuditLog;
import com.ubs.exception.ResourceNotFoundException;
import com.ubs.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;

	public AuditLogService(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void log(AuditAction action, String entityType, Long entityId, String details) {
		log(action, entityType, entityId, details,
				AuditContext.currentUserId(),
				AuditContext.currentUsername(),
				AuditContext.currentRole(),
				AuditContext.currentIpAddress());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void log(AuditAction action,
					String entityType,
					Long entityId,
					String details,
					Long userId,
					String username,
					String role,
					String ipAddress) {
		auditLogRepository.save(AuditLog.builder()
				.userId(userId)
				.username(username)
				.role(role)
				.action(action)
				.entityType(blankToNull(entityType))
				.entityId(entityId)
				.details(truncate(details))
				.ipAddress(ipAddress)
				.build());
	}

	@Transactional(readOnly = true)
	public AuditLogResponse getById(Long id) {
		return toResponse(findOrThrow(id));
	}

	@Transactional(readOnly = true)
	public Page<AuditLogResponse> search(Long userId,
										 AuditAction action,
										 String entityType,
										 Long entityId,
										 Instant from,
										 Instant to,
										 Pageable pageable) {
		return auditLogRepository.search(userId, action, blankToNull(entityType), entityId, from, to, pageable)
				.map(this::toResponse);
	}

	private AuditLog findOrThrow(Long id) {
		return auditLogRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Audit log not found with id: " + id));
	}

	private AuditLogResponse toResponse(AuditLog auditLog) {
		return new AuditLogResponse(
				auditLog.getId(),
				auditLog.getUserId(),
				auditLog.getUsername(),
				auditLog.getRole(),
				auditLog.getAction(),
				auditLog.getEntityType(),
				auditLog.getEntityId(),
				auditLog.getDetails(),
				auditLog.getIpAddress(),
				auditLog.getCreatedAt()
		);
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private String truncate(String details) {
		if (details == null) {
			return null;
		}
		return details.length() > 1000 ? details.substring(0, 1000) : details;
	}

}
