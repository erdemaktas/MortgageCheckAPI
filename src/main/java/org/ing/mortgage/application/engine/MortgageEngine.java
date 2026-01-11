package org.ing.mortgage.application.engine;

import lombok.RequiredArgsConstructor;
import org.ing.mortgage.application.service.InterestRateService;
import org.ing.mortgage.application.service.MortgageService;
import org.ing.mortgage.domain.InterestRate;
import org.ing.mortgage.domain.MortgageInput;
import org.ing.mortgage.domain.MortgageResult;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MortgageEngine {
    private final MortgageService mortgageService;
    private final InterestRateService interestRateService;

    public MortgageResult calculateMortgageCost(MortgageInput mortgageInput) {
        if (mortgageService.isFeasible(mortgageInput)) {
            var rate = resolveRateOrThrow(mortgageInput.maturityPeriod());
            var monthlyCost = mortgageService.calculateMonthlyCost(mortgageInput.loanValue(), rate);
            return new MortgageResult(true, monthlyCost);
        }
        return new MortgageResult(false, null);
    }

    private InterestRate resolveRateOrThrow(Integer maturityPeriod) {
        return interestRateService.findByMaturity(maturityPeriod)
                .orElseThrow(() -> new IllegalArgumentException("No interest rate configured for maturity period=" + maturityPeriod + "years"));
    }
}
