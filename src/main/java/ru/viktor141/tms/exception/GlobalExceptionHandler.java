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

/**
 * GlobalExceptionHandler handles exceptions globally.
 * <p>
 * This class defines handlers for various exceptions and returns appropriate HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String DEVELOPMENT_MODE = "development";

    /**
     * Handles NoHandlerFoundException.
     *
     * @param ex The exception object.
     * @return A ResponseEntity with a 404 status code.
     */
    @ExceptionHandler(value = {NoHandlerFoundException.class})
    public ResponseEntity<ResponseError> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex);
    }

    /**
     * Handles IllegalArgumentException.
     *
     * @param ex The exception object.
     * @return A ResponseEntity with a 400 status code.
     */
    @ExceptionHandler(value = {IllegalArgumentException.class})
    protected ResponseEntity<ResponseError> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid input: " + ex.getMessage(), ex);
    }

    /**
     * Handles generic exceptions.
     *
     * @param ex The exception object.
     * @return A ResponseEntity with a 500 status code.
     */
    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<ResponseError> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + ex.getMessage(), ex);
    }

    /**
     * Handles NoPermissionException.
     *
     * @param ex The exception object.
     * @return A ResponseEntity with a 403 status code.
     */
    @ExceptionHandler(value = {NoPermissionException.class})
    protected ResponseEntity<ResponseError> handleNoPermissionException(NoPermissionException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Don't have permission: " + ex.getMessage(), ex);
    }

    /**
     * Handles EntityNotFoundException.
     *
     * @param ex The exception object.
     * @return A ResponseEntity with a 404 status code.
     */
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

    /**
     * ResponseError represents an error response returned by the GlobalExceptionHandler.
     * <p>
     * This class encapsulates details about an error, including timestamp, status code, message, and stack trace.
     */
    @Setter
    @Getter
    public static class ResponseError {

        /**
         * The timestamp when the error occurred.
         */
        private LocalDateTime timestamp;
        /**
         * The HTTP status code of the error.
         */
        private int status;
        /**
         * The error message.
         */
        private String message;
        /**
         * The error message.
         */
        private StackTraceElement[] stackTrace;

        /**
         * Constructs a new ResponseError object.
         *
         * @param timestamp   The timestamp when the error occurred.
         * @param status      The HTTP status code of the error.
         * @param message     The error message.
         * @param stackTrace  The stack trace of the error.
         */
        public ResponseError(LocalDateTime timestamp, int status, String message, StackTraceElement[] stackTrace) {
            this.timestamp = timestamp;
            this.status = status;
            this.message = message;
            this.stackTrace = stackTrace;
        }

    }
}
