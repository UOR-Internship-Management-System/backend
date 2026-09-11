package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.domain.CompetencyLevel;

public record DeclaredSkillUpdateRequest(@NotNull CompetencyLevel competencyLevel) {
}
