package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.util.Map;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicLedgerCommitResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerErrors;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Coordinates the durable commit claim, atomic promotion transaction, and retry-safe failure recovery. */
@Service
public class AcademicLedgerCommitService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AcademicLedgerCommitService.class);
    private static final String AUDIT_RESOURCE = "ACADEMIC_LEDGER_UPLOAD";

    private final AcademicLedgerCommitClaimService claimService;
    private final AcademicLedgerCommitTransactionService transactionService;
    private final CurrentActorProvider currentActorProvider;
    private final AuditEventPublisher auditEventPublisher;

    public AcademicLedgerCommitService(
            AcademicLedgerCommitClaimService claimService,
            AcademicLedgerCommitTransactionService transactionService,
            CurrentActorProvider currentActorProvider,
            AuditEventPublisher auditEventPublisher) {
        this.claimService = claimService;
        this.transactionService = transactionService;
        this.currentActorProvider = currentActorProvider;
        this.auditEventPublisher = auditEventPublisher;
    }

    public AcademicLedgerCommitResponse commit(UUID uploadId) {
        CurrentActor actor = currentAdmin();
        claimService.claim(uploadId);
        try {
            return transactionService.promote(uploadId, actor.userId());
        } catch (RuntimeException exception) {
            LOGGER.error("Atomic Academic Ledger commit failed for upload {}.", uploadId, exception);
            try {
                claimService.releaseAfterFailure(uploadId);
            } catch (RuntimeException recoveryFailure) {
                LOGGER.error("Academic Ledger commit recovery failed for upload {}.", uploadId, recoveryFailure);
            }
            try {
                auditEventPublisher.recordRequired(
                        actor.userId(), "ADMIN", "LEDGER_COMMIT_FAILED", AuditEventCategory.ACADEMIC_LEDGER,
                        AUDIT_RESOURCE, uploadId.toString(),
                        Map.of("uploadId", uploadId.toString(), "transactionRolledBack", true));
            } catch (RuntimeException auditFailure) {
                LOGGER.error("Academic Ledger commit failure audit could not be persisted for upload {}.", uploadId, auditFailure);
            }
            throw AcademicLedgerErrors.commitFailed();
        }
    }

    private CurrentActor currentAdmin() {
        CurrentActor actor = currentActorProvider.currentActor().orElseThrow(AcademicLedgerErrors::unauthorized);
        if (!actor.hasRole(RoleName.ADMIN)) {
            throw AcademicLedgerErrors.forbidden();
        }
        return actor;
    }
}
