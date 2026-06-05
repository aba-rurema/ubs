package com.ubs.dto.payment;

import com.ubs.entity.BillStatus;
import com.ubs.entity.PaymentMethod;
import com.ubs.entity.PaymentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PaymentResponse(
		Long id,
		String paymentReference,
		Long billId,
		String billNumber,
		Long customerId,
		String customerFullNames,
		BigDecimal amount,
		PaymentMethod paymentMethod,
		PaymentType paymentType,
		LocalDate paymentDate,
		BigDecimal balanceBefore,
		BigDecimal balanceAfter,
		BigDecimal billBalance,
		BillStatus billStatus,
		String notes,
		Instant createdAt
) {
}
