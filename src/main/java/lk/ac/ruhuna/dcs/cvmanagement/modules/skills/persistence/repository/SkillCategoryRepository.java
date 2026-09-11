package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity.SkillCategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SkillCategoryRepository extends JpaRepository<SkillCategoryEntity, UUID> {

    List<SkillCategoryEntity> findByCoreClusterIdOrderByDisplayOrderAsc(UUID coreClusterId);

    @Query("SELECT c FROM SkillCategoryEntity c WHERE (:clusterId IS NULL OR c.coreClusterId = :clusterId)")
    Page<SkillCategoryEntity> search(@Param("clusterId") UUID clusterId, Pageable pageable);
}
