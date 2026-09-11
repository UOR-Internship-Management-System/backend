package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

public final class DuplicateRequiredSkillException extends ApplicationException {
    public DuplicateRequiredSkillException() {
        super(ApiErrorCode.DUPLICATE_REQUIRED_SKILL, HttpStatus.CONFLICT,
                "The skill is already required by this internship request.");
    }
}
