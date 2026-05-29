package ch.hatbe.jbof.core.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> conflict(ConflictException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> illegalArgument(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    ResponseEntity<ApiError> denied(AuthorizationDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "Access denied", List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        return error(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                fieldViolations(ex.getBindingResult().getFieldErrors())
        );
    }

    @ExceptionHandler(BindException.class)
    ResponseEntity<ApiError> bind(BindException ex) {
        return error(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                fieldViolations(ex.getBindingResult().getFieldErrors())
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> constraintViolation(ConstraintViolationException ex) {
        return error(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                constraintViolations(ex.getConstraintViolations().stream().toList())
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> maxUploadSize(MaxUploadSizeExceededException ex) {
        long maxUploadSize = ex.getMaxUploadSize();

        String maxUploadSizeText = maxUploadSize > 0
                ? (maxUploadSize / (1024 * 1024)) + " MB"
                : "configured server limit";

        return error(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Upload too large",
                List.of(new ApiError.FieldViolation(
                        "file",
                        "Maximum upload size is " + maxUploadSizeText
                ))
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> malformedJson(HttpMessageNotReadableException ex) {
        return error(
                HttpStatus.BAD_REQUEST,
                "Malformed request body",
                List.of(new ApiError.FieldViolation(
                        "body",
                        "Request body is missing or invalid"
                ))
        );
    }

    @ExceptionHandler(RestClientResponseException.class)
    ResponseEntity<ApiError> restClient(RestClientResponseException ex) {
        return error(
                HttpStatus.BAD_GATEWAY,
                "External service request failed: " + ex.getStatusCode(),
                List.of()
        );
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> fallback(Exception ex) {
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                List.of()
        );
    }

    private List<ApiError.FieldViolation> fieldViolations(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(this::fieldViolation)
                .toList();
    }

    private ApiError.FieldViolation fieldViolation(FieldError fieldError) {
        String message = fieldError.getDefaultMessage();

        return new ApiError.FieldViolation(
                fieldError.getField(),
                message == null || message.isBlank()
                        ? "Invalid value for " + fieldError.getField()
                        : message
        );
    }

    private List<ApiError.FieldViolation> constraintViolations(List<ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(this::constraintViolation)
                .toList();
    }

    private ApiError.FieldViolation constraintViolation(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath() == null
                ? "value"
                : violation.getPropertyPath().toString();

        String message = violation.getMessage();

        return new ApiError.FieldViolation(
                field,
                message == null || message.isBlank()
                        ? "Invalid value for " + field
                        : message
        );
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status,
            String message,
            List<ApiError.FieldViolation> violations
    ) {
        return ResponseEntity
                .status(status)
                .body(new ApiError(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        violations
                ));
    }
}