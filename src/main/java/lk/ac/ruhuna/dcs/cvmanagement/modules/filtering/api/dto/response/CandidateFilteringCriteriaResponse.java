package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillMatchMode;

/** Sanitized persisted criteria returned for a Candidate Filtering run. */
public record CandidateFilteringCriteriaResponse(
        UUID requestId,
        BigDecimal runtimeGpaLowerBound,
        BigDecimal runtimeGpaUpperBound,
        List<UUID> requestSkillIds,
        List<UUID> additionalSkillIds,
        FilterSkillMatchMode skillMatchMode) {

    public CandidateFilteringCriteriaResponse {
        Objects.requireNonNull(requestId, "requestId is required.");
        Objects.requireNonNull(skillMatchMode, "skillMatchMode is required.");
        requestSkillIds = requestSkillIds == null ? List.of() : List.copyOf(requestSkillIds);
        additionalSkillIds = additionalSkillIds == null ? List.of() : List.copyOf(additionalSkillIds);
    }
}
