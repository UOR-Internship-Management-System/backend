package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.application;

import java.util.Objects;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminLatestCvResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentCvSupportingDataResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentDetailResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception.AdminStudentErrors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.mapper.AdminStudentMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.RegisteredStudentRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.AdminStudentDetailReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.RegisteredStudentReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Application service for Admin-only, read-only Student deep-dive inspection. */
@Service
public class AdminStudentInspectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminStudentInspectionService.class);

    private final RegisteredStudentReadRepository registeredStudentRepository;
    private final AdminStudentDetailReadRepository detailRepository;
    private final AdminStudentMapper mapper;
    private final CurrentActorProvider currentActorProvider;

    public AdminStudentInspectionService(
            RegisteredStudentReadRepository registeredStudentRepository,
            AdminStudentDetailReadRepository detailRepository,
            AdminStudentMapper mapper,
            CurrentActorProvider currentActorProvider) {
        this.registeredStudentRepository = registeredStudentRepository;
        this.detailRepository = detailRepository;
        this.mapper = mapper;
        this.currentActorProvider = currentActorProvider;
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
            // The registered Student summary includes the authoritative academic GPA projection.
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

            /*
             * BMD-007 persisted CV lifecycle is not present in the current source/database yet.
             * Therefore the authoritative current database contains no saved CV versions. Patch 5
             * replaces this availability value with the persisted latest-CV read repository once
             * BMD-007 exists; this module does not create CV persistence on its behalf.
             */
            AdminLatestCvResponse latestCv = AdminLatestCvResponse.notSaved();
            return mapper.toDetail(student, profile, supportingData, latestCv);
        } catch (DataAccessException exception) {
            LOGGER.error("Admin Student deep-dive could not load persisted Student inspection data.", exception);
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
}
