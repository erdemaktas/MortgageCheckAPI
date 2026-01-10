package org.ing.mortgage.application.service;

import lombok.RequiredArgsConstructor;
import org.ing.mortgage.domain.InterestRate;
import org.ing.mortgage.domain.MortgageInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RequiredArgsConstructor
@Service
public class MortgageService {
    private static final Logger log = LoggerFactory.getLogger(MortgageService.class);

    @Value("${mortgage.incomeMultiplier:4}")
    private Integer incomeMultiplier;

    public boolean isFeasible(MortgageInput mortgageInput) {
        if (mortgageInput.loanValue().compareTo(
                mortgageInput.income().multiply(BigDecimal.valueOf(incomeMultiplier))) > 0) {
            log.warn("Loan Value exceeds {} time the income", incomeMultiplier);
            return false;
        }
        if (mortgageInput.loanValue().compareTo(mortgageInput.homeValue()) > 0){
            log.warn("Loan value exceeds home value");
            return false;
        }
        return true;
    }

    public BigDecimal calculateMonthlyCost(BigDecimal loanValue, InterestRate rate) {
        int maturityMonth = rate.maturityPeriod() * 12;
        BigDecimal annualRate = rate.interestRate().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0){
            return loanValue.divide(BigDecimal.valueOf(maturityMonth), 2, RoundingMode.HALF_UP);
        }
        double P = loanValue.doubleValue();
        double r = monthlyRate.doubleValue();
        double amount = (P * (r * Math.pow(r + 1, maturityMonth))) / (Math.pow(1+r, maturityMonth) - 1);

       return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);


    }
}
