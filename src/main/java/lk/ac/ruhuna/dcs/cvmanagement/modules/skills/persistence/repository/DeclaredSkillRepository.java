package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.domain.CompetencyLevel;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity.DeclaredSkillEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeclaredSkillRepository extends JpaRepository<DeclaredSkillEntity, UUID> {

    @Query("""
        SELECT d FROM DeclaredSkillEntity d
        JOIN SkillEntity s ON s.id = d.skillId
        WHERE d.studentId = :studentId
          AND LOWER(s.skillName) LIKE :searchPattern
        """)
    Page<DeclaredSkillEntity> search(
        @Param("studentId") UUID studentId, @Param("searchPattern") String searchPattern, Pageable pageable);

    @Query("""
        SELECT d.id AS declaredSkillId, d.skillId AS skillId, s.skillName AS skillName,
               s.displayOrder AS displayOrder, d.competencyLevel AS competencyLevel,
               d.version AS version, d.updatedAt AS updatedAt
        FROM DeclaredSkillEntity d
        JOIN SkillEntity s ON s.id = d.skillId
        WHERE d.studentId = :studentId
        ORDER BY s.displayOrder ASC NULLS LAST, LOWER(s.skillName) ASC, s.id ASC
        """)
    List<DeclaredSkillCvProjection> findCvSkills(@Param("studentId") UUID studentId);

    interface DeclaredSkillCvProjection {
        UUID getDeclaredSkillId();
        UUID getSkillId();
        String getSkillName();
        Integer getDisplayOrder();
        CompetencyLevel getCompetencyLevel();
        Long getVersion();
        OffsetDateTime getUpdatedAt();
    }

    boolean existsByStudentIdAndSkillId(UUID studentId, UUID skillId);

    Optional<DeclaredSkillEntity> findByIdAndStudentId(UUID id, UUID studentId);
}
