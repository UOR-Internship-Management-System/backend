package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerErrors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerValidationStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerUploadEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerUploadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Owns the short durable transaction that claims an upload for commit. */
@Service
class AcademicLedgerCommitClaimService {

    private final AcademicLedgerUploadRepository uploadRepository;

    AcademicLedgerCommitClaimService(AcademicLedgerUploadRepository uploadRepository) {
        this.uploadRepository = uploadRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void claim(UUID uploadId) {
        AcademicLedgerUploadEntity upload = uploadRepository.findByIdForUpdate(uploadId)
                .orElseThrow(AcademicLedgerErrors::uploadNotFound);

        if (upload.getUploadStatus() == AcademicLedgerUploadStatus.COMMITTED) {
            throw AcademicLedgerErrors.alreadyCommitted();
        }
        if (upload.getUploadStatus() == AcademicLedgerUploadStatus.COMMITTING) {
            throw AcademicLedgerErrors.commitConflict();
        }
        if (upload.getUploadStatus() == AcademicLedgerUploadStatus.VALIDATION_FAILED
                || upload.getValidationStatus() == AcademicLedgerValidationStatus.FAILED
                || upload.getInvalidRows() > 0) {
            throw AcademicLedgerErrors.validationFailed(upload.getInvalidRows());
        }
        if (upload.getUploadStatus() != AcademicLedgerUploadStatus.READY_TO_COMMIT
                || upload.getValidationStatus() != AcademicLedgerValidationStatus.PASSED
                || upload.getValidRows() != upload.getTotalRows()) {
            throw AcademicLedgerErrors.notReadyToCommit(upload.getUploadStatus().name());
        }

        upload.setUploadStatus(AcademicLedgerUploadStatus.COMMITTING);
        upload.setFailureSummary(null);
        // @UpdateTimestamp provides the durable age marker used by stale-COMMITTING recovery.
        uploadRepository.saveAndFlush(upload);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void releaseAfterFailure(UUID uploadId) {
        uploadRepository.findByIdForUpdate(uploadId).ifPresent(upload -> {
            if (upload.getUploadStatus() != AcademicLedgerUploadStatus.COMMITTING) {
                return;
            }
            upload.setUploadStatus(AcademicLedgerUploadStatus.READY_TO_COMMIT);
            upload.setFailureSummary(null);
            uploadRepository.saveAndFlush(upload);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    boolean recoverOneStaleCommit(OffsetDateTime staleBefore) {
        Optional<AcademicLedgerUploadEntity> candidate =
                uploadRepository.findOneStaleCommittingForUpdateSkipLocked(staleBefore);
        if (candidate.isEmpty()) {
            return false;
        }
        AcademicLedgerUploadEntity upload = candidate.get();
        upload.setUploadStatus(AcademicLedgerUploadStatus.READY_TO_COMMIT);
        upload.setFailureSummary(null);
        uploadRepository.saveAndFlush(upload);
        return true;
    }

}
