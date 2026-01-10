package org.ing.mortgage.adapters.web;


import org.ing.mortgage.application.engine.MortgageEngine;
import org.ing.mortgage.application.service.InterestRateService;
import org.ing.mortgage.domain.InterestRate;
import org.ing.mortgage.domain.MortgageInput;
import org.ing.mortgage.domain.MortgageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MortgageController.class)
class MortgageControllerIT {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private InterestRateService interestRateService;
    @MockBean
    private MortgageEngine mortgageEngine;

    @Test
    @DisplayName("GET /api/interest-rates: returns mapped DTOs sorted by maturityPeriod ASC")
    void getInterestRates_sortedAndMapped() throws Exception {
        InterestRate r20 = mock(InterestRate.class);
        when(r20.maturityPeriod()).thenReturn(20);
        when(r20.interestRate()).thenReturn(new BigDecimal("4.25"));
        when(r20.lastUpdate()).thenReturn(Instant.parse("2025-12-01T10:30:00Z"));
        InterestRate r10 = mock(InterestRate.class);
        when(r10.maturityPeriod()).thenReturn(10);
        when(r10.interestRate()).thenReturn(new BigDecimal("3.75"));
        when(r10.lastUpdate()).thenReturn(Instant.parse("2025-12-01T10:30:00Z"));
        InterestRate r30 = mock(InterestRate.class);
        when(r30.maturityPeriod()).thenReturn(30);
        when(r30.interestRate()).thenReturn(new BigDecimal("5.25"));
        when(r30.lastUpdate()).thenReturn(Instant.parse("2025-12-01T10:30:00Z"));
        when(interestRateService.getAllRates()).thenReturn(List.of(r30, r10, r20));


        mockMvc.perform(get("/api/interest-rates"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].maturityPeriod").value(10))
                .andExpect(jsonPath("$[0].interestRate").value(3.75))
                .andExpect(jsonPath("$[0].lastUpdate").value("2024-12-01T10:30:00Z"))
                .andExpect(jsonPath("$[1].maturityPeriod").value(20))
                .andExpect(jsonPath("$[1].interestRate").value(4.25))
                .andExpect(jsonPath("$[1].lastUpdate").value("2025-12-01T10:30:00Z"))
                .andExpect(jsonPath("$[2].maturityPeriod").value(30))
                .andExpect(jsonPath("$[2].interestRate").value(5.25))
                .andExpect(jsonPath("$[2].lastUpdate").value("2024-12-31T10:30:00Z"));

        verify(interestRateService).getAllRates();
        verifyNoMoreInteractions(interestRateService);
        verifyNoInteractions(mortgageEngine);
    }

    @Test
    @DisplayName("GET /api/interest-rates: returns mapped DTOs sorted by maturityPeriod ASC")
    void getInterestRates_nullMaturity_ignoredNullAndsortedAndMapped() throws Exception {
        InterestRate r20 = mock(InterestRate.class);
        when(r20.maturityPeriod()).thenReturn(null);
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

        mockMvc.perform(get("/api/interest-rates"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].maturityPeriod").value(10))
                .andExpect(jsonPath("$[0].interestRate").value(3.75))
                .andExpect(jsonPath("$[0].lastUpdate").value("2024-12-01T10:30:00Z"))
                .andExpect(jsonPath("$[1].maturityPeriod").value(30))
                .andExpect(jsonPath("$[1].interestRate").value(5.25))
                .andExpect(jsonPath("$[1].lastUpdate").value("2024-12-31T10:30:00Z"));

        verify(interestRateService).getAllRates();
        verifyNoMoreInteractions(interestRateService);
        verifyNoInteractions(mortgageEngine);
    }

    @Test
    @DisplayName("POST /api/mortgage-check: builds MortgageInput, delegates to engine, returns response")
    void mortgageCheck_happyPath() throws Exception {
        when(mortgageEngine.calculateMortgageCost(any()))
                .thenReturn(new MortgageResult(true, new BigDecimal("1200.50")));

        String requestJson = """
                {
                  "income": 60000.00,
                  "loanValue": 200000.00,
                  "homeValue": 220000.00,
                  "maturityPeriod": 30
                }
                """;

        mockMvc.perform(post("/api/mortgage-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.feasible").value(true))
                .andExpect(jsonPath("$.monthlyCost").value(1200.50));

        // Capture and assert the MortgageInput passed to engine
        ArgumentCaptor<MortgageInput> captor = ArgumentCaptor.forClass(MortgageInput.class);
        verify(mortgageEngine).calculateMortgageCost(captor.capture());

        MortgageInput input = captor.getValue();

        assert input != null;
        assertEquals(new BigDecimal("60000.00"), input.income());
        assertEquals(new BigDecimal("200000.00"), input.loanValue());
        assertEquals(new BigDecimal("220000.00"), input.homeValue());
        assertEquals(30, input.maturityPeriod());

        verifyNoMoreInteractions(mortgageEngine);
        verifyNoInteractions(interestRateService);
    }


    @ParameterizedTest(name = "{index}: Invalid payload -> {0}")
    @MethodSource("invalidRequests")
    @DisplayName("POST /api/mortgage-check — invalid payloads return 400 with expected messages; engine not invoked")
    void mortgageCheck_failedRequestBodyValidations(String requestJson, List<String> expectedMessages) throws Exception {

        mockMvc.perform(post("/api/mortgage-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details", hasSize(expectedMessages.size())))
                .andExpect(jsonPath("$.details").value(
                        containsInAnyOrder(expectedMessages.toArray(new String[0])))
                );

        verifyNoInteractions(mortgageEngine);
    }


    static Stream<Arguments> invalidRequests() {
        return Stream.of(
                // income <= 0
                Arguments.of("""
                            {
                              "income": 0,
                              "maturityPeriod": 20,
                              "loanValue": 150000,
                              "homeValue": 200000
                            }
                        """, List.of("income: Income must be greater than 0")),

                // maturity < 1
                Arguments.of("""
                            {
                              "income": 50000,
                              "maturityPeriod": 0,
                              "loanValue": 150000,
                              "homeValue": 200000
                            }
                        """, List.of("maturityPeriod: Maturity period must be between 1 and 30 years")),

                // maturity > 30
                Arguments.of("""
                            {
                              "income": 50000,
                              "maturityPeriod": 31,
                              "loanValue": 150000,
                              "homeValue": 200000
                            }
                        """, List.of("maturityPeriod: Maturity period must be between 1 and 30 years")),

                // loanValue <= 0
                Arguments.of("""
                            {
                              "income": 50000,
                              "maturityPeriod": 20,
                              "loanValue": 0,
                              "homeValue": 200000
                            }
                        """, List.of("loanValue: Loan value must be greater than 0")),

                // homeValue <= 0
                Arguments.of("""
                            {
                              "income": 50000,
                              "maturityPeriod": 20,
                              "loanValue": 150000,
                              "homeValue": 0
                            }
                        """, List.of("homeValue: Home value must be greater than 0")),

                // homeValue null + multiple violations together
                Arguments.of("""
                            {
                              "income": 0,
                              "maturityPeriod": 0,
                              "loanValue": -10,
                              "homeValue": null
                            }
                        """, List.of(
                        "income: Income must be greater than 0",
                        "maturityPeriod: Maturity period must be between 1 and 30 years",
                        "loanValue: Loan value must be greater than 0",
                        "homeValue: must not be null"
                ))
        );
    }
}
