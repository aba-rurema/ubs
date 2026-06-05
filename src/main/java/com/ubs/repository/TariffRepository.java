package com.ubs.repository;

import com.ubs.entity.MeterType;
import com.ubs.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TariffRepository extends JpaRepository<Tariff, Long> {

	Optional<Tariff> findByMeterType(MeterType meterType);

	boolean existsByMeterType(MeterType meterType);

	boolean existsByMeterTypeAndIdNot(MeterType meterType, Long id);

}
