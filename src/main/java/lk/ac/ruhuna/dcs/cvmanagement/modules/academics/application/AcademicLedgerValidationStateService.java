package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerRowValidationStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerUploadEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerStagingRowRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerUploadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerValidationErrorRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Owns the short, durable transactions used by the Academic Ledger validation workflow. */
@Service
class AcademicLedgerValidationStateService {

    private static final String AUDIT_RESOURCE = "ACADEMIC_LEDGER_UPLOAD";

    private final AcademicLedgerUploadRepository uploadRepository;
    private final AcademicLedgerStagingRowRepository stagingRepository;
    private final AcademicLedgerValidationErrorRepository validationErrorRepository;
    private final AuditEventPublisher auditEventPublisher;
    private final Clock clock;

    AcademicLedgerValidationStateService(
            AcademicLedgerUploadRepository uploadRepository,
            AcademicLedgerStagingRowRepository stagingRepository,
            AcademicLedgerValidationErrorRepository validationErrorRepository,
            AuditEventPublisher auditEventPublisher,
            Clock clock) {
        this.uploadRepository = uploadRepository;
        this.stagingRepository = stagingRepository;
        this.validationErrorRepository = validationErrorRepository;
        this.auditEventPublisher = auditEventPublisher;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    boolean startValidation(UUID uploadId) {
        AcademicLedgerUploadEntity upload = uploadRepository.findByIdForUpdate(uploadId).orElseThrow();
        if (upload.getUploadStatus() != AcademicLedgerUploadStatus.STAGED
                || upload.getValidationStatus() != AcademicLedgerValidationStatus.NOT_STARTED) {
            return false;
        }
        validationErrorRepository.deleteAllByUploadId(uploadId);
        stagingRepository.resetValidationArtifacts(uploadId);
        upload.setValidationStatus(AcademicLedgerValidationStatus.IN_PROGRESS);
        upload.setValidRows(0);
        upload.setInvalidRows(0);
        upload.setValidationCompletedAt(null);
        uploadRepository.saveAndFlush(upload);
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void heartbeatValidation(UUID uploadId) {
        uploadRepository.touchValidationHeartbeat(uploadId, now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void completeValidation(UUID uploadId) {
        AcademicLedgerUploadEntity upload = uploadRepository.findByIdForUpdate(uploadId).orElseThrow();
        if (upload.getUploadStatus() != AcademicLedgerUploadStatus.STAGED
                || upload.getValidationStatus() != AcademicLedgerValidationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Academic Ledger validation state changed before completion.");
        }
        int invalidRows = Math.toIntExact(stagingRepository.countByAcademicLedgerUploadIdAndValidationStatus(
                uploadId, AcademicLedgerRowValidationStatus.INVALID));
        int warningRows = Math.toIntExact(stagingRepository.countByAcademicLedgerUploadIdAndValidationStatus(
                uploadId, AcademicLedgerRowValidationStatus.WARNING));
        int validOnly = Math.toIntExact(stagingRepository.countByAcademicLedgerUploadIdAndValidationStatus(
                uploadId, AcademicLedgerRowValidationStatus.VALID));
        int validRows = validOnly + warningRows;
        if (validRows + invalidRows != upload.getTotalRows()) {
            throw new IllegalStateException("Academic Ledger row counts are inconsistent after validation.");
        }

        upload.setValidRows(validRows);
        upload.setInvalidRows(invalidRows);
        upload.setValidationCompletedAt(now());
        if (invalidRows == 0) {
            upload.setValidationStatus(AcademicLedgerValidationStatus.PASSED);
            upload.setUploadStatus(AcademicLedgerUploadStatus.READY_TO_COMMIT);
            auditEventPublisher.recordRequired(
                    upload.getUploadedByAccountId(), "ADMIN", "LEDGER_VALIDATION_PASSED",
                    AuditEventCategory.ACADEMIC_LEDGER, AUDIT_RESOURCE, uploadId.toString(),
                    Map.of("uploadId", uploadId.toString(), "totalRows", upload.getTotalRows()));
        } else {
            upload.setValidationStatus(AcademicLedgerValidationStatus.FAILED);
            upload.setUploadStatus(AcademicLedgerUploadStatus.VALIDATION_FAILED);
            auditEventPublisher.recordRequired(
                    upload.getUploadedByAccountId(), "ADMIN", "LEDGER_VALIDATION_FAILED",
                    AuditEventCategory.ACADEMIC_LEDGER, AUDIT_RESOURCE, uploadId.toString(),
                    Map.of(
                            "uploadId", uploadId.toString(),
                            "totalRows", upload.getTotalRows(),
                            "invalidRows", invalidRows));
        }
        uploadRepository.saveAndFlush(upload);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    boolean recoverOneStaleValidation(OffsetDateTime staleBefore) {
        Optional<AcademicLedgerUploadEntity> candidate =
                uploadRepository.findOneStaleValidationForUpdateSkipLocked(staleBefore);
        if (candidate.isEmpty()) {
            return false;
        }
        AcademicLedgerUploadEntity upload = candidate.get();
        validationErrorRepository.deleteAllByUploadId(upload.getId());
        stagingRepository.resetValidationArtifacts(upload.getId());
        upload.setValidationStatus(AcademicLedgerValidationStatus.NOT_STARTED);
        upload.setValidationCompletedAt(null);
        upload.setValidRows(0);
        upload.setInvalidRows(0);
        uploadRepository.saveAndFlush(upload);
        return true;
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
