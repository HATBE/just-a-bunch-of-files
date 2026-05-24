package ch.hatbe.jbof.core.exception;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        List<FieldViolation> fieldViolations
) {
    public record FieldViolation(
            String field,
            String message
    ) {}
}

