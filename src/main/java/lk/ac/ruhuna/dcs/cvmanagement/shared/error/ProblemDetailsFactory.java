package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.CorrelationIdContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Creates safe OpenAPI-compatible Problem Details responses. */
@Component
public class ProblemDetailsFactory {

    public ApiErrorResponse create(
            HttpStatus status,
            ApiErrorCode code,
            String message,
            HttpServletRequest request,
            List<ApiFieldError> fieldErrors,
            Map<String, Object> details) {
        String correlationId = CorrelationIdContext.ensure(request);
        return new ApiErrorResponse(
                "https://uor-cv-system/errors/" + errorSlug(code),
                title(code, status),
                status.value(),
                code.name(),
                message,
                correlationId,
                fieldErrors == null || fieldErrors.isEmpty() ? null : List.copyOf(fieldErrors),
                details == null || details.isEmpty() ? null : Map.copyOf(details));
    }

    private String title(ApiErrorCode code, HttpStatus status) {
        return switch (code) {
            case VALIDATION_FAILED -> "Validation failed";
            case IF_MATCH_REQUIRED, PRECONDITION_REQUIRED -> "Precondition required";
            case PRECONDITION_FAILED, STALE_VERSION -> "Precondition failed";
            case CV_CONFIGURATION_INVALID -> "CV configuration is invalid";
            case CV_GENERATION_FAILED -> "CV generation unavailable";
            case CV_PREVIEW_EXPIRED -> "CV preview expired";
            case CV_NOT_SAVED -> "No saved CV is available";
            case CV_FILE_UNAVAILABLE -> "CV file unavailable";
            case DUPLICATE_COMPANY -> "Company already exists";
            case COMPANY_NOT_FOUND -> "Company not found";
            case INTERNSHIP_REQUEST_NOT_FOUND -> "Internship request not found";
            case DUPLICATE_REQUIRED_SKILL -> "Required skill already exists";
            case INVALID_TAXONOMY_SKILL -> "Invalid taxonomy skill";
            case UNSUPPORTED_MEDIA_TYPE -> "Unsupported media type";
            default -> sentenceCase(status.getReasonPhrase());
        };
    }

    private String errorSlug(ApiErrorCode code) {
        return code.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private String sentenceCase(String value) {
        if (value == null || value.isBlank()) {
            return "Request failed";
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
