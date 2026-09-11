package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.EducationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EducationRepository extends JpaRepository<EducationEntity, UUID> {

    @Query("""
            SELECT e FROM EducationEntity e
            WHERE e.studentId = :studentId
              AND (LOWER(e.degree) LIKE :searchPattern OR LOWER(e.institution) LIKE :searchPattern)
            """)
    Page<EducationEntity> search(
        @Param("studentId") UUID studentId, @Param("searchPattern") String searchPattern, Pageable pageable);

    List<EducationEntity> findAllByStudentIdAndCvIncludeTrue(UUID studentId);
}
