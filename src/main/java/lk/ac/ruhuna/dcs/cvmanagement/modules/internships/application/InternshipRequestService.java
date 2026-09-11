package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequestCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequestSearchCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequestUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequiredSkillRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.response.InternshipRequestResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.response.InternshipRequiredSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.domain.exception.DuplicateRequiredSkillException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.domain.exception.InternshipCompanyNotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.domain.exception.InternshipRequestNotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.domain.exception.InvalidTaxonomySkillException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.domain.policy.InternshipRequestSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.mapper.InternshipRequestMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.entity.InternshipRequestEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.entity.InternshipRequestSkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection.InternshipRequestDetailProjection;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection.InternshipRequiredSkillProjection;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.projection.SkillSnapshotProjection;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.repository.InternshipReferenceQuery;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.repository.InternshipRequestQueryRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.repository.InternshipRequestRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.persistence.repository.InternshipRequestSkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventType;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.PreconditionFailedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.UnauthorizedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ValidationException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for Admin Internship Request and required-skill management. */
@Service
public class InternshipRequestService {

    private static final String AUDIT_RESOURCE = "INTERNSHIP_REQUEST";
    private static final String REQUEST_COMPANY_FK = "fk_internship_requests_company";
    private static final String REQUEST_SKILL_FK = "fk_internship_request_skills_skill";
    private static final String REQUEST_SKILL_UNIQUE = "uq_internship_request_skills_request_skill";

    private final InternshipRequestRepository requestRepository;
    private final InternshipRequestSkillRepository skillRepository;
    private final InternshipRequestQueryRepository queryRepository;
    private final InternshipReferenceQuery referenceQuery;
    private final InternshipRequestMapper mapper;
    private final CurrentActorProvider currentActorProvider;
    private final AuditEventPublisher auditEventPublisher;
    private final Clock clock;

