package com.ubs.controller;

import com.ubs.audit.Auditable;
import com.ubs.dto.customer.CustomerCreateRequest;
import com.ubs.dto.customer.CustomerResponse;
import com.ubs.dto.customer.CustomerUpdateRequest;
import com.ubs.entity.AuditAction;
import com.ubs.security.SecurityExpressions;
import com.ubs.service.CustomerService;
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
@RequestMapping("/customers")
@Tag(name = "Customers", description = "Customer management operations")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@PostMapping
	@PreAuthorize(SecurityExpressions.MANAGE_CUSTOMERS)
	@Auditable(action = AuditAction.CUSTOMER_CREATED, entityType = "Customer")
	@Operation(summary = "Create a new customer")
	public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerCreateRequest request) {
		CustomerResponse response = customerService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{id}")
	@PreAuthorize(SecurityExpressions.VIEW_CUSTOMERS)
	@Operation(summary = "Get a customer by ID")
	public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
		return ResponseEntity.ok(customerService.getById(id));
	}

	@GetMapping
	@PreAuthorize(SecurityExpressions.VIEW_CUSTOMERS)
	@Operation(summary = "List all customers with pagination")
	public ResponseEntity<Page<CustomerResponse>> getAll(
			@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(customerService.getAll(pageable));
	}

	@PutMapping("/{id}")
	@PreAuthorize(SecurityExpressions.MANAGE_CUSTOMERS)
	@Auditable(action = AuditAction.CUSTOMER_UPDATED, entityType = "Customer")
	@Operation(summary = "Update an existing customer")
	public ResponseEntity<CustomerResponse> update(@PathVariable Long id,
												   @Valid @RequestBody CustomerUpdateRequest request) {
		return ResponseEntity.ok(customerService.update(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize(SecurityExpressions.MANAGE_CUSTOMERS)
	@Auditable(action = AuditAction.CUSTOMER_DELETED, entityType = "Customer")
	@Operation(summary = "Delete a customer")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		customerService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
