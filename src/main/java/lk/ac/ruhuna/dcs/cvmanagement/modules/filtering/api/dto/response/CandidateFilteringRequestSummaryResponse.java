package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response;

import java.util.Objects;
import java.util.UUID;

/** Filtering-owned projection of the OpenAPI InternshipRequestSummaryResponse contract. */
public record CandidateFilteringRequestSummaryResponse(
        UUID requestId,
        UUID companyId,
        String companyName,
        String title,
        Integer shortlistGuidanceValue) {

    public CandidateFilteringRequestSummaryResponse {
        Objects.requireNonNull(requestId, "requestId is required.");
        Objects.requireNonNull(companyId, "companyId is required.");
        Objects.requireNonNull(companyName, "companyName is required.");
        Objects.requireNonNull(title, "title is required.");
        if (shortlistGuidanceValue != null && shortlistGuidanceValue < 0) {
            throw new IllegalArgumentException("shortlistGuidanceValue must not be negative.");
        }
    }
}
