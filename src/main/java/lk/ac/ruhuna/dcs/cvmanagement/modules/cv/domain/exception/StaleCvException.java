package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

public class StaleCvException extends ApplicationException {
    public StaleCvException() {
        super(ApiErrorCode.STALE_VERSION, HttpStatus.PRECONDITION_FAILED,
                "The saved CV changed in another request.");
    }
}
