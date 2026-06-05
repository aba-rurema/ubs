package com.ubs.controller;

import com.ubs.audit.Auditable;
import com.ubs.dto.user.UserCreateRequest;
import com.ubs.dto.user.UserResponse;
import com.ubs.dto.user.UserUpdateRequest;
import com.ubs.entity.AuditAction;
import com.ubs.security.SecurityExpressions;
import com.ubs.service.UserManagementService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springdoc.core.annotations.ParameterObject;


@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User account management (admin only)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

	private final UserManagementService userManagementService;

	public UserController(UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}

	@PostMapping
	@PreAuthorize(SecurityExpressions.MANAGE_USERS)
	@Auditable(action = AuditAction.USER_CREATED, entityType = "User")
	@Operation(summary = "Create a new user account")
	public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
		UserResponse response = userManagementService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{id}")
	@PreAuthorize(SecurityExpressions.MANAGE_USERS)
	@Operation(summary = "Get a user by ID")
	public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
		return ResponseEntity.ok(userManagementService.getById(id));
	}

	@GetMapping
	@PreAuthorize(SecurityExpressions.MANAGE_USERS)
	@Operation(summary = "List all users with pagination")
	public ResponseEntity<Page<UserResponse>> getAll(
			@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(userManagementService.getAll(pageable));
	}

	@PutMapping("/{id}")
	@PreAuthorize(SecurityExpressions.MANAGE_USERS)
	@Auditable(action = AuditAction.USER_UPDATED, entityType = "User")
	@Operation(summary = "Update a user account")
	public ResponseEntity<UserResponse> update(@PathVariable Long id,
											   @Valid @RequestBody UserUpdateRequest request) {
		return ResponseEntity.ok(userManagementService.update(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize(SecurityExpressions.MANAGE_USERS)
	@Auditable(action = AuditAction.USER_DELETED, entityType = "User")
	@Operation(summary = "Delete a user account")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		userManagementService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
