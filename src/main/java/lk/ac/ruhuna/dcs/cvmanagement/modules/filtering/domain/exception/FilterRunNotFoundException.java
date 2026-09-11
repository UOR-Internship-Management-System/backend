package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

/** Raised when an Admin addresses a Candidate Filtering run that no longer exists. */
public final class FilterRunNotFoundException extends ApplicationException {

    public FilterRunNotFoundException() {
        super(
                ApiErrorCode.FILTER_RUN_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "The filtering run does not exist.");
    }
}
