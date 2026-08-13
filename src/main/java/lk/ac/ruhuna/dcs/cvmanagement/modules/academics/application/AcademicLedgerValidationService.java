package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicLedgerProcessingProperties;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerUploadEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerStagingRowRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerUploadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Coordinates durable validation lifecycle transitions and bounded row validation. */
@Service
public class AcademicLedgerValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcademicLedgerValidationService.class);
    private final AcademicLedgerUploadRepository uploadRepository;
    private final AcademicLedgerStagingRowRepository stagingRepository;
    private final AcademicLedgerValidationBatchService batchService;
    private final AcademicLedgerValidationStateService stateService;
    private final AcademicLedgerProcessingProperties properties;

    public AcademicLedgerValidationService(
            AcademicLedgerUploadRepository uploadRepository,
            AcademicLedgerStagingRowRepository stagingRepository,
            AcademicLedgerValidationBatchService batchService,
            AcademicLedgerValidationStateService stateService,
            AcademicLedgerProcessingProperties properties) {
        this.uploadRepository = uploadRepository;
        this.stagingRepository = stagingRepository;
        this.batchService = batchService;
        this.stateService = stateService;
        this.properties = properties;
    }

    public void validate(UUID uploadId) {
        if (!stateService.startValidation(uploadId)) {
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
                stateService.heartbeatValidation(uploadId);
            }
            stateService.completeValidation(uploadId);
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

    public boolean recoverOneStaleValidation(OffsetDateTime staleBefore) {
        return stateService.recoverOneStaleValidation(staleBefore);
    }
}
