package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SkillCategoryResponse(
    UUID categoryId, String name, String description, List<IndividualSkillResponse> skills) {
}
