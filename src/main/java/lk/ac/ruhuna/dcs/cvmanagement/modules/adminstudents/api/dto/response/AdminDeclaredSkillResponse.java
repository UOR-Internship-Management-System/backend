package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.AdminCompetencyLevel;

/** Read-only Student-declared skill projection. */
public record AdminDeclaredSkillResponse(
        UUID declaredSkillId,
        UUID skillId,
        String skillName,
        AdminCompetencyLevel competencyLevel,
        long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
