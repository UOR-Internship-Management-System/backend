package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

public class CvPreconditionRequiredException extends ApplicationException {
    public CvPreconditionRequiredException() {
        super(ApiErrorCode.PRECONDITION_REQUIRED, HttpStatus.PRECONDITION_REQUIRED,
                "A current conditional request header is required.");
    }
}
