package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

public final class InvalidTaxonomySkillException extends ApplicationException {
    public InvalidTaxonomySkillException() {
        super(ApiErrorCode.INVALID_TAXONOMY_SKILL, HttpStatus.UNPROCESSABLE_CONTENT,
                "One or more selected skills are missing, inactive, or outside the managed taxonomy.");
    }
}
