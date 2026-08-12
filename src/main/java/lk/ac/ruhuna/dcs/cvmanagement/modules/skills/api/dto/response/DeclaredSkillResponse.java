package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.domain.CompetencyLevel;

public record DeclaredSkillResponse(
    UUID declaredSkillId,
    UUID skillId,
    String skillName,
    CompetencyLevel competencyLevel,
    long version,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
}
