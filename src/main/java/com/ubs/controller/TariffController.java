package com.ubs.controller;

import com.ubs.audit.Auditable;
import com.ubs.dto.tariff.TariffCreateRequest;
import com.ubs.dto.tariff.TariffResponse;
import com.ubs.dto.tariff.TariffUpdateRequest;
import com.ubs.entity.AuditAction;
import com.ubs.entity.MeterType;
import com.ubs.security.SecurityExpressions;
import com.ubs.service.TariffService;
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
@RequestMapping("/tariffs")
@Tag(name = "Tariffs", description = "Utility tariff management (admin only)")
@SecurityRequirement(name = "bearerAuth")
public class TariffController {

	private final TariffService tariffService;

	public TariffController(TariffService tariffService) {
		this.tariffService = tariffService;
	}

	@PostMapping
	@PreAuthorize(SecurityExpressions.MANAGE_TARIFFS)
	@Auditable(action = AuditAction.TARIFF_CREATED, entityType = "Tariff")
	@Operation(summary = "Create a tariff for a meter type")
	public ResponseEntity<TariffResponse> create(@Valid @RequestBody TariffCreateRequest request) {
		TariffResponse response = tariffService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{id}")
	@PreAuthorize(SecurityExpressions.MANAGE_TARIFFS)
	@Operation(summary = "Get a tariff by ID")
	public ResponseEntity<TariffResponse> getById(@PathVariable Long id) {
		return ResponseEntity.ok(tariffService.getById(id));
	}

	@GetMapping("/meter-type/{meterType}")
	@PreAuthorize(SecurityExpressions.MANAGE_TARIFFS)
	@Operation(summary = "Get a tariff by meter type")
	public ResponseEntity<TariffResponse> getByMeterType(@PathVariable MeterType meterType) {
		return ResponseEntity.ok(tariffService.getByMeterType(meterType));
	}

	@GetMapping
	@PreAuthorize(SecurityExpressions.MANAGE_TARIFFS)
	@Operation(summary = "List all tariffs with pagination")
	public ResponseEntity<Page<TariffResponse>> getAll(
			@ParameterObject @PageableDefault(size = 20, sort = "meterType", direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok(tariffService.getAll(pageable));
	}

	@PutMapping("/{id}")
	@PreAuthorize(SecurityExpressions.MANAGE_TARIFFS)
	@Auditable(action = AuditAction.TARIFF_UPDATED, entityType = "Tariff")
	@Operation(summary = "Update a tariff")
	public ResponseEntity<TariffResponse> update(@PathVariable Long id,
												 @Valid @RequestBody TariffUpdateRequest request) {
		return ResponseEntity.ok(tariffService.update(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize(SecurityExpressions.MANAGE_TARIFFS)
	@Auditable(action = AuditAction.TARIFF_DELETED, entityType = "Tariff")
	@Operation(summary = "Delete a tariff")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		tariffService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
