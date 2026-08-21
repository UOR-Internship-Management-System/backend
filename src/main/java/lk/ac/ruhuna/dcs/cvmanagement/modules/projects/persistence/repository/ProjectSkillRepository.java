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

    @Query("""
            SELECT ps.id.projectId AS projectId, ps.id.skillId AS skillId,
                   s.skillName AS skillName, s.displayOrder AS displayOrder
            FROM ProjectSkillEntity ps
            JOIN SkillEntity s ON s.id = ps.id.skillId
            WHERE ps.id.projectId IN :projectIds
            ORDER BY ps.id.projectId ASC, s.displayOrder ASC NULLS LAST, LOWER(s.skillName) ASC, s.id ASC
            """)
    List<ProjectSkillCvProjection> findCvSkillsByProjectIds(@Param("projectIds") java.util.Collection<UUID> projectIds);

    interface ProjectSkillCvProjection {
        UUID getProjectId();
        UUID getSkillId();
        String getSkillName();
        Integer getDisplayOrder();
    }

    @Modifying
    @Query("DELETE FROM ProjectSkillEntity ps WHERE ps.id.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);
}
