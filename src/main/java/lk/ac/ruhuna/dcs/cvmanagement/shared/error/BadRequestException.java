package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApplicationException {

    public BadRequestException(String message) {
        super(ApiErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, message);
    }
}
