package lk.ac.ruhuna.dcs.cvmanagement.modules.companies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanyRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanyUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.domain.exception.DuplicateCompanyException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.mapper.CompanyMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.persistence.entity.CompanyEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.persistence.repository.CompanyQueryRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.persistence.repository.CompanyRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.application.CompanyService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventType;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.PreconditionFailedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CompanyServiceTest {

    private final CompanyRepository repository = mock(CompanyRepository.class);
    private final CompanyQueryRepository queryRepository = mock(CompanyQueryRepository.class);
    private final CurrentActorProvider actorProvider = mock(CurrentActorProvider.class);
    private final AuditEventPublisher auditPublisher = mock(AuditEventPublisher.class);
    private final CompanyMapper mapper = new CompanyMapper();
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-15T12:00:00Z"), ZoneOffset.UTC);

    private CompanyService service;
    private UUID actorId;

    @BeforeEach
    void setUp() {
        actorId = UUID.randomUUID();
        when(actorProvider.currentActor()).thenReturn(Optional.of(
                new CurrentActor(actorId, "admin@dcs.ruh.ac.lk", Set.of(RoleName.ADMIN))));
        service = new CompanyService(repository, queryRepository, mapper, actorProvider, auditPublisher, clock);
    }

    @Test
    void createsNormalizedCompanyAndRecordsRequiredAudit() {
        when(repository.existsByNormalizedName("example technologies")).thenReturn(false);
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            CompanyEntity entity = invocation.getArgument(0);
            entity.setVersion(0L);
            return entity;
        });

        var response = service.create(new CompanyRequest(
                "  Example   Technologies ",
                " https://example.com ",
                " HR Coordinator ",
                " hr@example.com ",
                " +94 11 234 5678 ",
                "  Preferred partner  "));

        assertThat(response.name()).isEqualTo("Example Technologies");
        assertThat(response.websiteUrl()).isEqualTo("https://example.com");
        assertThat(response.contactPerson()).isEqualTo("HR Coordinator");
        assertThat(response.contactEmail()).isEqualTo("hr@example.com");
        assertThat(response.notes()).isEqualTo("Preferred partner");
        assertThat(response.version()).isZero();
        assertThat(response.createdAt()).isEqualTo(response.updatedAt());

        verify(auditPublisher).recordRequired(
                eq(actorId),
                eq("ADMIN"),
                eq(AuditEventType.COMPANY_CREATED.name()),
                eq(AuditEventCategory.INTERNSHIP_MANAGEMENT),
                eq("COMPANY"),
                eq(response.companyId().toString()),
                any());
    }

    @Test
    void rejectsFriendlyDuplicateBeforeInsert() {
        when(repository.existsByNormalizedName("example technologies")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CompanyRequest(
                "Example Technologies", null, null, null, null, null)))
                .isInstanceOf(DuplicateCompanyException.class);

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void patchCanExplicitlyClearNullableFields() {
        UUID companyId = UUID.randomUUID();
        CompanyEntity entity = existing(companyId, 4L);
        entity.setContactPhone("+94 11 234 5678");
        when(repository.findById(companyId)).thenReturn(Optional.of(entity));
        when(repository.saveAndFlush(entity)).thenAnswer(invocation -> {
            entity.setVersion(5L);
            return entity;
        });

        CompanyUpdateRequest request = new CompanyUpdateRequest();
        request.setContactPhone(null);

        var response = service.update(companyId, request, 4L);

        assertThat(response.contactPhone()).isNull();
        assertThat(response.version()).isEqualTo(5L);

        ArgumentCaptor<java.util.Map<String, ?>> metadata = ArgumentCaptor.forClass(java.util.Map.class);
        verify(auditPublisher).recordRequired(
                eq(actorId),
                eq("ADMIN"),
                eq(AuditEventType.COMPANY_UPDATED.name()),
                eq(AuditEventCategory.INTERNSHIP_MANAGEMENT),
                eq("COMPANY"),
                eq(companyId.toString()),
                metadata.capture());
        assertThat(metadata.getValue().get("changedFields")).isEqualTo(Set.of("contactPhone"));
    }

    @Test
    void staleUpdateFailsBeforeMutationOrAudit() {
        UUID companyId = UUID.randomUUID();
        CompanyEntity entity = existing(companyId, 3L);
        when(repository.findById(companyId)).thenReturn(Optional.of(entity));

        CompanyUpdateRequest request = new CompanyUpdateRequest();
        request.setNotes("new notes");

        assertThatThrownBy(() -> service.update(companyId, request, 2L))
                .isInstanceOf(PreconditionFailedException.class);

        verify(repository, never()).saveAndFlush(any());
        verify(auditPublisher, never()).recordRequired(
                any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void deleteReliesOnDatabaseCascadeAndRecordsAuditInSameServiceTransaction() {
        UUID companyId = UUID.randomUUID();
        CompanyEntity entity = existing(companyId, 7L);
        when(repository.findById(companyId)).thenReturn(Optional.of(entity));
        doAnswer(invocation -> null).when(repository).delete(entity);

        service.delete(companyId, 7L);

        verify(auditPublisher).recordRequired(
                eq(actorId),
                eq("ADMIN"),
                eq(AuditEventType.COMPANY_DELETED.name()),
                eq(AuditEventCategory.INTERNSHIP_MANAGEMENT),
                eq("COMPANY"),
                eq(companyId.toString()),
                any());
        verify(repository).delete(entity);
        verify(repository).flush();
    }

    private CompanyEntity existing(UUID id, long version) {
        CompanyEntity entity = new CompanyEntity();
        entity.setId(id);
        entity.setName("Example Technologies");
        entity.setVersion(version);
        entity.setCreatedAt(java.time.OffsetDateTime.parse("2026-08-14T10:00:00Z"));
        entity.setUpdatedAt(java.time.OffsetDateTime.parse("2026-08-14T10:00:00Z"));
        return entity;
    }
}
