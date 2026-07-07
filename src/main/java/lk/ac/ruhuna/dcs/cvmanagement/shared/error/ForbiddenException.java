package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApplicationException {

    public ForbiddenException(String message) {
        super(ApiErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, message);
    }
}
