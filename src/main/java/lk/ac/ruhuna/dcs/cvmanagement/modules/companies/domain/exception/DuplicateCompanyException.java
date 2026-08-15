package lk.ac.ruhuna.dcs.cvmanagement.modules.companies.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

/** Raised when a normalized Company name conflicts with an existing record. */
public final class DuplicateCompanyException extends ApplicationException {

    public DuplicateCompanyException() {
        super(
                ApiErrorCode.DUPLICATE_COMPANY,
                HttpStatus.CONFLICT,
                "A company with the same normalized name already exists.");
    }
}
