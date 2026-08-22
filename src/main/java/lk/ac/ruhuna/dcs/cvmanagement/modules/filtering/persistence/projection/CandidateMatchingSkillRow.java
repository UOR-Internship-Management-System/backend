package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.projection;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.CandidateDeclaredSkillCompetencyLevel;

/** Filtering-owned read projection for one selected skill currently declared by one candidate. */
public record CandidateMatchingSkillRow(
        UUID studentId,
        UUID declaredSkillId,
        UUID skillId,
        String skillName,
        CandidateDeclaredSkillCompetencyLevel competencyLevel,
        long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
