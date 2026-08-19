package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.repository;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.persistence.entity.CvSourceFreshnessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CvSourceFreshnessRepository extends JpaRepository<CvSourceFreshnessEntity, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO cv_source_freshness (student_id, profile_changed_at)
            VALUES (:studentId, :changedAt)
            ON CONFLICT (student_id) DO UPDATE
            SET profile_changed_at = CASE
                WHEN cv_source_freshness.profile_changed_at IS NULL
                  OR cv_source_freshness.profile_changed_at < EXCLUDED.profile_changed_at
                THEN EXCLUDED.profile_changed_at
                ELSE cv_source_freshness.profile_changed_at
            END
            """, nativeQuery = true)
    void upsertProfileChangedAt(@Param("studentId") UUID studentId, @Param("changedAt") OffsetDateTime changedAt);

    @Modifying
    @Query(value = """
            INSERT INTO cv_source_freshness (student_id, declared_skills_changed_at)
            VALUES (:studentId, :changedAt)
            ON CONFLICT (student_id) DO UPDATE
            SET declared_skills_changed_at = CASE
                WHEN cv_source_freshness.declared_skills_changed_at IS NULL
                  OR cv_source_freshness.declared_skills_changed_at < EXCLUDED.declared_skills_changed_at
                THEN EXCLUDED.declared_skills_changed_at
                ELSE cv_source_freshness.declared_skills_changed_at
            END
            """, nativeQuery = true)
    void upsertDeclaredSkillsChangedAt(@Param("studentId") UUID studentId, @Param("changedAt") OffsetDateTime changedAt);

    @Modifying
    @Query(value = """
            INSERT INTO cv_source_freshness (student_id, projects_changed_at)
            VALUES (:studentId, :changedAt)
            ON CONFLICT (student_id) DO UPDATE
            SET projects_changed_at = CASE
                WHEN cv_source_freshness.projects_changed_at IS NULL
                  OR cv_source_freshness.projects_changed_at < EXCLUDED.projects_changed_at
                THEN EXCLUDED.projects_changed_at
                ELSE cv_source_freshness.projects_changed_at
            END
            """, nativeQuery = true)
    void upsertProjectsChangedAt(@Param("studentId") UUID studentId, @Param("changedAt") OffsetDateTime changedAt);

    @Modifying
    @Query(value = """
            INSERT INTO cv_source_freshness (student_id, academic_records_changed_at)
            VALUES (:studentId, :changedAt)
            ON CONFLICT (student_id) DO UPDATE
            SET academic_records_changed_at = CASE
                WHEN cv_source_freshness.academic_records_changed_at IS NULL
                  OR cv_source_freshness.academic_records_changed_at < EXCLUDED.academic_records_changed_at
                THEN EXCLUDED.academic_records_changed_at
                ELSE cv_source_freshness.academic_records_changed_at
            END
            """, nativeQuery = true)
    void upsertAcademicRecordsChangedAt(@Param("studentId") UUID studentId, @Param("changedAt") OffsetDateTime changedAt);
}
