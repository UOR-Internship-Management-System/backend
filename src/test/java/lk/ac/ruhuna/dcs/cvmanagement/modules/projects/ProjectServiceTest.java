package lk.ac.ruhuna.dcs.cvmanagement.modules.projects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.request.ProjectCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.request.ProjectUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application.ProjectService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application.port.ProjectSkillLookup;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application.port.ProjectSkillSummary;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application.port.ProjectStudentIdentityLookup;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.mapper.ProjectMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.entity.ProjectEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.entity.ProjectSkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.repository.ProjectRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.repository.ProjectSkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.cv.CvSourceArea;
import lk.ac.ruhuna.dcs.cvmanagement.shared.cv.CvSourceFreshnessUpdatePort;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.PreconditionFailedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ValidationException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class ProjectServiceTest {

    private final CurrentActorProvider actorProvider = mock(CurrentActorProvider.class);
    private final ProjectStudentIdentityLookup studentIdentityLookup = mock(ProjectStudentIdentityLookup.class);
    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final ProjectSkillRepository projectSkillRepository = mock(ProjectSkillRepository.class);
    private final ProjectSkillLookup skillLookup = mock(ProjectSkillLookup.class);
    private final ProjectMapper mapper = new ProjectMapper();
    private final CvSourceFreshnessUpdatePort cvFreshnessUpdatePort = mock(CvSourceFreshnessUpdatePort.class);

    private ProjectService service;
    private UUID actorId;
    private UUID studentId;

    @BeforeEach
    void setUp() {
        actorId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        when(actorProvider.currentActor()).thenReturn(Optional.of(
                new CurrentActor(actorId, "student@dcs.ruh.ac.lk", Set.of(RoleName.STUDENT))));
        when(studentIdentityLookup.findStudentIdByUserAccountId(actorId)).thenReturn(Optional.of(studentId));
        service = new ProjectService(
                actorProvider,
                studentIdentityLookup,
                projectRepository,
                projectSkillRepository,
                skillLookup,
                mapper,
                cvFreshnessUpdatePort);
    }

    @Test
    void createsStudentOwnedProjectAndNormalizedSkillLinks() {
        UUID skillId = UUID.randomUUID();
        ProjectSkillSummary skill = new ProjectSkillSummary(skillId, "Spring Boot", "Backend framework");
        when(skillLookup.findByIds(any())).thenReturn(Map.of(skillId, skill));
        when(projectRepository.save(any())).thenAnswer(invocation -> {
            ProjectEntity entity = invocation.getArgument(0);
            entity.setVersion(0L);
            return entity;
        });
        when(projectSkillRepository.findByIdProjectId(any())).thenAnswer(invocation ->
                List.of(new ProjectSkillEntity(invocation.getArgument(0), skillId)));

        var response = service.create(new ProjectCreateRequest(
                "Portfolio API",
                "Student project",
                "https://github.com/example/portfolio-api",
                null,
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-02-01"),
                List.of(skillId),
                true));

        ArgumentCaptor<ProjectEntity> projectCaptor = ArgumentCaptor.forClass(ProjectEntity.class);
        verify(projectRepository).save(projectCaptor.capture());
        assertThat(projectCaptor.getValue().getStudentId()).isEqualTo(studentId);
        assertThat(response.title()).isEqualTo("Portfolio API");
        assertThat(response.skills()).singleElement().satisfies(actual -> {
            assertThat(actual.skillId()).isEqualTo(skillId);
            assertThat(actual.name()).isEqualTo("Spring Boot");
            assertThat(actual.description()).isEqualTo("Backend framework");
        });
        verify(projectSkillRepository).save(any(ProjectSkillEntity.class));
        verify(cvFreshnessUpdatePort).markChanged(studentId, CvSourceArea.PROJECTS);
    }

    @Test
    void listsOnlyProjectsOwnedByCurrentStudent() {
        ProjectEntity project = project(studentId, 0L);
        when(projectRepository.search(eq(studentId), eq("%portal%"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(project)));
        when(projectSkillRepository.findByIdProjectId(project.getId())).thenReturn(List.of());

        var response = service.list("Portal", 0, 20, "updatedAt,desc");

        assertThat(response.items()).singleElement().extracting(item -> item.projectId())
                .isEqualTo(project.getId());
        verify(projectRepository).search(eq(studentId), eq("%portal%"), any(Pageable.class));
    }

    @Test
    void rejectsAccessToAnotherStudentsProject() {
        ProjectEntity project = project(UUID.randomUUID(), 0L);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> service.get(project.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("does not belong");
        verify(projectSkillRepository, never()).findByIdProjectId(any());
    }

    @Test
    void rejectsAdminActorBeforeResolvingStudentIdentity() {
        when(actorProvider.currentActor()).thenReturn(Optional.of(
                new CurrentActor(actorId, "admin@dcs.ruh.ac.lk", Set.of(RoleName.ADMIN))));

        assertThatThrownBy(() -> service.list(null, null, null, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Student account");
        verify(studentIdentityLookup, never()).findStudentIdByUserAccountId(any());
    }

    @Test
    void rejectsUnknownSkillBeforeProjectPersistence() {
        UUID missingSkillId = UUID.randomUUID();
        when(skillLookup.findByIds(any())).thenReturn(Map.of());

        assertThatThrownBy(() -> service.create(createRequest(List.of(missingSkillId))))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(missingSkillId.toString());
        verify(projectRepository, never()).save(any());
        verify(projectSkillRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateSkillIdsBeforeProjectPersistence() {
        UUID skillId = UUID.randomUUID();

        assertThatThrownBy(() -> service.create(createRequest(List.of(skillId, skillId))))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("unique");
        verify(skillLookup, never()).findByIds(any());
        verify(projectRepository, never()).save(any());
    }

    @Test
    void rejectsStaleUpdateBeforeChangingProjectOrSkillLinks() {
        ProjectEntity project = project(studentId, 4L);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> service.update(project.getId(), emptyUpdate(), 3L))
                .isInstanceOf(PreconditionFailedException.class);
        verify(projectRepository, never()).save(any());
        verify(projectSkillRepository, never()).deleteByProjectId(any());
    }

    @Test
    void validatesCombinedDateRangeDuringPartialUpdate() {
        ProjectEntity project = project(studentId, 0L);
        project.setStartDate(LocalDate.parse("2026-01-01"));
        project.setEndDate(LocalDate.parse("2026-03-01"));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        ProjectUpdateRequest request = new ProjectUpdateRequest(
                null, null, null, null, LocalDate.parse("2026-04-01"), null, null, null);

        assertThatThrownBy(() -> service.update(project.getId(), request, 0L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("End date");
        verify(projectRepository, never()).save(any());
    }

    @Test
    void updatesOwnedProjectAndReplacesSkillLinks() {
        ProjectEntity project = project(studentId, 2L);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);
        when(projectSkillRepository.findByIdProjectId(project.getId())).thenReturn(List.of());

        var response = service.update(
                project.getId(),
                new ProjectUpdateRequest("Updated title", null, null, null, null, null, List.of(), false),
                2L);

        assertThat(response.title()).isEqualTo("Updated title");
        assertThat(response.includeInCv()).isFalse();
        verify(projectSkillRepository).deleteByProjectId(project.getId());
        verify(projectRepository).save(project);
        verify(cvFreshnessUpdatePort).markChanged(studentId, CvSourceArea.PROJECTS);
    }

    @Test
    void deletesOwnedProjectWithMatchingVersion() {
        ProjectEntity project = project(studentId, 6L);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        service.delete(project.getId(), 6L);

        verify(projectSkillRepository).deleteByProjectId(project.getId());
        verify(projectRepository).delete(project);
        verify(cvFreshnessUpdatePort).markChanged(studentId, CvSourceArea.PROJECTS);
    }

    private ProjectCreateRequest createRequest(List<UUID> skillIds) {
        return new ProjectCreateRequest(
                "Portfolio API", null, null, null, null, null, skillIds, true);
    }

    private ProjectUpdateRequest emptyUpdate() {
        return new ProjectUpdateRequest(null, null, null, null, null, null, null, null);
    }

    private ProjectEntity project(UUID ownerId, long version) {
        ProjectEntity entity = new ProjectEntity();
        entity.setId(UUID.randomUUID());
        entity.setStudentId(ownerId);
        entity.setTitle("Internship Portal");
        entity.setIncludeInCv(true);
        entity.setVersion(version);
        entity.setCreatedAt(OffsetDateTime.parse("2026-08-16T06:00:00Z"));
        entity.setUpdatedAt(OffsetDateTime.parse("2026-08-16T06:00:00Z"));
        return entity;
    }
}
