package lk.ac.ruhuna.dcs.cvmanagement.modules.companies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.domain.policy.CompanySort;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.BadRequestException;
import org.junit.jupiter.api.Test;

class CompanySortTest {

    @Test
    void defaultsToNameAscending() {
        assertThat(CompanySort.fromApiValue(null)).isEqualTo(CompanySort.NAME_ASC);
        assertThat(CompanySort.fromApiValue("")).isEqualTo(CompanySort.NAME_ASC);
    }

    @Test
    void acceptsOnlyFrozenCompanySortValues() {
        assertThat(CompanySort.fromApiValue("name,asc")).isEqualTo(CompanySort.NAME_ASC);
        assertThat(CompanySort.fromApiValue("name,desc")).isEqualTo(CompanySort.NAME_DESC);
        assertThat(CompanySort.fromApiValue("updatedAt,desc")).isEqualTo(CompanySort.UPDATED_AT_DESC);

        assertThatThrownBy(() -> CompanySort.fromApiValue("notes,asc"))
                .isInstanceOf(BadRequestException.class);
    }
}
