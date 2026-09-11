package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

public class CvFileUnavailableException extends ApplicationException {
    public CvFileUnavailableException() {
        super(ApiErrorCode.CV_FILE_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE,
                "The saved CV file is temporarily unavailable.");
    }
}
