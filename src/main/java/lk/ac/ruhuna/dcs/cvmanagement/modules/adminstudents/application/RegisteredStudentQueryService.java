package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.application;

import java.util.Locale;
import java.util.Objects;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request.AdminStudentSearchCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentListItemResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception.AdminStudentErrors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.RegisteredStudentSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.mapper.AdminStudentMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.RegisteredStudentReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for the Admin-only registered-Student roster. */
@Service
public class RegisteredStudentQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredStudentQueryService.class);

    private final RegisteredStudentReadRepository repository;
    private final AdminStudentMapper mapper;
    private final CurrentActorProvider currentActorProvider;

    public RegisteredStudentQueryService(
            RegisteredStudentReadRepository repository,
            AdminStudentMapper mapper,
            CurrentActorProvider currentActorProvider) {
        this.repository = repository;
        this.mapper = mapper;
        this.currentActorProvider = currentActorProvider;
    }

    /**
     * Returns one bounded, deterministic page of registered Students.
     *
     * <p>The method is explicitly read-only. It validates all query controls before any SQL is
     * executed and maps all client sort values through an allowlist.
     */
    @Transactional(readOnly = true)
    public PagedResponse<AdminStudentListItemResponse> list(AdminStudentSearchCriteria criteria) {
        currentAdmin();
        AdminStudentSearchCriteria safeCriteria = Objects.requireNonNull(criteria, "criteria");

        int page = validatePage(safeCriteria.page());
        int size = validateSize(safeCriteria.size());
        RegisteredStudentSort sort = safeCriteria.parsedSort();
        String search = validateSearch(safeCriteria.search());
        Integer level = validateLevel(safeCriteria.level());

        try {
            return PagedResponse.of(
                    repository.search(search, level, page, size, sort).map(mapper::toListItem),
                    sort.apiValue());
        } catch (DataAccessException exception) {
            LOGGER.error("Registered Student roster could not resolve authoritative academic data.", exception);
            throw AdminStudentErrors.academicDataUnavailable(exception);
        }
    }

    private CurrentActor currentAdmin() {
        CurrentActor actor = currentActorProvider.currentActor().orElseThrow(AdminStudentErrors::unauthorized);
        if (!actor.hasRole(RoleName.ADMIN)) {
            throw AdminStudentErrors.forbidden();
        }
        return actor;
    }

    private int validatePage(Integer page) {
        int value = page == null ? AdminStudentSearchCriteria.DEFAULT_PAGE : page;
        if (value < 0) {
            throw AdminStudentErrors.badRequest("page must be greater than or equal to 0.");
        }
        return value;
    }

    private int validateSize(Integer size) {
        int value = size == null ? AdminStudentSearchCriteria.DEFAULT_SIZE : size;
        if (value < 1 || value > 100) {
            throw AdminStudentErrors.badRequest("size must be between 1 and 100.");
        }
        return value;
    }

    private String validateSearch(String search) {
        if (search == null) {
            return null;
        }
        String value = search.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.length() > 120) {
            throw AdminStudentErrors.badRequest("search must not exceed 120 characters.");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private Integer validateLevel(Integer level) {
        if (level == null) {
            return null;
        }
        if (level != 3 && level != 4) {
            throw AdminStudentErrors.badRequest("level must be either 3 or 4.");
        }
        return level;
    }
}
