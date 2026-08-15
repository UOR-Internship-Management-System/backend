package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection;

import java.util.UUID;

/** Read-model row for a required-skill association and its taxonomy display name. */
public record InternshipRequiredSkillProjection(
        UUID requestId,
        UUID requiredSkillId,
        UUID skillId,
        String skillName) {
}
