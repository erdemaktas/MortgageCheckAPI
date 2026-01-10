package org.ing.mortgage.domain;

import java.math.BigDecimal;

public record MortgageResult(boolean feasible, BigDecimal monthlyCost) {
}
