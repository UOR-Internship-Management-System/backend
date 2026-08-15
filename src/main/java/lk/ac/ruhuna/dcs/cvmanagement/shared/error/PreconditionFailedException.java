package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

import org.springframework.http.HttpStatus;

/** Raised when a supplied optimistic-concurrency version no longer matches persisted state. */
public class PreconditionFailedException extends ApplicationException {

    public PreconditionFailedException(String message) {
        super(ApiErrorCode.PRECONDITION_FAILED, HttpStatus.PRECONDITION_FAILED, message);
    }
}
