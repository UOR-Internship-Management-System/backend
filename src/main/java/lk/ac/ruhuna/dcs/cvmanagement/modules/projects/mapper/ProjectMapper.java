package lk.ac.ruhuna.dcs.cvmanagement.modules.projects.mapper;

import java.util.List;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.response.ProjectResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.entity.ProjectEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.IndividualSkillResponse;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(ProjectEntity entity, List<IndividualSkillResponse> skills) {
        return new ProjectResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getRepositoryUrl(),
            entity.getDemoUrl(),
            entity.getStartDate(),
            entity.getEndDate(),
            skills,
            entity.isIncludeInCv(),
            entity.getVersion() != null ? entity.getVersion() : 0L,
            entity.getCreatedAt(),
            entity.getUpdatedAt());
    }
}
