package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

public class CvNotSavedException extends ApplicationException {
    public CvNotSavedException() {
        super(ApiErrorCode.CV_NOT_SAVED, HttpStatus.NOT_FOUND, "No saved CV is available.");
    }
}
