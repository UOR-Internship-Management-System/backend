package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

/** Raised when an authoritative dependency required by the public filtering contract is unavailable. */
public final class FilterDependencyUnavailableException extends ApplicationException {

    public FilterDependencyUnavailableException() {
        this("Committed academic or declared-skill data is temporarily unavailable.");
    }

    public FilterDependencyUnavailableException(String message) {
        super(
                ApiErrorCode.FILTER_DEPENDENCY_UNAVAILABLE,
                HttpStatus.SERVICE_UNAVAILABLE,
                message);
    }

    public FilterDependencyUnavailableException(Throwable cause) {
        this();
        if (cause != null) {
            initCause(cause);
        }
    }
}
