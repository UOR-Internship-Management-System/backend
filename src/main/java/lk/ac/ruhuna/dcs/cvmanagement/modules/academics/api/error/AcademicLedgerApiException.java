package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error;

import java.io.Serial;
import java.util.Map;
import org.springframework.http.HttpStatus;

/** Stable API exception for Academic Ledger transport and lifecycle errors. */
public class AcademicLedgerApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    private final String code;
    private final String title;
    private final transient Map<String, Object> details;

    public AcademicLedgerApiException(
            HttpStatus status,
            String code,
            String title,
            String message,
            Map<String, ?> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.title = title;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String title() {
        return title;
    }

    public Map<String, Object> details() {
        return details;
    }
}
