package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.domain.policy.ShortlistStatus;

/** Successful atomic shortlist finalization. */
public record ShortlistFinalizeResponse(
        UUID shortlistId,
        ShortlistStatus status,
        long selectedCandidateCount,
        Integer guidanceValue,
        boolean guidanceExceeded,
        boolean guidanceAcknowledged,
        long version,
        OffsetDateTime finalizedAt) {
}
