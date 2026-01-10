package org.ing.mortgage.adapters.persistence;

import org.ing.mortgage.adapters.persistance.InMemoryInterestRateRepository;
import org.ing.mortgage.domain.InterestRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InMemoryInterestRateRepositoryTest {
    private InMemoryInterestRateRepository repository;

    @BeforeEach
    void setUp(){ repository = new InMemoryInterestRateRepository();}

    private InterestRate mockRate(int maturity){
        InterestRate r = mock(InterestRate.class);
        when(r.maturityPeriod()).thenReturn(maturity);
        return r;
    }

    @Test
    void findAll_empty_returnsEmptyList(){
        List<InterestRate> all = repository.findAll();
        assertNotNull(all);
        assertTrue(all.isEmpty(), "Expected empty list when repository is empty");
    }

    @Test
    void saveRates_then_findAll_containsRates(){
        InterestRate r5 = mockRate(5);
        InterestRate r10 = mockRate(10);

        repository.saveRates(List.of(r5,r10));

        List<InterestRate> all = repository.findAll();
        assertEquals(2, all.size());
        assertTrue(all.contains(r5));
        assertTrue(all.contains(r10));
    }

    @Test
    void saveRates_then_findByMaturity_existing_returnOptionalWithValue(){
        InterestRate r5 = mockRate(5);
        InterestRate r10 = mockRate(10);

        repository.saveRates(List.of(r5,r10));

        Optional<InterestRate> rate = repository.findByMaturity(5);
        assertTrue(rate.isPresent());
        assertSame(r5, rate.get());
    }

    @Test
    void saveRates_then_findByMaturity_missingReturnEmpty(){
        repository.saveRates(List.of(mockRate(5)));
        assertTrue(repository.findByMaturity(10).isEmpty());
    }
}
