package org.ing.mortgage.adapters.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MortgageCheckRequest {
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Income must be greater than 0")
    private BigDecimal income;

    @NotNull
    @Min(value = 1, message = "Maturity Period must be between 1 and 30 years")
    @Max(value = 30, message = "Maturity Period must be between 1 and 30 years")
    private Integer maturityPeriod;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Loan Value must be greater than 0")
    private BigDecimal loanValue;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Home Value must be greater than 0")
    private BigDecimal homeValue;


}
