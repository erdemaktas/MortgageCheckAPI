package org.ing.mortgage.application.service;

import org.ing.mortgage.domain.InterestRate;
import org.ing.mortgage.domain.MortgageInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MortgageServiceTest {
    private final MortgageService service = new MortgageService();

    @Test
    @DisplayName("isFeasible: true when loan <= 4*income and loan <= homeValue")
    void isFeasible_true_whenBothRulesPass() {
        MortgageInput input = mock(MortgageInput.class);
        ReflectionTestUtils.setField(service, "incomeMultiplier", 4);

        when(input.loanValue()).thenReturn(new BigDecimal("200000"));
        when(input.income()).thenReturn(new BigDecimal("60000"));
        when(input.homeValue()).thenReturn(new BigDecimal("220000"));
        boolean feasible = service.isFeasible(input);

        assertTrue(feasible, "Expected feasibility to be true");
        verify(input, times(2)).loanValue();
        verify(input).income();
        verify(input).homeValue();
        verifyNoMoreInteractions(input);
    }

    @Test
    @DisplayName("isFeasible: true when loan <= 4*income and loan <= homeValue")
    void isFeasible_false_whenIncomeRulesFails() {
        MortgageInput input = mock(MortgageInput.class);
        ReflectionTestUtils.setField(service, "incomeMultiplier", 4);
        when(input.loanValue()).thenReturn(new BigDecimal("250000"));
        when(input.income()).thenReturn(new BigDecimal("60000"));
        when(input.homeValue()).thenReturn(new BigDecimal("300000"));
        boolean feasible = service.isFeasible(input);
        assertFalse(feasible, "Expected feasibility to be false due to income rule");
        verify(input).loanValue();
        verify(input).income();
        verify(input, never()).homeValue();
        verifyNoMoreInteractions(input);
    }


    @Test
    @DisplayName("isFeasible: false when loan > homeValue (income rule passes)")
    void isFeasible_false_whenHomeVaLueRuleFails() {
        MortgageInput input = mock(MortgageInput.class);
        ReflectionTestUtils.setField(service, "incomeMultiplier", 4);

        when(input.loanValue()).thenReturn(new BigDecimal("260000"));
        when(input.income()).thenReturn(new BigDecimal("100000"));
        when(input.homeValue()).thenReturn(new BigDecimal("200000"));
        boolean feasible = service.isFeasible(input);

        assertFalse(feasible, "Expected feasibility to be false due to home value");
        verify(input, times(2)).loanValue();
        verify(input).income();
        verify(input).homeValue();
        verifyNoMoreInteractions(input);
    }

    @Test
    @DisplayName("isFeasible: boundary equals (loan == 4*income) returns true")
    void isFeasible_true_onIncomeBoundary() {
        MortgageInput input = mock(MortgageInput.class);
        ReflectionTestUtils.setField(service, "incomeMultiplier", 4);

        when(input.loanValue()).thenReturn(new BigDecimal("240000"));
        when(input.income()).thenReturn(new BigDecimal("60000"));
        when(input.homeValue()).thenReturn(new BigDecimal("300000"));

        assertTrue(service.isFeasible(input));
    }

    @Test
    @DisplayName("calculateMonthlyCost: zero interest → straight amortization with HALF_UP to 2 decimals")
    void calculateMonthlyCost_zeroInterest_returnsStraightAmortization() {
        InterestRate rate = mock(InterestRate.class);

        when(rate.maturityPeriod()).thenReturn(30);
        when(rate.interestRate()).thenReturn(BigDecimal.ZERO);

        BigDecimal loanValue = new BigDecimal("200000");
        BigDecimal result = service.calculateMonthlyCost(loanValue, rate);
        assertEquals(new BigDecimal("555.56"), result);
        verify(rate).maturityPeriod();
        verify(rate).interestRate();
        verifyNoMoreInteractions(rate);
    }

    @Test
    @DisplayName("isFeasible: boundary equals (loan == homeValue) returns true")
    void isFeasible_true_onHomeValueBoundary() {
        MortgageInput input = mock(MortgageInput.class);
        ReflectionTestUtils.setField(service, "incomeMultiplier", 4);

        when(input.loanValue()).thenReturn(new BigDecimal("200000"));
        when(input.income()).thenReturn(new BigDecimal("80000"));
        when(input.homeValue()).thenReturn(new BigDecimal("200000"));
        assertTrue(service.isFeasible(input));

    }

    @ParameterizedTest(name = "calculateMonthlyCost: loan={0}, years={1}, annual%={2}")
    @CsvSource({
// loan, maturityYears, annualInterestPercent
            "200000, 30, 6.0",
            "120000, 30, 6.0",
            "150000, 20, 5.0",
            "100000, 10, 7.25"})
    void calculateMonthlyCost_positiveInterest_matchesAnnuityFormula(String loanStr, int maturityYears, String annualPercentStr) {
        InterestRate rate = mock(InterestRate.class);

        when(rate.maturityPeriod()).thenReturn(maturityYears);
        when(rate.interestRate()).thenReturn(new BigDecimal(annualPercentStr));
        BigDecimal loanValue = new BigDecimal(loanStr);
        BigDecimal actual = service.calculateMonthlyCost(loanValue, rate);
        int n = maturityYears * 12;
        BigDecimal annualRate = new BigDecimal(annualPercentStr).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal monthlyRateBD = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        double r = monthlyRateBD.doubleValue();
        double P = loanValue.doubleValue();
        double M = P * r / (1 - Math.pow(1 + r, -n));
        BigDecimal expected = BigDecimal.valueOf(M).setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, actual, "Monthly payment should match annuity formula");
    }

    @Test
    @DisplayName("calculateMonthlyCost: rounding is HALF_UP to 2 decimals for positive rate")
    void calculateMonthlyCost_roundingHalfUp_twoDecimals() {
        InterestRate rate = mock(InterestRate.class);

        when(rate.maturityPeriod()).thenReturn(25);

        when(rate.interestRate()).thenReturn(new BigDecimal("3.3333"));
        BigDecimal loanValue = new BigDecimal("123456.78");
        BigDecimal actual = service.calculateMonthlyCost(loanValue, rate);
        assertEquals(2, actual.scale(), "Result must have 2 decimals");
        BigDecimal zeroRateMonthly = loanValue.divide(BigDecimal.valueOf(25 * 12L), 2, RoundingMode.HALF_UP);
        assertTrue(actual.compareTo(zeroRateMonthly) > 0,
                "With positive interest, monthly should exceed zero-interest amortization");
    }
}



