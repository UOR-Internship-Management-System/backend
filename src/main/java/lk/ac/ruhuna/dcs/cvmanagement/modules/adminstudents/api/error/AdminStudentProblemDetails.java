package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/** OpenAPI v1.6.0 compatible problem response for Admin Student Inspection errors. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminStudentProblemDetails(
        String type,
        String title,
        int status,
        String code,
        String message,
        String correlationId,
        List<AdminStudentFieldError> fieldErrors,
        Map<String, Object> details) {
}
