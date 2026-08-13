package lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.entity.ProjectSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectSkillRepository extends JpaRepository<ProjectSkillEntity, ProjectSkillEntity.ProjectSkillId> {

    List<ProjectSkillEntity> findByIdProjectId(UUID projectId);

    @Modifying
    @Query("DELETE FROM ProjectSkillEntity ps WHERE ps.id.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);
}
