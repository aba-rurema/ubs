package com.ubs.repository;

import com.ubs.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CustomerRepository extends JpaRepository<Customer, Long> {

	boolean existsByNationalId(String nationalId);

	boolean existsByNationalIdAndIdNot(String nationalId, Long id);

}
