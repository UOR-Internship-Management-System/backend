package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception.RegisteredStudentNotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.mapper.AdminStudentMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminStudentProfileRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.RegisteredStudentRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.AdminStudentDetailReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.RegisteredStudentReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.port.ActiveCvFileResolver;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.port.LatestSavedCvQuery;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request.AdminAcademicRecordCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request.AdminStudentCollectionCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.AdminAcademicRecordReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.AdminDeclaredSkillReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.AdminProjectReadRepository;
import org.junit.jupiter.api.Test;

class AdminStudentInspectionServiceTest {

    private final RegisteredStudentReadRepository registeredRepository = mock(RegisteredStudentReadRepository.class);
    private final AdminStudentDetailReadRepository detailRepository = mock(AdminStudentDetailReadRepository.class);
    private final AdminDeclaredSkillReadRepository declaredSkillRepository = mock(AdminDeclaredSkillReadRepository.class);
    private final AdminProjectReadRepository projectRepository = mock(AdminProjectReadRepository.class);
    private final AdminAcademicRecordReadRepository academicRecordRepository = mock(AdminAcademicRecordReadRepository.class);
    private final CurrentActorProvider currentActorProvider = mock(CurrentActorProvider.class);
    private final LatestSavedCvQuery latestSavedCvQuery = mock(LatestSavedCvQuery.class);
    private final ActiveCvFileResolver activeCvFileResolver = mock(ActiveCvFileResolver.class);
    private final AuditEventPublisher auditEventPublisher = mock(AuditEventPublisher.class);
    private final AdminStudentInspectionService service = new AdminStudentInspectionService(
            registeredRepository,
            detailRepository,
            declaredSkillRepository,
            projectRepository,
            academicRecordRepository,
            new AdminStudentMapper(),
            currentActorProvider,
            latestSavedCvQuery,
            activeCvFileResolver,
            auditEventPublisher);

    @Test
    void returnsReadOnlyDeepDiveAndCurrentSchemaHasNoSavedCvState() {
        UUID studentId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-08-14T10:00:00Z");
        when(currentActorProvider.currentActor()).thenReturn(Optional.of(adminActor()));
        when(registeredRepository.findById(studentId)).thenReturn(Optional.of(new RegisteredStudentRow(
                studentId,
                "SC/2022/12345",
                "Asha Silva",
                "asha@dcs.ruh.ac.lk",
                "2022",
                3,
                null)));
        when(detailRepository.findProfile(studentId)).thenReturn(Optional.of(new AdminStudentProfileRow(
                studentId,
                "Asha Silva",
                "SC/2022/12345",
                "asha@dcs.ruh.ac.lk",
                3,
                2022,
                null,
                null,
                null,
                null,
                null,
                0,
                now,
                now)));
        when(detailRepository.findExperiences(studentId)).thenReturn(List.of());
        when(detailRepository.findCertificates(studentId)).thenReturn(List.of());
        when(detailRepository.findAwards(studentId)).thenReturn(List.of());
        when(detailRepository.findActivities(studentId)).thenReturn(List.of());

        var response = service.getDetail(studentId);

        assertThat(response.student().studentId()).isEqualTo(studentId);
        assertThat(response.profile().studentId()).isEqualTo(studentId);
        assertThat(response.cvSupportingData().experiences()).isEmpty();
        assertThat(response.latestCv().availability().name()).isEqualTo("NOT_SAVED");
        verify(detailRepository).findExperiences(studentId);
        verify(detailRepository).findCertificates(studentId);
        verify(detailRepository).findAwards(studentId);
        verify(detailRepository).findActivities(studentId);
    }

    @Test
    void rejectsIdentifiersThatDoNotResolveToRegisteredStudentsBeforeReadingSupportingData() {
        UUID studentId = UUID.randomUUID();
        when(currentActorProvider.currentActor()).thenReturn(Optional.of(adminActor()));
        when(registeredRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDetail(studentId))
                .isInstanceOf(RegisteredStudentNotFoundException.class);
    }


    @Test
    void childCollectionsRejectUnregisteredStudentsBeforeQueryingOwnedTables() {
        UUID studentId = UUID.randomUUID();
        when(currentActorProvider.currentActor()).thenReturn(Optional.of(adminActor()));
        when(registeredRepository.existsRegisteredStudent(studentId)).thenReturn(false);

        assertThatThrownBy(() -> service.getDeclaredSkills(
                        studentId,
                        new AdminStudentCollectionCriteria(0, 20, "")))
                .isInstanceOf(RegisteredStudentNotFoundException.class);
    }

    @Test
    void childCollectionValidationRejectsUnsafePagingAndAcademicSorts() {
        UUID studentId = UUID.randomUUID();
        when(currentActorProvider.currentActor()).thenReturn(Optional.of(adminActor()));
        when(registeredRepository.existsRegisteredStudent(studentId)).thenReturn(true);

        assertThatThrownBy(() -> service.getProjects(
                        studentId,
                        new AdminStudentCollectionCriteria(-1, 20, null)))
                .hasMessageContaining("page");
        assertThatThrownBy(() -> service.getDeclaredSkills(
                        studentId,
                        new AdminStudentCollectionCriteria(0, 101, null)))
                .hasMessageContaining("size");
        assertThatThrownBy(() -> service.getAcademicRecords(
                        studentId,
                        new AdminAcademicRecordCriteria(0, 20, "DROP TABLE", null, null)))
                .hasMessageContaining("sort");
    }

    @Test
    void returnsPersistedLatestCvMetadataForRegisteredStudent() {
        UUID studentId = UUID.randomUUID();
        UUID cvId = UUID.randomUUID();
        OffsetDateTime generatedAt = OffsetDateTime.parse("2026-08-19T03:00:00Z");
        OffsetDateTime savedAt = generatedAt.plusMinutes(1);
        when(currentActorProvider.currentActor()).thenReturn(Optional.of(adminActor()));
        when(registeredRepository.existsRegisteredStudent(studentId)).thenReturn(true);
        when(latestSavedCvQuery.findByStudentId(studentId)).thenReturn(Optional.of(
                new LatestSavedCvQuery.LatestSavedCv(
                        studentId, cvId, 3, generatedAt, savedAt, "CURRENT",
                        "cv-" + studentId + ".pdf", 2048)));

        var response = service.getLatestCv(studentId);

        assertThat(response.availability().name()).isEqualTo("AVAILABLE");
        assertThat(response.cvId()).isEqualTo(cvId);
        assertThat(response.revision()).isEqualTo(3);
        assertThat(response.downloadUrl()).isEqualTo("/admin/students/" + studentId + "/latest-cv/download");
    }

    private CurrentActor adminActor() {
        return new CurrentActor(
                UUID.randomUUID(),
                "admin@dcs.ruh.ac.lk",
                Set.of(RoleName.ADMIN));
    }
}
