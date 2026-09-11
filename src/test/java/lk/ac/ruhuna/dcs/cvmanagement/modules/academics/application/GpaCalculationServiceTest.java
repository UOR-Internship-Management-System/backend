package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicGpaProperties;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.OfficialStudentGradeEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.OfficialStudentGradeRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.StudentAcademicSummaryRepository;
import org.junit.jupiter.api.Test;

class GpaCalculationServiceTest {

    private final GpaCalculationService service = new GpaCalculationService(
            mock(OfficialStudentGradeRepository.class),
            mock(StudentAcademicSummaryRepository.class),
            new AcademicGpaProperties(RoundingMode.HALF_UP));

    @Test
    void weightedGpaUsesCanonicalCredits() {
        UUID student = UUID.randomUUID();
        var result = service.calculate(List.of(
                grade(student, UUID.randomUUID(), "4.00", "3.0", 1),
                grade(student, UUID.randomUUID(), "3.00", "2.0", 1)));

        assertThat(result.gpa()).isEqualByComparingTo("3.60");
        assertThat(result.totalCredits()).isEqualByComparingTo("5.0");
    }

    @Test
    void repeatedSubjectKeepsBestResultAndCountsCreditsOnce() {
        UUID student = UUID.randomUUID();
        UUID subject = UUID.randomUUID();
        var result = service.calculate(List.of(
                grade(student, subject, "1.30", "3.0", 1),
                grade(student, subject, "2.00", "3.0", 2),
                grade(student, subject, "1.70", "3.0", 3)));

        assertThat(result.gpa()).isEqualByComparingTo("2.00");
        assertThat(result.totalCredits()).isEqualByComparingTo("3.0");
    }

    @Test
    void failedEffectiveResultStillContributesItsCredits() {
        UUID student = UUID.randomUUID();
        var result = service.calculate(List.of(
                grade(student, UUID.randomUUID(), "0.00", "2.0", 1),
                grade(student, UUID.randomUUID(), "4.00", "2.0", 1)));

        assertThat(result.gpa()).isEqualByComparingTo("2.00");
        assertThat(result.totalCredits()).isEqualByComparingTo("4.0");
    }

    @Test
    void finalRoundingIsCentralizedAndDoesNotRoundIntermediateProducts() {
        UUID student = UUID.randomUUID();
        var result = service.calculate(List.of(
                grade(student, UUID.randomUUID(), "3.70", "1.0", 1),
                grade(student, UUID.randomUUID(), "3.30", "2.0", 1)));

        assertThat(result.gpa()).isEqualByComparingTo("3.43");
    }

    private OfficialStudentGradeEntity grade(
            UUID studentId, UUID subjectId, String gradePoint, String credits, int attemptNumber) {
        OfficialStudentGradeEntity grade = new OfficialStudentGradeEntity();
        grade.setStudentId(studentId);
        grade.setSubjectId(subjectId);
        grade.setGradePoint(new BigDecimal(gradePoint));
        grade.setCredits(new BigDecimal(credits));
        grade.setAttemptNumber((short) attemptNumber);
        grade.setCommittedAt(OffsetDateTime.parse("2026-08-13T00:00:00Z").plusDays(attemptNumber));
        return grade;
    }
}
