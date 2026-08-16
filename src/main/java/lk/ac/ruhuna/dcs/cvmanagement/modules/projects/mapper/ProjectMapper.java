package lk.ac.ruhuna.dcs.cvmanagement.modules.projects.mapper;

import java.util.List;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.response.ProjectResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.response.ProjectSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application.port.ProjectSkillSummary;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.entity.ProjectEntity;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(ProjectEntity entity, List<ProjectSkillSummary> skills) {
        return new ProjectResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getRepositoryUrl(),
            entity.getDemoUrl(),
            entity.getStartDate(),
            entity.getEndDate(),
            skills.stream()
                    .map(skill -> new ProjectSkillResponse(skill.skillId(), skill.name(), skill.description()))
                    .toList(),
            entity.isIncludeInCv(),
            entity.getVersion() != null ? entity.getVersion() : 0L,
            entity.getCreatedAt(),
            entity.getUpdatedAt());
    }
}
