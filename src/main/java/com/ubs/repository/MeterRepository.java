package com.ubs.repository;

import com.ubs.entity.Meter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MeterRepository extends JpaRepository<Meter, Long> {

	boolean existsByMeterNumber(String meterNumber);

	boolean existsByMeterNumberAndIdNot(String meterNumber, Long id);

	Page<Meter> findByCustomerId(Long customerId, Pageable pageable);

}
