package com.ubs.controller;

import com.ubs.audit.Auditable;
import com.ubs.dto.payment.PaymentCreateRequest;
import com.ubs.dto.payment.PaymentResponse;
import com.ubs.entity.AuditAction;
import com.ubs.security.CustomUserDetails;
import com.ubs.security.SecurityExpressions;
import com.ubs.service.PaymentService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springdoc.core.annotations.ParameterObject;


@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Partial and full payment processing")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping
	@PreAuthorize(SecurityExpressions.MANAGE_PAYMENTS)
	@Auditable(action = AuditAction.PAYMENT_RECORDED, entityType = "Payment")
	@Operation(summary = "Process a partial or full payment against an approved bill")
	public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentCreateRequest request,
														  @AuthenticationPrincipal CustomUserDetails principal) {
		PaymentResponse response = paymentService.processPayment(request, principal);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/me")
	@PreAuthorize(SecurityExpressions.VIEW_OWN_PAYMENTS)
	@Operation(summary = "List payments for the logged-in customer")
	public ResponseEntity<Page<PaymentResponse>> getMyPayments(
			@AuthenticationPrincipal CustomUserDetails principal,
			@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(paymentService.getMyPayments(principal, pageable));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
	@Operation(summary = "Get a payment by ID")
	public ResponseEntity<PaymentResponse> getById(@PathVariable Long id,
												   @AuthenticationPrincipal CustomUserDetails principal) {
		return ResponseEntity.ok(paymentService.getById(id, principal));
	}

	@GetMapping
	@PreAuthorize(SecurityExpressions.VIEW_ALL_PAYMENTS)
	@Operation(summary = "List all payments (staff only)")
	public ResponseEntity<Page<PaymentResponse>> getAll(
			@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(paymentService.getAll(pageable));
	}

	@GetMapping("/bill/{billId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
	@Operation(summary = "List payments for a specific bill")
	public ResponseEntity<Page<PaymentResponse>> getByBillId(
			@PathVariable Long billId,
			@AuthenticationPrincipal CustomUserDetails principal,
			@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(paymentService.getByBillId(billId, principal, pageable));
	}

	@GetMapping("/customer/{customerId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
	@Operation(summary = "List payments for a customer")
	public ResponseEntity<Page<PaymentResponse>> getByCustomerId(
			@PathVariable Long customerId,
			@AuthenticationPrincipal CustomUserDetails principal,
			@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(paymentService.getByCustomerId(customerId, principal, pageable));
	}

}
