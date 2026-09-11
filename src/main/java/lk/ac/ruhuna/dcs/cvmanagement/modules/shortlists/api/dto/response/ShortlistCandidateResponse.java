package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.domain.policy.ShortlistGpaAvailabilityStatus;

/** Current factual Student data plus persisted shortlist selection metadata. */
public record ShortlistCandidateResponse(
        UUID studentId,
        String indexNumber,
        String fullName,
        BigDecimal officialGpa,
        ShortlistGpaAvailabilityStatus gpaAvailabilityStatus,
        boolean hasLatestSavedCv,
        boolean hasExistingActiveShortlist,
        int existingActiveShortlistCount,
        OffsetDateTime selectedAt,
        String selectionNote) {
}
