package org.ing.mortgage.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record InterestRate(Integer maturityPeriod, BigDecimal interestRate, Instant lastUpdate) {
}
