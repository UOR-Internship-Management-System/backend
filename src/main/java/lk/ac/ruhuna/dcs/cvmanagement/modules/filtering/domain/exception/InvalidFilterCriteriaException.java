package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

/** Raised when syntactically valid Candidate Filtering criteria violate deterministic filter rules. */
public final class InvalidFilterCriteriaException extends ApplicationException {

    public InvalidFilterCriteriaException(String message) {
        super(ApiErrorCode.INVALID_FILTER_CRITERIA, HttpStatus.UNPROCESSABLE_CONTENT, message);
    }
}
