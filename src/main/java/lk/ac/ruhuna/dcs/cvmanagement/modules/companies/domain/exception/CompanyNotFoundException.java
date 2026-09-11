package lk.ac.ruhuna.dcs.cvmanagement.modules.companies.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

/** Raised when an Admin addresses a Company identifier that does not exist. */
public final class CompanyNotFoundException extends ApplicationException {

    public CompanyNotFoundException() {
        super(ApiErrorCode.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND, "The company was not found.");
    }
}
