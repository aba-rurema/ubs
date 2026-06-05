package com.ubs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
@Table(name = "bills", indexes = {
		@Index(name = "idx_bills_bill_number", columnList = "bill_number", unique = true),
		@Index(name = "idx_bills_customer_id", columnList = "customer_id"),
		@Index(name = "idx_bills_meter_id", columnList = "meter_id"),
		@Index(name = "idx_bills_status", columnList = "status")
}, uniqueConstraints = {
		@UniqueConstraint(name = "uk_bills_meter_reading", columnNames = "meter_reading_id"),
		@UniqueConstraint(name = "uk_bills_meter_period",
				columnNames = {"meter_id", "billing_month", "billing_year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "bill_number", nullable = false, unique = true, length = 30)
	private String billNumber;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "meter_id", nullable = false)
	private Meter meter;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "meter_reading_id", nullable = false, unique = true)
	private MeterReading meterReading;

	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal consumption;

	@Column(name = "unit_rate", nullable = false, precision = 12, scale = 2)
	private BigDecimal unitRate;

	@Column(name = "fixed_charges", nullable = false, precision = 12, scale = 2)
	@Builder.Default
	private BigDecimal fixedCharges = BigDecimal.ZERO;

	@Column(name = "base_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal baseAmount;

	@Column(name = "vat_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal vatAmount;

	@Column(name = "penalty_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal penaltyAmount;

	@Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
	@Builder.Default
	private BigDecimal amountPaid = BigDecimal.ZERO;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal balance;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private BillStatus status = BillStatus.PENDING;

	@Column(name = "billing_month", nullable = false)
	private int billingMonth;

	@Column(name = "billing_year", nullable = false)
	private int billingYear;

	@Column(name = "due_date", nullable = false)
	private LocalDate dueDate;

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
