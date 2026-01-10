package org.ing.mortgage.application.service;

import org.ing.mortgage.domain.InterestRate;
import org.ing.mortgage.ports.InterestRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InterestRateServiceTest {
    @Mock
    InterestRateRepository repository;
    @InjectMocks
    InterestRateService
            interestRateService;

    @Test
    void getAllRates_returnsListFromRepositor() {
        InterestRate r1 = mock(InterestRate.class);
        InterestRate r2 = mock(InterestRate.class);
        List repoResult = List.of(r1, r2);
        when(repository.findAll()).thenReturn(repoResult);
        List result = interestRateService.getAllRates();
        assertSame(repoResult, result,
                "Service should return the exact list from rep");
        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findByMaturity_returnsOptionalWhenPresent() {
        int years = 30;
        InterestRate rate = mock(InterestRate.class);
        when(repository.findByMaturity(years)).thenReturn(Optional.of(rate));

        Optional<InterestRate> result = interestRateService.findByMaturity(years);

        assertTrue(result.isPresent(), " Expected Optional to be present ");
        assertSame(rate, result.get(), " Expected to return the same InterestRate instance from repo");
        verify(repository).findByMaturity(years);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findByMaturity_returnsEmptyWhenRepositoryEmpty() {

        int years = 15;

        when(repository.findByMaturity(years)).thenReturn(Optional.empty());
        Optional<InterestRate> result = interestRateService.findByMaturity(years);

        assertTrue(result.isEmpty(), "Expected Optional to be empty");

        verify(repository).findByMaturity(years);

        verifyNoMoreInteractions(repository);
    }
}
