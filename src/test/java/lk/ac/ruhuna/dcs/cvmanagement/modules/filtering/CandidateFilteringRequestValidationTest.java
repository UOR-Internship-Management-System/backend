package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.request.CandidateFilteringRunRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillMatchMode;
import org.junit.jupiter.api.Test;

class CandidateFilteringRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsContractOptionalFieldsAndBoundaryGpaValues() {
        CandidateFilteringRunRequest request = new CandidateFilteringRunRequest(
                UUID.randomUUID(),
                new BigDecimal("0.00"),
                new BigDecimal("4.00"),
                null,
                null,
                FilterSkillMatchMode.AND);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsMissingRequiredContextAndMatchMode() {
        CandidateFilteringRunRequest request = new CandidateFilteringRunRequest(
                null, null, null, null, null, null);

        var fields = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());

        assertThat(fields).containsExactlyInAnyOrder("requestId", "skillMatchMode");
    }

    @Test
    void rejectsOutOfRangeAndOverPrecisionGpaValues() {
        CandidateFilteringRunRequest request = new CandidateFilteringRunRequest(
                UUID.randomUUID(),
                new BigDecimal("-0.01"),
                new BigDecimal("4.001"),
                List.of(),
                List.of(),
                FilterSkillMatchMode.OR);

        var fields = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());

        assertThat(fields).contains("runtimeGpaLowerBound", "runtimeGpaUpperBound");
    }

    @Test
    void rejectsOversizedSkillListsAndNullSkillElements() {
        List<UUID> oversized = new ArrayList<>();
        for (int index = 0; index < 101; index++) {
            oversized.add(UUID.randomUUID());
        }
        List<UUID> withNull = new ArrayList<>();
        withNull.add(null);

        CandidateFilteringRunRequest request = new CandidateFilteringRunRequest(
                UUID.randomUUID(),
                null,
                null,
                oversized,
                withNull,
                FilterSkillMatchMode.AND);

        var paths = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .toList();

        assertThat(paths).anyMatch(path -> path.equals("requestSkillIds"));
        assertThat(paths).anyMatch(path -> path.startsWith("additionalSkillIds"));
    }
}
