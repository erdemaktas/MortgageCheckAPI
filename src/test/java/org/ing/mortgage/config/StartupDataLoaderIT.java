package org.ing.mortgage.config;

import org.ing.mortgage.adapters.persistance.InMemoryInterestRateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {InMemoryInterestRateRepository.class,
        StartupDataLoader.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableConfigurationProperties(value = RateProperties.class)
public class StartupDataLoaderIT {
    @Autowired
    InMemoryInterestRateRepository repository;

    @Test
    void applicationRunner_saveRatesOnStartUp() {
        var rates = repository.findAll();
        assertEquals(3, rates.size());
        assertTrue(rates.stream().anyMatch(ir -> ir.maturityPeriod() == 5 && ir.interestRate().equals(new BigDecimal("3.5"))), "Should contain with maturityPeriod 5");
        assertTrue(rates.stream().anyMatch(ir -> ir.maturityPeriod() == 10 && ir.interestRate().equals(new BigDecimal("3.8"))), "Should contain with maturityPeriod 10");
        assertTrue(rates.stream().anyMatch(ir -> ir.maturityPeriod() == 20 && ir.interestRate().equals(new BigDecimal("4.5"))), "Should contain with maturityPeriod 20");

    }
}
