package org.ing.mortgage.exception;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record ApiError(Instant timestamp, int status, String error, String message, List<String> details, String path) {
    public ApiError(int status, String error, String message, List<String> details, String path) {
        this(Instant.now(), status, error, message, details, path);
    }
}
