package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

/** Raised when saved CV metadata exists but the persisted PDF cannot currently be served. */
public final class CvFileUnavailableException extends AdminStudentApiException {
    public CvFileUnavailableException(Throwable cause) {
        super(
                HttpStatus.SERVICE_UNAVAILABLE,
                "CV_FILE_UNAVAILABLE",
                "CV PDF unavailable",
                "The saved CV PDF cannot be downloaded at this time.",
                Map.of());
        if (cause != null) {
            initCause(cause);
        }
    }
}
