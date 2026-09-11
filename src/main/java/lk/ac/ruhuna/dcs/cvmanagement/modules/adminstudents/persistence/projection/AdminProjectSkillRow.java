package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection;

import java.util.UUID;

/** Immutable database projection for one canonical skill linked to a Student project. */
public record AdminProjectSkillRow(
        UUID projectId,
        UUID skillId,
        String name,
        String description) {
}
