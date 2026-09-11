package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

/** Raised when an identifier does not resolve to an active registered Student. */
public final class RegisteredStudentNotFoundException extends AdminStudentApiException {
    public RegisteredStudentNotFoundException() {
        super(
                HttpStatus.NOT_FOUND,
                "REGISTERED_STUDENT_NOT_FOUND",
                "Registered Student not found",
                "No registered Student exists for the supplied identifier.",
                Map.of());
    }
}
