package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository;

import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.OfficialStudentGradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficialStudentGradeRepository extends JpaRepository<OfficialStudentGradeEntity, UUID> {

    boolean existsByStudentIdAndSubjectIdAndSemesterAndAcademicYearAndAttemptNumber(
            UUID studentId, UUID subjectId, String semester, String academicYear, short attemptNumber);

    List<OfficialStudentGradeEntity> findByStudentId(UUID studentId);
}
