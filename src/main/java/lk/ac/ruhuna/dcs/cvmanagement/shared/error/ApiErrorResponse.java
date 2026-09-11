package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/**
 * Canonical RFC 9457-style Problem Details response used by shared exception handling.
 *
 * <p>The shape intentionally matches the frozen OpenAPI ProblemDetails schema. Internal exception
 * names, stack traces, SQL details and filesystem information are never exposed.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        String type,
        String title,
        int status,
        String code,
        String message,
        String correlationId,
        List<ApiFieldError> fieldErrors,
        Map<String, Object> details) {
}
