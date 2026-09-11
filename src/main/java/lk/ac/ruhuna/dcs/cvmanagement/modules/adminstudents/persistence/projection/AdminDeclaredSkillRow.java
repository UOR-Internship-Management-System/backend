package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.AdminCompetencyLevel;

/** Immutable database projection for one Student-declared skill. */
public record AdminDeclaredSkillRow(
        UUID declaredSkillId,
        UUID skillId,
        String skillName,
        AdminCompetencyLevel competencyLevel,
        long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
