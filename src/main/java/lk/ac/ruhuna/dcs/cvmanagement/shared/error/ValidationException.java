package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

import org.springframework.http.HttpStatus;

public class ValidationException extends ApplicationException {

    public ValidationException(String message) {
        super(ApiErrorCode.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, message);
    }
}
