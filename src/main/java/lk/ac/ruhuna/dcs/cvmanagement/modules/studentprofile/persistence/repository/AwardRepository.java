package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.AwardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AwardRepository extends JpaRepository<AwardEntity, UUID> {

    @Query("""
            SELECT a FROM AwardEntity a
            WHERE a.studentId = :studentId
              AND LOWER(a.title) LIKE :searchPattern
            """)
    Page<AwardEntity> search(
        @Param("studentId") UUID studentId, @Param("searchPattern") String searchPattern, Pageable pageable);
    List<AwardEntity> findAllByStudentIdAndIdIn(UUID studentId, java.util.Collection<UUID> ids);

}
