package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.request.ShortlistFinalizeRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.ShortlistFinalizeResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.domain.policy.ShortlistStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.entity.ShortlistEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.repository.ShortlistCandidateRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.repository.ShortlistRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventType;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ConflictException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.PreconditionFailedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.UnauthorizedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ValidationException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Atomic, pessimistically locked shortlist finalization. */
@Service
public class ShortlistFinalizationService {

    private final ShortlistRepository shortlistRepository;
    private final ShortlistCandidateRepository candidateRepository;
    private final CurrentActorProvider currentActorProvider;
    private final AuditEventPublisher auditEventPublisher;
    private final Clock clock;

    public ShortlistFinalizationService(
            ShortlistRepository shortlistRepository,
            ShortlistCandidateRepository candidateRepository,
            CurrentActorProvider currentActorProvider,
            AuditEventPublisher auditEventPublisher,
            Clock clock) {
        this.shortlistRepository = shortlistRepository;
        this.candidateRepository = candidateRepository;
        this.currentActorProvider = currentActorProvider;
        this.auditEventPublisher = auditEventPublisher;
        this.clock = clock;
    }

    @Transactional
    public ShortlistFinalizeResponse finalizeShortlist(
            UUID shortlistId,
            ShortlistFinalizeRequest request,
            long expectedVersion) {
        CurrentActor actor = currentAdmin();
        if (request == null || request.acknowledgeGuidanceWarning() == null) {
            throw new ValidationException("acknowledgeGuidanceWarning is required.");
        }
        ShortlistEntity shortlist = shortlistRepository.findByIdForUpdate(shortlistId)
                .orElseThrow(() -> new NotFoundException("Shortlist was not found."));
        long actualVersion = shortlist.getVersion() == null ? 0 : shortlist.getVersion();
        if (actualVersion != expectedVersion) {
            throw new PreconditionFailedException("The shortlist changed after it was loaded.");
        }
        if (shortlist.getStatus() != ShortlistStatus.DRAFT) {
            throw new ConflictException("The shortlist has already been finalized.");
        }
        long selectedCount = candidateRepository.countByShortlistId(shortlistId);
        if (selectedCount == 0) {
            throw new ValidationException("Add at least one Student before finalizing the shortlist.");
        }
        boolean guidanceExceeded = shortlist.getGuidanceValueSnapshot() != null
                && selectedCount > shortlist.getGuidanceValueSnapshot();
        if (guidanceExceeded && !request.acknowledgeGuidanceWarning()) {
            throw new ConflictException(
                    "Acknowledge that the selected count exceeds the shortlist guidance value.");
        }

        OffsetDateTime finalizedAt = OffsetDateTime.now(clock);
        shortlist.setStatus(ShortlistStatus.FINALIZED);
        shortlist.setGuidanceWarningAcknowledged(guidanceExceeded);
        shortlist.setFinalizationNote(normalizeNote(request.finalizationNote()));
        shortlist.setFinalizedByAccountId(actor.userId());
        shortlist.setFinalizedAt(finalizedAt);
        shortlist.setUpdatedAt(finalizedAt);
        shortlistRepository.saveAndFlush(shortlist);

        auditEventPublisher.recordRequired(
                actor.userId(),
                RoleName.ADMIN.name(),
                AuditEventType.SHORTLIST_FINALIZED.name(),
                AuditEventCategory.SHORTLIST_MANAGEMENT,
                "SHORTLIST",
                shortlistId.toString(),
                Map.of(
                        "shortlistId", shortlistId.toString(),
                        "selectedCandidateCount", selectedCount,
                        "guidanceExceeded", guidanceExceeded));
        return new ShortlistFinalizeResponse(
                shortlistId,
                ShortlistStatus.FINALIZED,
                selectedCount,
                shortlist.getGuidanceValueSnapshot(),
                guidanceExceeded,
                !guidanceExceeded || shortlist.isGuidanceWarningAcknowledged(),
                shortlist.getVersion() == null ? 0 : shortlist.getVersion(),
                finalizedAt);
    }

    private String normalizeNote(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > 1000) {
            throw new ValidationException("finalizationNote must not exceed 1000 characters.");
        }
        return normalized;
    }

    private CurrentActor currentAdmin() {
        CurrentActor actor = currentActorProvider.currentActor()
                .orElseThrow(() -> new UnauthorizedException("Authentication is required."));
        if (!actor.hasRole(RoleName.ADMIN)) {
            throw new ForbiddenException("The current account cannot finalize shortlists.");
        }
        return actor;
    }
}
