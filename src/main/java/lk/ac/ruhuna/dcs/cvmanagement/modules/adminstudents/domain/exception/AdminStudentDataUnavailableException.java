package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

/** Raised when persisted Student inspection data cannot be loaded safely. */
public final class AdminStudentDataUnavailableException extends AdminStudentApiException {

    public AdminStudentDataUnavailableException(Throwable cause) {
        super(
                HttpStatus.SERVICE_UNAVAILABLE,
                "ADMIN_STUDENT_DATA_UNAVAILABLE",
                "Student inspection data unavailable",
                "Student inspection data cannot be loaded at this time.",
                Map.of());
        if (cause != null) {
            initCause(cause);
        }
    }
}
