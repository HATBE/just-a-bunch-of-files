package ch.hatbe.jbof.core.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant; import java.util.List;
import org.springframework.http.*; import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError; import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*; import org.springframework.web.client.RestClientResponseException;


@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class) ResponseEntity<ApiError> notFound(ResourceNotFoundException e) { return error(HttpStatus.NOT_FOUND, e.getMessage(), List.of()); }
    @ExceptionHandler(ConflictException.class) ResponseEntity<ApiError> conflict(ConflictException e) { return error(HttpStatus.CONFLICT, e.getMessage(), List.of()); }
    @ExceptionHandler(AuthorizationDeniedException.class) ResponseEntity<ApiError> denied(AuthorizationDeniedException e) { return error(HttpStatus.FORBIDDEN, "Access denied", List.of()); }
    @ExceptionHandler(RestClientResponseException.class) ResponseEntity<ApiError> keycloak(RestClientResponseException e) { return error(HttpStatus.BAD_GATEWAY, "Keycloak request failed: " + e.getStatusCode(), List.of()); }
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> validation(MethodArgumentNotValidException e) {
        return error(HttpStatus.BAD_REQUEST, "Validation failed", e.getBindingResult().getFieldErrors().stream().map(this::violation).toList());
    }
    private ApiError.FieldViolation violation(FieldError e) { return new ApiError.FieldViolation(e.getField(), e.getDefaultMessage()); }
    private ResponseEntity<ApiError> error(HttpStatus s, String m, List<ApiError.FieldViolation> v) { return ResponseEntity.status(s).body(new ApiError(Instant.now(), s.value(), s.getReasonPhrase(), m, v)); }
}