    public InternshipRequestService(
            InternshipRequestRepository requestRepository,
            InternshipRequestSkillRepository skillRepository,
            InternshipRequestQueryRepository queryRepository,
            InternshipReferenceQuery referenceQuery,
            InternshipRequestMapper mapper,
            CurrentActorProvider currentActorProvider,
            AuditEventPublisher auditEventPublisher,
            Clock clock) {
        this.requestRepository = requestRepository;
        this.skillRepository = skillRepository;
        this.queryRepository = queryRepository;
        this.referenceQuery = referenceQuery;
        this.mapper = mapper;
        this.currentActorProvider = currentActorProvider;
        this.auditEventPublisher = auditEventPublisher;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PagedResponse<InternshipRequestResponse> list(InternshipRequestSearchCriteria criteria) {
        currentAdmin();
        InternshipRequestSearchCriteria safe = criteria == null
                ? new InternshipRequestSearchCriteria(null, null, null, null, null)
                : criteria;
        int page = validatePage(safe.page());
        int size = validateSize(safe.size());
        validateOffset(page, size);
        String search = validateSearch(safe.search());
        InternshipRequestSort sort = InternshipRequestSort.fromApiValue(safe.sort());

        Page<InternshipRequestDetailProjection> requestPage =
                queryRepository.search(search, safe.companyId(), page, size, sort);
        Map<UUID, List<InternshipRequiredSkillProjection>> skills = groupSkills(
                queryRepository.findRequiredSkills(requestPage.getContent().stream()
                        .map(InternshipRequestDetailProjection::requestId)
                        .toList()));
        return PagedResponse.of(
                requestPage.map(row -> mapper.toResponse(row, skills.getOrDefault(row.requestId(), List.of()))),
                sort.apiValue());
    }

    @Transactional(readOnly = true)
    public InternshipRequestResponse get(UUID requestId) {
        currentAdmin();
        return readResponse(requestId);
    }

    @Transactional
    public InternshipRequestResponse create(InternshipRequestCreateRequest request) {
        CurrentActor actor = currentAdmin();
        if (request == null) {
            throw new ValidationException("Internship request body is required.");
        }
        if (request.companyId() == null) {
            throw new ValidationException("companyId is required.");
        }
        if (referenceQuery.findCompany(request.companyId()).isEmpty()) {
            throw new InternshipCompanyNotFoundException();
        }
        ValidatedSkills validatedSkills = validateSkills(request.requiredSkills());

        OffsetDateTime now = OffsetDateTime.now(clock);
        InternshipRequestEntity entity = new InternshipRequestEntity();
        entity.setId(UUID.randomUUID());
        entity.setCompanyId(request.companyId());
        entity.setTitle(normalizeTitle(request.title()));
        entity.setDescription(normalizeNullable(request.description(), 10000, "description"));
        entity.setShortlistGuidanceValue(validateGuidance(request.shortlistGuidanceValue()));
        entity.setCreatedByAccountId(actor.userId());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        try {
            requestRepository.saveAndFlush(entity);
            persistSkills(entity.getId(), validatedSkills.skillIds(), now);
        } catch (DataIntegrityViolationException exception) {
            if (containsConstraint(exception, REQUEST_COMPANY_FK)) {
                throw new InternshipCompanyNotFoundException();
            }
            if (containsConstraint(exception, REQUEST_SKILL_FK)) {
                throw new InvalidTaxonomySkillException();
            }
            throw exception;
        }

        auditEventPublisher.recordRequired(
                actor.userId(), RoleName.ADMIN.name(), AuditEventType.INTERNSHIP_REQUEST_CREATED.name(),
                AuditEventCategory.INTERNSHIP_MANAGEMENT, AUDIT_RESOURCE, entity.getId().toString(),
                Map.of("requestId", entity.getId().toString(), "companyId", entity.getCompanyId().toString(),
                        "requiredSkillCount", validatedSkills.skillIds().size()));
        return readResponse(entity.getId());
    }

    @Transactional
    public InternshipRequestResponse update(UUID requestId, InternshipRequestUpdateRequest request, long expectedVersion) {
        CurrentActor actor = currentAdmin();
        if (request == null || !request.hasAnyField()) {
            throw new ValidationException("Change at least one internship request field.");
        }
        InternshipRequestEntity entity = findRequest(requestId);
        assertVersion(entity, expectedVersion);

        String normalizedTitle = request.hasTitle() ? normalizeTitle(request.title()) : null;
        String normalizedDescription = request.hasDescription()
                ? normalizeNullable(request.description(), 10000, "description")
                : null;
        Integer normalizedGuidance = request.hasShortlistGuidanceValue()
                ? validateGuidance(request.shortlistGuidanceValue())
                : null;
        ValidatedSkills replacement = request.hasRequiredSkills()
                ? validateSkills(request.requiredSkills())
                : null;

        // Bulk skill deletion triggers an automatic persistence-context flush. Replace the
        // associations before making the managed parent dirty so its @Version advances only once.
        if (replacement != null) {
            replaceSkills(entity.getId(), replacement.skillIds());
        }

        Set<String> changedFields = new HashSet<>();
        if (request.hasTitle()) {
            entity.setTitle(normalizedTitle);
            changedFields.add("title");
        }
        if (request.hasDescription()) {
            entity.setDescription(normalizedDescription);
            changedFields.add("description");
        }
        if (request.hasShortlistGuidanceValue()) {
            entity.setShortlistGuidanceValue(normalizedGuidance);
            changedFields.add("shortlistGuidanceValue");
        }
        if (replacement != null) {
            changedFields.add("requiredSkills");
        }

        entity.setUpdatedAt(OffsetDateTime.now(clock));
        requestRepository.saveAndFlush(entity);
        auditEventPublisher.recordRequired(
                actor.userId(), RoleName.ADMIN.name(), AuditEventType.INTERNSHIP_REQUEST_UPDATED.name(),
                AuditEventCategory.INTERNSHIP_MANAGEMENT, AUDIT_RESOURCE, entity.getId().toString(),
                Map.of("requestId", entity.getId().toString(), "changedFields", Set.copyOf(changedFields)));
        if (replacement != null) {
            auditEventPublisher.recordRequired(
                    actor.userId(), RoleName.ADMIN.name(), AuditEventType.INTERNSHIP_REQUIRED_SKILLS_REPLACED.name(),
                    AuditEventCategory.INTERNSHIP_MANAGEMENT, AUDIT_RESOURCE, entity.getId().toString(),
                    Map.of("requestId", entity.getId().toString(), "requiredSkillCount", replacement.skillIds().size()));
        }
        return readResponse(entity.getId());
    }

    @Transactional
    public void delete(UUID requestId, long expectedVersion) {
        CurrentActor actor = currentAdmin();
        InternshipRequestEntity entity = findRequest(requestId);
        assertVersion(entity, expectedVersion);
        auditEventPublisher.recordRequired(
                actor.userId(), RoleName.ADMIN.name(), AuditEventType.INTERNSHIP_REQUEST_DELETED.name(),
                AuditEventCategory.INTERNSHIP_MANAGEMENT, AUDIT_RESOURCE, entity.getId().toString(),
                Map.of("requestId", entity.getId().toString(), "companyId", entity.getCompanyId().toString()));
        requestRepository.delete(entity);
        requestRepository.flush();
    }

    @Transactional(readOnly = true)
    public PagedResponse<InternshipRequiredSkillResponse> listRequiredSkills(UUID requestId, Integer pageValue, Integer sizeValue) {
        currentAdmin();
        findRequest(requestId);
        int page = validatePage(pageValue);
        int size = validateSize(sizeValue);
        validateOffset(page, size);
        return PagedResponse.of(queryRepository.findRequiredSkills(requestId, page, size).map(mapper::toSkillResponse),
                "skillName,asc");
    }

    @Transactional
    public RequiredSkillMutationResult addRequiredSkill(
            UUID requestId, InternshipRequiredSkillRequest request, long expectedVersion) {
        CurrentActor actor = currentAdmin();
        InternshipRequestEntity parent = findRequest(requestId);
        assertVersion(parent, expectedVersion);
        if (request == null) {
            throw new ValidationException("Required skill request body is required.");
        }
        ValidatedSkills validated = validateSkills(List.of(request));
        UUID skillId = validated.skillIds().getFirst();
        if (skillRepository.existsByInternshipRequestIdAndSkillId(requestId, skillId)) {
            throw new DuplicateRequiredSkillException();
        }

        InternshipRequestSkillEntity association = new InternshipRequestSkillEntity();
        association.setId(UUID.randomUUID());
        association.setInternshipRequestId(requestId);
        association.setSkillId(skillId);
        association.setCreatedAt(OffsetDateTime.now(clock));
        try {
            skillRepository.saveAndFlush(association);
        } catch (DataIntegrityViolationException exception) {
            if (containsConstraint(exception, REQUEST_SKILL_UNIQUE)) {
                throw new DuplicateRequiredSkillException();
            }
            if (containsConstraint(exception, REQUEST_SKILL_FK)) {
                throw new InvalidTaxonomySkillException();
            }
            throw exception;
        }
        parent.setUpdatedAt(OffsetDateTime.now(clock));
        InternshipRequestEntity savedParent = requestRepository.saveAndFlush(parent);

        auditEventPublisher.recordRequired(
                actor.userId(), RoleName.ADMIN.name(), AuditEventType.INTERNSHIP_REQUIRED_SKILL_ADDED.name(),
                AuditEventCategory.INTERNSHIP_MANAGEMENT, AUDIT_RESOURCE, requestId.toString(),
                Map.of("requestId", requestId.toString(), "skillId", skillId.toString()));
        InternshipRequiredSkillProjection projection = queryRepository.findRequiredSkill(association.getId(), requestId)
                .orElseThrow(IllegalStateException::new);
        return new RequiredSkillMutationResult(mapper.toSkillResponse(projection), safeVersion(savedParent));
    }

    @Transactional
    public void removeRequiredSkill(UUID requestId, UUID requiredSkillId, long expectedVersion) {
        CurrentActor actor = currentAdmin();
        InternshipRequestEntity parent = findRequest(requestId);
        assertVersion(parent, expectedVersion);
        var association = skillRepository.findByIdAndInternshipRequestId(requiredSkillId, requestId);
        if (association.isEmpty()) {
            return; // DELETE is idempotent once the parent request itself is known to exist.
        }
        UUID skillId = association.get().getSkillId();
        skillRepository.delete(association.get());
        skillRepository.flush();
        parent.setUpdatedAt(OffsetDateTime.now(clock));
        requestRepository.saveAndFlush(parent);
        auditEventPublisher.recordRequired(
                actor.userId(), RoleName.ADMIN.name(), AuditEventType.INTERNSHIP_REQUIRED_SKILL_REMOVED.name(),
                AuditEventCategory.INTERNSHIP_MANAGEMENT, AUDIT_RESOURCE, requestId.toString(),
                Map.of("requestId", requestId.toString(), "skillId", skillId.toString()));
    }

    private InternshipRequestResponse readResponse(UUID requestId) {
        InternshipRequestDetailProjection detail = queryRepository.findDetail(requestId)
                .orElseThrow(InternshipRequestNotFoundException::new);
        return mapper.toResponse(detail, queryRepository.findRequiredSkills(List.of(requestId)));
    }

    private InternshipRequestEntity findRequest(UUID requestId) {
        if (requestId == null) {
            throw new InternshipRequestNotFoundException();
        }
        return requestRepository.findById(requestId).orElseThrow(InternshipRequestNotFoundException::new);
    }

    private ValidatedSkills validateSkills(List<InternshipRequiredSkillRequest> requestedSkills) {
        if (requestedSkills == null) {
            throw new ValidationException("requiredSkills must be an array.");
        }
        if (requestedSkills.size() > 100) {
            throw new ValidationException("At most 100 required skills may be selected.");
        }
        List<UUID> ids = requestedSkills.stream()
                .map(skill -> skill == null ? null : skill.skillId())
                .toList();
        if (ids.stream().anyMatch(java.util.Objects::isNull)) {
            throw new ValidationException("Every required skill must contain a skillId.");
        }
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new ValidationException("Select each required skill only once.");
        }
        Map<UUID, SkillSnapshotProjection> snapshots = referenceQuery.findSkills(ids);
        if (snapshots.size() != ids.size() || ids.stream().anyMatch(id -> !snapshots.get(id).selectable())) {
            throw new InvalidTaxonomySkillException();
        }
        return new ValidatedSkills(List.copyOf(ids));
    }

