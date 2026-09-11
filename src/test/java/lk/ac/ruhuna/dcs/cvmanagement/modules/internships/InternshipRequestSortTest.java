package lk.ac.ruhuna.dcs.cvmanagement.modules.internships;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.domain.policy.InternshipRequestSort;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ValidationException;
import org.junit.jupiter.api.Test;

class InternshipRequestSortTest {

    @Test
    void defaultsToCreatedAtDescendingAndUsesStableRequestIdSecondaryOrder() {
        assertThat(InternshipRequestSort.fromApiValue(null)).isEqualTo(InternshipRequestSort.CREATED_AT_DESC);
        assertThat(InternshipRequestSort.CREATED_AT_DESC.sqlOrder()).endsWith("ir.id ASC");
        assertThat(InternshipRequestSort.TITLE_ASC.sqlOrder()).endsWith("ir.id ASC");
        assertThat(InternshipRequestSort.COMPANY_NAME_ASC.sqlOrder()).endsWith("ir.id ASC");
    }

    @Test
    void acceptsOnlyFrozenSortValues() {
        assertThat(InternshipRequestSort.fromApiValue("title,asc"))
                .isEqualTo(InternshipRequestSort.TITLE_ASC);
        assertThat(InternshipRequestSort.fromApiValue("companyName,asc"))
                .isEqualTo(InternshipRequestSort.COMPANY_NAME_ASC);
        assertThatThrownBy(() -> InternshipRequestSort.fromApiValue("title,desc"))
                .isInstanceOf(ValidationException.class);
    }
}
