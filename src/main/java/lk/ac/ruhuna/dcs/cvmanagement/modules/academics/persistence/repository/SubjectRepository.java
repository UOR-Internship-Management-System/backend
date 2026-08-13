package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubjectRepository extends JpaRepository<SubjectEntity, UUID> {

    Optional<SubjectEntity> findByCatalogVersionAndCourseCode(String catalogVersion, String courseCode);

    List<SubjectEntity> findByCourseCodeInAndActiveTrue(Collection<String> courseCodes);

    @Query("""
            select s
            from SubjectEntity s
            where s.courseCode = :courseCode
              and s.active = true
              and s.cohortStartYear <= :cohortYear
              and (s.cohortEndYear is null or s.cohortEndYear >= :cohortYear)
            order by s.cohortStartYear desc
            """)
    List<SubjectEntity> findApplicableSubjects(
            @Param("courseCode") String courseCode, @Param("cohortYear") short cohortYear);
}
