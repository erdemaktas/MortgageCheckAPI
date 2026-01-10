package org.ing.mortgage.domain;

import java.math.BigDecimal;

public record Rate(int maturityPeriod, BigDecimal interestRate) {
}
