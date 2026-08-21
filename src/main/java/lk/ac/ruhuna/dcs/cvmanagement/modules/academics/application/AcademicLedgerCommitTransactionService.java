package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerCommitResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerRowValidationStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerStagingRowEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerUploadEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.OfficialStudentGradeEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.SubjectEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerStagingRowRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerUploadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.OfficialStudentGradeRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.SubjectRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.cv.CvSourceArea;
import lk.ac.ruhuna.dcs.cvmanagement.shared.cv.CvSourceFreshnessUpdatePort;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Performs official-grade promotion, GPA recalculation, audit success, and COMMITTED transition atomically. */
@Service
class AcademicLedgerCommitTransactionService {

    private static final String AUDIT_RESOURCE = "ACADEMIC_LEDGER_UPLOAD";

    private final AcademicLedgerUploadRepository uploadRepository;
    private final AcademicLedgerStagingRowRepository stagingRepository;
    private final OfficialStudentGradeRepository officialGradeRepository;
    private final SubjectRepository subjectRepository;
    private final GpaCalculationService gpaCalculationService;
    private final AuditEventPublisher auditEventPublisher;
    private final CvSourceFreshnessUpdatePort cvFreshnessUpdatePort;
    private final Clock clock;

    AcademicLedgerCommitTransactionService(
            AcademicLedgerUploadRepository uploadRepository,
            AcademicLedgerStagingRowRepository stagingRepository,
            OfficialStudentGradeRepository officialGradeRepository,
            SubjectRepository subjectRepository,
            GpaCalculationService gpaCalculationService,
            AuditEventPublisher auditEventPublisher,
            CvSourceFreshnessUpdatePort cvFreshnessUpdatePort,
            Clock clock) {
        this.uploadRepository = uploadRepository;
        this.stagingRepository = stagingRepository;
        this.officialGradeRepository = officialGradeRepository;
        this.subjectRepository = subjectRepository;
        this.gpaCalculationService = gpaCalculationService;
        this.auditEventPublisher = auditEventPublisher;
        this.cvFreshnessUpdatePort = cvFreshnessUpdatePort;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    AcademicLedgerCommitResponse promote(UUID uploadId, UUID committingAdminId) {
        AcademicLedgerUploadEntity upload = uploadRepository.findByIdForUpdate(uploadId).orElseThrow();
        requireCommitState(upload);

        List<AcademicLedgerStagingRowEntity> stagedRows =
                stagingRepository.findAllByAcademicLedgerUploadIdOrderByRowNumberAsc(uploadId);
        requireCommitReadyRows(upload, stagedRows);

        Map<String, List<SubjectEntity>> subjectsByCode = loadSubjects(stagedRows);
        OffsetDateTime committedAt = now();
        Set<UUID> affectedStudents = new LinkedHashSet<>();
        List<OfficialStudentGradeEntity> officialGrades = new ArrayList<>(stagedRows.size());

        for (AcademicLedgerStagingRowEntity row : stagedRows) {
            SubjectEntity subject = resolveCanonicalSubject(row, subjectsByCode);
            OfficialStudentGradeEntity official = new OfficialStudentGradeEntity();
            official.setStudentId(row.getStudentId());
            official.setSubjectId(subject.getId());
            official.setAcademicLedgerUploadId(uploadId);
            official.setSemester(row.getSemester());
            official.setAcademicYear(row.getAcademicYear());
            official.setAttemptNumber(row.getAttemptNumber());
            // The staging value was checked against the canonical subject during validation. Re-check here
            // before using it as the immutable official snapshot.
            if (row.getCredits().compareTo(subject.getCredits()) != 0) {
                throw new IllegalStateException("Canonical subject credits changed after ledger validation.");
            }
            official.setCredits(subject.getCredits());
            official.setGradePoint(row.getGradePoint());
            official.setLetterGrade(row.getLetterGrade().toUpperCase(Locale.ROOT));
            official.setResultStatus(row.getResultStatus());
            official.setCommittedAt(committedAt);
            officialGrades.add(official);
            affectedStudents.add(row.getStudentId());
        }

        officialGradeRepository.saveAll(officialGrades);
        officialGradeRepository.flush();

        int recalculatedGpaCount =
                gpaCalculationService.recalculate(affectedStudents, uploadId, committedAt);

        affectedStudents.stream()
                .sorted()
                .forEach(studentId -> cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.ACADEMIC_RECORDS));

        upload.setUploadStatus(AcademicLedgerUploadStatus.COMMITTED);
        upload.setCommittedAt(committedAt);
        upload.setFailureSummary(null);

        auditEventPublisher.recordRequired(
                committingAdminId,
                "ADMIN",
                "LEDGER_COMMIT_SUCCEEDED",
                AuditEventCategory.ACADEMIC_LEDGER,
                AUDIT_RESOURCE,
                uploadId.toString(),
                Map.of(
                        "uploadId", uploadId.toString(),
                        "committedRecords", officialGrades.size(),
                        "affectedStudents", affectedStudents.size(),
                        "recalculatedGpaCount", recalculatedGpaCount));
        uploadRepository.saveAndFlush(upload);

        return new AcademicLedgerCommitResponse(
                uploadId,
                AcademicLedgerUploadStatus.COMMITTED.name(),
                officialGrades.size(),
                affectedStudents.size(),
                recalculatedGpaCount,
                committedAt);
    }

