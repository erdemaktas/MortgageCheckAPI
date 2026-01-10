package org.ing.mortgage.config;

import org.ing.mortgage.adapters.persistance.InMemoryInterestRateRepository;
import org.ing.mortgage.domain.InterestRate;
import org.ing.mortgage.domain.Rate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.ApplicationArguments;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class StartupDataLoaderTest {

    @Test
    void run_doesNothing_when_props_empty() throws Exception {
        var repo = mock(InMemoryInterestRateRepository.class);
        var props = mock(RateProperties.class);
        when(props.getRates()).thenReturn(Collections.emptyList());

        var runner = new StartupDataLoader(repo, props);
        runner.run(mock(ApplicationArguments.class));
        verify(repo, never()).saveRates(anyList());
    }

    @Test
    void run_saveRates_when_propertiesHaveItems() throws Exception{
        var repo = mock(InMemoryInterestRateRepository.class);
        var props = mock(RateProperties.class);
        var r5 = mock(Rate.class);
        when(r5.interestRate()).thenReturn(new BigDecimal("3.5"));
        when(r5.maturityPeriod()).thenReturn(5);

        var r10 = mock(Rate.class);
        when(r10.interestRate()).thenReturn(new BigDecimal("4.5"));
        when(r10.maturityPeriod()).thenReturn(10);

        when(props.getRates()).thenReturn(List.of(r5,r10));

        var runner = new StartupDataLoader(repo, props);
        runner.run(mock(ApplicationArguments.class));

        ArgumentCaptor<List<InterestRate>> captor =ArgumentCaptor.forClass((Class) List.class);
        verify(repo, times(1)).saveRates(captor.capture());
        List<InterestRate> rates = captor.getValue();

        assertEquals(2, rates.size());
        assertTrue(rates.stream().anyMatch(ir-> ir.maturityPeriod() == 5 && ir.interestRate().equals(new BigDecimal("3.5"))), "Should contain with maturityPeriod 5");
        assertTrue(rates.stream().anyMatch(ir-> ir.maturityPeriod() == 10 && ir.interestRate().equals(new BigDecimal("4.5"))), "Should contain with maturityPeriod 10");

    }
}
