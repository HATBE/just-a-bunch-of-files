package ch.hatbe.jbof.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(), "INVALID_INPUT"));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), "NOT_FOUND"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Validation failed", "INVALID_INPUT", extractMessages(ex.getBindingResult().getFieldErrors())));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Validation failed", "INVALID_INPUT", extractMessages(ex.getBindingResult().getFieldErrors())));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        long maxUploadSize = ex.getMaxUploadSize();
        String maxUploadSizeText = maxUploadSize > 0
                ? (maxUploadSize / (1024 * 1024)) + " MB"
                : "configured server limit";

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse("upload too large", "PAYLOAD_TOO_LARGE", List.of("Maximum upload size is " + maxUploadSizeText)));
    }

    private List<String> extractMessages(List<FieldError> fieldErrors) {
        List<String> messages = new ArrayList<>(fieldErrors.size());

        for (FieldError fieldError : fieldErrors) {
            String message = fieldError.getDefaultMessage();
            messages.add(message == null || message.isBlank() ? "Invalid value for " + fieldError.getField() : message);
        }

        return messages;
    }
}
