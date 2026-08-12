package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.domain.CompetencyLevel;

public record DeclaredSkillCreateRequest(
    @NotNull UUID skillId,
    @NotNull CompetencyLevel competencyLevel) {
}
