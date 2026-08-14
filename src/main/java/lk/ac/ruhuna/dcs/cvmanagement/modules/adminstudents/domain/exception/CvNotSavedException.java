package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

/** Raised when a CV download is requested but the Student has no saved CV. */
public final class CvNotSavedException extends AdminStudentApiException {
    public CvNotSavedException() {
        super(
                HttpStatus.NOT_FOUND,
                "CV_NOT_SAVED",
                "No saved CV is available",
                "Save the CV before downloading it.",
                Map.of());
    }
}
