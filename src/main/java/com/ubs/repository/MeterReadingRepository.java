package com.ubs.repository;

import com.ubs.entity.MeterReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {

	boolean existsByMeterIdAndReadingMonthAndReadingYear(Long meterId, int readingMonth, int readingYear);

	boolean existsByMeterIdAndReadingMonthAndReadingYearAndIdNot(Long meterId,
																   int readingMonth,
																   int readingYear,
																   Long id);

	Page<MeterReading> findByMeterId(Long meterId, Pageable pageable);

	@Query("""
			SELECT r FROM MeterReading r
			WHERE r.meter.id = :meterId
			AND (r.readingYear < :year OR (r.readingYear = :year AND r.readingMonth < :month))
			ORDER BY r.readingYear DESC, r.readingMonth DESC
			LIMIT 1
			""")
	Optional<MeterReading> findLatestBeforePeriod(@Param("meterId") Long meterId,
													@Param("month") int month,
													@Param("year") int year);

	@Query("""
			SELECT r FROM MeterReading r
			WHERE r.meter.id = :meterId
			AND (r.readingYear > :year OR (r.readingYear = :year AND r.readingMonth > :month))
			ORDER BY r.readingYear ASC, r.readingMonth ASC
			LIMIT 1
			""")
	Optional<MeterReading> findEarliestAfterPeriod(@Param("meterId") Long meterId,
												   @Param("month") int month,
												   @Param("year") int year);

}
