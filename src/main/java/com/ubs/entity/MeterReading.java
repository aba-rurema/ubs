package com.ubs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "meter_readings", indexes = {
		@Index(name = "idx_meter_readings_meter_id", columnList = "meter_id")
}, uniqueConstraints = {
		@UniqueConstraint(name = "uk_meter_readings_meter_period",
				columnNames = {"meter_id", "reading_month", "reading_year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeterReading {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "meter_id", nullable = false)
	private Meter meter;

	@Column(name = "current_reading", nullable = false, precision = 12, scale = 3)
	private BigDecimal currentReading;

	@Column(name = "previous_reading", nullable = false, precision = 12, scale = 3)
	private BigDecimal previousReading;

	@Column(name = "consumption", nullable = false, precision = 12, scale = 3)
	private BigDecimal consumption;

	@Column(name = "reading_month", nullable = false)
	private int readingMonth;

	@Column(name = "reading_year", nullable = false)
	private int readingYear;

	@Column(name = "reading_date", nullable = false)
	private LocalDate readingDate;

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
