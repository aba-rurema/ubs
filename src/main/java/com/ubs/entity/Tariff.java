package com.ubs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tariffs", indexes = {
		@Index(name = "idx_tariffs_meter_type", columnList = "meter_type", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tariff {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "meter_type", nullable = false, unique = true, length = 20)
	private MeterType meterType;

	@Column(name = "unit_rate", nullable = false, precision = 12, scale = 2)
	private BigDecimal unitRate;

	@Column(name = "fixed_charges", nullable = false, precision = 12, scale = 2)
	@Builder.Default
	private BigDecimal fixedCharges = BigDecimal.ZERO;

	@Column(name = "vat_percentage", nullable = false, precision = 5, scale = 2)
	@Builder.Default
	private BigDecimal vatPercentage = new BigDecimal("18.00");

	@Column(name = "penalty_percentage", nullable = false, precision = 5, scale = 2)
	@Builder.Default
	private BigDecimal penaltyPercentage = new BigDecimal("5.00");

	@Column(length = 255)
	private String description;

	@Column(nullable = false)
	@Builder.Default
	private boolean active = true;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

}
