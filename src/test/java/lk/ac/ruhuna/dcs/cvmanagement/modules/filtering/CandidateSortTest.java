package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.CandidateSort;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.BadRequestException;
import org.junit.jupiter.api.Test;

class CandidateSortTest {

    @Test
    void exposesExactlyTheCurrentOpenApiSortAllowList() {
        assertThat(CandidateSort.values())
                .extracting(CandidateSort::apiValue)
                .containsExactly(
                        "officialGpa,desc",
                        "officialGpa,asc",
                        "fullName,asc",
                        "indexNumber,asc");
    }

    @Test
    void defaultsToOfficialGpaDescendingAndParsesApprovedValues() {
        assertThat(CandidateSort.fromApiValue(null)).isEqualTo(CandidateSort.OFFICIAL_GPA_DESC);
        assertThat(CandidateSort.fromApiValue("  fullName,asc  ")).isEqualTo(CandidateSort.FULL_NAME_ASC);
    }

    @Test
    void rejectsArbitrarySortInput() {
        assertThatThrownBy(() -> CandidateSort.fromApiValue("createdAt,desc"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("sort must be one of officialGpa,desc; officialGpa,asc; fullName,asc; indexNumber,asc.");
    }
}
