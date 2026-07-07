package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ProblemDetailsFactory problemDetailsFactory;

    public GlobalExceptionHandler(ProblemDetailsFactory problemDetailsFactory) {
        this.problemDetailsFactory = problemDetailsFactory;
    }

    @ExceptionHandler(ApplicationException.class)
    ResponseEntity<ApiErrorResponse> handleApplicationException(
            ApplicationException exception,
            HttpServletRequest request) {
        return build(exception.getStatus(), exception.getErrorCode(), exception.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return build(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_FAILED,
                "Request validation failed.",
                request,
                fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                fieldErrors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage()));
        return build(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_FAILED,
                "Request validation failed.",
                request,
                fieldErrors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiErrorResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException exception,
            HttpServletRequest request) {
        return build(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_FAILED,
                "Request validation failed.",
                request,
                null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorCode.INTERNAL_ERROR,
                "An unexpected server error occurred.",
                request,
                null);
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            ApiErrorCode code,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors) {
        return ResponseEntity
                .status(status)
                .body(problemDetailsFactory.create(status, code, message, request, fieldErrors));
    }
}
