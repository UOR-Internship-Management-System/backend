package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository;

import java.util.Optional;
import java.util.UUID;
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

    boolean existsByStudentIdAndSkillId(UUID studentId, UUID skillId);

    Optional<DeclaredSkillEntity> findByIdAndStudentId(UUID id, UUID studentId);
}
