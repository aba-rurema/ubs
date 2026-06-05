package com.ubs.config;

import com.ubs.entity.MeterType;
import com.ubs.exception.BusinessRuleViolationException;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Map;

@ConfigurationProperties(prefix = "app.billing")
@Validated
public record BillingProperties(
		@NotNull @Positive BigDecimal vatRate,
		@NotNull @Positive BigDecimal penaltyRate,
		@Positive int dueDays,
		@NotEmpty Map<String, BigDecimal> rates
) {

	public BigDecimal getRateFor(MeterType meterType) {
		BigDecimal rate = rates.get(meterType.name().toLowerCase());
		if (rate == null) {
			throw new BusinessRuleViolationException(
					"No billing rate configured for meter type: " + meterType);
		}
		return rate;
	}

}
