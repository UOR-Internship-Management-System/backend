package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.ActivityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityRepository extends JpaRepository<ActivityEntity, UUID> {

    @Query("""
            SELECT a FROM ActivityEntity a
            WHERE a.studentId = :studentId
              AND LOWER(a.activityName) LIKE :searchPattern
            """)
    Page<ActivityEntity> search(
        @Param("studentId") UUID studentId, @Param("searchPattern") String searchPattern, Pageable pageable);
    List<ActivityEntity> findAllByStudentIdAndIdIn(UUID studentId, java.util.Collection<UUID> ids);

}
