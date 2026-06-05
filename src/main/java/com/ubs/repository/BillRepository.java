package com.ubs.repository;

import com.ubs.entity.Bill;
import com.ubs.entity.BillStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {

	boolean existsByMeterReadingId(Long meterReadingId);

	boolean existsByMeterIdAndBillingMonthAndBillingYear(Long meterId, int billingMonth, int billingYear);

	Page<Bill> findByCustomerId(Long customerId, Pageable pageable);

	Page<Bill> findByMeterId(Long meterId, Pageable pageable);

	@Query("""
			SELECT b FROM Bill b
			WHERE b.customer.id = :customerId
			AND b.balance > 0
			AND b.dueDate < :today
			AND b.status IN :statuses
			""")
	List<Bill> findOverdueBills(@Param("customerId") Long customerId,
								  @Param("today") LocalDate today,
								  @Param("statuses") List<BillStatus> statuses);

	@Query("""
			SELECT b FROM Bill b
			WHERE b.balance > 0
			AND b.dueDate < :today
			AND b.status IN :statuses
			""")
	List<Bill> findBillsEligibleForOverdue(@Param("today") LocalDate today,
										   @Param("statuses") List<BillStatus> statuses);

	long countByBillingYearAndBillingMonth(int billingYear, int billingMonth);

}
