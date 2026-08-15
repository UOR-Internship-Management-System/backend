package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

import org.springframework.http.HttpStatus;

/** Raised when an optimistic-concurrency protected mutation omits its required If-Match header. */
public class PreconditionRequiredException extends ApplicationException {

    public PreconditionRequiredException(String message) {
        super(ApiErrorCode.IF_MATCH_REQUIRED, HttpStatus.PRECONDITION_REQUIRED, message);
    }
}
