package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicGpaProperties;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.OfficialStudentGradeEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.StudentAcademicSummaryEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.OfficialStudentGradeRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.StudentAcademicSummaryRepository;
import org.springframework.stereotype.Service;

/** Calculates the authoritative Computer Science GPA read model from committed CSC grade history only. */
@Service
public class GpaCalculationService {

    private static final int GPA_SCALE = 2;
    private static final MathContext CALCULATION_CONTEXT = MathContext.DECIMAL128;

    private final OfficialStudentGradeRepository officialGradeRepository;
    private final StudentAcademicSummaryRepository summaryRepository;
    private final AcademicGpaProperties properties;

    public GpaCalculationService(
            OfficialStudentGradeRepository officialGradeRepository,
            StudentAcademicSummaryRepository summaryRepository,
            AcademicGpaProperties properties) {
        this.officialGradeRepository = officialGradeRepository;
        this.summaryRepository = summaryRepository;
        this.properties = properties;
    }

    /**
     * Recalculates summaries for exactly the Students affected by the current commit.
     *
     * <p>Every official attempt is retained historically. For GPA purposes each canonical subject contributes
     * credits once, using the highest official grade-point value recorded for that subject. This preserves the
     * approved "worse repeat does not replace the previous result" rule without incorrectly inferring repeat type
     * from {@code attempt_number} alone.
     */
    public int recalculate(Collection<UUID> studentIds, UUID sourceUploadId, OffsetDateTime calculatedAt) {
        if (studentIds == null || studentIds.isEmpty()) {
            return 0;
        }
        List<OfficialStudentGradeEntity> history = officialGradeRepository.findByStudentIdIn(studentIds);
        Map<UUID, List<OfficialStudentGradeEntity>> byStudent = history.stream()
                .collect(Collectors.groupingBy(OfficialStudentGradeEntity::getStudentId));

        int recalculated = 0;
        for (UUID studentId : studentIds) {
            List<OfficialStudentGradeEntity> grades = byStudent.getOrDefault(studentId, List.of());
            if (grades.isEmpty()) {
                continue;
            }
            GpaResult result = calculate(grades);
            StudentAcademicSummaryEntity summary = summaryRepository.findById(studentId)
                    .orElseGet(StudentAcademicSummaryEntity::new);
            summary.setStudentId(studentId);
            summary.setComputerScienceGpa(result.gpa());
            summary.setTotalCredits(result.totalCredits());
            summary.setCalculatedAt(calculatedAt);
            summary.setSourceUploadId(sourceUploadId);
            summaryRepository.save(summary);
            recalculated++;
        }
        summaryRepository.flush();
        return recalculated;
    }

    GpaResult calculate(List<OfficialStudentGradeEntity> history) {
        if (history == null || history.isEmpty()) {
            throw new IllegalArgumentException("At least one committed academic result is required for GPA calculation.");
        }

        Map<UUID, OfficialStudentGradeEntity> effectiveBySubject = new LinkedHashMap<>();
        Comparator<OfficialStudentGradeEntity> effectiveResultOrder = Comparator
                .comparing(OfficialStudentGradeEntity::getGradePoint)
                .thenComparingInt(OfficialStudentGradeEntity::getAttemptNumber)
                .thenComparing(OfficialStudentGradeEntity::getCommittedAt);

        for (OfficialStudentGradeEntity grade : history) {
            requireValidGrade(grade);
            effectiveBySubject.merge(
                    grade.getSubjectId(), grade,
                    (left, right) -> effectiveResultOrder.compare(left, right) >= 0 ? left : right);
        }

        BigDecimal weightedPoints = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        for (OfficialStudentGradeEntity effective : effectiveBySubject.values()) {
            weightedPoints = weightedPoints.add(
                    effective.getGradePoint().multiply(effective.getCredits(), CALCULATION_CONTEXT),
                    CALCULATION_CONTEXT);
            totalCredits = totalCredits.add(effective.getCredits(), CALCULATION_CONTEXT);
        }
        if (totalCredits.signum() <= 0) {
            throw new IllegalStateException("Computer Science GPA cannot be calculated with zero effective credits.");
        }

        BigDecimal rawGpa = weightedPoints.divide(totalCredits, CALCULATION_CONTEXT);
        BigDecimal persistedGpa = rawGpa.setScale(GPA_SCALE, properties.roundingMode());
        return new GpaResult(persistedGpa, totalCredits.setScale(1));
    }

    private void requireValidGrade(OfficialStudentGradeEntity grade) {
        if (grade.getStudentId() == null
                || grade.getSubjectId() == null
                || grade.getGradePoint() == null
                || grade.getCredits() == null
                || grade.getCredits().signum() <= 0) {
            throw new IllegalStateException("Committed academic history contains an incomplete GPA input.");
        }
    }

    record GpaResult(BigDecimal gpa, BigDecimal totalCredits) {
    }
}
