package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/** Persisted Candidate Filtering run metadata with a current recomputed candidate count. */
public record CandidateFilteringRunResponse(
        UUID filterRunId,
        CandidateFilteringRequestSummaryResponse request,
        CandidateFilteringCriteriaResponse criteria,
        long candidateCount,
        OffsetDateTime createdAt) {

    public CandidateFilteringRunResponse {
        Objects.requireNonNull(filterRunId, "filterRunId is required.");
        Objects.requireNonNull(request, "request is required.");
        Objects.requireNonNull(criteria, "criteria is required.");
        Objects.requireNonNull(createdAt, "createdAt is required.");
        if (candidateCount < 0) {
            throw new IllegalArgumentException("candidateCount must not be negative.");
        }
    }
}
