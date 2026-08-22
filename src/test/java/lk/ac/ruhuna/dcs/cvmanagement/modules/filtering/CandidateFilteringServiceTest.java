package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.request.CandidateFilteringRunRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.CandidateFilteringDependencyExecutor;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.CandidateFilteringMetrics;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.CandidateFilteringService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.FilterDependencyUnavailableException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.FilteringInternshipRequestNotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.InvalidFilterCriteriaException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillCriteriaSource;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillMatchMode;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.mapper.CandidateFilteringMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.entity.FilterRunEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.entity.FilterRunSkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.projection.FilterRequestSummaryRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.query.CandidateFilteringReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.repository.FilterRunRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.repository.FilterRunSkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventType;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;

class CandidateFilteringServiceTest {

    private final FilterRunRepository runRepository = mock(FilterRunRepository.class);
    private final FilterRunSkillRepository runSkillRepository = mock(FilterRunSkillRepository.class);
    private final CandidateFilteringReadRepository readRepository = mock(CandidateFilteringReadRepository.class);
    private final CurrentActorProvider actorProvider = mock(CurrentActorProvider.class);
    private final AuditEventPublisher auditPublisher = mock(AuditEventPublisher.class);
    private final CandidateFilteringMetrics metrics = mock(CandidateFilteringMetrics.class);
    private final CandidateFilteringMapper mapper = new CandidateFilteringMapper();
    private final CandidateFilteringDependencyExecutor dependencyExecutor =
            new CandidateFilteringDependencyExecutor(metrics);
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-17T11:30:00Z"), ZoneOffset.UTC);

    private final UUID actorId = UUID.fromString("91000000-0000-4000-8000-000000000001");
    private final UUID requestId = UUID.fromString("91000000-0000-4000-8000-000000000002");
    private final UUID companyId = UUID.fromString("91000000-0000-4000-8000-000000000003");
    private final UUID requestSkill = UUID.fromString("91000000-0000-4000-8000-000000000004");
    private final UUID additionalSkill = UUID.fromString("91000000-0000-4000-8000-000000000005");

    private CandidateFilteringService service;

