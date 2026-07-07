package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApplicationException {

    public ConflictException(String message) {
        super(ApiErrorCode.CONFLICT, HttpStatus.CONFLICT, message);
    }
}
