package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

/** Raised when authoritative academic data cannot currently be queried. */
public final class AdminAcademicDataUnavailableException extends AdminStudentApiException {
    public AdminAcademicDataUnavailableException(Throwable cause) {
        super(
                HttpStatus.SERVICE_UNAVAILABLE,
                "ACADEMIC_DATA_UNAVAILABLE",
                "Academic data unavailable",
                "Official academic data cannot be loaded at this time.",
                Map.of());
        initCause(cause);
    }
}
