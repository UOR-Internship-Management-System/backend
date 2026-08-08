package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.mapper;

import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.DeclaredSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.api.dto.response.IndividualSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity.DeclaredSkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity.SkillEntity;
import org.springframework.stereotype.Component;

@Component
public class SkillMapper {

    public IndividualSkillResponse toResponse(SkillEntity entity) {
        return new IndividualSkillResponse(entity.getId(), entity.getSkillName(), entity.getSkillDescription());
    }

    public DeclaredSkillResponse toResponse(DeclaredSkillEntity entity, String skillName) {
        return new DeclaredSkillResponse(
            entity.getId(),
            entity.getSkillId(),
            skillName,
            entity.getCompetencyLevel(),
            entity.getVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt());
    }
}
