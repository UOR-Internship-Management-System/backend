package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillMatchMode;

/** Public request contract for creating one deterministic Candidate Filtering run. */
public record CandidateFilteringRunRequest(
        @NotNull(message = "requestId is required.")
        UUID requestId,

        @DecimalMin(value = "0.00", message = "runtimeGpaLowerBound must be between 0.00 and 4.00.")
        @DecimalMax(value = "4.00", message = "runtimeGpaLowerBound must be between 0.00 and 4.00.")
        @Digits(integer = 1, fraction = 2, message = "runtimeGpaLowerBound may use at most two decimal places.")
        BigDecimal runtimeGpaLowerBound,

        @DecimalMin(value = "0.00", message = "runtimeGpaUpperBound must be between 0.00 and 4.00.")
        @DecimalMax(value = "4.00", message = "runtimeGpaUpperBound must be between 0.00 and 4.00.")
        @Digits(integer = 1, fraction = 2, message = "runtimeGpaUpperBound may use at most two decimal places.")
        BigDecimal runtimeGpaUpperBound,

        @Size(max = 100, message = "requestSkillIds may contain at most 100 skills.")
        List<@NotNull(message = "requestSkillIds must not contain null skill IDs.") UUID> requestSkillIds,

        @Size(max = 100, message = "additionalSkillIds may contain at most 100 skills.")
        List<@NotNull(message = "additionalSkillIds must not contain null skill IDs.") UUID> additionalSkillIds,

        @NotNull(message = "skillMatchMode is required.")
        FilterSkillMatchMode skillMatchMode) {
}
