package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
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

/** Owns short durable lifecycle transactions used by the background worker. */
@Service
class AcademicLedgerProcessingStateService {

    private static final String AUDIT_RESOURCE = "ACADEMIC_LEDGER_UPLOAD";

    private final AcademicLedgerUploadRepository uploadRepository;
    private final AcademicLedgerStagingRowRepository stagingRepository;
    private final AcademicLedgerValidationErrorRepository validationErrorRepository;
    private final AuditEventPublisher auditEventPublisher;
    private final Clock clock;

    AcademicLedgerProcessingStateService(
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
    Optional<ProcessingJob> claimNextReceived() {
        return uploadRepository.findNextReceivedForUpdateSkipLocked().map(upload -> {
            upload.setUploadStatus(AcademicLedgerUploadStatus.PROCESSING);
            upload.setValidationStatus(AcademicLedgerValidationStatus.NOT_STARTED);
            upload.setProcessingStartedAt(now());
            upload.setValidationCompletedAt(null);
            upload.setFailureSummary(null);
            upload.setTotalRows(0);
            upload.setValidRows(0);
            upload.setInvalidRows(0);
            uploadRepository.saveAndFlush(upload);
            return new ProcessingJob(upload.getId(), upload.getSourceFileAssetId());
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void clearStaging(UUID uploadId) {
        validationErrorRepository.deleteAllByUploadId(uploadId);
        stagingRepository.deleteAllByUploadId(uploadId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void heartbeatProcessing(UUID uploadId) {
        uploadRepository.touchProcessingHeartbeat(uploadId, now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void markStaged(UUID uploadId, int totalRows) {
        AcademicLedgerUploadEntity upload = uploadRepository.findByIdForUpdate(uploadId).orElseThrow();
        requireStatus(upload, AcademicLedgerUploadStatus.PROCESSING);
        upload.setTotalRows(totalRows);
        upload.setValidRows(0);
        upload.setInvalidRows(0);
        upload.setUploadStatus(AcademicLedgerUploadStatus.STAGED);
        upload.setValidationStatus(AcademicLedgerValidationStatus.NOT_STARTED);
        uploadRepository.saveAndFlush(upload);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void markProcessingFailed(UUID uploadId, String failureSummary) {
        uploadRepository.findByIdForUpdate(uploadId).ifPresent(upload -> {
            if (upload.getUploadStatus() != AcademicLedgerUploadStatus.PROCESSING) {
                return;
            }
            upload.setUploadStatus(AcademicLedgerUploadStatus.PROCESSING_FAILED);
            upload.setValidationStatus(AcademicLedgerValidationStatus.NOT_STARTED);
            upload.setFailureSummary(safeSummary(failureSummary));
            uploadRepository.saveAndFlush(upload);
            auditEventPublisher.recordRequired(
                    upload.getUploadedByAccountId(),
                    "ADMIN",
                    "LEDGER_PROCESSING_FAILED",
                    AuditEventCategory.ACADEMIC_LEDGER,
                    AUDIT_RESOURCE,
                    upload.getId().toString(),
                    java.util.Map.of("uploadId", upload.getId().toString(), "reason", safeSummary(failureSummary)));
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    boolean recoverOneStaleProcessing(OffsetDateTime staleBefore) {
        Optional<AcademicLedgerUploadEntity> candidate =
                uploadRepository.findOneStaleProcessingForUpdateSkipLocked(staleBefore);
        if (candidate.isEmpty()) {
            return false;
        }
        AcademicLedgerUploadEntity upload = candidate.get();
        validationErrorRepository.deleteAllByUploadId(upload.getId());
        stagingRepository.deleteAllByUploadId(upload.getId());
        upload.setUploadStatus(AcademicLedgerUploadStatus.RECEIVED);
        upload.setValidationStatus(AcademicLedgerValidationStatus.NOT_STARTED);
        upload.setProcessingStartedAt(null);
        upload.setValidationCompletedAt(null);
        upload.setFailureSummary(null);
        upload.setTotalRows(0);
        upload.setValidRows(0);
        upload.setInvalidRows(0);
        uploadRepository.saveAndFlush(upload);
        return true;
    }

    private void requireStatus(AcademicLedgerUploadEntity upload, AcademicLedgerUploadStatus expected) {
        if (upload.getUploadStatus() != expected) {
            throw new IllegalStateException(
                    "Academic Ledger upload " + upload.getId() + " is not in expected state " + expected + '.');
        }
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private String safeSummary(String value) {
        String normalized = value == null || value.isBlank() ? "Academic Ledger processing failed." : value.trim();
        return normalized.length() <= 500 ? normalized : normalized.substring(0, 500);
    }

    record ProcessingJob(UUID uploadId, UUID fileAssetId) {
    }
}
