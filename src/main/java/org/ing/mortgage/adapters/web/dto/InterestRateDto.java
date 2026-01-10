package org.ing.mortgage.adapters.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record InterestRateDto(Integer maturityPeriod, BigDecimal interestRate, Instant lastUpdate) {
}
