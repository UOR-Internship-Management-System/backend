package lk.ac.ruhuna.dcs.cvmanagement.shared.http;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Central correlation-id resolution shared by HTTP errors, security responses, logs and auditing. */
public final class CorrelationIdContext {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String REQUEST_ATTRIBUTE = CorrelationIdContext.class.getName() + ".correlationId";

    private static final int MAX_LENGTH = 100;
    private static final Pattern SAFE_VALUE = Pattern.compile("[A-Za-z0-9._:-]{1," + MAX_LENGTH + "}");

    private CorrelationIdContext() {
    }

    /** Returns the established request correlation id, creating one if the request has none. */
    public static String ensure(HttpServletRequest request) {
        Object existing = request.getAttribute(REQUEST_ATTRIBUTE);
        if (existing instanceof String value && !value.isBlank()) {
            return value;
        }

        String value = safeHeader(request.getHeader(CORRELATION_ID_HEADER))
                .or(() -> safeHeader(request.getHeader(REQUEST_ID_HEADER)))
                .orElseGet(() -> UUID.randomUUID().toString());
        request.setAttribute(REQUEST_ATTRIBUTE, value);
        return value;
    }

    /** Returns the current request correlation id when called from an HTTP request thread. */
    public static Optional<String> current() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return Optional.empty();
        }
        return Optional.of(ensure(servletAttributes.getRequest()));
    }

    private static Optional<String> safeHeader(String candidate) {
        if (candidate == null) {
            return Optional.empty();
        }
        String trimmed = candidate.trim();
        return SAFE_VALUE.matcher(trimmed).matches() ? Optional.of(trimmed) : Optional.empty();
    }
}
