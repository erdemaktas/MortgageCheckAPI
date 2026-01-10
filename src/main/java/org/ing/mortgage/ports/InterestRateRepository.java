package org.ing.mortgage.ports;

import org.ing.mortgage.domain.InterestRate;

import java.util.List;
import java.util.Optional;

public interface InterestRateRepository {
    List<InterestRate> findAll();
    Optional<InterestRate> findByMaturity(int years);

}
