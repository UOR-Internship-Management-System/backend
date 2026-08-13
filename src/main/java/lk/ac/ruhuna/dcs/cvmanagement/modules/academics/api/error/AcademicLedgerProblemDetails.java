package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/** OpenAPI v1.6.0 compatible problem response for Academic Ledger errors. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AcademicLedgerProblemDetails(
        String type,
        String title,
        int status,
        String code,
        String message,
        String correlationId,
        Map<String, Object> details) {
}
