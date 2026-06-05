package com.ubs.dto.payment;

import com.ubs.entity.PaymentMethod;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentCreateRequest(
		@NotNull(message = "Bill ID is required")
		Long billId,

		@Digits(integer = 10, fraction = 2, message = "Payment amount must have at most 10 integer digits and 2 decimal places")
		BigDecimal amount,

		boolean payFullBalance,

		@NotNull(message = "Payment method is required")
		PaymentMethod paymentMethod,

		@NotNull(message = "Payment date is required")
		LocalDate paymentDate,

		@Size(max = 255, message = "Notes must not exceed 255 characters")
		String notes
) {
}
