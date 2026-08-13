package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.dto.response.AcademicRecordResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerErrors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicRecordSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.AcademicRecordQueryRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for Admin-only, committed official academic-record inspection. */
@Service
public class AcademicRecordQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcademicRecordQueryService.class);
    private static final Pattern COURSE_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");
    private final AcademicRecordQueryRepository queryRepository;
    private final CurrentActorProvider currentActorProvider;

    public AcademicRecordQueryService(AcademicRecordQueryRepository queryRepository, CurrentActorProvider currentActorProvider) {
        this.queryRepository = queryRepository;
        this.currentActorProvider = currentActorProvider;
    }

    @Transactional(readOnly = true)
    public PagedResponse<AcademicRecordResponse> listAdminRecords(
            Integer page, Integer size, String sort, String search, String courseCode, String studentId) {
        currentAdmin();
        int safePage = validatePage(page);
        int safeSize = validateSize(size);
        AcademicRecordSort safeSort = AcademicRecordSort.fromApiValue(sort);
        String safeSearch = validateSearch(search);
        String safeCourseCode = validateCourseCode(courseCode);
        UUID safeStudentId = validateStudentId(studentId);
        try {
            return PagedResponse.of(
                    queryRepository.search(safeSearch, safeCourseCode, safeStudentId, safePage, safeSize, safeSort),
                    safeSort.apiValue());
        } catch (DataAccessException exception) {
            LOGGER.error("Official Academic Ledger data could not be queried.", exception);
            throw AcademicLedgerErrors.academicDataUnavailable(exception);
        }
    }

    private CurrentActor currentAdmin() {
        CurrentActor actor = currentActorProvider.currentActor().orElseThrow(AcademicLedgerErrors::unauthorized);
        if (!actor.hasRole(RoleName.ADMIN)) throw AcademicLedgerErrors.forbidden();
        return actor;
    }

    private int validatePage(Integer page) {
        int value = page == null ? 0 : page;
        if (value < 0) throw AcademicLedgerErrors.badRequest("page must be greater than or equal to 0.");
        return value;
    }

    private int validateSize(Integer size) {
        int value = size == null ? 20 : size;
        if (value < 1 || value > 100) throw AcademicLedgerErrors.badRequest("size must be between 1 and 100.");
        return value;
    }

    private String validateSearch(String search) {
        if (search == null) return null;
        String value = search.trim();
        if (value.isEmpty() || value.length() > 120) {
            throw AcademicLedgerErrors.badRequest("search must contain between 1 and 120 characters.");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private String validateCourseCode(String courseCode) {
        if (courseCode == null) return null;
        String value = courseCode.trim();
        if (value.isEmpty() || value.length() > 30 || !COURSE_CODE_PATTERN.matcher(value).matches()) {
            throw AcademicLedgerErrors.badRequest(
                    "courseCode must contain 1 to 30 letters, digits, dots, underscores, or hyphens.");
        }
        return value;
    }

    private UUID validateStudentId(String studentId) {
        if (studentId == null) return null;
        String value = studentId.trim();
        if (value.isEmpty()) throw AcademicLedgerErrors.badRequest("studentId must be a valid UUID when supplied.");
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw AcademicLedgerErrors.badRequest("studentId must be a valid UUID when supplied.");
        }
    }
}
