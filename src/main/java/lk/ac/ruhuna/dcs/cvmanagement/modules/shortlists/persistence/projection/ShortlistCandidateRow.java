package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.projection;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Current candidate facts combined with immutable selection metadata. */
public record ShortlistCandidateRow(
        UUID studentId,
        String indexNumber,
        String fullName,
        BigDecimal officialGpa,
        boolean hasLatestSavedCv,
        int activeShortlistCount,
        OffsetDateTime selectedAt,
        String selectionNote) {
}
