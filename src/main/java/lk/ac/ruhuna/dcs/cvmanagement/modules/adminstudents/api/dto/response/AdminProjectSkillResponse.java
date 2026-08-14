package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response;

import java.util.UUID;

/** Read-only skill attached to a Student project. */
public record AdminProjectSkillResponse(UUID skillId, String name, String description) {
}
