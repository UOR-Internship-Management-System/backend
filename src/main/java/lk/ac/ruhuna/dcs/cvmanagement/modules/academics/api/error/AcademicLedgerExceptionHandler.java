package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicLedgerProperties;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.CorrelationIdContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** Centralized Academic Ledger problem-details mapping. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AcademicLedgerExceptionHandler {

    private static final String LEDGER_UPLOAD_PATH = "/api/v1/admin/academic-ledger/uploads";

    private final AcademicLedgerProperties properties;

    public AcademicLedgerExceptionHandler(AcademicLedgerProperties properties) {
        this.properties = properties;
    }

    @ExceptionHandler(AcademicLedgerApiException.class)
    ResponseEntity<AcademicLedgerProblemDetails> handle(
            AcademicLedgerApiException exception,
            HttpServletRequest request) {
        return build(
                exception.status().value(),
                exception.code(),
                exception.title(),
                exception.getMessage(),
                exception.details(),
                request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<?> handleMaxUploadSize(MaxUploadSizeExceededException exception, HttpServletRequest request) {
        if (!isLedgerUploadRequest(request)) {
            throw exception;
        }
        return build(
                413,
                "LEDGER_FILE_TOO_LARGE",
                "Academic ledger file is too large",
                "Upload a CSV file no larger than 5 MiB.",
                Map.of("maxSizeBytes", properties.maxFileSizeBytes()),
                request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        if (!isLedgerUploadRequest(request)) {
            throw exception;
        }
        return build(
                400,
                "BAD_REQUEST",
                "Invalid academic ledger request",
                "One or more Academic Ledger request parameters are invalid.",
                Map.of(),
                request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<?> handleHttpMediaType(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request) throws HttpMediaTypeNotSupportedException {
        if (!isLedgerUploadRequest(request)) {
            throw exception;
        }
        return build(
                415,
                "LEDGER_MEDIA_TYPE_UNSUPPORTED",
                "Unsupported academic ledger format",
                "Upload a UTF-8 CSV file with media type text/csv.",
                Map.of("acceptedMediaType", "text/csv", "acceptedExtension", ".csv"),
                request);
    }

    private boolean isLedgerUploadRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith(LEDGER_UPLOAD_PATH);
    }

    private ResponseEntity<AcademicLedgerProblemDetails> build(
            int status,
            String code,
            String title,
            String message,
            Map<String, Object> details,
            HttpServletRequest request) {
        String correlationId = CorrelationIdContext.ensure(request);
        AcademicLedgerProblemDetails body = new AcademicLedgerProblemDetails(
                "https://uor-cv-system/errors/" + errorSlug(code),
                title,
                status,
                code,
                message,
                correlationId,
                details == null || details.isEmpty() ? null : details);
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .header(CorrelationIdContext.CORRELATION_ID_HEADER, correlationId)
                .body(body);
    }

    private String errorSlug(String code) {
        return code.toLowerCase().replace('_', '-');
    }
}
