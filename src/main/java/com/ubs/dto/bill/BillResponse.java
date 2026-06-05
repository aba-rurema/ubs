package com.ubs.dto.bill;

import com.ubs.entity.BillStatus;
import com.ubs.entity.MeterType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BillResponse(
		Long id,
		String billNumber,
		Long customerId,
		String customerFullNames,
		Long meterId,
		String meterNumber,
		MeterType meterType,
		Long meterReadingId,
		BigDecimal consumption,
		BigDecimal unitRate,
		BigDecimal fixedCharges,
		BigDecimal baseAmount,
		BigDecimal vatAmount,
		BigDecimal penaltyAmount,
		BigDecimal totalAmount,
		BigDecimal amountPaid,
		BigDecimal balance,
		BillStatus status,
		int billingMonth,
		int billingYear,
		LocalDate dueDate,
		Instant createdAt,
		Instant updatedAt
) {
}
