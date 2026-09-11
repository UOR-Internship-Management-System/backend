package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response;

import java.util.UUID;

/** Result of an idempotent shortlist membership mutation. */
public record ShortlistCandidateMutationResponse(
        UUID shortlistId,
        int addedCount,
        int alreadyPresentCount,
        int removedCount,
        long selectedCandidateCount,
        boolean guidanceExceeded,
        long version) {
}
