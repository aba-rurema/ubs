package com.ubs.security;

import com.ubs.entity.Bill;
import com.ubs.entity.Customer;
import com.ubs.entity.Role;
import com.ubs.exception.BusinessRuleViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;


@Component
public class CustomerAccessService {

	public boolean isCustomer(CustomUserDetails principal) {
		return principal.getRoles().contains(Role.ROLE_CUSTOMER);
	}

	public Long requireLinkedCustomerId(CustomUserDetails principal) {
		if (principal.getCustomerId() == null) {
			throw new BusinessRuleViolationException("No customer profile linked to this user account");
		}
		return principal.getCustomerId();
	}

	public void validateCustomerOwnership(CustomUserDetails principal, Long customerId) {
		if (!isCustomer(principal)) {
			return;
		}
		Long linkedCustomerId = requireLinkedCustomerId(principal);
		if (!linkedCustomerId.equals(customerId)) {
			throw new AccessDeniedException("Access denied: you can only access your own customer data");
		}
	}

	public void validateBillAccess(CustomUserDetails principal, Bill bill) {
		validateCustomerOwnership(principal, bill.getCustomer().getId());
	}

	public void validateCustomerAccess(CustomUserDetails principal, Customer customer) {
		validateCustomerOwnership(principal, customer.getId());
	}

}
