package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

import org.springframework.http.HttpStatus;

public class PreconditionFailedException extends ApplicationException {
    public PreconditionFailedException(String message) {
        super(ApiErrorCode.CONFLICT, HttpStatus.PRECONDITION_FAILED, message);
    }
}
