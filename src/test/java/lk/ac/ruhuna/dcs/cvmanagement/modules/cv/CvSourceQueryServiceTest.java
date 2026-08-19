package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.StudentAcademicSummaryRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvSourceQueryService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvConfigurationInvalidException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.repository.ProjectRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.repository.ProjectSkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository.DeclaredSkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.WorkExperienceEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.ActivityRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.AwardRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.CertificateRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.ContactLinkRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentProfileRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.WorkExperienceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CvSourceQueryServiceTest {

    private final StudentProfileRepository profileRepository = mock(StudentProfileRepository.class);
    private final ContactLinkRepository contactRepository = mock(ContactLinkRepository.class);
    private final WorkExperienceRepository experienceRepository = mock(WorkExperienceRepository.class);
    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final ProjectSkillRepository projectSkillRepository = mock(ProjectSkillRepository.class);
    private final CertificateRepository certificateRepository = mock(CertificateRepository.class);
    private final AwardRepository awardRepository = mock(AwardRepository.class);
    private final ActivityRepository activityRepository = mock(ActivityRepository.class);
    private final DeclaredSkillRepository skillRepository = mock(DeclaredSkillRepository.class);
    private final StudentAcademicSummaryRepository academicRepository = mock(StudentAcademicSummaryRepository.class);
    private final CvSourceQueryService service = new CvSourceQueryService(
            profileRepository, contactRepository, experienceRepository, projectRepository, projectSkillRepository,
            certificateRepository, awardRepository, activityRepository, skillRepository, academicRepository);

    private StudentEntity student;

    @BeforeEach
    void defaults() {
        student = new StudentEntity();
        student.setId(UUID.randomUUID());
        student.setFullName("Student");
        student.setUniversityEmail("student@ruh.ac.lk");
        student.setUpdatedAt(OffsetDateTime.parse("2026-08-19T02:00:00Z"));
        when(profileRepository.findByStudentId(student.getId())).thenReturn(Optional.empty());
        when(contactRepository.findAllByStudentIdAndCvIncludeTrueOrderByDisplayOrderAscLabelAscIdAsc(student.getId()))
                .thenReturn(List.of());
        when(skillRepository.findCvSkills(student.getId())).thenReturn(List.of());
        when(academicRepository.findById(student.getId())).thenReturn(Optional.empty());
    }

    @Test
    void rejectsTheWholeConfigurationWhenAnySelectedIdIsNotOwnedByTheStudent() {
        UUID requestedId = UUID.randomUUID();
        when(experienceRepository.findAllByStudentIdAndIdIn(eq(student.getId()), anyCollection()))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.load(student,
                        new CvConfiguration(List.of(requestedId), List.of(), List.of(), List.of(), List.of())))
                .isInstanceOf(CvConfigurationInvalidException.class)
                .hasMessageNotContaining(requestedId.toString());
    }

    @Test
    void submittedSelectionIsAuthoritativeEvenWhenSourceDefaultIncludeFlagIsFalse() {
        UUID experienceId = UUID.randomUUID();
        WorkExperienceEntity experience = new WorkExperienceEntity();
        experience.setId(experienceId);
        experience.setStudentId(student.getId());
        experience.setOrganization("Example Ltd");
        experience.setPositionTitle("Intern");
        experience.setStartDate(LocalDate.of(2026, 1, 1));
        experience.setCurrentRole(true);
        experience.setCvInclude(false);
        experience.setVersion(1L);
        experience.setUpdatedAt(student.getUpdatedAt());
        when(experienceRepository.findAllByStudentIdAndIdIn(eq(student.getId()), anyCollection()))
                .thenReturn(List.of(experience));

        var document = service.load(student,
                new CvConfiguration(List.of(experienceId), List.of(), List.of(), List.of(), List.of()));

        assertThat(document.experiences()).extracting(item -> item.id()).containsExactly(experienceId);
    }
}
