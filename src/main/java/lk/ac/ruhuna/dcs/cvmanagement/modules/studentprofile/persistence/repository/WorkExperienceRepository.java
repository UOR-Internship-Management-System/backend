package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.WorkExperienceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkExperienceRepository extends JpaRepository<WorkExperienceEntity, UUID> {

    @Query("""
            SELECT w FROM WorkExperienceEntity w
            WHERE w.studentId = :studentId
              AND LOWER(w.organization) LIKE :searchPattern
            """)
    Page<WorkExperienceEntity> search(
        @Param("studentId") UUID studentId, @Param("searchPattern") String searchPattern, Pageable pageable);
}
