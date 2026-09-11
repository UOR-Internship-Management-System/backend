package lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity.SkillEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SkillRepository extends JpaRepository<SkillEntity, UUID> {

    // Primary-category skills for a category, used for the flat /skill-taxonomy/skills list.
    @Query("""
        SELECT s FROM SkillEntity s
        JOIN SkillCategoryEntity c ON c.id = s.skillCategoryId
        WHERE (:clusterId IS NULL OR c.coreClusterId = :clusterId)
          AND (:categoryId IS NULL OR s.skillCategoryId = :categoryId)
          AND LOWER(s.skillName) LIKE :searchPattern
        """)
    Page<SkillEntity> search(
        @Param("clusterId") UUID clusterId,
        @Param("categoryId") UUID categoryId,
        @Param("searchPattern") String searchPattern,
        Pageable pageable);

    // Primary OR additionally-mapped skills for a category — used when assembling the nested tree,
    // so a skill like "Python" correctly shows under every category it's mapped to.
    @Query(value = """
            SELECT DISTINCT s.* FROM skills s
            WHERE s.skill_category_id = :categoryId
               OR s.id IN (SELECT skill_id FROM skill_category_mappings WHERE skill_category_id = :categoryId)
            ORDER BY s.display_order
            """, nativeQuery = true)
    List<SkillEntity> findAllForCategory(@Param("categoryId") UUID categoryId);
}
