package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

import org.springframework.http.HttpStatus;

public class DependencyUnavailableException extends ApplicationException {

    public DependencyUnavailableException(String message) {
        super(ApiErrorCode.DEPENDENCY_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
