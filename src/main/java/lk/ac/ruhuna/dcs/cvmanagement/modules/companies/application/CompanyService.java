package lk.ac.ruhuna.dcs.cvmanagement.modules.companies.application;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanyRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanySearchCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanyUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.response.CompanyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.domain.exception.CompanyNotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.domain.exception.DuplicateCompanyException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.domain.policy.CompanyNameNormalizer;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.domain.policy.CompanySort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.mapper.CompanyMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.persistence.entity.CompanyEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.persistence.repository.CompanyQueryRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.persistence.repository.CompanyRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for Admin Company metadata management. */
@Service
public class CompanyService {

    private static final Pattern SIMPLE_EMAIL =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final String DUPLICATE_NAME_CONSTRAINT = "uq_companies_normalized_name";
    private static final String AUDIT_RESOURCE = "COMPANY";

    private final CompanyRepository companyRepository;
    private final CompanyQueryRepository companyQueryRepository;
    private final CompanyMapper mapper;
    private final CurrentActorProvider currentActorProvider;
    private final AuditEventPublisher auditEventPublisher;
    private final Clock clock;

    public CompanyService(
            CompanyRepository companyRepository,
            CompanyQueryRepository companyQueryRepository,
            CompanyMapper mapper,
            CurrentActorProvider currentActorProvider,
            AuditEventPublisher auditEventPublisher,
            Clock clock) {
        this.companyRepository = companyRepository;
        this.companyQueryRepository = companyQueryRepository;
        this.mapper = mapper;
        this.currentActorProvider = currentActorProvider;
        this.auditEventPublisher = auditEventPublisher;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PagedResponse<CompanyResponse> list(CompanySearchCriteria criteria) {
        currentAdmin();
        CompanySearchCriteria safeCriteria = criteria == null
                ? new CompanySearchCriteria(null, null, null, null)
                : criteria;
        int page = validatePage(safeCriteria.page());
        int size = validateSize(safeCriteria.size());
        validateOffset(page, size);
        CompanySort sort = CompanySort.fromApiValue(safeCriteria.sort());
        String search = validateSearch(safeCriteria.search());

        return PagedResponse.of(
                companyQueryRepository.search(search, page, size, sort).map(mapper::toResponse),
                sort.apiValue());
    }

    @Transactional(readOnly = true)
    public CompanyResponse get(UUID companyId) {
        currentAdmin();
        return mapper.toResponse(findCompany(companyId));
    }

    @Transactional
    public CompanyResponse create(CompanyRequest request) {
        CurrentActor actor = currentAdmin();
        if (request == null) {
            throw new ValidationException("Company request body is required.");
        }

        CompanyEntity entity = new CompanyEntity();
        entity.setId(UUID.randomUUID());
        entity.setName(normalizeName(request.name()));
        entity.setWebsiteUrl(normalizeWebsite(request.websiteUrl()));
        entity.setContactPerson(normalizeNullable(request.contactPerson(), 150, "contactPerson"));
        entity.setContactEmail(normalizeEmail(request.contactEmail()));
        entity.setContactPhone(normalizeNullable(request.contactPhone(), 30, "contactPhone"));
        entity.setNotes(normalizeNullable(request.notes(), 4000, "notes"));

        String duplicateKey = CompanyNameNormalizer.duplicateKey(entity.getName());
        if (companyRepository.existsByNormalizedName(duplicateKey)) {
            throw new DuplicateCompanyException();
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        CompanyEntity saved = saveAndFlushWithDuplicateTranslation(entity);

        auditEventPublisher.recordRequired(
                actor.userId(),
                RoleName.ADMIN.name(),
                AuditEventType.COMPANY_CREATED.name(),
                AuditEventCategory.INTERNSHIP_MANAGEMENT,
                AUDIT_RESOURCE,
                saved.getId().toString(),
                Map.of("companyId", saved.getId().toString(), "companyName", saved.getName()));
        return mapper.toResponse(saved);
    }

    @Transactional
    public CompanyResponse update(UUID companyId, CompanyUpdateRequest request, long expectedVersion) {
        CurrentActor actor = currentAdmin();
        if (request == null || !request.hasAnyField()) {
            throw new ValidationException("Change at least one company field.");
        }

        CompanyEntity entity = findCompany(companyId);
        assertVersion(entity, expectedVersion);

        Set<String> changedFields = new LinkedHashSet<>();

        if (request.hasName()) {
            String name = normalizeName(request.name());
            String duplicateKey = CompanyNameNormalizer.duplicateKey(name);
            if (companyRepository.existsByNormalizedNameAndIdNot(duplicateKey, companyId)) {
                throw new DuplicateCompanyException();
            }
            entity.setName(name);
            changedFields.add("name");
        }
        if (request.hasWebsiteUrl()) {
            entity.setWebsiteUrl(normalizeWebsite(request.websiteUrl()));
            changedFields.add("websiteUrl");
        }
        if (request.hasContactPerson()) {
            entity.setContactPerson(normalizeNullable(request.contactPerson(), 150, "contactPerson"));
            changedFields.add("contactPerson");
        }
        if (request.hasContactEmail()) {
            entity.setContactEmail(normalizeEmail(request.contactEmail()));
            changedFields.add("contactEmail");
        }
        if (request.hasContactPhone()) {
            entity.setContactPhone(normalizeNullable(request.contactPhone(), 30, "contactPhone"));
            changedFields.add("contactPhone");
        }
        if (request.hasNotes()) {
            entity.setNotes(normalizeNullable(request.notes(), 4000, "notes"));
            changedFields.add("notes");
        }

        entity.setUpdatedAt(OffsetDateTime.now(clock));
        CompanyEntity saved = saveAndFlushWithDuplicateTranslation(entity);
        auditEventPublisher.recordRequired(
                actor.userId(),
                RoleName.ADMIN.name(),
                AuditEventType.COMPANY_UPDATED.name(),
                AuditEventCategory.INTERNSHIP_MANAGEMENT,
                AUDIT_RESOURCE,
                saved.getId().toString(),
                Map.of(
                        "companyId", saved.getId().toString(),
                        "changedFields", Set.copyOf(changedFields)));
        return mapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID companyId, long expectedVersion) {
        CurrentActor actor = currentAdmin();
        CompanyEntity entity = findCompany(companyId);
        assertVersion(entity, expectedVersion);

        auditEventPublisher.recordRequired(
                actor.userId(),
                RoleName.ADMIN.name(),
                AuditEventType.COMPANY_DELETED.name(),
                AuditEventCategory.INTERNSHIP_MANAGEMENT,
                AUDIT_RESOURCE,
                entity.getId().toString(),
                Map.of("companyId", entity.getId().toString(), "companyName", entity.getName()));
        companyRepository.delete(entity);
        companyRepository.flush();
    }

    private CompanyEntity findCompany(UUID companyId) {
        if (companyId == null) {
            throw new CompanyNotFoundException();
        }
        return companyRepository.findById(companyId).orElseThrow(CompanyNotFoundException::new);
    }

    private CurrentActor currentAdmin() {
        CurrentActor actor = currentActorProvider.currentActor()
                .orElseThrow(() -> new UnauthorizedException("Authentication is required."));
        if (!actor.hasRole(RoleName.ADMIN)) {
            throw new ForbiddenException("The current account cannot access Company management.");
        }
        return actor;
    }

    private void assertVersion(CompanyEntity entity, long expectedVersion) {
        long currentVersion = entity.getVersion() == null ? 0L : entity.getVersion();
        if (currentVersion != expectedVersion) {
            throw new PreconditionFailedException(
                    "Company data changed since it was loaded. Reload the latest version and try again.");
        }
    }

    private CompanyEntity saveAndFlushWithDuplicateTranslation(CompanyEntity entity) {
        try {
            return companyRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException exception) {
            if (containsConstraint(exception, DUPLICATE_NAME_CONSTRAINT)) {
                throw new DuplicateCompanyException();
            }
            throw exception;
        }
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

    private int validatePage(Integer page) {
        int value = page == null ? CompanySearchCriteria.DEFAULT_PAGE : page;
        if (value < 0) {
            throw new ValidationException("page must be greater than or equal to 0.");
        }
        return value;
    }

    private int validateSize(Integer size) {
        int value = size == null ? CompanySearchCriteria.DEFAULT_SIZE : size;
        if (value < 1 || value > CompanySearchCriteria.MAX_SIZE) {
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
        if (search == null) {
            return null;
        }
        String value = search.strip();
        if (value.isEmpty() || value.codePointCount(0, value.length()) > CompanySearchCriteria.MAX_SEARCH_LENGTH) {
            throw new ValidationException("search must contain between 1 and 120 characters.");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private String normalizeName(String value) {
        return CompanyNameNormalizer.displayName(value);
    }

    private String normalizeWebsite(String value) {
        String normalized = normalizeNullable(value, 500, "websiteUrl");
        if (normalized == null) {
            return null;
        }
        try {
            URI uri = new URI(normalized);
            String scheme = uri.getScheme();
            if (!uri.isAbsolute()
                    || scheme == null
                    || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))
                    || uri.getRawAuthority() == null
                    || uri.getRawAuthority().isBlank()) {
                throw new ValidationException("websiteUrl must be an absolute HTTP or HTTPS URL.");
            }
            return normalized;
        } catch (URISyntaxException exception) {
            throw new ValidationException("websiteUrl must be a valid absolute HTTP or HTTPS URL.");
        }
    }

    private String normalizeEmail(String value) {
        String normalized = normalizeNullable(value, 254, "contactEmail");
        if (normalized == null) {
            return null;
        }
        if (!SIMPLE_EMAIL.matcher(normalized).matches()) {
            throw new ValidationException("contactEmail must be a valid email address.");
        }
        return normalized;
    }

    private String normalizeNullable(String value, int maximumCodePoints, String fieldName) {
        if (value == null) {
            return null;
        }
        String normalized = value.strip();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.codePointCount(0, normalized.length()) > maximumCodePoints) {
            throw new ValidationException(fieldName + " exceeds the maximum supported length.");
        }
        return normalized;
    }
}
