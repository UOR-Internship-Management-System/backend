package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

/** Raised only for transient failures while reading or persisting filtering dependencies. */
public final class FilterDependencyUnavailableException extends ApplicationException {

    public FilterDependencyUnavailableException() {
        super(
                ApiErrorCode.FILTER_DEPENDENCY_UNAVAILABLE,
                HttpStatus.SERVICE_UNAVAILABLE,
                "Committed academic or declared-skill data is temporarily unavailable.");
    }

    public FilterDependencyUnavailableException(Throwable cause) {
        this();
        if (cause != null) {
            initCause(cause);
        }
    }
}
