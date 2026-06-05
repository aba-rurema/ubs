package com.ubs.controller;

import com.ubs.dto.audit.AuditLogResponse;
import com.ubs.entity.AuditAction;
import com.ubs.security.SecurityExpressions;
import com.ubs.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springdoc.core.annotations.ParameterObject;

import java.time.Instant;

@RestController
@RequestMapping("/audit-logs")
@Tag(name = "Audit Logs", description = "System activity audit trail (admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

	private final AuditLogService auditLogService;

	public AuditLogController(AuditLogService auditLogService) {
		this.auditLogService = auditLogService;
	}

	@GetMapping
	@PreAuthorize(SecurityExpressions.VIEW_AUDIT_LOGS)
	@Operation(summary = "Search audit logs with optional filters")
	public ResponseEntity<Page<AuditLogResponse>> search(
			@RequestParam(required = false) Long userId,
			@RequestParam(required = false) AuditAction action,
			@RequestParam(required = false) String entityType,
			@RequestParam(required = false) Long entityId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
			@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(auditLogService.search(userId, action, entityType, entityId, from, to, pageable));
	}

	@GetMapping("/{id}")
	@PreAuthorize(SecurityExpressions.VIEW_AUDIT_LOGS)
	@Operation(summary = "Get an audit log entry by ID")
	public ResponseEntity<AuditLogResponse> getById(@PathVariable Long id) {
		return ResponseEntity.ok(auditLogService.getById(id));
	}

}
