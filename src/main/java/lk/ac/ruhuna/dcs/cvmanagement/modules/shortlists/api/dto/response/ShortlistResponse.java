package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.domain.policy.ShortlistStatus;

/** Public shortlist summary contract. */
public record ShortlistResponse(
        UUID shortlistId,
        InternshipRequestSummaryResponse request,
        UUID filterRunId,
        String name,
        ShortlistStatus status,
        Integer guidanceValue,
        long selectedCandidateCount,
        boolean guidanceExceeded,
        String guidanceWarning,
        long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime finalizedAt) {
}
