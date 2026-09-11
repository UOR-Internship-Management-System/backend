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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.CandidateFilteringDependencyExecutor;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.CandidateFilteringMetrics;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.CandidateFilteringQueryService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.model.CandidateFilteringCandidateCore;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.FilterDependencyUnavailableException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.FilterRunNotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.CandidateDeclaredSkillCompetencyLevel;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.CandidateSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillCriteriaSource;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillMatchMode;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.GpaAvailabilityStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.mapper.CandidateFilteringMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.entity.FilterRunEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.entity.FilterRunSkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.projection.CandidateFilterRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.projection.CandidateMatchingSkillRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.projection.FilterRequestSummaryRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.query.CandidateFilteringReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.repository.FilterRunRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.persistence.repository.FilterRunSkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.BadRequestException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.UnauthorizedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.filtering.CandidateEnrichmentQuery;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class CandidateFilteringQueryServiceTest {

    private final FilterRunRepository runRepository = mock(FilterRunRepository.class);
    private final FilterRunSkillRepository runSkillRepository = mock(FilterRunSkillRepository.class);
    private final CandidateFilteringReadRepository readRepository = mock(CandidateFilteringReadRepository.class);
    private final CurrentActorProvider actorProvider = mock(CurrentActorProvider.class);
    private final CandidateFilteringMetrics metrics = mock(CandidateFilteringMetrics.class);
    private final CandidateEnrichmentQuery enrichmentQuery = mock(CandidateEnrichmentQuery.class);
    private final CandidateFilteringDependencyExecutor dependencyExecutor =
            new CandidateFilteringDependencyExecutor(metrics);
    private final CandidateFilteringMapper mapper = new CandidateFilteringMapper();

    private final UUID actorId = UUID.fromString("92000000-0000-4000-8000-000000000001");
    private final UUID runId = UUID.fromString("92000000-0000-4000-8000-000000000002");
    private final UUID requestId = UUID.fromString("92000000-0000-4000-8000-000000000003");
    private final UUID companyId = UUID.fromString("92000000-0000-4000-8000-000000000004");
    private final UUID requestSkill = UUID.fromString("92000000-0000-4000-8000-000000000005");
    private final UUID additionalSkill = UUID.fromString("92000000-0000-4000-8000-000000000006");
    private final UUID studentA = UUID.fromString("92000000-0000-4000-8000-000000000007");
    private final UUID studentB = UUID.fromString("92000000-0000-4000-8000-000000000008");

    private CandidateFilteringQueryService service;

    @BeforeEach
    void setUp() {
        when(actorProvider.currentActor()).thenReturn(Optional.of(
                new CurrentActor(actorId, "admin@dcs.ruh.ac.lk", Set.of(RoleName.ADMIN))));
        when(runRepository.findById(runId)).thenReturn(Optional.of(runEntity()));
        when(runSkillRepository.findAllByFilterRunId(runId)).thenReturn(List.of(
                new FilterRunSkillEntity(runId, requestSkill, FilterSkillCriteriaSource.REQUEST),
                new FilterRunSkillEntity(runId, additionalSkill, FilterSkillCriteriaSource.ADDITIONAL)));
        service = new CandidateFilteringQueryService(
                runRepository,
                runSkillRepository,
                readRepository,
                mapper,
                actorProvider,
                dependencyExecutor,
                metrics,
                enrichmentQuery);
    }

    @Test
    void getRunReconstructsPersistedCriteriaAndRecomputesCurrentCandidateCount() {
        when(readRepository.findRequestSummary(requestId)).thenReturn(Optional.of(
                new FilterRequestSummaryRow(requestId, companyId, "Example Company", "Backend Intern", 6)));
        when(readRepository.countCandidates(any())).thenReturn(9L);

        var response = service.getRun(runId);

        assertThat(response.filterRunId()).isEqualTo(runId);
        assertThat(response.criteria().requestSkillIds()).containsExactly(requestSkill);
        assertThat(response.criteria().additionalSkillIds()).containsExactly(additionalSkill);
        assertThat(response.criteria().runtimeGpaLowerBound()).isEqualByComparingTo("3.00");
        assertThat(response.criteria().skillMatchMode()).isEqualTo(FilterSkillMatchMode.AND);
        assertThat(response.candidateCount()).isEqualTo(9);
        verify(metrics).recordCandidateCount(9L);
    }

    @Test
    void listCandidateCoreUsesServerPagingSearchSortAndBulkMatchingSkillRead() {
        CandidateFilterRow first = new CandidateFilterRow(
                studentA, "SC/2022/10001", "Alpha Student", new BigDecimal("3.50"), 3);
        CandidateFilterRow second = new CandidateFilterRow(
                studentB, "SC/2022/10002", "Beta Student", null, 1);
        when(readRepository.searchCandidates(any(), eq("alpha"), eq(0), eq(20), eq(CandidateSort.FULL_NAME_ASC)))
                .thenReturn(new PageImpl<>(List.of(first, second), PageRequest.of(0, 20), 2));

        OffsetDateTime createdAt = OffsetDateTime.parse("2026-08-17T10:00:00Z");
        CandidateMatchingSkillRow matching = new CandidateMatchingSkillRow(
                studentA,
                UUID.randomUUID(),
                requestSkill,
                "React",
                CandidateDeclaredSkillCompetencyLevel.INTERMEDIATE,
                2L,
                createdAt,
                createdAt);
        when(readRepository.findMatchingDeclaredSkills(
                        List.of(studentA, studentB), List.of(requestSkill, additionalSkill)))
                .thenReturn(List.of(matching));

        var page = service.listCandidateCore(runId, null, null, "  Alpha  ", "fullName,asc");

        assertThat(page.page().page()).isZero();
        assertThat(page.page().size()).isEqualTo(20);
        assertThat(page.page().sort()).isEqualTo("fullName,asc");
        assertThat(page.page().totalElements()).isEqualTo(2);
        assertThat(page.items()).hasSize(2);

        CandidateFilteringCandidateCore alpha = page.items().getFirst();
        assertThat(alpha.studentId()).isEqualTo(studentA);
        assertThat(alpha.gpaAvailabilityStatus()).isEqualTo(GpaAvailabilityStatus.AVAILABLE);
        assertThat(alpha.matchingDeclaredSkills()).singleElement().satisfies(skill -> {
            assertThat(skill.skillId()).isEqualTo(requestSkill);
            assertThat(skill.skillName()).isEqualTo("React");
        });

        CandidateFilteringCandidateCore beta = page.items().get(1);
        assertThat(beta.gpaAvailabilityStatus()).isEqualTo(GpaAvailabilityStatus.NOT_AVAILABLE);
        assertThat(beta.matchingDeclaredSkills()).isEmpty();
    }

    @Test
    void publicCandidateContractUsesAuthoritativeCvAndShortlistEnrichment() {
        CandidateFilterRow row = new CandidateFilterRow(
                studentA, "SC/2022/10001", "Alpha Student", new BigDecimal("3.50"), 3);
        when(readRepository.searchCandidates(any(), eq(null), eq(0), eq(20), eq(CandidateSort.OFFICIAL_GPA_DESC)))
                .thenReturn(new PageImpl<>(List.of(row), PageRequest.of(0, 20), 1));
        when(readRepository.findMatchingDeclaredSkills(List.of(studentA), List.of(requestSkill, additionalSkill)))
                .thenReturn(List.of());
        when(enrichmentQuery.findAll(Set.of(studentA))).thenReturn(java.util.Map.of(
                studentA, new CandidateEnrichmentQuery.CandidateEnrichment(true, 2)));

        var page = service.listCandidates(runId, 0, 20, null, "officialGpa,desc");

        assertThat(page.items()).singleElement().satisfies(candidate -> {
            assertThat(candidate.hasLatestSavedCv()).isTrue();
            assertThat(candidate.hasExistingActiveShortlist()).isTrue();
            assertThat(candidate.existingActiveShortlistCount()).isEqualTo(2);
        });
    }

    @Test
    void publicCandidateContractValidatesQueryBeforeDependencyGate() {
        assertThatThrownBy(() -> service.listCandidates(runId, -1, 20, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("page");
    }

    @Test
    void coreCandidateModelDoesNotFabricateUnavailableCvOrShortlistEnrichment() {
        assertThat(java.util.Arrays.stream(CandidateFilteringCandidateCore.class.getRecordComponents())
                        .map(java.lang.reflect.RecordComponent::getName)
                        .toList())
                .doesNotContain(
                        "hasLatestSavedCv",
                        "hasExistingActiveShortlist",
                        "existingActiveShortlistCount");
    }

    @Test
    void missingRunReturnsStableFiltering404() {
        UUID missingRun = UUID.randomUUID();
        when(runRepository.findById(missingRun)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRun(missingRun))
                .isInstanceOf(FilterRunNotFoundException.class);
    }

    @Test
    void validatesPageSizeOffsetAndSearchBeforeCandidateSql() {
        assertThatThrownBy(() -> service.listCandidateCore(runId, -1, 20, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("page");
        assertThatThrownBy(() -> service.listCandidateCore(runId, 0, 101, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("size");
        assertThatThrownBy(() -> service.listCandidateCore(runId, Integer.MAX_VALUE, 100, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("too large");
        assertThatThrownBy(() -> service.listCandidateCore(runId, 0, 20, "x".repeat(121), null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("120");

        verify(readRepository, never()).searchCandidates(
                any(),
                any(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                any());
    }

    @Test
    void requiresAuthenticationAndAdminRoleAtApplicationBoundary() {
        when(actorProvider.currentActor()).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getRun(runId)).isInstanceOf(UnauthorizedException.class);

        when(actorProvider.currentActor()).thenReturn(Optional.of(
                new CurrentActor(actorId, "student@dcs.ruh.ac.lk", Set.of(RoleName.STUDENT))));
        assertThatThrownBy(() -> service.getRun(runId)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void transientDatabaseFailureBecomesFilteringDependencyUnavailable() {
        when(runRepository.findById(runId))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        assertThatThrownBy(() -> service.getRun(runId))
                .isInstanceOf(FilterDependencyUnavailableException.class)
                .hasCauseInstanceOf(DataAccessResourceFailureException.class);
    }

    private FilterRunEntity runEntity() {
        FilterRunEntity entity = new FilterRunEntity();
        entity.setId(runId);
        entity.setInternshipRequestId(requestId);
        entity.setRunByAccountId(actorId);
        entity.setRuntimeGpaLowerBound(new BigDecimal("3.00"));
        entity.setRuntimeGpaUpperBound(null);
        entity.setSkillMatchMode(FilterSkillMatchMode.AND);
        entity.setCreatedAt(OffsetDateTime.parse("2026-08-17T10:30:00Z"));
        return entity;
    }
}
