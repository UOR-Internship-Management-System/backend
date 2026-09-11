package lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.persistence.repository;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.persistence.entity.EligibleStudentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EligibleStudentRepository extends JpaRepository<EligibleStudentEntity, UUID> {

    @Query("""
            SELECT e FROM EligibleStudentEntity e
            WHERE LOWER(e.indexNumber) LIKE :searchPattern
               OR LOWER(e.universityEmail) LIKE :searchPattern
               OR LOWER(e.fullName) LIKE :searchPattern
            """)
    Page<EligibleStudentEntity> search(@Param("searchPattern") String searchPattern, Pageable pageable);

    boolean existsByIndexNumber(String indexNumber);

    boolean existsByUniversityEmail(String universityEmail);

    boolean existsByIndexNumberAndIdNot(String indexNumber, UUID id);

    boolean existsByUniversityEmailAndIdNot(String universityEmail, UUID id);
}
