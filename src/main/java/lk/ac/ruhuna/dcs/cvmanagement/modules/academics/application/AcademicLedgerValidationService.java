package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicLedgerProcessingProperties;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerUploadEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerStagingRowRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerUploadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerValidationErrorRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Coordinates durable validation lifecycle transitions and bounded row validation. */
@Service
public class AcademicLedgerValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcademicLedgerValidationService.class);
    private static final String AUDIT_RESOURCE = "ACADEMIC_LEDGER_UPLOAD";

    private final AcademicLedgerUploadRepository uploadRepository;
    private final AcademicLedgerStagingRowRepository stagingRepository;
    private final AcademicLedgerValidationErrorRepository validationErrorRepository;
    private final AcademicLedgerValidationBatchService batchService;
    private final AcademicLedgerProcessingProperties properties;
    private final AuditEventPublisher auditEventPublisher;
    private final Clock clock;

    public AcademicLedgerValidationService(
            AcademicLedgerUploadRepository uploadRepository,
            AcademicLedgerStagingRowRepository stagingRepository,
            AcademicLedgerValidationErrorRepository validationErrorRepository,
            AcademicLedgerValidationBatchService batchService,
            AcademicLedgerProcessingProperties properties,
            AuditEventPublisher auditEventPublisher,
            Clock clock) {
        this.uploadRepository = uploadRepository;
        this.stagingRepository = stagingRepository;
        this.validationErrorRepository = validationErrorRepository;
        this.batchService = batchService;
        this.properties = properties;
        this.auditEventPublisher = auditEventPublisher;
        this.clock = clock;
    }

    public void validate(UUID uploadId) {
        if (!startValidation(uploadId)) {
            return;
        }
        try {
            Map<Integer, Integer> duplicateRows = stagingRepository.findDuplicateRows(uploadId).stream()
                    .collect(Collectors.toUnmodifiableMap(
                            AcademicLedgerStagingRowRepository.DuplicateRowView::getRowNumber,
                            AcademicLedgerStagingRowRepository.DuplicateRowView::getRelatedRowNumber));
            int lastRowNumber = 1;
            while (true) {
                var result = batchService.validateNext(
                        uploadId, lastRowNumber, properties.stagingBatchSize(), duplicateRows);
                if (result.processedRows() == 0) {
                    break;
                }
                lastRowNumber = result.lastRowNumber();
                heartbeatValidation(uploadId);
            }
            completeValidation(uploadId);
        } catch (RuntimeException exception) {
            LOGGER.error("Academic Ledger validation interrupted for upload {}.", uploadId, exception);
            // Leave STAGED/IN_PROGRESS durable. The stale recovery path resets partial diagnostics
            // and retries deterministically from the persisted staging rows.
        }
    }

    public void resumeOneReadyBatch() {
        uploadRepository.findFirstByUploadStatusAndValidationStatusOrderByCreatedAtAsc(
                        AcademicLedgerUploadStatus.STAGED, AcademicLedgerValidationStatus.NOT_STARTED)
                .map(AcademicLedgerUploadEntity::getId)
                .ifPresent(this::validate);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean recoverOneStaleValidation(OffsetDateTime staleBefore) {
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
                uploadId, lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerRowValidationStatus.INVALID));
        int warningRows = Math.toIntExact(stagingRepository.countByAcademicLedgerUploadIdAndValidationStatus(
                uploadId, lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerRowValidationStatus.WARNING));
        int validOnly = Math.toIntExact(stagingRepository.countByAcademicLedgerUploadIdAndValidationStatus(
                uploadId, lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerRowValidationStatus.VALID));
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

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
