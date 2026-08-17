package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.entity.FilterRunSkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.entity.FilterRunSkillEntity.FilterRunSkillId;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence boundary for normalized Candidate Filtering run-skill criteria. */
public interface FilterRunSkillRepository extends JpaRepository<FilterRunSkillEntity, FilterRunSkillId> {

    List<FilterRunSkillEntity> findAllByIdFilterRunId(UUID filterRunId);
}
