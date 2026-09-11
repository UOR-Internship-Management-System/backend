package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.persistence;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application.port.ProjectSkillLookup;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application.port.ProjectSkillSummary;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity.SkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository.SkillRepository;
import org.springframework.stereotype.Component;

@Component
public class ProjectSkillLookupAdapter implements ProjectSkillLookup {

    private final SkillRepository skillRepository;

    public ProjectSkillLookupAdapter(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Override
    public Map<UUID, ProjectSkillSummary> findByIds(Collection<UUID> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return Map.of();
        }
        return skillRepository.findAllById(skillIds).stream()
                .map(this::toSummary)
                .collect(Collectors.toUnmodifiableMap(ProjectSkillSummary::skillId, Function.identity()));
    }

    private ProjectSkillSummary toSummary(SkillEntity skill) {
        return new ProjectSkillSummary(skill.getId(), skill.getSkillName(), skill.getSkillDescription());
    }
}
