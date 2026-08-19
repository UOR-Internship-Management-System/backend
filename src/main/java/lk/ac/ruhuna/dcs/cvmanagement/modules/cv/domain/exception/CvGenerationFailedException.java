package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

public class CvGenerationFailedException extends ApplicationException {
    public CvGenerationFailedException() {
        super(ApiErrorCode.CV_GENERATION_FAILED, HttpStatus.SERVICE_UNAVAILABLE,
                "The CV could not be generated at this time.");
    }
}
