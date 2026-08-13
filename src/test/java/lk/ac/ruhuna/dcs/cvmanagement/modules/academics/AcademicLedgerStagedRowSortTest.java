package lk.ac.ruhuna.dcs.cvmanagement.modules.academics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerApiException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerStagedRowSort;
import org.junit.jupiter.api.Test;

class AcademicLedgerStagedRowSortTest {

    @Test
    void defaultsToRowNumberAscendingWithStableIdTieBreak() {
        var sort = AcademicLedgerStagedRowSort.fromApiValue(null);
        assertThat(sort.apiValue()).isEqualTo("rowNumber,asc");
        assertThat(sort.sort().getOrderFor("rowNumber")).isNotNull();
        assertThat(sort.sort().getOrderFor("id")).isNotNull();
    }

    @Test
    void rejectsSortsOutsideTheOpenApiAllowlist() {
        assertThatThrownBy(() -> AcademicLedgerStagedRowSort.fromApiValue("courseTitle,desc"))
                .isInstanceOf(AcademicLedgerApiException.class);
    }
}
