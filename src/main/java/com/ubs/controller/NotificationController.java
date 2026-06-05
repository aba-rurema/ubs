package com.ubs.controller;

import com.ubs.audit.Auditable;
import com.ubs.dto.notification.NotificationCreateRequest;
import com.ubs.dto.notification.NotificationFailureRequest;
import com.ubs.dto.notification.NotificationResponse;
import com.ubs.entity.AuditAction;
import com.ubs.entity.NotificationStatus;
import com.ubs.security.SecurityExpressions;
import com.ubs.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springdoc.core.annotations.ParameterObject;


@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "Customer notification management")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@PostMapping
	@PreAuthorize(SecurityExpressions.MANAGE_NOTIFICATIONS)
	@Auditable(action = AuditAction.NOTIFICATION_CREATED, entityType = "Notification")
	@Operation(summary = "Create a notification for a customer")
	public ResponseEntity<NotificationResponse> create(@Valid @RequestBody NotificationCreateRequest request) {
		NotificationResponse response = notificationService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/{id}/sent")
	@PreAuthorize(SecurityExpressions.MANAGE_NOTIFICATIONS)
	@Auditable(action = AuditAction.NOTIFICATION_SENT, entityType = "Notification")
	@Operation(summary = "Mark a notification as sent")
	public ResponseEntity<NotificationResponse> markAsSent(@PathVariable Long id) {
		return ResponseEntity.ok(notificationService.markAsSent(id));
	}

	@PostMapping("/{id}/failed")
	@PreAuthorize(SecurityExpressions.MANAGE_NOTIFICATIONS)
	@Auditable(action = AuditAction.NOTIFICATION_FAILED, entityType = "Notification")
	@Operation(summary = "Mark a notification as failed")
	public ResponseEntity<NotificationResponse> markAsFailed(@PathVariable Long id,
														   @Valid @RequestBody NotificationFailureRequest request) {
		return ResponseEntity.ok(notificationService.markAsFailed(id, request));
	}

	@PostMapping("/{id}/read")
	@PreAuthorize(SecurityExpressions.VIEW_NOTIFICATIONS)
	@Auditable(action = AuditAction.NOTIFICATION_READ, entityType = "Notification")
	@Operation(summary = "Mark a notification as read")
	public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
		return ResponseEntity.ok(notificationService.markAsRead(id));
	}

	@GetMapping("/{id}")
	@PreAuthorize(SecurityExpressions.VIEW_NOTIFICATIONS)
	@Operation(summary = "Get a notification by ID")
	public ResponseEntity<NotificationResponse> getById(@PathVariable Long id) {
		return ResponseEntity.ok(notificationService.getById(id));
	}

	@GetMapping
	@PreAuthorize(SecurityExpressions.MANAGE_NOTIFICATIONS)
	@Operation(summary = "List all notifications with optional status filter")
	public ResponseEntity<Page<NotificationResponse>> getAll(
			@RequestParam(required = false) NotificationStatus status,
			@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		if (status != null) {
			return ResponseEntity.ok(notificationService.getByStatus(status, pageable));
		}
		return ResponseEntity.ok(notificationService.getAll(pageable));
	}

	@GetMapping("/customer/{customerId}")
	@PreAuthorize(SecurityExpressions.VIEW_NOTIFICATIONS)
	@Operation(summary = "List notifications for a customer with optional status filter")
	public ResponseEntity<Page<NotificationResponse>> getByCustomerId(
			@PathVariable Long customerId,
			@RequestParam(required = false) NotificationStatus status,
			@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		if (status != null) {
			return ResponseEntity.ok(notificationService.getByCustomerIdAndStatus(customerId, status, pageable));
		}
		return ResponseEntity.ok(notificationService.getByCustomerId(customerId, pageable));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize(SecurityExpressions.MANAGE_USERS)
	@Auditable(action = AuditAction.NOTIFICATION_DELETED, entityType = "Notification")
	@Operation(summary = "Delete a notification")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		notificationService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
