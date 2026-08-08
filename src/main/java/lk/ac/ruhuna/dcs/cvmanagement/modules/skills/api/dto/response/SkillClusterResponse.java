package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response;

import java.util.List;
import java.util.UUID;

public record SkillClusterResponse(
    UUID clusterId, String name, String description, List<SkillCategoryResponse> categories) {
}
