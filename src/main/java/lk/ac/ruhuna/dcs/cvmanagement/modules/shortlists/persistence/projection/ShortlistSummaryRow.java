package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.projection;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.domain.policy.ShortlistStatus;

/** One bounded shortlist list/detail summary row. */
public record ShortlistSummaryRow(
        UUID shortlistId,
        UUID requestId,
        UUID companyId,
        String companyName,
        String roleTitle,
        Integer requestGuidanceValue,
        UUID filterRunId,
        String name,
        ShortlistStatus status,
        Integer guidanceValue,
        long selectedCandidateCount,
        long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime finalizedAt) {
}