    private void requireCommitState(AcademicLedgerUploadEntity upload) {
        if (upload.getUploadStatus() != AcademicLedgerUploadStatus.COMMITTING
                || upload.getValidationStatus() != AcademicLedgerValidationStatus.PASSED
                || upload.getInvalidRows() != 0) {
            throw new IllegalStateException("Academic Ledger commit state changed after the commit claim.");
        }
    }

    private void requireCommitReadyRows(
            AcademicLedgerUploadEntity upload, List<AcademicLedgerStagingRowEntity> rows) {
        if (rows.size() != upload.getTotalRows() || upload.getValidRows() != upload.getTotalRows()) {
            throw new IllegalStateException("Academic Ledger staged-row counts changed after validation.");
        }
        for (AcademicLedgerStagingRowEntity row : rows) {
            if ((row.getValidationStatus() != AcademicLedgerRowValidationStatus.VALID
                            && row.getValidationStatus() != AcademicLedgerRowValidationStatus.WARNING)
                    || row.getStudentId() == null
                    || row.getGradePoint() == null) {
                throw new IllegalStateException("Academic Ledger contains an unresolved row after validation.");
            }
        }
    }

    private Map<String, List<SubjectEntity>> loadSubjects(Collection<AcademicLedgerStagingRowEntity> rows) {
        Set<String> courseCodes = rows.stream()
                .map(AcademicLedgerStagingRowEntity::getCourseCode)
                .collect(Collectors.toUnmodifiableSet());
        Map<String, List<SubjectEntity>> grouped = new HashMap<>();
        for (SubjectEntity subject : subjectRepository.findByCourseCodeInAndActiveTrue(courseCodes)) {
            grouped.computeIfAbsent(subject.getCourseCode(), ignored -> new ArrayList<>()).add(subject);
        }
        return grouped;
    }

    private SubjectEntity resolveCanonicalSubject(
            AcademicLedgerStagingRowEntity row, Map<String, List<SubjectEntity>> subjectsByCode) {
        Short cohortYear = AcademicLedgerValidationRules.cohortYear(row.getStudentIndexNumber());
        if (cohortYear == null) {
            throw new IllegalStateException("Student cohort cannot be resolved during Academic Ledger commit.");
        }
        List<SubjectEntity> applicable = subjectsByCode.getOrDefault(row.getCourseCode(), List.of()).stream()
                .filter(subject -> subject.getCohortStartYear() <= cohortYear)
                .filter(subject -> subject.getCohortEndYear() == null || subject.getCohortEndYear() >= cohortYear)
                .sorted(Comparator.comparingInt(SubjectEntity::getCohortStartYear).reversed())
                .toList();
        if (applicable.size() != 1) {
            throw new IllegalStateException("Canonical Computer Science subject resolution changed after validation.");
        }
        return applicable.get(0);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
