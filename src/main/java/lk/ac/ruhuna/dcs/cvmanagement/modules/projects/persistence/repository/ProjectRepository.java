package lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    @Query("""
            SELECT p FROM ProjectEntity p
            WHERE p.studentId = :studentId
              AND LOWER(p.title) LIKE :searchPattern
            """)
    Page<ProjectEntity> search(
        @Param("studentId") UUID studentId, @Param("searchPattern") String searchPattern, Pageable pageable);
    List<ProjectEntity> findAllByStudentIdAndIdIn(UUID studentId, java.util.Collection<UUID> ids);

}
