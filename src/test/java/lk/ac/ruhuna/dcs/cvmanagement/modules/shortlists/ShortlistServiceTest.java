package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.request.ShortlistCandidateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.request.ShortlistCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.application.ShortlistService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.domain.policy.ShortlistStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.mapper.ShortlistMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.entity.ShortlistCandidateEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.entity.ShortlistEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.projection.ShortlistRequestContext;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.projection.ShortlistSummaryRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.query.ShortlistReadRepository;
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

class ShortlistServiceTest {

    private final ShortlistRepository shortlistRepository = mock(ShortlistRepository.class);
    private final ShortlistCandidateRepository candidateRepository = mock(ShortlistCandidateRepository.class);
    private final ShortlistReadRepository readRepository = mock(ShortlistReadRepository.class);
    private final CurrentActorProvider actorProvider = mock(CurrentActorProvider.class);
    private final AuditEventPublisher auditPublisher = mock(AuditEventPublisher.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-22T02:30:00Z"), ZoneOffset.UTC);
    private final UUID adminId = UUID.fromString("b2000000-0000-4000-8000-000000000001");
    private final UUID requestId = UUID.fromString("b2000000-0000-4000-8000-000000000002");
    private final UUID companyId = UUID.fromString("b2000000-0000-4000-8000-000000000003");

    private ShortlistService service;

    @BeforeEach
    void setUp() {
        when(actorProvider.currentActor()).thenReturn(Optional.of(
                new CurrentActor(adminId, "admin@dcs.ruh.ac.lk", Set.of(RoleName.ADMIN))));
        service = new ShortlistService(
                shortlistRepository,
                candidateRepository,
                readRepository,
                new ShortlistMapper(),
                actorProvider,
                auditPublisher,
                clock);
    }

    @Test
    void createsOneDraftShortlistAndSnapshotsRequestGuidance() {
        when(readRepository.findRequest(requestId)).thenReturn(Optional.of(
                new ShortlistRequestContext(requestId, companyId, "Acme", "Backend Intern", 2)));
        AtomicReference<ShortlistEntity> saved = new AtomicReference<>();
        when(shortlistRepository.saveAndFlush(any(ShortlistEntity.class))).thenAnswer(invocation -> {
            ShortlistEntity entity = invocation.getArgument(0);
            entity.setVersion(0L);
            saved.set(entity);
            return entity;
        });
        when(readRepository.findSummary(any(UUID.class))).thenAnswer(invocation -> {
            ShortlistEntity entity = saved.get();
            return Optional.of(summary(entity, 0));
        });

        var response = service.create(new ShortlistCreateRequest(requestId, null, "  First choice  "));

        assertThat(response.status()).isEqualTo(ShortlistStatus.DRAFT);
        assertThat(response.guidanceValue()).isEqualTo(2);
        assertThat(response.name()).isEqualTo("First choice");
        assertThat(saved.get().getCreatedByAccountId()).isEqualTo(adminId);
    }

    @Test
    void rejectsSecondShortlistAndMismatchedFilterRun() {
        UUID filterRunId = UUID.randomUUID();
        when(readRepository.findRequest(requestId)).thenReturn(Optional.of(
                new ShortlistRequestContext(requestId, companyId, "Acme", "Backend Intern", 2)));
        when(readRepository.filterRunMatchesRequest(filterRunId, requestId)).thenReturn(false);

        assertThatThrownBy(() -> service.create(new ShortlistCreateRequest(requestId, filterRunId, null)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("filterRunId");

        when(readRepository.filterRunMatchesRequest(filterRunId, requestId)).thenReturn(true);
        when(shortlistRepository.existsByInternshipRequestId(requestId)).thenReturn(true);
        assertThatThrownBy(() -> service.create(new ShortlistCreateRequest(requestId, filterRunId, null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void candidateAddIsIdempotentAndRejectsStaleVersion() {
        UUID shortlistId = UUID.randomUUID();
        UUID existingStudent = UUID.randomUUID();
        UUID newStudent = UUID.randomUUID();
        ShortlistEntity shortlist = draft(shortlistId, 4L, 1);
        when(shortlistRepository.findByIdForUpdate(shortlistId)).thenReturn(Optional.of(shortlist));

        assertThatThrownBy(() -> service.addCandidates(
                shortlistId, new ShortlistCandidateRequest(List.of(newStudent), null), 3L))
                .isInstanceOf(PreconditionFailedException.class);

        when(readRepository.findActiveStudentIds(List.of(existingStudent, newStudent)))
                .thenReturn(Set.of(existingStudent, newStudent));
        ShortlistCandidateEntity existing = new ShortlistCandidateEntity();
        existing.setStudentId(existingStudent);
        when(candidateRepository.findAllByShortlistIdAndStudentIdIn(
                shortlistId, List.of(existingStudent, newStudent)))
                .thenReturn(List.of(existing));
        when(candidateRepository.countByShortlistId(shortlistId)).thenReturn(2L);

        var response = service.addCandidates(
                shortlistId,
                new ShortlistCandidateRequest(List.of(existingStudent, newStudent), "Selected"),
                4L);

        assertThat(response.addedCount()).isEqualTo(1);
        assertThat(response.alreadyPresentCount()).isEqualTo(1);
        assertThat(response.guidanceExceeded()).isTrue();
        verify(candidateRepository).saveAllAndFlush(any());
    }

    @Test
    void rejectsInactiveStudentBeforeWritingCandidates() {
        UUID shortlistId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        when(shortlistRepository.findByIdForUpdate(shortlistId))
                .thenReturn(Optional.of(draft(shortlistId, 0L, 2)));
        when(readRepository.findActiveStudentIds(List.of(studentId))).thenReturn(Set.of());

        assertThatThrownBy(() -> service.addCandidates(
                shortlistId, new ShortlistCandidateRequest(List.of(studentId), null), 0L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("active eligible");
        verify(candidateRepository, never()).saveAllAndFlush(any());
    }

    private ShortlistEntity draft(UUID id, long version, int guidance) {
        ShortlistEntity entity = new ShortlistEntity();
        entity.setId(id);
        entity.setStatus(ShortlistStatus.DRAFT);
        entity.setVersion(version);
        entity.setGuidanceValueSnapshot(guidance);
        entity.setUpdatedAt(OffsetDateTime.now(clock));
        return entity;
    }

    private ShortlistSummaryRow summary(ShortlistEntity entity, long candidateCount) {
        return new ShortlistSummaryRow(
                entity.getId(),
                requestId,
                companyId,
                "Acme",
                "Backend Intern",
                2,
                entity.getFilterRunId(),
                entity.getName(),
                entity.getStatus(),
                entity.getGuidanceValueSnapshot(),
                candidateCount,
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getFinalizedAt());
    }
}
