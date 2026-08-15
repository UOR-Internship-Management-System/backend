package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

public final class InternshipRequestNotFoundException extends ApplicationException {
    public InternshipRequestNotFoundException() {
        super(ApiErrorCode.INTERNSHIP_REQUEST_NOT_FOUND, HttpStatus.NOT_FOUND,
                "The requested internship request does not exist.");
    }
}
