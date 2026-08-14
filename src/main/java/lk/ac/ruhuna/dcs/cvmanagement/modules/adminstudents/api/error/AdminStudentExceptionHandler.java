package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception.AdminStudentApiException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Centralized OpenAPI problem-details mapping for Admin Student Inspection endpoints. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminStudentExceptionHandler {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @ExceptionHandler(AdminStudentApiException.class)
    ResponseEntity<AdminStudentProblemDetails> handle(
            AdminStudentApiException exception,
            HttpServletRequest request) {
        return build(
                exception.status().value(),
                exception.code(),
                exception.title(),
                exception.getMessage(),
                null,
                exception.details(),
                request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request)
            throws MethodArgumentNotValidException {
        if (!isAdminStudentRequest(request)) {
            throw exception;
        }

        List<AdminStudentFieldError> fieldErrors = new ArrayList<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.add(new AdminStudentFieldError(
                    error.getField(),
                    error.getCode() == null ? "INVALID" : error.getCode(),
                    error.getDefaultMessage() == null ? "Invalid value." : error.getDefaultMessage()));
        }
        return badRequest(fieldErrors, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<?> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request)
            throws ConstraintViolationException {
        if (!isAdminStudentRequest(request)) {
            throw exception;
        }

        List<AdminStudentFieldError> fieldErrors = exception.getConstraintViolations().stream()
                .map(violation -> new AdminStudentFieldError(
                        violation.getPropertyPath().toString(),
                        violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                        violation.getMessage()))
                .toList();
        return badRequest(fieldErrors, request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<?> handleMethodValidation(HandlerMethodValidationException exception, HttpServletRequest request)
            throws HandlerMethodValidationException {
        if (!isAdminStudentRequest(request)) {
            throw exception;
        }
        return badRequest(List.of(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request)
            throws MethodArgumentTypeMismatchException {
        if (!isAdminStudentRequest(request)) {
            throw exception;
        }

        return badRequest(
                List.of(new AdminStudentFieldError(
                        exception.getName(),
                        "TYPE_MISMATCH",
                        "The supplied value has an invalid type.")),
                request);
    }

    private ResponseEntity<AdminStudentProblemDetails> badRequest(
            List<AdminStudentFieldError> fieldErrors,
            HttpServletRequest request) {
        return build(
                400,
                "VALIDATION_FAILED",
                "Validation failed",
                "One or more request values are invalid.",
                fieldErrors.isEmpty() ? null : List.copyOf(fieldErrors),
                Map.of(),
                request);
    }

    private boolean isAdminStudentRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith(ApiPaths.ADMIN_STUDENTS);
    }

    private ResponseEntity<AdminStudentProblemDetails> build(
            int status,
            String code,
            String title,
            String message,
            List<AdminStudentFieldError> fieldErrors,
            Map<String, Object> details,
            HttpServletRequest request) {
        String correlationId = correlationId(request);
        AdminStudentProblemDetails body = new AdminStudentProblemDetails(
                "https://uor-cv-system/errors/" + errorSlug(code),
                title,
                status,
                code,
                message,
                correlationId,
                fieldErrors,
                details == null || details.isEmpty() ? null : Map.copyOf(details));
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .header(CORRELATION_HEADER, correlationId)
                .body(body);
    }

    private String correlationId(HttpServletRequest request) {
        String existing = request.getHeader(CORRELATION_HEADER);
        if (existing == null || existing.isBlank()) {
            existing = request.getHeader(REQUEST_ID_HEADER);
        }
        return existing == null || existing.isBlank() ? UUID.randomUUID().toString() : existing.trim();
    }

    private String errorSlug(String code) {
        return code.toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