    @BeforeEach
    void setUp() {
        when(actorProvider.currentActor()).thenReturn(Optional.of(
                new CurrentActor(actorId, "admin@dcs.ruh.ac.lk", Set.of(RoleName.ADMIN))));
        when(readRepository.findRequestSummary(requestId)).thenReturn(Optional.of(
                new FilterRequestSummaryRow(requestId, companyId, "Example Company", "Backend Intern", 8)));
        when(runRepository.saveAndFlush(any(FilterRunEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        service = new CandidateFilteringService(
                runRepository,
                runSkillRepository,
                readRepository,
                mapper,
                actorProvider,
                auditPublisher,
                dependencyExecutor,
                metrics,
                clock);
    }

    @Test
    void createsImmutableRunPersistsSkillSourcesAndWritesSanitizedRequiredAudit() {
        when(readRepository.findRequiredSkillIds(requestId, List.of(requestSkill)))
                .thenReturn(Set.of(requestSkill));
        when(readRepository.findActiveSkillIds(List.of(additionalSkill)))
                .thenReturn(Set.of(additionalSkill));
        when(readRepository.findRequiredSkillIds(requestId, List.of(additionalSkill)))
                .thenReturn(Set.of());
        when(readRepository.countCandidates(any())).thenReturn(12L);

        var response = service.createRun(new CandidateFilteringRunRequest(
                requestId,
                new BigDecimal("3.00"),
                new BigDecimal("4.00"),
                List.of(requestSkill),
                List.of(additionalSkill),
                FilterSkillMatchMode.AND));

        assertThat(response.request().requestId()).isEqualTo(requestId);
        assertThat(response.criteria().runtimeGpaLowerBound()).isEqualByComparingTo("3.00");
        assertThat(response.criteria().runtimeGpaUpperBound()).isEqualByComparingTo("4.00");
        assertThat(response.criteria().requestSkillIds()).containsExactly(requestSkill);
        assertThat(response.criteria().additionalSkillIds()).containsExactly(additionalSkill);
        assertThat(response.criteria().skillMatchMode()).isEqualTo(FilterSkillMatchMode.AND);
        assertThat(response.candidateCount()).isEqualTo(12);
        assertThat(response.createdAt()).isEqualTo(java.time.OffsetDateTime.parse("2026-08-17T11:30:00Z"));

        ArgumentCaptor<List<FilterRunSkillEntity>> skillCaptor = ArgumentCaptor.forClass(List.class);
        verify(runSkillRepository).saveAllAndFlush(skillCaptor.capture());
        assertThat(skillCaptor.getValue())
                .extracting(FilterRunSkillEntity::getCriteriaSource)
                .containsExactly(FilterSkillCriteriaSource.REQUEST, FilterSkillCriteriaSource.ADDITIONAL);

        ArgumentCaptor<Map<String, ?>> metadataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(auditPublisher).recordRequired(
                eq(actorId),
                eq(RoleName.ADMIN.name()),
                eq(AuditEventType.CANDIDATE_FILTER_RUN_CREATED.name()),
                eq(AuditEventCategory.CANDIDATE_FILTERING),
                eq("CANDIDATE_FILTER_RUN"),
                eq(response.filterRunId().toString()),
                metadataCaptor.capture());

        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) metadataCaptor.getValue();
        assertThat(metadata)
                .containsEntry("filterRunId", response.filterRunId().toString())
                .containsEntry("requestId", requestId.toString())
                .containsEntry("skillMatchMode", "AND")
                .containsEntry("requestSkillCount", 1)
                .containsEntry("additionalSkillCount", 1)
                .containsEntry("candidateCount", 12L);
        assertThat(metadata.values()).doesNotContain(requestSkill.toString(), additionalSkill.toString());
        verify(metrics).runCreated(12L);
    }

    @Test
    void rejectsMissingRequestContextBeforeAnyPersistence() {
        UUID missing = UUID.randomUUID();
        when(readRepository.findRequestSummary(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createRun(new CandidateFilteringRunRequest(
                        missing, null, null, List.of(), List.of(), FilterSkillMatchMode.OR)))
                .isInstanceOf(FilteringInternshipRequestNotFoundException.class);

        verify(runRepository, never()).saveAndFlush(any());
        verify(auditPublisher, never()).recordRequired(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void rejectsRequestSkillThatDoesNotBelongToSelectedRequest() {
        when(readRepository.findRequiredSkillIds(requestId, List.of(requestSkill))).thenReturn(Set.of());

        assertThatThrownBy(() -> service.createRun(new CandidateFilteringRunRequest(
                        requestId, null, null, List.of(requestSkill), List.of(), FilterSkillMatchMode.AND)))
                .isInstanceOf(InvalidFilterCriteriaException.class)
                .hasMessageContaining("not required");

        verify(runRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsMissingOrInactiveAdditionalSkill() {
        when(readRepository.findRequiredSkillIds(requestId, List.of())).thenReturn(Set.of());
        when(readRepository.findActiveSkillIds(List.of(additionalSkill))).thenReturn(Set.of());

        assertThatThrownBy(() -> service.createRun(new CandidateFilteringRunRequest(
                        requestId, null, null, List.of(), List.of(additionalSkill), FilterSkillMatchMode.OR)))
                .isInstanceOf(InvalidFilterCriteriaException.class)
                .hasMessageContaining("missing or inactive");

        verify(runRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsAdditionalSkillThatIsAlreadyRequiredByRequest() {
        when(readRepository.findRequiredSkillIds(requestId, List.of())).thenReturn(Set.of());
        when(readRepository.findActiveSkillIds(List.of(additionalSkill))).thenReturn(Set.of(additionalSkill));
        when(readRepository.findRequiredSkillIds(requestId, List.of(additionalSkill)))
                .thenReturn(Set.of(additionalSkill));

        assertThatThrownBy(() -> service.createRun(new CandidateFilteringRunRequest(
                        requestId, null, null, List.of(), List.of(additionalSkill), FilterSkillMatchMode.OR)))
                .isInstanceOf(InvalidFilterCriteriaException.class)
                .hasMessageContaining("already required");

        verify(runRepository, never()).saveAndFlush(any());
    }

    @Test
    void requiredAuditFailurePropagatesAndDoesNotReportSuccessfulRunMetric() {
        when(readRepository.findRequiredSkillIds(requestId, List.of())).thenReturn(Set.of());
        when(readRepository.findActiveSkillIds(List.of())).thenReturn(Set.of());
        when(readRepository.countCandidates(any())).thenReturn(0L);
        RuntimeException auditFailure = new RuntimeException("audit persistence failed");
        org.mockito.Mockito.doThrow(auditFailure)
                .when(auditPublisher)
                .recordRequired(any(), any(), any(), any(), any(), any(), any());

        assertThatThrownBy(() -> service.createRun(new CandidateFilteringRunRequest(
                        requestId, null, null, List.of(), List.of(), FilterSkillMatchMode.OR)))
                .isSameAs(auditFailure);

        verify(metrics, never()).runCreated(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void transientDatabaseResourceFailureBecomesFilteringDependencyUnavailable() {
        when(readRepository.findRequestSummary(requestId))
                .thenThrow(new DataAccessResourceFailureException("database offline"));

        assertThatThrownBy(() -> service.createRun(new CandidateFilteringRunRequest(
                        requestId, null, null, List.of(), List.of(), FilterSkillMatchMode.OR)))
                .isInstanceOf(FilterDependencyUnavailableException.class)
                .hasCauseInstanceOf(DataAccessResourceFailureException.class);

        verify(runRepository, never()).saveAndFlush(any());
    }
}