    private void persistSkills(UUID requestId, List<UUID> skillIds, OffsetDateTime now) {
        if (skillIds.isEmpty()) {
            return;
        }
        List<InternshipRequestSkillEntity> entities = skillIds.stream().map(skillId -> {
            InternshipRequestSkillEntity entity = new InternshipRequestSkillEntity();
            entity.setId(UUID.randomUUID());
            entity.setInternshipRequestId(requestId);
            entity.setSkillId(skillId);
            entity.setCreatedAt(now);
            return entity;
        }).toList();
        skillRepository.saveAllAndFlush(entities);
    }

    private void replaceSkills(UUID requestId, List<UUID> skillIds) {
        skillRepository.deleteAllByInternshipRequestId(requestId);
        skillRepository.flush();
        try {
            persistSkills(requestId, skillIds, OffsetDateTime.now(clock));
        } catch (DataIntegrityViolationException exception) {
            if (containsConstraint(exception, REQUEST_SKILL_FK)) {
                throw new InvalidTaxonomySkillException();
            }
            throw exception;
        }
    }

    private CurrentActor currentAdmin() {
        CurrentActor actor = currentActorProvider.currentActor()
                .orElseThrow(() -> new UnauthorizedException("Authentication is required."));
        if (!actor.hasRole(RoleName.ADMIN)) {
            throw new ForbiddenException("The current account cannot access Internship Request management.");
        }
        return actor;
    }

