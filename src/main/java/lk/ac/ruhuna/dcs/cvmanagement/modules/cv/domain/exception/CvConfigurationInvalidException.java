package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

public class CvConfigurationInvalidException extends ApplicationException {
    public CvConfigurationInvalidException() {
        super(ApiErrorCode.CV_CONFIGURATION_INVALID, HttpStatus.UNPROCESSABLE_ENTITY,
                "One or more selected records cannot be included in this CV preview.");
    }
}
