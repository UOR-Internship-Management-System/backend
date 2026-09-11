package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.request.CandidateFilteringRunRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringRunResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.FilteringInternshipRequestNotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.InvalidFilterCriteriaException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.CandidateFilteringCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillCriteriaSource;
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
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.UnauthorizedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.CorrelationIdContext;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Application service that creates immutable deterministic Candidate Filtering runs. */
@Service
public class CandidateFilteringService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CandidateFilteringService.class);
    private static final String AUDIT_RESOURCE = "CANDIDATE_FILTER_RUN";

    private final FilterRunRepository runRepository;
    private final FilterRunSkillRepository runSkillRepository;
    private final CandidateFilteringReadRepository readRepository;
    private final CandidateFilteringMapper mapper;
    private final CurrentActorProvider currentActorProvider;
    private final AuditEventPublisher auditEventPublisher;
    private final CandidateFilteringDependencyExecutor dependencyExecutor;
    private final CandidateFilteringMetrics metrics;
    private final Clock clock;

    public CandidateFilteringService(
            FilterRunRepository runRepository,
            FilterRunSkillRepository runSkillRepository,
            CandidateFilteringReadRepository readRepository,
            CandidateFilteringMapper mapper,
            CurrentActorProvider currentActorProvider,
            AuditEventPublisher auditEventPublisher,
            CandidateFilteringDependencyExecutor dependencyExecutor,
            CandidateFilteringMetrics metrics,
            Clock clock) {
        this.runRepository = runRepository;
        this.runSkillRepository = runSkillRepository;
        this.readRepository = readRepository;
        this.mapper = mapper;
        this.currentActorProvider = currentActorProvider;
        this.auditEventPublisher = auditEventPublisher;
        this.dependencyExecutor = dependencyExecutor;
        this.metrics = metrics;
        this.clock = clock;
    }

    /**
     * Validates, persists, audits, and returns one immutable filtering run.
     *
     * <p>REPEATABLE_READ ensures request/skill validation and the initial candidate count observe a
     * consistent committed snapshot. The required audit write participates in the same transaction;
     * an audit persistence failure therefore rolls back both run tables.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CandidateFilteringRunResponse createRun(CandidateFilteringRunRequest request) {
        CurrentActor actor = currentAdmin();
        if (request == null) {
            throw new InvalidFilterCriteriaException("Candidate Filtering request body is required.");
        }

        CandidateFilteringCriteria criteria = mapper.toCriteria(request);
        FilterRequestSummaryRow requestSummary = dependencyExecutor.execute(
                        "request_context",
                        () -> readRepository.findRequestSummary(criteria.requestId()))
                .orElseThrow(() -> {
                    LOGGER.warn(
                            "Candidate Filtering request context not found requestId={} correlationId={}",
                            criteria.requestId(),
                            correlationId());
                    return new FilteringInternshipRequestNotFoundException();
                });

        validateDatabaseCriteria(criteria);

        FilterRunEntity run = new FilterRunEntity();
        run.setId(UUID.randomUUID());
        run.setInternshipRequestId(criteria.requestId());
        run.setRunByAccountId(actor.userId());
        run.setRuntimeGpaLowerBound(criteria.runtimeGpaLowerBound());
        run.setRuntimeGpaUpperBound(criteria.runtimeGpaUpperBound());
        run.setSkillMatchMode(criteria.skillMatchMode());
        run.setCreatedAt(OffsetDateTime.now(clock));

        FilterRunEntity saved = dependencyExecutor.execute(
                "persist_run",
                () -> runRepository.saveAndFlush(run));
        persistRunSkills(saved.getId(), criteria);

        long candidateCount = dependencyExecutor.execute(
                "candidate_count",
                () -> readRepository.countCandidates(criteria));

        try {
            dependencyExecutor.execute("audit_write", () -> {
                auditEventPublisher.recordRequired(
                        actor.userId(),
                        RoleName.ADMIN.name(),
                        AuditEventType.CANDIDATE_FILTER_RUN_CREATED.name(),
                        AuditEventCategory.CANDIDATE_FILTERING,
                        AUDIT_RESOURCE,
                        saved.getId().toString(),
                        auditMetadata(saved.getId(), criteria, candidateCount));
                return null;
            });
        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Candidate Filtering required audit write failed filterRunId={} exceptionType={} correlationId={}",
                    saved.getId(),
                    exception.getClass().getSimpleName(),
                    correlationId());
            throw exception;
        }

        metrics.runCreated(candidateCount);
        LOGGER.info(
                "Candidate Filtering run created filterRunId={} requestId={} requestSkillCount={} "
                        + "additionalSkillCount={} candidateCount={} correlationId={}",
                saved.getId(),
                criteria.requestId(),
                criteria.requestSkillIds().size(),
                criteria.additionalSkillIds().size(),
                candidateCount,
                correlationId());

        return mapper.toRunResponse(saved, requestSummary, criteria, candidateCount);
    }

    private void validateDatabaseCriteria(CandidateFilteringCriteria criteria) {
        Set<UUID> submittedRequestSkills = Set.copyOf(criteria.requestSkillIds());
        Set<UUID> validRequestSkills = dependencyExecutor.execute(
                "request_skill_validation",
                () -> readRepository.findRequiredSkillIds(criteria.requestId(), criteria.requestSkillIds()));
        if (!validRequestSkills.equals(submittedRequestSkills)) {
            throw new InvalidFilterCriteriaException(
                    "requestSkillIds contains skills that are not required by the selected internship request.");
        }

        Set<UUID> submittedAdditionalSkills = Set.copyOf(criteria.additionalSkillIds());
        Set<UUID> activeAdditionalSkills = dependencyExecutor.execute(
                "additional_skill_validation",
                () -> readRepository.findActiveSkillIds(criteria.additionalSkillIds()));
        if (!activeAdditionalSkills.equals(submittedAdditionalSkills)) {
            throw new InvalidFilterCriteriaException(
                    "additionalSkillIds contains skills that are missing or inactive in the canonical taxonomy.");
        }

        Set<UUID> requestRequiredAdditionalSkills = dependencyExecutor.execute(
                "additional_request_overlap_validation",
                () -> readRepository.findRequiredSkillIds(criteria.requestId(), criteria.additionalSkillIds()));
        if (!requestRequiredAdditionalSkills.isEmpty()) {
            throw new InvalidFilterCriteriaException(
                    "additionalSkillIds must not contain skills already required by the selected internship request.");
        }
    }

    private void persistRunSkills(UUID runId, CandidateFilteringCriteria criteria) {
        List<FilterRunSkillEntity> entities = new ArrayList<>(criteria.selectedSkillCount());
        criteria.requestSkillIds().forEach(skillId -> entities.add(
                new FilterRunSkillEntity(runId, skillId, FilterSkillCriteriaSource.REQUEST)));
        criteria.additionalSkillIds().forEach(skillId -> entities.add(
                new FilterRunSkillEntity(runId, skillId, FilterSkillCriteriaSource.ADDITIONAL)));

        if (!entities.isEmpty()) {
            dependencyExecutor.execute("persist_run_skills", () -> runSkillRepository.saveAllAndFlush(entities));
        }
    }

    private Map<String, Object> auditMetadata(
            UUID filterRunId,
            CandidateFilteringCriteria criteria,
            long candidateCount) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("filterRunId", filterRunId.toString());
        metadata.put("requestId", criteria.requestId().toString());
        if (criteria.runtimeGpaLowerBound() != null) {
            metadata.put("runtimeGpaLowerBound", criteria.runtimeGpaLowerBound());
        }
        if (criteria.runtimeGpaUpperBound() != null) {
            metadata.put("runtimeGpaUpperBound", criteria.runtimeGpaUpperBound());
        }
        metadata.put("skillMatchMode", criteria.skillMatchMode().name());
        metadata.put("requestSkillCount", criteria.requestSkillIds().size());
        metadata.put("additionalSkillCount", criteria.additionalSkillIds().size());
        metadata.put("candidateCount", candidateCount);
        return Map.copyOf(metadata);
    }

    private CurrentActor currentAdmin() {
        CurrentActor actor = currentActorProvider.currentActor()
                .orElseThrow(() -> new UnauthorizedException("Authentication is required."));
        if (!actor.hasRole(RoleName.ADMIN)) {
            throw new ForbiddenException("The current account cannot access Candidate Filtering.");
        }
        return actor;
    }

    private String correlationId() {
        return CorrelationIdContext.current().orElse("none");
    }
}
