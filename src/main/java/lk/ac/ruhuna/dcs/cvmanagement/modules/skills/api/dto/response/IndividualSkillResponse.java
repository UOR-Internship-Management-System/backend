package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response;

import java.util.UUID;

public record IndividualSkillResponse(UUID skillId, String name, String description) {
}
