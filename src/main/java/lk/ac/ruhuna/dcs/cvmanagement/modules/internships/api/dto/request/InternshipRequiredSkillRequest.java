package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Taxonomy identifier selected as one required skill. */
public record InternshipRequiredSkillRequest(
        @NotNull(message = "skillId is required.") UUID skillId) {
}
