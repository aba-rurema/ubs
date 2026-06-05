package com.ubs.controller;

import com.ubs.audit.Auditable;
import com.ubs.dto.meter.MeterCreateRequest;
import com.ubs.dto.meter.MeterResponse;
import com.ubs.dto.meter.MeterUpdateRequest;
import com.ubs.entity.AuditAction;
import com.ubs.security.SecurityExpressions;
import com.ubs.service.MeterService;
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
@RequestMapping("/meters")
@Tag(name = "Meters", description = "Meter management operations")
@SecurityRequirement(name = "bearerAuth")
public class MeterController {

	private final MeterService meterService;

	public MeterController(MeterService meterService) {
		this.meterService = meterService;
	}

	@PostMapping
	@PreAuthorize(SecurityExpressions.MANAGE_METERS)
	@Auditable(action = AuditAction.METER_CREATED, entityType = "Meter")
	@Operation(summary = "Register a new meter for a customer")
	public ResponseEntity<MeterResponse> create(@Valid @RequestBody MeterCreateRequest request) {
		MeterResponse response = meterService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{id}")
	@PreAuthorize(SecurityExpressions.VIEW_METERS)
	@Operation(summary = "Get a meter by ID")
	public ResponseEntity<MeterResponse> getById(@PathVariable Long id) {
		return ResponseEntity.ok(meterService.getById(id));
	}

	@GetMapping
	@PreAuthorize(SecurityExpressions.VIEW_METERS)
	@Operation(summary = "List all meters with pagination")
	public ResponseEntity<Page<MeterResponse>> getAll(
			@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(meterService.getAll(pageable));
	}

	@GetMapping("/customer/{customerId}")
	@PreAuthorize(SecurityExpressions.VIEW_METERS)
	@Operation(summary = "List all meters owned by a customer")
	public ResponseEntity<Page<MeterResponse>> getByCustomerId(
			@PathVariable Long customerId,
			@ParameterObject @PageableDefault(size = 20, sort = "installationDate", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(meterService.getByCustomerId(customerId, pageable));
	}

	@PutMapping("/{id}")
	@PreAuthorize(SecurityExpressions.MANAGE_METERS)
	@Auditable(action = AuditAction.METER_UPDATED, entityType = "Meter")
	@Operation(summary = "Update an existing meter")
	public ResponseEntity<MeterResponse> update(@PathVariable Long id,
												@Valid @RequestBody MeterUpdateRequest request) {
		return ResponseEntity.ok(meterService.update(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize(SecurityExpressions.MANAGE_METERS)
	@Auditable(action = AuditAction.METER_DELETED, entityType = "Meter")
	@Operation(summary = "Delete a meter")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		meterService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
