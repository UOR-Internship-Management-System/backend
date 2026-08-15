package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

public final class InternshipCompanyNotFoundException extends ApplicationException {
    public InternshipCompanyNotFoundException() {
        super(ApiErrorCode.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND, "The company was not found.");
    }
}
