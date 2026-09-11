package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.application;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request.AdminAcademicRecordCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request.AdminStudentCollectionCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminAcademicRecordResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminDeclaredSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminLatestCvResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminProjectResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentCvSupportingDataResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentDetailResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception.AdminStudentErrors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.AdminAcademicRecordSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.mapper.AdminStudentMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminProjectSkillRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.RegisteredStudentRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.AdminAcademicRecordReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.AdminDeclaredSkillReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.AdminProjectReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.AdminStudentDetailReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.RegisteredStudentReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.port.ActiveCvFileResolver;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.port.LatestSavedCvQuery;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventType;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Application service for Admin-only, read-only Student inspection resources. */
@Service
public class AdminStudentInspectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminStudentInspectionService.class);
    private static final Pattern COURSE_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");

    private final RegisteredStudentReadRepository registeredStudentRepository;
    private final AdminStudentDetailReadRepository detailRepository;
    private final AdminDeclaredSkillReadRepository declaredSkillRepository;
    private final AdminProjectReadRepository projectRepository;
    private final AdminAcademicRecordReadRepository academicRecordRepository;
    private final AdminStudentMapper mapper;
    private final CurrentActorProvider currentActorProvider;
    private final LatestSavedCvQuery latestSavedCvQuery;
    private final ActiveCvFileResolver activeCvFileResolver;
    private final AuditEventPublisher auditEventPublisher;

    public AdminStudentInspectionService(
            RegisteredStudentReadRepository registeredStudentRepository,
            AdminStudentDetailReadRepository detailRepository,
            AdminDeclaredSkillReadRepository declaredSkillRepository,
            AdminProjectReadRepository projectRepository,
            AdminAcademicRecordReadRepository academicRecordRepository,
            AdminStudentMapper mapper,
            CurrentActorProvider currentActorProvider,
            LatestSavedCvQuery latestSavedCvQuery,
            ActiveCvFileResolver activeCvFileResolver,
            AuditEventPublisher auditEventPublisher) {
        this.registeredStudentRepository = registeredStudentRepository;
        this.detailRepository = detailRepository;
        this.declaredSkillRepository = declaredSkillRepository;
        this.projectRepository = projectRepository;
        this.academicRecordRepository = academicRecordRepository;
        this.mapper = mapper;
        this.currentActorProvider = currentActorProvider;
        this.latestSavedCvQuery = latestSavedCvQuery;
        this.activeCvFileResolver = activeCvFileResolver;
        this.auditEventPublisher = auditEventPublisher;
    }

    /**
     * Returns the read-only deep-dive summary for one active registered Student.
     *
     * <p>REPEATABLE_READ keeps the identity/profile/supporting-data projection internally
     * consistent across the bounded set of SELECT statements. No Student-owned entity is loaded
     * through JPA and no lazy create/update path can run.
     */
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public AdminStudentDetailResponse getDetail(UUID studentId) {
        currentAdmin();
        UUID safeStudentId = Objects.requireNonNull(studentId, "studentId");

        RegisteredStudentRow student;
        try {
            student = registeredStudentRepository.findById(safeStudentId)
                    .orElseThrow(AdminStudentErrors::registeredStudentNotFound);
        } catch (DataAccessException exception) {
            LOGGER.error("Admin Student deep-dive could not resolve authoritative academic data.", exception);
            throw AdminStudentErrors.academicDataUnavailable(exception);
        }

        try {
            var profile = detailRepository.findProfile(safeStudentId)
                    .orElseThrow(AdminStudentErrors::registeredStudentNotFound);
            AdminStudentCvSupportingDataResponse supportingData = mapper.toSupportingData(
                    detailRepository.findExperiences(safeStudentId),
                    detailRepository.findCertificates(safeStudentId),
                    detailRepository.findAwards(safeStudentId),
                    detailRepository.findActivities(safeStudentId));

            AdminLatestCvResponse latestCv = toAdminLatestCv(safeStudentId);
            return mapper.toDetail(student, profile, supportingData, latestCv);
        } catch (DataAccessException exception) {
            LOGGER.error("Admin Student deep-dive could not load persisted Student inspection data.", exception);
            throw AdminStudentErrors.studentDataUnavailable(exception);
        }
    }

    /** Returns latest saved CV availability without creating any review/submission state. */
    @Transactional(readOnly = true)
    public AdminLatestCvResponse getLatestCv(UUID studentId) {
        currentAdmin();
        UUID safeStudentId = requireRegisteredStudent(studentId);
        return toAdminLatestCv(safeStudentId);
    }

    /** Resolves the exact active PDF for an audited Admin download. */
    @Transactional
    public ActiveCvFileResolver.ResolvedCvFile downloadLatestCv(UUID studentId) {
        CurrentActor actor = currentAdmin();
        UUID safeStudentId = requireRegisteredStudent(studentId);
        ActiveCvFileResolver.ResolvedCvFile file;
        try {
            file = activeCvFileResolver.resolve(safeStudentId);
        } catch (ApplicationException exception) {
            if (exception.getErrorCode() == ApiErrorCode.CV_FILE_UNAVAILABLE) {
                auditEventPublisher.recordBestEffort(
                        actor.userId(),
                        "ADMIN",
                        AuditEventType.CV_FILE_UNAVAILABLE.name(),
                        AuditEventCategory.CV_MANAGEMENT,
                        "STUDENT_CV",
                        safeStudentId.toString(),
                        Map.of());
                throw AdminStudentErrors.cvFileUnavailable(exception);
            }
            if (exception.getErrorCode() == ApiErrorCode.CV_NOT_SAVED) {
                throw AdminStudentErrors.cvNotSaved();
            }
            throw exception;
        }
        auditEventPublisher.recordRequired(
                actor.userId(),
                "ADMIN",
                AuditEventType.CV_DOWNLOADED_BY_ADMIN.name(),
                AuditEventCategory.CV_MANAGEMENT,
                "CV",
                file.cvId().toString(),
                Map.of("studentId", safeStudentId.toString(), "revision", file.revision(), "fileSizeBytes", file.fileSizeBytes()));
        return file;
    }

    private AdminLatestCvResponse toAdminLatestCv(UUID studentId) {
        return latestSavedCvQuery.findByStudentId(studentId)
                .map(cv -> AdminLatestCvResponse.available(
                        cv.cvId(),
                        cv.revision(),
                        cv.generatedAt(),
                        cv.savedAt(),
                        lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.CvFreshnessStatus.valueOf(cv.freshnessStatus()),
                        cv.fileName(),
                        cv.fileSizeBytes(),
                        studentId))
                .orElseGet(AdminLatestCvResponse::notSaved);
    }

    /** Returns a bounded, deterministic page of declared skills for one registered Student. */
    @Transactional(readOnly = true)
    public PagedResponse<AdminDeclaredSkillResponse> getDeclaredSkills(
            UUID studentId,
            AdminStudentCollectionCriteria criteria) {
        currentAdmin();
        UUID safeStudentId = requireRegisteredStudent(studentId);
        AdminStudentCollectionCriteria safeCriteria = Objects.requireNonNull(criteria, "criteria");
        int page = validatePage(safeCriteria.page());
        int size = validateSize(safeCriteria.size());
        String search = validateSearch(safeCriteria.search());

        try {
            return PagedResponse.of(
                    declaredSkillRepository.search(safeStudentId, search, page, size)
                            .map(mapper::toDeclaredSkill),
                    AdminDeclaredSkillReadRepository.SORT);
        } catch (DataAccessException exception) {
            LOGGER.error("Admin Student declared skills could not be queried.", exception);
            throw AdminStudentErrors.studentDataUnavailable(exception);
        }
    }

    /**
     * Returns a bounded project page and batch-loads all canonical project skills in one query.
     */
    @Transactional(readOnly = true)
    public PagedResponse<AdminProjectResponse> getProjects(
            UUID studentId,
            AdminStudentCollectionCriteria criteria) {
        currentAdmin();
        UUID safeStudentId = requireRegisteredStudent(studentId);
        AdminStudentCollectionCriteria safeCriteria = Objects.requireNonNull(criteria, "criteria");
        int page = validatePage(safeCriteria.page());
        int size = validateSize(safeCriteria.size());
        String search = validateSearch(safeCriteria.search());

        try {
            var projectPage = projectRepository.search(safeStudentId, search, page, size);
            List<UUID> projectIds = projectPage.getContent().stream()
                    .map(row -> row.projectId())
                    .toList();
            Map<UUID, List<AdminProjectSkillRow>> skillsByProject =
                    projectRepository.findSkills(safeStudentId, projectIds).stream()
                            .collect(Collectors.groupingBy(
                                    AdminProjectSkillRow::projectId,
                                    java.util.LinkedHashMap::new,
                                    Collectors.toList()));

            return PagedResponse.of(
                    projectPage.map(row -> mapper.toProject(row, skillsByProject)),
                    AdminProjectReadRepository.SORT);
        } catch (DataAccessException exception) {
            LOGGER.error("Admin Student projects could not be queried.", exception);
            throw AdminStudentErrors.studentDataUnavailable(exception);
        }
    }

    /** Returns committed official academic records only; no staged or mutable ledger rows are exposed. */
    @Transactional(readOnly = true)
    public PagedResponse<AdminAcademicRecordResponse> getAcademicRecords(
            UUID studentId,
            AdminAcademicRecordCriteria criteria) {
        currentAdmin();
        UUID safeStudentId = requireRegisteredStudent(studentId);
        AdminAcademicRecordCriteria safeCriteria = Objects.requireNonNull(criteria, "criteria");
        int page = validatePage(safeCriteria.page());
        int size = validateSize(safeCriteria.size());
        String search = validateSearch(safeCriteria.search());
        String courseCode = validateCourseCode(safeCriteria.courseCode());
        AdminAcademicRecordSort sort = AdminAcademicRecordSort.fromApiValue(safeCriteria.sort());

        try {
            return PagedResponse.of(
                    academicRecordRepository
                            .search(safeStudentId, search, courseCode, page, size, sort)
                            .map(mapper::toAcademicRecord),
                    sort.apiValue());
        } catch (DataAccessException exception) {
            LOGGER.error("Admin Student official academic records could not be queried.", exception);
            throw AdminStudentErrors.academicDataUnavailable(exception);
        }
    }

    private UUID requireRegisteredStudent(UUID studentId) {
        UUID safeStudentId = Objects.requireNonNull(studentId, "studentId");
        try {
            if (!registeredStudentRepository.existsRegisteredStudent(safeStudentId)) {
                throw AdminStudentErrors.registeredStudentNotFound();
            }
            return safeStudentId;
        } catch (DataAccessException exception) {
            LOGGER.error("Registered Student access predicate could not be evaluated.", exception);
            throw AdminStudentErrors.studentDataUnavailable(exception);
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
        int value = page == null ? AdminStudentCollectionCriteria.DEFAULT_PAGE : page;
        if (value < 0) {
            throw AdminStudentErrors.badRequest("page must be greater than or equal to 0.");
        }
        return value;
    }

    private int validateSize(Integer size) {
        int value = size == null ? AdminStudentCollectionCriteria.DEFAULT_SIZE : size;
        if (value < 1 || value > AdminStudentCollectionCriteria.MAX_SIZE) {
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
        if (value.length() > AdminStudentCollectionCriteria.MAX_SEARCH_LENGTH) {
            throw AdminStudentErrors.badRequest("search must not exceed 120 characters.");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private String validateCourseCode(String courseCode) {
        if (courseCode == null) {
            return null;
        }
        String value = courseCode.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.length() > AdminAcademicRecordCriteria.MAX_COURSE_CODE_LENGTH
                || !COURSE_CODE_PATTERN.matcher(value).matches()) {
            throw AdminStudentErrors.badRequest(
                    "courseCode must contain 1 to 30 letters, digits, dots, underscores, or hyphens.");
        }
        return value;
    }
}
