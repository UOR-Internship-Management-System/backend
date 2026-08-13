package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerRowValidationStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationSeverity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.AcademicStudentReferenceQuery;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.AcademicStudentReferenceQuery.StudentReference;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerStagingRowEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerValidationErrorEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.GradeScaleEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.OfficialStudentGradeEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.SubjectEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerStagingRowRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerValidationErrorRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.GradeScaleRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.OfficialStudentGradeRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.SubjectRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Validates one bounded slice and persists resolved references plus diagnostics atomically. */
@Service
class AcademicLedgerValidationBatchService {

    private final AcademicLedgerStagingRowRepository stagingRepository;
    private final AcademicLedgerValidationErrorRepository validationErrorRepository;
    private final AcademicStudentReferenceQuery studentReferenceQuery;
    private final SubjectRepository subjectRepository;
    private final GradeScaleRepository gradeScaleRepository;
    private final OfficialStudentGradeRepository officialStudentGradeRepository;

    AcademicLedgerValidationBatchService(
            AcademicLedgerStagingRowRepository stagingRepository,
            AcademicLedgerValidationErrorRepository validationErrorRepository,
            AcademicStudentReferenceQuery studentReferenceQuery,
            SubjectRepository subjectRepository,
            GradeScaleRepository gradeScaleRepository,
            OfficialStudentGradeRepository officialStudentGradeRepository) {
        this.stagingRepository = stagingRepository;
        this.validationErrorRepository = validationErrorRepository;
        this.studentReferenceQuery = studentReferenceQuery;
        this.subjectRepository = subjectRepository;
        this.gradeScaleRepository = gradeScaleRepository;
        this.officialStudentGradeRepository = officialStudentGradeRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ValidationBatchResult validateNext(
            UUID uploadId,
            int afterRowNumber,
            int batchSize,
            Map<Integer, Integer> duplicateRows) {
        List<AcademicLedgerStagingRowEntity> rows = stagingRepository.findValidationBatch(
                uploadId, afterRowNumber, PageRequest.of(0, batchSize));
        if (rows.isEmpty()) {
            return ValidationBatchResult.empty(afterRowNumber);
        }

        Set<String> studentIndexes = rows.stream()
                .map(AcademicLedgerStagingRowEntity::getStudentIndexNumber)
                .collect(Collectors.toUnmodifiableSet());
        Map<String, StudentReference> students = studentReferenceQuery.findByIndexNumbers(studentIndexes);

        Set<String> courseCodes = rows.stream()
                .map(AcademicLedgerStagingRowEntity::getCourseCode)
                .collect(Collectors.toUnmodifiableSet());
        Map<String, List<SubjectEntity>> subjectsByCode = subjectRepository.findByCourseCodeInAndActiveTrue(courseCodes)
                .stream()
                .collect(Collectors.groupingBy(SubjectEntity::getCourseCode));

        Map<String, GradeScaleEntity> grades = gradeScaleRepository.findByActiveTrue().stream()
                .collect(Collectors.toUnmodifiableMap(
                        grade -> grade.getGradeCode().toUpperCase(Locale.ROOT), Function.identity()));

        Set<UUID> studentIds = students.values().stream()
                .filter(StudentReference::active)
                .map(StudentReference::studentId)
                .collect(Collectors.toUnmodifiableSet());
        Set<OfficialKey> officialKeys = studentIds.isEmpty()
                ? Set.of()
                : officialStudentGradeRepository.findByStudentIdIn(studentIds).stream()
                        .map(OfficialKey::from)
                        .collect(Collectors.toUnmodifiableSet());

        List<AcademicLedgerValidationErrorEntity> allErrors = new ArrayList<>();
        int validRows = 0;
        int invalidRows = 0;
        for (AcademicLedgerStagingRowEntity row : rows) {
            List<AcademicLedgerValidationErrorEntity> errors = validateRow(
                    row,
                    students,
                    subjectsByCode,
                    grades,
                    officialKeys,
                    duplicateRows.get(row.getRowNumber()));
            if (errors.isEmpty()) {
                row.setValidationStatus(AcademicLedgerRowValidationStatus.VALID);
                validRows++;
            } else {
                row.setValidationStatus(AcademicLedgerRowValidationStatus.INVALID);
                invalidRows++;
                allErrors.addAll(errors);
            }
        }
        stagingRepository.saveAll(rows);
        if (!allErrors.isEmpty()) {
            validationErrorRepository.saveAll(allErrors);
        }
        stagingRepository.flush();
        validationErrorRepository.flush();
        int lastRowNumber = rows.get(rows.size() - 1).getRowNumber();
        return new ValidationBatchResult(lastRowNumber, rows.size(), validRows, invalidRows);
    }

    private List<AcademicLedgerValidationErrorEntity> validateRow(
            AcademicLedgerStagingRowEntity row,
            Map<String, StudentReference> students,
            Map<String, List<SubjectEntity>> subjectsByCode,
            Map<String, GradeScaleEntity> grades,
            Set<OfficialKey> officialKeys,
            Integer relatedDuplicateRow) {
        List<AcademicLedgerValidationErrorEntity> errors = new ArrayList<>();

        StudentReference student = students.get(row.getStudentIndexNumber());
        if (student == null) {
            errors.add(error(row, "student_index_number", "STUDENT_NOT_FOUND",
                    "No eligible Student matches the supplied index number.", row.getStudentIndexNumber(), null));
        } else if (!student.active()) {
            errors.add(error(row, "student_index_number", "STUDENT_INACTIVE",
                    "The supplied Student is not active in the authoritative eligibility dataset.",
                    row.getStudentIndexNumber(), null));
        } else {
            row.setStudentId(student.studentId());
        }

        SubjectEntity subject = resolveSubject(row, subjectsByCode, errors);
        if (subject != null) {
            row.setCourseTitle(subject.getCourseTitle());
            if (!AcademicLedgerValidationRules.sameCredits(row.getCredits(), subject.getCredits())) {
                errors.add(error(row, "credits", "CREDITS_MISMATCH",
                        "Uploaded credits do not match the canonical Computer Science subject credits.",
                        row.getCredits().toPlainString(), null));
            }
            if (!AcademicLedgerValidationRules.sameSemester(row.getSemester(), subject.getSemester())) {
                errors.add(error(row, "semester", "SEMESTER_INVALID",
                        "The supplied semester does not match the canonical Computer Science subject catalogue.",
                        row.getSemester(), null));
            }
        }

        GradeScaleEntity grade = grades.get(row.getLetterGrade().toUpperCase(Locale.ROOT));
        if (grade == null) {
            errors.add(error(row, "letter_grade", "GRADE_NOT_FOUND",
                    "The supplied letter grade is not present in the active grade scale.", row.getLetterGrade(), null));
        } else {
            row.setGradePoint(grade.getGradePoint());
            validateResultStatus(row, grade, errors);
        }

        if (relatedDuplicateRow != null) {
            errors.add(error(row, null, "DUPLICATE_ROW_IN_UPLOAD",
                    "This row duplicates an earlier logical academic attempt in the same upload.",
                    null, relatedDuplicateRow));
        }

        if (student != null && student.active() && subject != null) {
            OfficialKey key = new OfficialKey(
                    student.studentId(),
                    subject.getId(),
                    row.getSemester(),
                    row.getAcademicYear(),
                    row.getAttemptNumber());
            if (officialKeys.contains(key)) {
                errors.add(error(row, null, "OFFICIAL_RECORD_CONFLICT",
                        "An official academic record already exists for this Student, subject, period, and attempt.",
                        null, null));
            }
        }

        return errors;
    }

    private SubjectEntity resolveSubject(
            AcademicLedgerStagingRowEntity row,
            Map<String, List<SubjectEntity>> subjectsByCode,
            List<AcademicLedgerValidationErrorEntity> errors) {
        List<SubjectEntity> candidates = subjectsByCode.getOrDefault(row.getCourseCode(), List.of());
        Short cohortYear = AcademicLedgerValidationRules.cohortYear(row.getStudentIndexNumber());
        if (cohortYear == null) {
            errors.add(error(row, "course_code", "COURSE_CATALOG_MISMATCH",
                    "A curriculum version cannot be resolved from the supplied Student index number.",
                    row.getCourseCode(), null));
            return null;
        }
        List<SubjectEntity> applicable = candidates.stream()
                .filter(subject -> subject.getCohortStartYear() <= cohortYear)
                .filter(subject -> subject.getCohortEndYear() == null || subject.getCohortEndYear() >= cohortYear)
                .toList();
        if (applicable.isEmpty()) {
            errors.add(error(row, "course_code", "COURSE_NOT_FOUND",
                    "The supplied course code is not present in the canonical Computer Science catalogue for the Student cohort.",
                    row.getCourseCode(), null));
            return null;
        }
        if (applicable.size() > 1) {
            errors.add(error(row, "course_code", "COURSE_CATALOG_MISMATCH",
                    "Multiple active curriculum definitions match the supplied Student cohort and course code.",
                    row.getCourseCode(), null));
            return null;
        }
        return applicable.get(0);
    }

    private void validateResultStatus(
            AcademicLedgerStagingRowEntity row,
            GradeScaleEntity grade,
            List<AcademicLedgerValidationErrorEntity> errors) {
        // OpenAPI v1.6 does not freeze a closed result-status enum. Only PASSED/FAILED semantics
        // are source-supported, so other non-blank official statuses are preserved without invention.
        if ("PASSED".equals(row.getResultStatus()) && !grade.isPassing()) {
            errors.add(error(row, "result_status", "RESULT_STATUS_GRADE_MISMATCH",
                    "The supplied result status conflicts with the authoritative passing grade rule.",
                    row.getResultStatus(), null));
        } else if ("FAILED".equals(row.getResultStatus()) && grade.isPassing()) {
            errors.add(error(row, "result_status", "RESULT_STATUS_GRADE_MISMATCH",
                    "The supplied result status conflicts with the authoritative passing grade rule.",
                    row.getResultStatus(), null));
        }
    }

    private AcademicLedgerValidationErrorEntity error(
            AcademicLedgerStagingRowEntity row,
            String field,
            String code,
            String message,
            String rejectedValue,
            Integer relatedRowNumber) {
        AcademicLedgerValidationErrorEntity error = new AcademicLedgerValidationErrorEntity();
        error.setStagingRowId(row.getId());
        error.setFieldName(field);
        error.setErrorCode(code);
        error.setErrorMessage(message);
        error.setSeverity(AcademicLedgerValidationSeverity.ERROR);
        error.setRejectedValue(AcademicLedgerValidationRules.sanitizeRejectedValue(rejectedValue));
        error.setRelatedRowNumber(relatedRowNumber);
        return error;
    }

    record ValidationBatchResult(int lastRowNumber, int processedRows, int validRows, int invalidRows) {
        static ValidationBatchResult empty(int lastRowNumber) {
            return new ValidationBatchResult(lastRowNumber, 0, 0, 0);
        }
    }

    private record OfficialKey(
            UUID studentId,
            UUID subjectId,
            String semester,
            String academicYear,
            short attemptNumber) {
        static OfficialKey from(OfficialStudentGradeEntity entity) {
            return new OfficialKey(
                    entity.getStudentId(),
                    entity.getSubjectId(),
                    AcademicLedgerSourceParser.normalizeSemester(entity.getSemester()),
                    entity.getAcademicYear(),
                    entity.getAttemptNumber());
        }

        OfficialKey {
            semester = AcademicLedgerSourceParser.normalizeSemester(semester);
        }
    }
}
