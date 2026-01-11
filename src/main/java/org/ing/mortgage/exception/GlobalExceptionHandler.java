package org.ing.mortgage.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req){
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", details, req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req){
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), Collections.emptyList(), req);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest req){
        return buildResponse(HttpStatus.resolve(ex.getStatusCode().value()), ex.getMessage(), Collections.emptyList(), req);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req){
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), Collections.emptyList(), req);
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus httpStatus, String message, List<String> details, HttpServletRequest req) {
        log.error("ERROR: path={} message={} details={}", req.getRequestURI(), message, details);
        ApiError err = new ApiError(httpStatus.value(), httpStatus.getReasonPhrase(), message, details, req.getRequestURI());
        return ResponseEntity.status(httpStatus).body(err);
    }

}
