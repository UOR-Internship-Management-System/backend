package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.CandidateDeclaredSkillCompetencyLevel;

/**
 * Filtering-owned declared-skill response matching the public DeclaredSkillResponse shape without
 * importing the Skills module's Java types.
 */
public record CandidateFilteringDeclaredSkillResponse(
        UUID declaredSkillId,
        UUID skillId,
        String skillName,
        CandidateDeclaredSkillCompetencyLevel competencyLevel,
        long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public CandidateFilteringDeclaredSkillResponse {
        Objects.requireNonNull(declaredSkillId, "declaredSkillId is required.");
        Objects.requireNonNull(skillId, "skillId is required.");
        Objects.requireNonNull(skillName, "skillName is required.");
        Objects.requireNonNull(competencyLevel, "competencyLevel is required.");
        Objects.requireNonNull(createdAt, "createdAt is required.");
        Objects.requireNonNull(updatedAt, "updatedAt is required.");
        if (version < 0) {
            throw new IllegalArgumentException("version must not be negative.");
        }
    }
}
