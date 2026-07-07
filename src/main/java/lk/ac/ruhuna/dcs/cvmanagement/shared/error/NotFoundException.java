package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApplicationException {

    public NotFoundException(String message) {
        super(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, message);
    }
}