    private void assertVersion(InternshipRequestEntity entity, long expectedVersion) {
        if (safeVersion(entity) != expectedVersion) {
            throw new PreconditionFailedException(
                    "Internship request data changed since it was loaded. Reload the latest version and try again.");
        }
    }

    private long safeVersion(InternshipRequestEntity entity) {
        return entity.getVersion() == null ? 0L : entity.getVersion();
    }

    private String normalizeTitle(String value) {
        if (value == null || value.strip().isEmpty()) {
            throw new ValidationException("Role title is required.");
        }
        String normalized = value.strip();
        if (normalized.codePointCount(0, normalized.length()) > 200) {
            throw new ValidationException("Role title must not exceed 200 characters.");
        }
        return normalized;
    }

    private String normalizeNullable(String value, int maximumCodePoints, String field) {
        if (value == null) return null;
        String normalized = value.strip();
        if (normalized.isEmpty()) return null;
        if (normalized.codePointCount(0, normalized.length()) > maximumCodePoints) {
            throw new ValidationException(field + " exceeds the maximum supported length.");
        }
        return normalized;
    }

    private Integer validateGuidance(Integer value) {
        if (value != null && (value < 0 || value > 10000)) {
            throw new ValidationException("shortlistGuidanceValue must be between 0 and 10000.");
        }
        return value;
    }

    private int validatePage(Integer value) {
        int page = value == null ? InternshipRequestSearchCriteria.DEFAULT_PAGE : value;
        if (page < 0) throw new ValidationException("page must be greater than or equal to 0.");
        return page;
    }

    private int validateSize(Integer value) {
        int size = value == null ? InternshipRequestSearchCriteria.DEFAULT_SIZE : value;
        if (size < 1 || size > InternshipRequestSearchCriteria.MAX_SIZE) {
            throw new ValidationException("size must be between 1 and 100.");
        }
        return size;
    }

    private void validateOffset(int page, int size) {
        if ((long) page * size > Integer.MAX_VALUE) {
            throw new ValidationException("page is too large for bounded database pagination.");
        }
    }

    private String validateSearch(String search) {
        if (search == null) return null;
        String normalized = search.strip();
        if (normalized.isEmpty() || normalized.codePointCount(0, normalized.length()) > 120) {
            throw new ValidationException("search must contain between 1 and 120 characters.");
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private boolean containsConstraint(Throwable throwable, String constraint) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase(Locale.ROOT).contains(constraint.toLowerCase(Locale.ROOT))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private Map<UUID, List<InternshipRequiredSkillProjection>> groupSkills(
            Collection<InternshipRequiredSkillProjection> skills) {
        return skills.stream().collect(Collectors.groupingBy(
                InternshipRequiredSkillProjection::requestId,
                java.util.LinkedHashMap::new,
                Collectors.toList()));
    }

    private record ValidatedSkills(List<UUID> skillIds) {
    }

    public record RequiredSkillMutationResult(InternshipRequiredSkillResponse skill, long requestVersion) {
    }
}
