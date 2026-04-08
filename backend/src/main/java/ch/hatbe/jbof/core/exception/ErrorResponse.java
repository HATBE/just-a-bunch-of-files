package ch.hatbe.jbof.core.exception;

import java.util.List;

public record ErrorResponse(
        String message,
        String code,
        List<String> details
) {
    public ErrorResponse(String message, String code) {
        this(message, code, List.of());
    }
}
