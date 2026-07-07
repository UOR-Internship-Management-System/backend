package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApplicationException {

    public UnauthorizedException(String message) {
        super(ApiErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, message);
    }
}
