package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.InvalidFilterCriteriaException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.CandidateFilteringCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillMatchMode;
import org.junit.jupiter.api.Test;

class CandidateFilteringCriteriaTest {

    private static final UUID REQUEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID SKILL_A = UUID.fromString("00000000-0000-0000-0000-000000000201");
    private static final UUID SKILL_B = UUID.fromString("00000000-0000-0000-0000-000000000202");

    @Test
    void normalizesOptionalSkillListsAndExposesUsefulPolicyState() {
        CandidateFilteringCriteria criteria = new CandidateFilteringCriteria(
                REQUEST_ID,
                null,
                null,
                null,
                null,
                FilterSkillMatchMode.OR);

        assertThat(criteria.requestSkillIds()).isEmpty();
        assertThat(criteria.additionalSkillIds()).isEmpty();
        assertThat(criteria.selectedSkillIds()).isEmpty();
        assertThat(criteria.selectedSkillCount()).isZero();
        assertThat(criteria.hasGpaCriteria()).isFalse();
        assertThat(criteria.hasSkillCriteria()).isFalse();
    }

    @Test
    void acceptsInclusiveGpaBoundariesAndDefensivelyCopiesSkillLists() {
        List<UUID> requestSkills = new ArrayList<>(List.of(SKILL_A));
        List<UUID> additionalSkills = new ArrayList<>(List.of(SKILL_B));

        CandidateFilteringCriteria criteria = new CandidateFilteringCriteria(
                REQUEST_ID,
                new BigDecimal("0.00"),
                new BigDecimal("4.00"),
                requestSkills,
                additionalSkills,
                FilterSkillMatchMode.AND);

        requestSkills.clear();
        additionalSkills.clear();

        assertThat(criteria.requestSkillIds()).containsExactly(SKILL_A);
        assertThat(criteria.additionalSkillIds()).containsExactly(SKILL_B);
        assertThat(criteria.selectedSkillIds()).containsExactly(SKILL_A, SKILL_B);
        assertThat(criteria.selectedSkillCount()).isEqualTo(2);
        assertThat(criteria.hasGpaCriteria()).isTrue();
        assertThat(criteria.hasSkillCriteria()).isTrue();
    }

    @Test
    void acceptsEqualMinimumAndMaximumGpa() {
        CandidateFilteringCriteria criteria = criteria(
                new BigDecimal("3.25"),
                new BigDecimal("3.25"),
                List.of(),
                List.of());

        assertThat(criteria.runtimeGpaLowerBound()).isEqualByComparingTo("3.25");
        assertThat(criteria.runtimeGpaUpperBound()).isEqualByComparingTo("3.25");
    }

    @Test
    void rejectsMissingRequiredContextAndMode() {
        assertThatThrownBy(() -> new CandidateFilteringCriteria(
                        null, null, null, List.of(), List.of(), FilterSkillMatchMode.AND))
                .isInstanceOf(InvalidFilterCriteriaException.class)
                .hasMessage("requestId is required.");

        assertThatThrownBy(() -> new CandidateFilteringCriteria(
                        REQUEST_ID, null, null, List.of(), List.of(), null))
                .isInstanceOf(InvalidFilterCriteriaException.class)
                .hasMessage("skillMatchMode is required.");
    }

    @Test
    void rejectsInvalidGpaRangeBoundsAndPrecision() {
        assertThatThrownBy(() -> criteria(new BigDecimal("-0.01"), null, List.of(), List.of()))
                .isInstanceOf(InvalidFilterCriteriaException.class)
                .hasMessage("runtimeGpaLowerBound must be between 0.00 and 4.00.");

        assertThatThrownBy(() -> criteria(null, new BigDecimal("4.01"), List.of(), List.of()))
                .isInstanceOf(InvalidFilterCriteriaException.class)
                .hasMessage("runtimeGpaUpperBound must be between 0.00 and 4.00.");

        assertThatThrownBy(() -> criteria(new BigDecimal("3.001"), null, List.of(), List.of()))
                .isInstanceOf(InvalidFilterCriteriaException.class)
                .hasMessage("runtimeGpaLowerBound may use at most two decimal places.");

        assertThatThrownBy(() -> criteria(
                        new BigDecimal("3.50"), new BigDecimal("3.49"), List.of(), List.of()))
                .isInstanceOf(InvalidFilterCriteriaException.class)
                .hasMessage("Minimum GPA cannot exceed maximum GPA.");
    }

    @Test
    void rejectsNullDuplicateOverlappingAndOversizedSkillCriteria() {
        List<UUID> withNull = new ArrayList<>();
        withNull.add(SKILL_A);
        withNull.add(null);
        assertThatThrownBy(() -> criteria(null, null, withNull, List.of()))
                .isInstanceOf(InvalidFilterCriteriaException.class)
                .hasMessage("requestSkillIds must not contain null skill IDs.");

        assertThatThrownBy(() -> criteria(null, null, List.of(SKILL_A, SKILL_A), List.of()))
                .isInstanceOf(InvalidFilterCriteriaException.class)
                .hasMessage("requestSkillIds must not contain duplicate skill IDs.");

        assertThatThrownBy(() -> criteria(null, null, List.of(SKILL_A), List.of(SKILL_A)))
                .isInstanceOf(InvalidFilterCriteriaException.class)
                .hasMessage("requestSkillIds and additionalSkillIds must not overlap.");

        List<UUID> tooMany = java.util.stream.IntStream.range(0, 101)
                .mapToObj(ignored -> UUID.randomUUID())
                .toList();
        assertThatThrownBy(() -> criteria(null, null, tooMany, List.of()))
                .isInstanceOf(InvalidFilterCriteriaException.class)
                .hasMessage("requestSkillIds may contain at most 100 skills.");
    }

    private CandidateFilteringCriteria criteria(
            BigDecimal lower,
            BigDecimal upper,
            List<UUID> requestSkills,
            List<UUID> additionalSkills) {
        return new CandidateFilteringCriteria(
                REQUEST_ID,
                lower,
                upper,
                requestSkills,
                additionalSkills,
                FilterSkillMatchMode.AND);
    }
}
