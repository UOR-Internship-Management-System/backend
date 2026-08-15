package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection;

import java.util.UUID;

/** Module-local taxonomy snapshot used for validation and response mapping. */
public record SkillSnapshotProjection(UUID skillId, String skillName, boolean selectable) {
}
