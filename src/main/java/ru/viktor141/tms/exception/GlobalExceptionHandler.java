package ru.viktor141.tms.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.naming.NoPermissionException;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String DEVELOPMENT_MODE = "development";

    @ExceptionHandler(value = {NoHandlerFoundException.class})
    public ResponseEntity<ResponseError> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    protected ResponseEntity<ResponseError> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid input: " + ex.getMessage(), ex);
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<ResponseError> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + ex.getMessage(), ex);
    }

    @ExceptionHandler(value = {NoPermissionException.class})
    protected ResponseEntity<ResponseError> handleNoPermissionException(NoPermissionException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Don't have permission: " + ex.getMessage(), ex);
    }

    @ExceptionHandler(value = {EntityNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(EntityNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponseException(HttpStatus.NOT_FOUND, ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<ResponseError> buildErrorResponse(HttpStatus status, String message, Exception ex) {
        ResponseError responseError = new ResponseError(
                LocalDateTime.now(),
                status.value(),
                message,
                ex.getStackTrace()
        );

        if (!DEVELOPMENT_MODE.equals(System.getenv("APP_ENV"))) {
            responseError.setStackTrace(null); // Remove stack trace in production
        }

        return new ResponseEntity<>(responseError, status);
    }

    @Setter
    @Getter
    public static class ResponseError {

        private LocalDateTime timestamp;
        private int status;
        private String message;
        private StackTraceElement[] stackTrace;

        public ResponseError(LocalDateTime timestamp, int status, String message, StackTraceElement[] stackTrace) {
            this.timestamp = timestamp;
            this.status = status;
            this.message = message;
            this.stackTrace = stackTrace;
        }

    }
}
