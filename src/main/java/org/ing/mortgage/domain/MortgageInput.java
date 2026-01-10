package org.ing.mortgage.domain;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record MortgageInput(BigDecimal income, Integer maturityPeriod, BigDecimal loanValue, BigDecimal homeValue) {
}
