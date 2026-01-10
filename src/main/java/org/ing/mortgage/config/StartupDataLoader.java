package org.ing.mortgage.config;

import lombok.RequiredArgsConstructor;
import org.ing.mortgage.adapters.persistance.InMemoryInterestRateRepository;
import org.ing.mortgage.domain.InterestRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class StartupDataLoader implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupDataLoader.class);

    private final InMemoryInterestRateRepository interestRateRepository;
    private final RateProperties rateProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Startup Loader initializing");
        var list = rateProperties.getRates().stream()
                .map(r -> new InterestRate(r.maturityPeriod(), r.interestRate(), Instant.now()))
                .toList();
        if (!list.isEmpty()) {
            log.info("Rates will be saved");
            interestRateRepository.saveRates(list);
        }
    }
}
