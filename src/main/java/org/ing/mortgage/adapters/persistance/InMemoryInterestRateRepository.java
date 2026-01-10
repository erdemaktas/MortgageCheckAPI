package org.ing.mortgage.adapters.persistance;

import org.ing.mortgage.domain.InterestRate;
import org.ing.mortgage.ports.InterestRateRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryInterestRateRepository implements InterestRateRepository {
    private final Map<Integer, InterestRate> map = new HashMap<>();


    @Override
    public List<InterestRate> findAll() {
        return new ArrayList<>(map.values());
    }

    @Override
    public Optional<InterestRate> findByMaturity(int years) {
        return Optional.ofNullable(map.get(years));
    }

    public void saveRates(List<InterestRate> rates){
        map.clear();
        for(var r:rates) map.put(r.maturityPeriod(), r);
    }
}
