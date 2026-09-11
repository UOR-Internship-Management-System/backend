package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.projection;

import java.math.BigDecimal;
import java.util.UUID;

/** Base database projection for one deterministic Candidate Filtering result row. */
public record CandidateFilterRow(
        UUID studentId,
        String indexNumber,
        String fullName,
        BigDecimal officialGpa,
        int declaredSkillCount) {
}
