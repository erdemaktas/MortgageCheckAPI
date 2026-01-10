package org.ing.mortgage.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgument_returnApiError(){
        HttpServletRequest req= mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test");
        ResponseEntity<ApiError> resp = handler.handleIllegalArgument(new IllegalArgumentException("bad arg"), req);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("bad arg", resp.getBody().message());
        assertEquals("/test", resp.getBody().path());
    }

    @Test
    void handleGeneric_returnApiError(){
        HttpServletRequest req= mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test");
        ResponseEntity<ApiError> resp = handler.handleGeneric(new Exception("unexpected"), req);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("unexpected", resp.getBody().message());
        assertEquals("/test", resp.getBody().path());
    }

    @Test
    void handleValidation_returnApiError(){
        HttpServletRequest req= mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test");

        FieldError fieldError= new FieldError("object", "field", "must not be blank");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exc = mock(MethodArgumentNotValidException.class);
        when(exc.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiError> resp = handler.handleGeneric(exc, req);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("ValidationFailed", resp.getBody().message());
        assertEquals("/test", resp.getBody().path());
        assertTrue(resp.getBody().details().contains("field: must not be blank"));
    }
}
