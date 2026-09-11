package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request.AdminStudentSearchCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.RegisteredStudentSort;
import org.junit.jupiter.api.Test;

class AdminStudentSearchCriteriaTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void appliesContractDefaultsAndTrimsSearch() {
        AdminStudentSearchCriteria criteria = new AdminStudentSearchCriteria(null, null, null, "  Perera  ", null);

        assertThat(criteria.pageOrDefault()).isZero();
        assertThat(criteria.sizeOrDefault()).isEqualTo(20);
        assertThat(criteria.normalizedSearch()).isEqualTo("Perera");
        assertThat(criteria.parsedSort()).isEqualTo(RegisteredStudentSort.FULL_NAME_ASC);
        assertThat(validator.validate(criteria)).isEmpty();
    }

    @Test
    void rejectsOutOfContractPaginationAndLevel() {
        AdminStudentSearchCriteria criteria = new AdminStudentSearchCriteria(-1, 101, null, null, 5);

        assertThat(validator.validate(criteria)).hasSize(3);
    }
}
