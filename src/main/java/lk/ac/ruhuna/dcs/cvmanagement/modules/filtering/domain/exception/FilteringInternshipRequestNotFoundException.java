package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

/** Filtering-owned 404 translation for a missing Internship Request context. */
public final class FilteringInternshipRequestNotFoundException extends ApplicationException {

    public FilteringInternshipRequestNotFoundException() {
        super(
                ApiErrorCode.INTERNSHIP_REQUEST_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "The internship request does not exist.");
    }
}
