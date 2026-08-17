package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringCandidateResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringRunResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.model.CandidateFilteringCandidateCore;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.FilterDependencyUnavailableException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.FilterRunNotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.CandidateFilteringCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.CandidateSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillCriteriaSource;
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
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.CorrelationIdContext;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Admin-only read service for persisted filtering runs and current deterministic candidate data. */
@Service
public class CandidateFilteringQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CandidateFilteringQueryService.class);
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final int MAX_SEARCH_LENGTH = 120;

    private final FilterRunRepository runRepository;
    private final FilterRunSkillRepository runSkillRepository;
    private final CandidateFilteringReadRepository readRepository;
    private final CandidateFilteringMapper mapper;
    private final CurrentActorProvider currentActorProvider;
    private final CandidateFilteringDependencyExecutor dependencyExecutor;
    private final CandidateFilteringMetrics metrics;

    public CandidateFilteringQueryService(
            FilterRunRepository runRepository,
            FilterRunSkillRepository runSkillRepository,
            CandidateFilteringReadRepository readRepository,
            CandidateFilteringMapper mapper,
            CurrentActorProvider currentActorProvider,
            CandidateFilteringDependencyExecutor dependencyExecutor,
            CandidateFilteringMetrics metrics) {
        this.runRepository = runRepository;
        this.runSkillRepository = runSkillRepository;
        this.readRepository = readRepository;
        this.mapper = mapper;
        this.currentActorProvider = currentActorProvider;
        this.dependencyExecutor = dependencyExecutor;
        this.metrics = metrics;
    }

    /** Returns persisted run criteria together with a candidate count recomputed from current data. */
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public CandidateFilteringRunResponse getRun(UUID filterRunId) {
        currentAdmin();
        LoadedRun loadedRun = loadRun(filterRunId);
        FilterRequestSummaryRow requestSummary = dependencyExecutor.execute(
                        "request_context",
                        () -> readRepository.findRequestSummary(loadedRun.criteria().requestId()))
                .orElseThrow(() -> {
                    LOGGER.error(
                            "Candidate Filtering run references missing request filterRunId={} requestId={} correlationId={}",
                            loadedRun.entity().getId(),
                            loadedRun.criteria().requestId(),
                            correlationId());
                    return new IllegalStateException(
                            "Persisted filtering run references a missing internship request.");
                });
        long candidateCount = dependencyExecutor.execute(
                "candidate_count",
                () -> readRepository.countCandidates(loadedRun.criteria()));
        metrics.recordCandidateCount(candidateCount);

        LOGGER.info(
                "Candidate Filtering run retrieved filterRunId={} candidateCount={} correlationId={}",
                loadedRun.entity().getId(),
                candidateCount,
                correlationId());
        return mapper.toRunResponse(loadedRun.entity(), requestSummary, loadedRun.criteria(), candidateCount);
    }

    /**
     * Public candidate-list contract.
     *
     * <p>The deterministic core result is already implemented, but the current OpenAPI response also
     * requires latest-saved-CV and cross-shortlist facts. Those facts have no authoritative
     * persistence in the current backend. The endpoint therefore fails closed instead of inventing
     * values. A factual downstream enrichment implementation can replace this gate while preserving
     * the method and HTTP contract.
     */
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public PagedResponse<CandidateFilteringCandidateResponse> listCandidates(
            UUID filterRunId,
            Integer page,
            Integer size,
            String search,
            String sort) {
        // Validate authorization, paging/search/sort, and the referenced run before reporting the
        // downstream contract dependency. This preserves stable 400/401/403/404 semantics.
        currentAdmin();
        int safePage = validatePage(page);
        int safeSize = validateSize(size);
        validateOffset(safePage, safeSize);
        validateSearch(search);
        CandidateSort.fromApiValue(sort);
        loadRun(filterRunId);

        throw new FilterDependencyUnavailableException(
                "Candidate CV and shortlist enrichment data is not available from authoritative persistence.");
    }

    /**
     * Returns the authoritative BMD-010 portion of a candidate page.
     *
     * <p>This method intentionally returns a core application model rather than the final public
     * candidate DTO because CV and cross-shortlist enrichment have no authoritative persistence yet.
     * Patch 6 can enrich this core model without changing the deterministic filtering query.
     */
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public PagedResponse<CandidateFilteringCandidateCore> listCandidateCore(
            UUID filterRunId,
            Integer page,
            Integer size,
            String search,
            String sort) {
        currentAdmin();
        int safePage = validatePage(page);
        int safeSize = validateSize(size);
        validateOffset(safePage, safeSize);
        String safeSearch = validateSearch(search);
        CandidateSort safeSort = CandidateSort.fromApiValue(sort);

        LoadedRun loadedRun = loadRun(filterRunId);
        Page<CandidateFilterRow> candidatePage = dependencyExecutor.execute(
                "candidate_page",
                () -> readRepository.searchCandidates(
                        loadedRun.criteria(), safeSearch, safePage, safeSize, safeSort));

        List<UUID> studentIds = candidatePage.getContent().stream()
                .map(CandidateFilterRow::studentId)
                .toList();
        List<CandidateMatchingSkillRow> matchingSkills = dependencyExecutor.execute(
                "matching_declared_skills",
                () -> readRepository.findMatchingDeclaredSkills(
                        studentIds,
                        loadedRun.criteria().selectedSkillIds()));
        Map<UUID, List<CandidateMatchingSkillRow>> skillsByStudent = matchingSkills.stream()
                .collect(Collectors.groupingBy(
                        CandidateMatchingSkillRow::studentId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        Page<CandidateFilteringCandidateCore> mappedPage = candidatePage.map(row -> mapper.toCandidateCore(
                row,
                skillsByStudent.getOrDefault(row.studentId(), List.of())));

        LOGGER.info(
                "Candidate Filtering candidates queried filterRunId={} page={} size={} totalElements={} "
                        + "selectedSkillCount={} correlationId={}",
                loadedRun.entity().getId(),
                safePage,
                safeSize,
                mappedPage.getTotalElements(),
                loadedRun.criteria().selectedSkillCount(),
                correlationId());
        return PagedResponse.of(mappedPage, safeSort.apiValue());
    }

    private LoadedRun loadRun(UUID filterRunId) {
        if (filterRunId == null) {
            throw new BadRequestException("filterRunId is required.");
        }
        FilterRunEntity entity = dependencyExecutor.execute(
                        "load_run",
                        () -> runRepository.findById(filterRunId))
                .orElseThrow(() -> {
                    LOGGER.warn(
                            "Candidate Filtering run not found filterRunId={} correlationId={}",
                            filterRunId,
                            correlationId());
                    return new FilterRunNotFoundException();
                });
        List<FilterRunSkillEntity> skillEntities = dependencyExecutor.execute(
                "load_run_skills",
                () -> runSkillRepository.findAllByFilterRunId(filterRunId));
        return new LoadedRun(entity, reconstructCriteria(entity, skillEntities));
    }

    private CandidateFilteringCriteria reconstructCriteria(
            FilterRunEntity entity,
            List<FilterRunSkillEntity> skillEntities) {
        Map<FilterSkillCriteriaSource, List<UUID>> skillIds = new EnumMap<>(FilterSkillCriteriaSource.class);
        for (FilterSkillCriteriaSource source : FilterSkillCriteriaSource.values()) {
            skillIds.put(source, skillEntities.stream()
                    .filter(skill -> skill.getCriteriaSource() == source)
                    .map(skill -> skill.getId().getSkillId())
                    .toList());
        }
        return new CandidateFilteringCriteria(
                entity.getInternshipRequestId(),
                entity.getRuntimeGpaLowerBound(),
                entity.getRuntimeGpaUpperBound(),
                skillIds.get(FilterSkillCriteriaSource.REQUEST),
                skillIds.get(FilterSkillCriteriaSource.ADDITIONAL),
                entity.getSkillMatchMode());
    }

    private CurrentActor currentAdmin() {
        CurrentActor actor = currentActorProvider.currentActor()
                .orElseThrow(() -> new UnauthorizedException("Authentication is required."));
        if (!actor.hasRole(RoleName.ADMIN)) {
            throw new ForbiddenException("The current account cannot access Candidate Filtering.");
        }
        return actor;
    }

    private int validatePage(Integer page) {
        int value = page == null ? DEFAULT_PAGE : page;
        if (value < 0) {
            throw new BadRequestException("page must be greater than or equal to 0.");
        }
        return value;
    }

    private int validateSize(Integer size) {
        int value = size == null ? DEFAULT_SIZE : size;
        if (value < 1 || value > MAX_SIZE) {
            throw new BadRequestException("size must be between 1 and 100.");
        }
        return value;
    }

    private void validateOffset(int page, int size) {
        if ((long) page * size > Integer.MAX_VALUE) {
            throw new BadRequestException("page is too large for bounded database pagination.");
        }
    }

    private String validateSearch(String search) {
        if (search == null) {
            return null;
        }
        String value = search.strip();
        if (value.isEmpty()) {
            return null;
        }
        if (value.length() > MAX_SEARCH_LENGTH) {
            throw new BadRequestException("search must not exceed 120 characters.");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private String correlationId() {
        return CorrelationIdContext.current().orElse("none");
    }

    private record LoadedRun(FilterRunEntity entity, CandidateFilteringCriteria criteria) {
    }
}
