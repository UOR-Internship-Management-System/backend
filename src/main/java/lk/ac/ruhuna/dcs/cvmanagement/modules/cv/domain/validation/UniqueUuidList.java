package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = UniqueUuidListValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueUuidList {
    String message() default "Selected record IDs must be unique.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
