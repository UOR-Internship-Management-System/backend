package lk.ac.ruhuna.dcs.cvmanagement.modules.internships;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequestCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequestUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequiredSkillRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.application.InternshipRequestService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.domain.exception.InvalidTaxonomySkillException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.mapper.InternshipRequestMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.entity.InternshipRequestEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection.CompanySnapshotProjection;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection.InternshipRequestDetailProjection;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection.SkillSnapshotProjection;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.repository.InternshipReferenceQuery;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.repository.InternshipRequestQueryRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.repository.InternshipRequestRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.repository.InternshipRequestSkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventType;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.PreconditionFailedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ValidationException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InternshipRequestServiceTest {

    private final InternshipRequestRepository requestRepository = mock(InternshipRequestRepository.class);
    private final InternshipRequestSkillRepository skillRepository = mock(InternshipRequestSkillRepository.class);
    private final InternshipRequestQueryRepository queryRepository = mock(InternshipRequestQueryRepository.class);
    private final InternshipReferenceQuery referenceQuery = mock(InternshipReferenceQuery.class);
    private final CurrentActorProvider actorProvider = mock(CurrentActorProvider.class);
    private final AuditEventPublisher auditPublisher = mock(AuditEventPublisher.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-15T15:00:00Z"), ZoneOffset.UTC);
    private InternshipRequestService service;
    private UUID actorId;

    @BeforeEach
    void setUp() {
        actorId = UUID.randomUUID();
        when(actorProvider.currentActor()).thenReturn(Optional.of(
                new CurrentActor(actorId, "admin@dcs.ruh.ac.lk", Set.of(RoleName.ADMIN))));
        service = new InternshipRequestService(
                requestRepository, skillRepository, queryRepository, referenceQuery,
                new InternshipRequestMapper(), actorProvider, auditPublisher, clock);
    }

    @Test
    void createsRequestAndRequiredSkillsAtomicallyThenAudits() {
        UUID companyId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        when(referenceQuery.findCompany(companyId)).thenReturn(Optional.of(company(companyId)));
        when(referenceQuery.findSkills(List.of(skillId))).thenReturn(Map.of(
                skillId, new SkillSnapshotProjection(skillId, "Java", true)));
        when(requestRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            InternshipRequestEntity entity = invocation.getArgument(0);
            entity.setVersion(0L);
            return entity;
        });
        when(queryRepository.findDetail(any())).thenAnswer(invocation -> Optional.of(detail(
                invocation.getArgument(0), companyId, 0L)));
        when(queryRepository.findRequiredSkills(any(java.util.Collection.class))).thenReturn(List.of());

        var response = service.create(new InternshipRequestCreateRequest(
                companyId, " Software Engineer Intern ", " Build services ", 10,
                List.of(new InternshipRequiredSkillRequest(skillId))));

        assertThat(response.title()).isEqualTo("Software Engineer Intern");
        assertThat(response.company().companyId()).isEqualTo(companyId);
        verify(skillRepository).saveAllAndFlush(any());
        verify(auditPublisher).recordRequired(
                eq(actorId), eq("ADMIN"), eq(AuditEventType.INTERNSHIP_REQUEST_CREATED.name()),
                eq(AuditEventCategory.INTERNSHIP_MANAGEMENT), eq("INTERNSHIP_REQUEST"), any(), any());
    }

    @Test
    void rejectsInactiveOrMissingTaxonomySkillBeforePersistence() {
        UUID companyId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        when(referenceQuery.findCompany(companyId)).thenReturn(Optional.of(company(companyId)));
        when(referenceQuery.findSkills(List.of(skillId))).thenReturn(Map.of(
                skillId, new SkillSnapshotProjection(skillId, "Legacy Skill", false)));

        assertThatThrownBy(() -> service.create(new InternshipRequestCreateRequest(
                companyId, "Intern", null, null, List.of(new InternshipRequiredSkillRequest(skillId)))))
                .isInstanceOf(InvalidTaxonomySkillException.class);
        verify(requestRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsDuplicateSkillIdsInAtomicPayload() {
        UUID companyId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        when(referenceQuery.findCompany(companyId)).thenReturn(Optional.of(company(companyId)));

        assertThatThrownBy(() -> service.create(new InternshipRequestCreateRequest(
                companyId, "Intern", null, null,
                List.of(new InternshipRequiredSkillRequest(skillId), new InternshipRequiredSkillRequest(skillId)))))
                .isInstanceOf(ValidationException.class);
        verify(referenceQuery, never()).findSkills(any());
    }

    @Test
    void staleUpdateFailsBeforeSkillReplacementOrAudit() {
        UUID requestId = UUID.randomUUID();
        InternshipRequestEntity entity = entity(requestId, 4L);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(entity));
        InternshipRequestUpdateRequest update = new InternshipRequestUpdateRequest();
        update.setRequiredSkills(List.of());

        assertThatThrownBy(() -> service.update(requestId, update, 3L))
                .isInstanceOf(PreconditionFailedException.class);
        verify(skillRepository, never()).deleteAllByInternshipRequestId(any());
        verify(auditPublisher, never()).recordRequired(any(), any(), any(), any(), any(), any(), any());
    }

    private CompanySnapshotProjection company(UUID companyId) {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-15T12:00:00Z");
        return new CompanySnapshotProjection(companyId, "Example Technologies", null, null, null, null,
                null, 0L, now, now);
    }

    private InternshipRequestDetailProjection detail(UUID requestId, UUID companyId, long version) {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-15T15:00:00Z");
        return new InternshipRequestDetailProjection(
                requestId, companyId, "Example Technologies", null, null, null, null, null,
                0L, now, now, "Software Engineer Intern", "Build services", 10,
                version, now, now);
    }

    private InternshipRequestEntity entity(UUID requestId, long version) {
        InternshipRequestEntity entity = new InternshipRequestEntity();
        entity.setId(requestId);
        entity.setCompanyId(UUID.randomUUID());
        entity.setTitle("Intern");
        entity.setVersion(version);
        entity.setCreatedAt(OffsetDateTime.parse("2026-08-15T12:00:00Z"));
        entity.setUpdatedAt(OffsetDateTime.parse("2026-08-15T12:00:00Z"));
        return entity;
    }
}
