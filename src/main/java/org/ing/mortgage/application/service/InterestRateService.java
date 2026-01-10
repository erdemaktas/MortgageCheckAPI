package org.ing.mortgage.application.service;

import lombok.RequiredArgsConstructor;
import org.ing.mortgage.domain.InterestRate;
import org.ing.mortgage.ports.InterestRateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class InterestRateService {
    private final InterestRateRepository interestRateRepository;

    public List<InterestRate> getAllRates() {
        return interestRateRepository.findAll();
    }

    public Optional<InterestRate> findByMaturity(Integer maturityPeriod) {
        return interestRateRepository.findByMaturity(maturityPeriod);
    }

}
