package com.ubs.controller;

import com.ubs.audit.Auditable;
import com.ubs.dto.bill.BillGenerateRequest;
import com.ubs.dto.bill.BillResponse;
import com.ubs.entity.AuditAction;
import com.ubs.security.CustomUserDetails;
import com.ubs.security.SecurityExpressions;
import com.ubs.service.BillService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springdoc.core.annotations.ParameterObject;


@RestController
@RequestMapping("/bills")
@Tag(name = "Bills", description = "Bill generation, approval, and billing lifecycle")
@SecurityRequirement(name = "bearerAuth")
public class BillController {

	private final BillService billService;

	public BillController(BillService billService) {
		this.billService = billService;
	}

	@PostMapping("/generate")
	@PreAuthorize(SecurityExpressions.MANAGE_BILLS)
	@Auditable(action = AuditAction.BILL_GENERATED, entityType = "Bill")
	@Operation(summary = "Generate a bill from a meter reading")
	public ResponseEntity<BillResponse> generate(@Valid @RequestBody BillGenerateRequest request) {
		BillResponse response = billService.generate(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/{id}/approve")
	@PreAuthorize(SecurityExpressions.MANAGE_BILLS)
	@Auditable(action = AuditAction.BILL_APPROVED, entityType = "Bill")
	@Operation(summary = "Approve a pending bill and notify the customer")
	public ResponseEntity<BillResponse> approve(@PathVariable Long id) {
		return ResponseEntity.ok(billService.approve(id));
	}

	@GetMapping("/me")
	@PreAuthorize(SecurityExpressions.VIEW_OWN_BILLS)
	@Operation(summary = "List bills for the logged-in customer")
	public ResponseEntity<Page<BillResponse>> getMyBills(
			@AuthenticationPrincipal CustomUserDetails principal,
			@ParameterObject @PageableDefault(size = 20, sort = "billingYear", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(billService.getMyBills(principal, pageable));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
	@Operation(summary = "Get a bill by ID")
	public ResponseEntity<BillResponse> getById(@PathVariable Long id,
												@AuthenticationPrincipal CustomUserDetails principal) {
		return ResponseEntity.ok(billService.getById(id, principal));
	}

	@GetMapping
	@PreAuthorize(SecurityExpressions.VIEW_ALL_BILLS)
	@Operation(summary = "List all bills (staff only)")
	public ResponseEntity<Page<BillResponse>> getAll(
			@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(billService.getAll(pageable));
	}

	@GetMapping("/customer/{customerId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
	@Operation(summary = "List bills for a customer")
	public ResponseEntity<Page<BillResponse>> getByCustomerId(
			@PathVariable Long customerId,
			@AuthenticationPrincipal CustomUserDetails principal,
			@ParameterObject @PageableDefault(size = 20, sort = "billingYear", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(billService.getByCustomerId(customerId, principal, pageable));
	}

	@GetMapping("/meter/{meterId}")
	@PreAuthorize(SecurityExpressions.VIEW_ALL_BILLS)
	@Operation(summary = "List bills for a meter")
	public ResponseEntity<Page<BillResponse>> getByMeterId(
			@PathVariable Long meterId,
			@ParameterObject @PageableDefault(size = 20, sort = "billingYear", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(billService.getByMeterId(meterId, pageable));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize(SecurityExpressions.MANAGE_BILLS)
	@Auditable(action = AuditAction.BILL_DELETED, entityType = "Bill")
	@Operation(summary = "Delete a bill with no recorded payments")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		billService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
