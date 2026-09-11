package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringCandidateResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringCriteriaResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillMatchMode;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.GpaAvailabilityStatus;
import org.junit.jupiter.api.Test;

class CandidateFilteringResponseContractTest {

    @Test
    void criteriaResponseDefensivelyCopiesSkillLists() {
        List<UUID> requestSkills = new ArrayList<>(List.of(UUID.randomUUID()));
        CandidateFilteringCriteriaResponse response = new CandidateFilteringCriteriaResponse(
                UUID.randomUUID(), null, null, requestSkills, null, FilterSkillMatchMode.OR);

        requestSkills.clear();

        assertThat(response.requestSkillIds()).hasSize(1);
        assertThat(response.additionalSkillIds()).isEmpty();
    }

    @Test
    void candidateResponseEnforcesGpaAvailabilityConsistency() {
        UUID studentId = UUID.randomUUID();

        assertThatThrownBy(() -> new CandidateFilteringCandidateResponse(
                        studentId,
                        "E/20/001",
                        "Student One",
                        new BigDecimal("3.50"),
                        GpaAvailabilityStatus.NOT_AVAILABLE,
                        List.of(),
                        0,
                        false,
                        false,
                        0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GPA availability must agree with the GPA value.");
    }

    @Test
    void candidateResponseEnforcesShortlistIndicatorConsistency() {
        assertThatThrownBy(() -> new CandidateFilteringCandidateResponse(
                        UUID.randomUUID(),
                        "E/20/002",
                        "Student Two",
                        null,
                        GpaAvailabilityStatus.NOT_AVAILABLE,
                        List.of(),
                        0,
                        false,
                        true,
                        0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Shortlist availability must agree with its count.");
    }
}
