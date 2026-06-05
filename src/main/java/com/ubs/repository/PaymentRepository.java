package com.ubs.repository;

import com.ubs.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface PaymentRepository extends JpaRepository<Payment, Long> {

	boolean existsByBillId(Long billId);

	Page<Payment> findByBillId(Long billId, Pageable pageable);

	Page<Payment> findByBillCustomerId(Long customerId, Pageable pageable);

	@Query("SELECT COUNT(p) FROM Payment p WHERE EXTRACT(YEAR FROM p.paymentDate) = :year")
	long countByPaymentYear(@Param("year") int year);

}
