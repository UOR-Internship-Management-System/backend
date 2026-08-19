package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class UniqueUuidListValidator implements ConstraintValidator<UniqueUuidList, List<UUID>> {
    @Override
    public boolean isValid(List<UUID> value, ConstraintValidatorContext context) {
        return value == null || new HashSet<>(value).size() == value.size();
    }
}
