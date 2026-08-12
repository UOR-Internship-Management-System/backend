package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response;

import java.util.List;
import java.util.UUID;

public record SkillCategoryResponse(
    UUID categoryId, String name, String description, List<IndividualSkillResponse> skills) {
}
