package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity.SkillCoreClusterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillCoreClusterRepository extends JpaRepository<SkillCoreClusterEntity, UUID> {
    List<SkillCoreClusterEntity> findByActiveTrueOrderByDisplayOrderAsc();
    Page<SkillCoreClusterEntity> findByActiveTrue(Pageable pageable);
}
