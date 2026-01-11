package org.ing.mortgage.application.engine;

import org.ing.mortgage.application.service.InterestRateService;
import org.ing.mortgage.application.service.MortgageService;
import org.ing.mortgage.domain.InterestRate;
import org.ing.mortgage.domain.MortgageInput;
import org.ing.mortgage.domain.MortgageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MortgageEngineTest {

    @Mock
    private MortgageService mortgageService;

    @Mock
    private InterestRateService interestRateService;

    @InjectMocks
    private MortgageEngine mortgageEngine;

    @Test
    @DisplayName("calculateMortgageCost: feasible input and rate found returns monthly cost")
    void calculateMortgageCost_feasibleAndRateFound_returnsMonthlyCost() {

        int maturity = 30;
        BigDecimal loanValue = new BigDecimal("200000");
        MortgageInput input = MortgageInput.builder().loanValue(loanValue).maturityPeriod(maturity).build();

        InterestRate mockRate = mock(InterestRate.class);
        BigDecimal expectedMonthly = new BigDecimal("1200.50");

        when(mortgageService.isFeasible(input)).thenReturn(true);
        when(interestRateService.findByMaturity(maturity)).thenReturn(Optional.of(mockRate));
        when(mortgageService.calculateMonthlyCost(loanValue, mockRate)).thenReturn(expectedMonthly);

        MortgageResult result = mortgageEngine.calculateMortgageCost(input);

        assertTrue(result.feasible(), "Expected mortgage to be feasible");
        assertNotNull(result.monthlyCost(), "Expected monthly cost to be present");
        assertEquals(0, expectedMonthly.compareTo(result.monthlyCost()), "Monthly cost should match");

        verify(mortgageService).isFeasible(input);
        verify(interestRateService).findByMaturity(maturity);
        verify(mortgageService).calculateMonthlyCost(loanValue, mockRate);
        verifyNoMoreInteractions(mortgageService, interestRateService);
    }

    @Test
    @DisplayName("calculateMortgageCost: not feasible input skips rate lookup and returns null cost")
    void calculateMortgageCost_notFeasible_returnsFalseAndNullAndSkipsRateLookup() {
        MortgageInput input = mock(MortgageInput.class);

        when(mortgageService.isFeasible(input)).thenReturn(false);

        MortgageResult result = mortgageEngine.calculateMortgageCost(input);

        assertFalse(result.feasible(), "Expected mortgage to be not feasible");
        assertNull(result.monthlyCost(), "Expected monthly cost to be null when not feasible");

        verify(mortgageService).isFeasible(input);
        verifyNoInteractions(interestRateService);
        verify(mortgageService, never()).calculateMonthlyCost(any(), any());
        verifyNoMoreInteractions(mortgageService);
    }

    @Test
    @DisplayName("calculateMortgageCost: feasible input but missing rate throws IllegalArgumentException")
    void calculateMortgageCost_feasibleButMissingRate_throwsIllegalArgumentException() {
        int maturity = 15;
        BigDecimal loanValue = new BigDecimal("300000");
        MortgageInput input = MortgageInput.builder().loanValue(loanValue).maturityPeriod(maturity).build();

        when(mortgageService.isFeasible(input)).thenReturn(true);
        when(interestRateService.findByMaturity(maturity)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> mortgageEngine.calculateMortgageCost(input),
                "Expected exception when rate not configured"
        );
        assertEquals("No interest rate configured for maturity period=" + maturity + "years",
                ex.getMessage(),
                "Exception message should include maturity period"
        );

        verify(mortgageService).isFeasible(input);
        verify(interestRateService).findByMaturity(maturity);
        verify(mortgageService, never()).calculateMonthlyCost(any(), any());
        verifyNoMoreInteractions(mortgageService, interestRateService);
    }
}
