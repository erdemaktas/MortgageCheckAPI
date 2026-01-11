package org.ing.mortgage.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestController.class)
@Import(CorrelationIdFilter.class)
class CorrelationIdFilterIT {

    @Autowired
    private MockMvc mockMvc;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger = (Logger) LoggerFactory.getLogger(CorrelationIdFilter.class);

    @BeforeEach
    public void createAppender() {
        // Create an in-memory appender
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    public void detachAppender() {
        logger.detachAppender(listAppender);
    }

    @Test
    void shouldLogInfoFor200Response() throws Exception {

        mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"));

        assertThat(listAppender.list).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.INFO);
            assertThat(event.getFormattedMessage()).contains("request.completed");
            assertThat(event.getFormattedMessage()).contains("status=200");
        });

        assertThat(MDC.get("CorrelationId")).isNull();

    }

    @Test
    void shouldLogWarnFor400Response() throws Exception {

        mockMvc.perform(get("/bad-request"))
                .andExpect(status().isBadRequest());

        assertThat(listAppender.list).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.WARN);
            assertThat(event.getFormattedMessage()).contains("request.completed");
            assertThat(event.getFormattedMessage()).contains("status=400");
        });
    }

    @Test
    void shouldLogErrorFor500Response() throws Exception {

        mockMvc.perform(get("/server-error"))
                .andExpect(status().isInternalServerError());

        assertThat(listAppender.list).anySatisfy(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.ERROR);
            assertThat(event.getFormattedMessage()).contains("request.completed");
            assertThat(event.getFormattedMessage()).contains("status=500");
        });
    }
}

@RestController
class TestController {

    @GetMapping("/test")
    public String test() {
        return "ok";
    }

    @GetMapping("/bad-request")
    public void badRequest(HttpServletResponse response) throws java.io.IOException {
        response.sendError(400, "Bad Request");
    }

    @GetMapping("/server-error")
    public void serverError() {
        throw new RuntimeException("Server error");
    }
}