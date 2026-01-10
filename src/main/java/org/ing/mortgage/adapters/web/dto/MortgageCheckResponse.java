package org.ing.mortgage.adapters.web.dto;

import java.math.BigDecimal;

public record MortgageCheckResponse(Boolean feasible, BigDecimal monthlyCost) {
}
