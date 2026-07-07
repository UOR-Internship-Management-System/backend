package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ProblemDetailsFactory {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    public ApiErrorResponse create(
            HttpStatus status,
            ApiErrorCode code,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code.name(),
                message,
                request.getRequestURI(),
                request.getHeader(CORRELATION_ID_HEADER),
                fieldErrors == null || fieldErrors.isEmpty() ? null : Map.copyOf(fieldErrors));
    }
}
