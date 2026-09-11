package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.request.ShortlistFinalizeRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.application.ShortlistFinalizationService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.domain.policy.ShortlistStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.entity.ShortlistEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.repository.ShortlistCandidateRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.repository.ShortlistRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ConflictException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.PreconditionFailedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ValidationException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShortlistFinalizationServiceTest {

    private final ShortlistRepository shortlistRepository = mock(ShortlistRepository.class);
    private final ShortlistCandidateRepository candidateRepository = mock(ShortlistCandidateRepository.class);
    private final CurrentActorProvider actorProvider = mock(CurrentActorProvider.class);
    private final AuditEventPublisher auditPublisher = mock(AuditEventPublisher.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-22T02:30:00Z"), ZoneOffset.UTC);
    private final UUID shortlistId = UUID.fromString("b1000000-0000-4000-8000-000000000001");
    private final UUID adminId = UUID.fromString("b1000000-0000-4000-8000-000000000002");

    private ShortlistFinalizationService service;
    private ShortlistEntity shortlist;

    @BeforeEach
    void setUp() {
        when(actorProvider.currentActor()).thenReturn(Optional.of(
                new CurrentActor(adminId, "admin@dcs.ruh.ac.lk", Set.of(RoleName.ADMIN))));
        shortlist = new ShortlistEntity();
        shortlist.setId(shortlistId);
        shortlist.setStatus(ShortlistStatus.DRAFT);
        shortlist.setVersion(3L);
        shortlist.setGuidanceValueSnapshot(2);
        when(shortlistRepository.findByIdForUpdate(shortlistId)).thenReturn(Optional.of(shortlist));
        service = new ShortlistFinalizationService(
                shortlistRepository, candidateRepository, actorProvider, auditPublisher, clock);
    }

    @Test
    void finalizesNonEmptyDraftAndTreatsGuidanceAsAdvisoryWhenAcknowledged() {
        when(candidateRepository.countByShortlistId(shortlistId)).thenReturn(3L);

        var response = service.finalizeShortlist(
                shortlistId, new ShortlistFinalizeRequest(true, " Reviewed "), 3L);

        assertThat(response.status()).isEqualTo(ShortlistStatus.FINALIZED);
        assertThat(response.selectedCandidateCount()).isEqualTo(3);
        assertThat(response.guidanceExceeded()).isTrue();
        assertThat(response.guidanceAcknowledged()).isTrue();
        assertThat(shortlist.getStatus()).isEqualTo(ShortlistStatus.FINALIZED);
        assertThat(shortlist.getFinalizationNote()).isEqualTo("Reviewed");
        assertThat(shortlist.getFinalizedByAccountId()).isEqualTo(adminId);
        verify(shortlistRepository).saveAndFlush(shortlist);
    }

    @Test
    void rejectsGuidanceExceedanceWithoutAcknowledgement() {
        when(candidateRepository.countByShortlistId(shortlistId)).thenReturn(3L);

        assertThatThrownBy(() -> service.finalizeShortlist(
                shortlistId, new ShortlistFinalizeRequest(false, null), 3L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Acknowledge");

        verify(shortlistRepository, never()).saveAndFlush(shortlist);
    }

    @Test
    void rejectsEmptyStaleAndAlreadyFinalizedShortlists() {
        when(candidateRepository.countByShortlistId(shortlistId)).thenReturn(0L);
        assertThatThrownBy(() -> service.finalizeShortlist(
                shortlistId, new ShortlistFinalizeRequest(false, null), 3L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("at least one");

        assertThatThrownBy(() -> service.finalizeShortlist(
                shortlistId, new ShortlistFinalizeRequest(false, null), 2L))
                .isInstanceOf(PreconditionFailedException.class);

        shortlist.setStatus(ShortlistStatus.FINALIZED);
        assertThatThrownBy(() -> service.finalizeShortlist(
                shortlistId, new ShortlistFinalizeRequest(false, null), 3L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already been finalized");
    }
}
