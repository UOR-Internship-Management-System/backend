package lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.response;

import java.util.UUID;

/** Project-owned representation that preserves the existing skill JSON contract. */
public record ProjectSkillResponse(UUID skillId, String name, String description) {
}
