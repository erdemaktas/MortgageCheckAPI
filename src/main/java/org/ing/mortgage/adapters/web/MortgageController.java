package org.ing.mortgage.adapters.web;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ing.mortgage.adapters.web.dto.InterestRateDto;
import org.ing.mortgage.adapters.web.dto.MortgageCheckRequest;
import org.ing.mortgage.adapters.web.dto.MortgageCheckResponse;
import org.ing.mortgage.application.engine.MortgageEngine;
import org.ing.mortgage.application.service.InterestRateService;
import org.ing.mortgage.domain.MortgageInput;
import org.ing.mortgage.domain.MortgageResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class MortgageController {
    private final InterestRateService interestRateService;
    private final MortgageEngine mortgageEngine;

    @GetMapping("/interest-rates")
    @RateLimiter(name = "apiRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<List<InterestRateDto>> getInterestRates(){
        List<InterestRateDto> dto = interestRateService.getAllRates().stream()
                .filter(interestRate -> interestRate.maturityPeriod() != null)
                .map(r -> new InterestRateDto(r.maturityPeriod(), r.interestRate(), r.lastUpdate()))
                .sorted(Comparator.comparingInt(InterestRateDto::maturityPeriod))
                .toList();
        return ResponseEntity.ok(dto);
    }

    public ResponseEntity rateLimitFallback(RequestNotPermitted ex) {
        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests");
    }

    @PostMapping("/mortgage-check")
    @RateLimiter(name = "apiRateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<MortgageCheckResponse> mortgageCheck(@Valid @RequestBody MortgageCheckRequest request){
        MortgageInput mortgageInput = MortgageInput.builder()
                .income(request.getIncome())
                .maturityPeriod(request.getMaturityPeriod())
                .loanValue(request.getLoanValue())
                .homeValue(request.getHomeValue()).build();
        MortgageResult result = mortgageEngine.calculateMortgageCost(mortgageInput);
        return ResponseEntity.ok(new MortgageCheckResponse(result.feasible(), result.monthlyCost()));
    }
}


