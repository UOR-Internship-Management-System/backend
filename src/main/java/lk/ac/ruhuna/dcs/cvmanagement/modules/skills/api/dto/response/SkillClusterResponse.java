package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SkillClusterResponse(
    UUID clusterId, String name, String description, List<SkillCategoryResponse> categories) {
}
