package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.entity.FilterRunSkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.entity.FilterRunSkillEntity.FilterRunSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Persistence boundary for normalized Candidate Filtering run-skill criteria. */
public interface FilterRunSkillRepository extends JpaRepository<FilterRunSkillEntity, FilterRunSkillId> {

    @Query("""
            SELECT skill
            FROM FilterRunSkillEntity skill
            WHERE skill.id.filterRunId = :filterRunId
            ORDER BY skill.id.skillId ASC
            """)
    List<FilterRunSkillEntity> findAllByFilterRunId(@Param("filterRunId") UUID filterRunId);
}
