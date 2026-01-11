package org.ing.mortgage.adapters.web;

import org.ing.mortgage.application.engine.MortgageEngine;
import org.ing.mortgage.application.service.InterestRateService;
import org.ing.mortgage.domain.InterestRate;
import org.ing.mortgage.exception.ApiError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimiterIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private InterestRateService interestRateService;

    @MockBean
    private MortgageEngine mortgageEngine;

    @Test
    @DisplayName("GET /api/interest-rates: Test Rate Limiter")
    void getInterestRates_sortedAndMapped() throws Exception {
        InterestRate r20 = mock(InterestRate.class);
        when(r20.maturityPeriod()).thenReturn(20);
        when(r20.interestRate()).thenReturn(new BigDecimal("4.25"));
        when(r20.lastUpdate()).thenReturn(Instant.parse("2025-12-01T10:30:00Z"));
        InterestRate r10 = mock(InterestRate.class);
        when(r10.maturityPeriod()).thenReturn(10);
        when(r10.interestRate()).thenReturn(new BigDecimal("3.75"));
        when(r10.lastUpdate()).thenReturn(Instant.parse("2024-12-01T10:30:00Z"));
        InterestRate r30 = mock(InterestRate.class);
        when(r30.maturityPeriod()).thenReturn(30);
        when(r30.interestRate()).thenReturn(new BigDecimal("5.25"));
        when(r30.lastUpdate()).thenReturn(Instant.parse("2024-12-31T10:30:00Z"));
        when(interestRateService.getAllRates()).thenReturn(List.of(r30, r10, r20));

        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/interest-rates", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseEntity<ApiError> responseError =
                restTemplate.getForEntity("/api/interest-rates", ApiError.class);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, responseError.getStatusCode());
        assertEquals("Too Many Requests", responseError.getBody().error());

    }

}
