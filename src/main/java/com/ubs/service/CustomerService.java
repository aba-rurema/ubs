package com.ubs.service;

import com.ubs.dto.customer.CustomerCreateRequest;
import com.ubs.dto.customer.CustomerResponse;
import com.ubs.dto.customer.CustomerUpdateRequest;
import com.ubs.entity.Customer;
import com.ubs.exception.DuplicateResourceException;
import com.ubs.exception.ResourceNotFoundException;
import com.ubs.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CustomerService {

	private final CustomerRepository customerRepository;

	public CustomerService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Transactional
	public CustomerResponse create(CustomerCreateRequest request) {
		validateNationalIdUnique(request.nationalId(), null);

		Customer customer = Customer.builder()
				.fullNames(request.fullNames().trim())
				.nationalId(request.nationalId().trim())
				.email(request.email().trim().toLowerCase())
				.phone(request.phone().trim())
				.address(request.address().trim())
				.status(request.status())
				.build();

		return toResponse(customerRepository.save(customer));
	}

	@Transactional(readOnly = true)
	public CustomerResponse getById(Long id) {
		return toResponse(findCustomerOrThrow(id));
	}

	@Transactional(readOnly = true)
	public Page<CustomerResponse> getAll(Pageable pageable) {
		return customerRepository.findAll(pageable).map(this::toResponse);
	}

	@Transactional
	public CustomerResponse update(Long id, CustomerUpdateRequest request) {
		Customer customer = findCustomerOrThrow(id);
		validateNationalIdUnique(request.nationalId(), id);

		customer.setFullNames(request.fullNames().trim());
		customer.setNationalId(request.nationalId().trim());
		customer.setEmail(request.email().trim().toLowerCase());
		customer.setPhone(request.phone().trim());
		customer.setAddress(request.address().trim());
		customer.setStatus(request.status());

		return toResponse(customerRepository.save(customer));
	}

	@Transactional
	public void delete(Long id) {
		Customer customer = findCustomerOrThrow(id);
		customerRepository.delete(customer);
	}

	private Customer findCustomerOrThrow(Long id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
	}

	private void validateNationalIdUnique(String nationalId, Long excludeId) {
		String normalizedNationalId = nationalId.trim();
		boolean exists = excludeId == null
				? customerRepository.existsByNationalId(normalizedNationalId)
				: customerRepository.existsByNationalIdAndIdNot(normalizedNationalId, excludeId);

		if (exists) {
			throw new DuplicateResourceException("National ID is already registered: " + normalizedNationalId);
		}
	}

	private CustomerResponse toResponse(Customer customer) {
		return new CustomerResponse(
				customer.getId(),
				customer.getFullNames(),
				customer.getNationalId(),
				customer.getEmail(),
				customer.getPhone(),
				customer.getAddress(),
				customer.getStatus(),
				customer.getCreatedAt(),
				customer.getUpdatedAt()
		);
	}

}
