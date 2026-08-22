package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.request.ShortlistCandidateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.request.ShortlistCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.ShortlistCandidateMutationResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.ShortlistCandidateResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.ShortlistDetailResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.ShortlistResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.domain.policy.ShortlistStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.mapper.ShortlistMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.entity.ShortlistCandidateEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.entity.ShortlistEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.query.ShortlistReadRepository;
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
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Admin-only application service for shortlist creation, reads and draft membership. */
@Service
public class ShortlistService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final int MAX_SEARCH_LENGTH = 120;
    private static final String AUDIT_RESOURCE = "SHORTLIST";

    private final ShortlistRepository shortlistRepository;
    private final ShortlistCandidateRepository candidateRepository;
    private final ShortlistReadRepository readRepository;
    private final ShortlistMapper mapper;
    private final CurrentActorProvider currentActorProvider;
    private final AuditEventPublisher auditEventPublisher;
    private final Clock clock;

    public ShortlistService(
            ShortlistRepository shortlistRepository,
            ShortlistCandidateRepository candidateRepository,
            ShortlistReadRepository readRepository,
            ShortlistMapper mapper,
            CurrentActorProvider currentActorProvider,
            AuditEventPublisher auditEventPublisher,
            Clock clock) {
        this.shortlistRepository = shortlistRepository;
        this.candidateRepository = candidateRepository;
        this.readRepository = readRepository;
        this.mapper = mapper;
        this.currentActorProvider = currentActorProvider;
        this.auditEventPublisher = auditEventPublisher;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ShortlistResponse> list(
            Integer page,
            Integer size,
            String search,
            String sort,
            ShortlistStatus status,
            UUID companyId) {
        currentAdmin();
        int safePage = validatePage(page);
        int safeSize = validateSize(size);
        validateOffset(safePage, safeSize);
        String safeSearch = validateSearch(search);
        ShortlistSort safeSort = ShortlistSort.from(sort);
        Page<ShortlistResponse> result = readRepository
                .searchShortlists(safeSearch, status, companyId, safePage, safeSize, safeSort.orderBy())
                .map(mapper::toResponse);
        return PagedResponse.of(result, safeSort.apiValue());
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ShortlistDetailResponse getDetail(
            UUID shortlistId,
            Integer candidatePage,
            Integer candidateSize,
            String candidateSearch,
            String candidateSort) {
        currentAdmin();
        int safePage = validatePage(candidatePage);
        int safeSize = validateSize(candidateSize == null ? 100 : candidateSize);
        validateOffset(safePage, safeSize);
        String safeSearch = validateSearch(candidateSearch);
        CandidateSort safeSort = CandidateSort.from(candidateSort);
        ShortlistResponse shortlist = readResponse(shortlistId);
        Page<ShortlistCandidateResponse> candidates = readRepository
                .searchCandidates(shortlistId, safeSearch, safePage, safeSize, safeSort.orderBy())
                .map(mapper::toCandidateResponse);
        return new ShortlistDetailResponse(shortlist, PagedResponse.of(candidates, safeSort.apiValue()));
    }

    @Transactional
    public ShortlistResponse create(ShortlistCreateRequest request) {
        CurrentActor actor = currentAdmin();
        if (request == null || request.requestId() == null) {
            throw new ValidationException("requestId is required.");
        }
        var requestContext = readRepository.findRequest(request.requestId())
                .orElseThrow(() -> new NotFoundException("Internship request was not found."));
        if (request.filterRunId() != null
                && !readRepository.filterRunMatchesRequest(request.filterRunId(), request.requestId())) {
            throw new ValidationException("filterRunId must belong to the selected Internship Request.");
        }
        if (shortlistRepository.existsByInternshipRequestId(request.requestId())) {
            throw new ConflictException("A shortlist already exists for this Internship Request.");
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        ShortlistEntity entity = new ShortlistEntity();
        entity.setId(UUID.randomUUID());
        entity.setInternshipRequestId(request.requestId());
        entity.setFilterRunId(request.filterRunId());
        entity.setName(normalizeNullable(request.name(), 200, "name"));
        entity.setStatus(ShortlistStatus.DRAFT);
        entity.setGuidanceValueSnapshot(requestContext.guidanceValue());
        entity.setGuidanceWarningAcknowledged(false);
        entity.setCreatedByAccountId(actor.userId());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        try {
            shortlistRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException exception) {
            if (containsConstraint(exception, "uq_shortlists_internship_request")) {
                throw new ConflictException("A shortlist already exists for this Internship Request.");
            }
            throw exception;
        }

        auditEventPublisher.recordRequired(
                actor.userId(),
                RoleName.ADMIN.name(),
                AuditEventType.SHORTLIST_CREATED.name(),
                AuditEventCategory.SHORTLIST_MANAGEMENT,
                AUDIT_RESOURCE,
                entity.getId().toString(),
                Map.of(
                        "shortlistId", entity.getId().toString(),
                        "requestId", entity.getInternshipRequestId().toString()));
        return readResponse(entity.getId());
    }

    @Transactional
    public ShortlistCandidateMutationResponse addCandidates(
            UUID shortlistId,
            ShortlistCandidateRequest request,
            long expectedVersion) {
        CurrentActor actor = currentAdmin();
        ShortlistEntity shortlist = findLocked(shortlistId);
        assertMutable(shortlist, expectedVersion);
        List<UUID> studentIds = validateStudentIds(request);
        Set<UUID> activeIds = readRepository.findActiveStudentIds(studentIds);
        if (activeIds.size() != studentIds.size()) {
            throw new ValidationException("Every studentId must identify an active eligible Student.");
        }

        Set<UUID> existingIds = candidateRepository
                .findAllByShortlistIdAndStudentIdIn(shortlistId, studentIds)
                .stream()
                .map(ShortlistCandidateEntity::getStudentId)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        OffsetDateTime now = OffsetDateTime.now(clock);
        String note = normalizeNullable(request.note(), 1000, "note");
        List<ShortlistCandidateEntity> additions = studentIds.stream()
                .filter(studentId -> !existingIds.contains(studentId))
                .map(studentId -> newCandidate(shortlistId, studentId, actor.userId(), now, note))
                .toList();
        if (!additions.isEmpty()) {
            candidateRepository.saveAllAndFlush(additions);
            shortlist.setUpdatedAt(now);
            shortlistRepository.saveAndFlush(shortlist);
            auditEventPublisher.recordRequired(
                    actor.userId(),
                    RoleName.ADMIN.name(),
                    AuditEventType.SHORTLIST_CANDIDATES_ADDED.name(),
                    AuditEventCategory.SHORTLIST_MANAGEMENT,
                    AUDIT_RESOURCE,
                    shortlistId.toString(),
                    Map.of(
                            "shortlistId", shortlistId.toString(),
                            "addedCount", additions.size()));
        }
        long count = candidateRepository.countByShortlistId(shortlistId);
        return mutationResponse(
                shortlist,
                additions.size(),
                existingIds.size(),
                0,
                count);
    }

    @Transactional
    public ShortlistCandidateMutationResponse removeCandidate(
            UUID shortlistId,
            UUID studentId,
            long expectedVersion) {
        CurrentActor actor = currentAdmin();
        if (studentId == null) {
            throw new ValidationException("studentId is required.");
        }
        ShortlistEntity shortlist = findLocked(shortlistId);
        assertMutable(shortlist, expectedVersion);
        var candidate = candidateRepository.findByShortlistIdAndStudentId(shortlistId, studentId);
        int removed = 0;
        if (candidate.isPresent()) {
            candidateRepository.delete(candidate.get());
            candidateRepository.flush();
            shortlist.setUpdatedAt(OffsetDateTime.now(clock));
            shortlistRepository.saveAndFlush(shortlist);
            removed = 1;
            auditEventPublisher.recordRequired(
                    actor.userId(),
                    RoleName.ADMIN.name(),
                    AuditEventType.SHORTLIST_CANDIDATE_REMOVED.name(),
                    AuditEventCategory.SHORTLIST_MANAGEMENT,
                    AUDIT_RESOURCE,
                    shortlistId.toString(),
                    Map.of(
                            "shortlistId", shortlistId.toString(),
                            "studentId", studentId.toString()));
        }
        long count = candidateRepository.countByShortlistId(shortlistId);
        return mutationResponse(shortlist, 0, 0, removed, count);
    }

    private ShortlistResponse readResponse(UUID shortlistId) {
        if (shortlistId == null) {
            throw new ValidationException("shortlistId is required.");
        }
        return readRepository.findSummary(shortlistId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Shortlist was not found."));
    }

    private ShortlistEntity findLocked(UUID shortlistId) {
        if (shortlistId == null) {
            throw new ValidationException("shortlistId is required.");
        }
        return shortlistRepository.findByIdForUpdate(shortlistId)
                .orElseThrow(() -> new NotFoundException("Shortlist was not found."));
    }

    private void assertMutable(ShortlistEntity shortlist, long expectedVersion) {
        long actualVersion = safeVersion(shortlist);
        if (actualVersion != expectedVersion) {
            throw new PreconditionFailedException("The shortlist changed after it was loaded.");
        }
        if (shortlist.getStatus() != ShortlistStatus.DRAFT) {
            throw new ConflictException("A finalized shortlist cannot be changed.");
        }
    }

    private List<UUID> validateStudentIds(ShortlistCandidateRequest request) {
        if (request == null || request.studentIds() == null || request.studentIds().isEmpty()) {
            throw new ValidationException("Select at least one Student.");
        }
        if (request.studentIds().size() > 100) {
            throw new ValidationException("Select no more than 100 Students per request.");
        }
        if (request.studentIds().stream().anyMatch(java.util.Objects::isNull)) {
            throw new ValidationException("studentIds must not contain null values.");
        }
        if (new HashSet<>(request.studentIds()).size() != request.studentIds().size()) {
            throw new ValidationException("Select each Student only once.");
        }
        return List.copyOf(request.studentIds());
    }

    private ShortlistCandidateEntity newCandidate(
            UUID shortlistId,
            UUID studentId,
            UUID actorId,
            OffsetDateTime selectedAt,
            String note) {
        ShortlistCandidateEntity entity = new ShortlistCandidateEntity();
        entity.setId(UUID.randomUUID());
        entity.setShortlistId(shortlistId);
        entity.setStudentId(studentId);
        entity.setSelectedByAccountId(actorId);
        entity.setSelectedAt(selectedAt);
        entity.setSelectionNote(note);
        return entity;
    }

    private ShortlistCandidateMutationResponse mutationResponse(
            ShortlistEntity shortlist,
            int added,
            int alreadyPresent,
            int removed,
            long selectedCount) {
        boolean exceeded = shortlist.getGuidanceValueSnapshot() != null
                && selectedCount > shortlist.getGuidanceValueSnapshot();
        return new ShortlistCandidateMutationResponse(
                shortlist.getId(),
                added,
                alreadyPresent,
                removed,
                selectedCount,
                exceeded,
                safeVersion(shortlist));
    }

    private CurrentActor currentAdmin() {
        CurrentActor actor = currentActorProvider.currentActor()
                .orElseThrow(() -> new UnauthorizedException("Authentication is required."));
        if (!actor.hasRole(RoleName.ADMIN)) {
            throw new ForbiddenException("The current account cannot manage shortlists.");
        }
        return actor;
    }

    private int validatePage(Integer page) {
        int value = page == null ? DEFAULT_PAGE : page;
        if (value < 0) {
            throw new ValidationException("page must be greater than or equal to 0.");
        }
        return value;
    }

    private int validateSize(Integer size) {
        int value = size == null ? DEFAULT_SIZE : size;
        if (value < 1 || value > MAX_SIZE) {
            throw new ValidationException("size must be between 1 and 100.");
        }
        return value;
    }

    private void validateOffset(int page, int size) {
        if ((long) page * size > Integer.MAX_VALUE) {
            throw new ValidationException("page is too large for bounded database pagination.");
        }
    }

    private String validateSearch(String search) {
        String value = search == null ? "" : search.trim();
        if (value.length() > MAX_SEARCH_LENGTH) {
            throw new ValidationException("search must not exceed 120 characters.");
        }
        return value;
    }

    private String normalizeNullable(String value, int maxLength, String field) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new ValidationException(field + " must not exceed " + maxLength + " characters.");
        }
        return normalized;
    }

    private long safeVersion(ShortlistEntity entity) {
        return entity.getVersion() == null ? 0 : entity.getVersion();
    }

    private boolean containsConstraint(Throwable throwable, String constraintName) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase(Locale.ROOT)
                    .contains(constraintName.toLowerCase(Locale.ROOT))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private enum ShortlistSort {
        UPDATED_AT("updatedAt,desc", "s.updated_at DESC, s.id ASC"),
        CREATED_AT("createdAt,desc", "s.created_at DESC, s.id ASC"),
        COMPANY_NAME("companyName,asc", "LOWER(c.name) ASC, s.id ASC"),
        ROLE_TITLE("roleTitle,asc", "LOWER(ir.title) ASC, s.id ASC");

        private final String apiValue;
        private final String orderBy;

        ShortlistSort(String apiValue, String orderBy) {
            this.apiValue = apiValue;
            this.orderBy = orderBy;
        }

        static ShortlistSort from(String value) {
            String normalized = value == null || value.isBlank() ? UPDATED_AT.apiValue : value.trim();
            for (ShortlistSort sort : values()) {
                if (sort.apiValue.equals(normalized)) {
                    return sort;
                }
            }
            throw new ValidationException("Unsupported shortlist sort value.");
        }

        String apiValue() { return apiValue; }
        String orderBy() { return orderBy; }
    }

    private enum CandidateSort {
        GPA_DESC("officialGpa,desc", "sas.computer_science_gpa DESC NULLS LAST, es.id ASC"),
        GPA_ASC("officialGpa,asc", "sas.computer_science_gpa ASC NULLS LAST, es.id ASC"),
        FULL_NAME("fullName,asc", "LOWER(es.full_name) ASC, es.id ASC"),
        INDEX_NUMBER("indexNumber,asc", "es.index_number ASC, es.id ASC");

        private final String apiValue;
        private final String orderBy;

        CandidateSort(String apiValue, String orderBy) {
            this.apiValue = apiValue;
            this.orderBy = orderBy;
        }

        static CandidateSort from(String value) {
            String normalized = value == null || value.isBlank() ? GPA_DESC.apiValue : value.trim();
            for (CandidateSort sort : values()) {
                if (sort.apiValue.equals(normalized)) {
                    return sort;
                }
            }
            throw new ValidationException("Unsupported shortlist candidate sort value.");
        }

        String apiValue() { return apiValue; }
        String orderBy() { return orderBy; }
    }
}
