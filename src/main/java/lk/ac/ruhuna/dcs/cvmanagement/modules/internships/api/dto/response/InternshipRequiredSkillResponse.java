package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.response;

import java.util.UUID;

/** Public normalized required-skill response. */
public record InternshipRequiredSkillResponse(UUID requiredSkillId, UUID skillId, String skillName) {
}
