package com.ubs.controller;

import com.ubs.audit.Auditable;
import com.ubs.dto.meterreading.MeterReadingCreateRequest;
import com.ubs.dto.meterreading.MeterReadingResponse;
import com.ubs.dto.meterreading.MeterReadingUpdateRequest;
import com.ubs.entity.AuditAction;
import com.ubs.security.SecurityExpressions;
import com.ubs.service.MeterReadingService;
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
@RequestMapping("/meter-readings")
@Tag(name = "Meter Readings", description = "Meter reading capture and management")
@SecurityRequirement(name = "bearerAuth")
public class MeterReadingController {

	private final MeterReadingService meterReadingService;

	public MeterReadingController(MeterReadingService meterReadingService) {
		this.meterReadingService = meterReadingService;
	}

	@PostMapping
	@PreAuthorize(SecurityExpressions.MANAGE_METER_READINGS)
	@Auditable(action = AuditAction.METER_READING_CREATED, entityType = "MeterReading")
	@Operation(summary = "Record a new meter reading")
	public ResponseEntity<MeterReadingResponse> create(@Valid @RequestBody MeterReadingCreateRequest request) {
		MeterReadingResponse response = meterReadingService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{id}")
	@PreAuthorize(SecurityExpressions.VIEW_METER_READINGS)
	@Operation(summary = "Get a meter reading by ID")
	public ResponseEntity<MeterReadingResponse> getById(@PathVariable Long id) {
		return ResponseEntity.ok(meterReadingService.getById(id));
	}

	@GetMapping
	@PreAuthorize(SecurityExpressions.VIEW_METER_READINGS)
	@Operation(summary = "List all meter readings with pagination")
	public ResponseEntity<Page<MeterReadingResponse>> getAll(
			@ParameterObject @PageableDefault(size = 20, sort = "readingYear", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(meterReadingService.getAll(pageable));
	}

	@GetMapping("/meter/{meterId}")
	@PreAuthorize(SecurityExpressions.VIEW_METER_READINGS)
	@Operation(summary = "List all readings for a specific meter")
	public ResponseEntity<Page<MeterReadingResponse>> getByMeterId(
			@PathVariable Long meterId,
			@ParameterObject @PageableDefault(size = 20, sort = "readingYear", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(meterReadingService.getByMeterId(meterId, pageable));
	}

	@PutMapping("/{id}")
	@PreAuthorize(SecurityExpressions.MANAGE_METER_READINGS)
	@Auditable(action = AuditAction.METER_READING_UPDATED, entityType = "MeterReading")
	@Operation(summary = "Update an existing meter reading")
	public ResponseEntity<MeterReadingResponse> update(@PathVariable Long id,
													   @Valid @RequestBody MeterReadingUpdateRequest request) {
		return ResponseEntity.ok(meterReadingService.update(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize(SecurityExpressions.MANAGE_METER_READINGS)
	@Auditable(action = AuditAction.METER_READING_DELETED, entityType = "MeterReading")
	@Operation(summary = "Delete a meter reading")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		meterReadingService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
