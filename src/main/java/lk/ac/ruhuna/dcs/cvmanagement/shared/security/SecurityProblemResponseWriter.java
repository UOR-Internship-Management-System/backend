package lk.ac.ruhuna.dcs.cvmanagement.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/** Writes OpenAPI-compatible problem details for authentication and authorization failures. */
@Component
public class SecurityProblemResponseWriter {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final ObjectMapper objectMapper;

    public SecurityProblemResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void writeUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        write(
                request,
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "UNAUTHORIZED",
                "Authentication required",
                "Authentication is required to access this resource.");
    }

    public void writeForbidden(HttpServletRequest request, HttpServletResponse response) throws IOException {
        write(
                request,
                response,
                HttpServletResponse.SC_FORBIDDEN,
                "FORBIDDEN",
                "Access denied",
                "The current account cannot access this resource.");
    }

    private void write(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String code,
            String title,
            String message) throws IOException {
        String correlationId = correlationId(request);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "https://uor-cv-system/errors/" + code.toLowerCase().replace('_', '-'));
        body.put("title", title);
        body.put("status", status);
        body.put("code", code);
        body.put("message", message);
        body.put("correlationId", correlationId);

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(CORRELATION_HEADER, correlationId);
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private String correlationId(HttpServletRequest request) {
        String value = request.getHeader(CORRELATION_HEADER);
        if (value == null || value.isBlank()) {
            value = request.getHeader(REQUEST_ID_HEADER);
        }
        return value == null || value.isBlank() ? UUID.randomUUID().toString() : value.trim();
    }
}
