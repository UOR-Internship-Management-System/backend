package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request.StudentProfileUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.application.StudentProfileService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.mapper.StudentProfileMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentProfileEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.ActivityRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.AwardRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.CertificateRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.ContactLinkRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.EducationRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentProfileRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.WorkExperienceRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.cv.CvSourceFreshnessUpdatePort;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ValidationException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.files.ProfileFileService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Regression coverage for clearing a nullable Profile field: a request must distinguish an
 * explicitly supplied {@code null} (clear the field) from an omitted key (leave it untouched).
 */
class StudentProfileUpdateTest {

    private StudentProfileService service;
    private StudentProfileRepository studentProfileRepository;
    private StudentProfileEntity existingProfile;

    @BeforeEach
    void setUp() {
        CurrentActorProvider actorProvider = mock(CurrentActorProvider.class);
        StudentRepository studentRepository = mock(StudentRepository.class);
        studentProfileRepository = mock(StudentProfileRepository.class);
        ContactLinkRepository contactLinkRepository = mock(ContactLinkRepository.class);
        EducationRepository educationRepository = mock(EducationRepository.class);
        CertificateRepository certificateRepository = mock(CertificateRepository.class);
        AwardRepository awardRepository = mock(AwardRepository.class);
        ActivityRepository activityRepository = mock(ActivityRepository.class);
        WorkExperienceRepository workExperienceRepository = mock(WorkExperienceRepository.class);
        StudentProfileMapper mapper = mock(StudentProfileMapper.class);
        CvSourceFreshnessUpdatePort freshnessPort = mock(CvSourceFreshnessUpdatePort.class);
        ProfileFileService profileFileService = mock(ProfileFileService.class);

        UUID accountId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        StudentEntity student = new StudentEntity();
        student.setId(studentId);

        when(actorProvider.currentActor()).thenReturn(
            Optional.of(new CurrentActor(accountId, "student@ruh.ac.lk", Set.of(RoleName.STUDENT))));
        when(studentRepository.findByUserAccountId(accountId)).thenReturn(Optional.of(student));

        existingProfile = new StudentProfileEntity();
        existingProfile.setId(UUID.randomUUID());
        existingProfile.setStudentId(studentId);
        existingProfile.setSummary("Existing summary text");
        when(studentProfileRepository.findByStudentId(studentId)).thenReturn(Optional.of(existingProfile));
        when(studentProfileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any(), any(), any())).thenReturn(null);

        service = new StudentProfileService(
            actorProvider,
            studentRepository,
            studentProfileRepository,
            contactLinkRepository,
            educationRepository,
            certificateRepository,
            awardRepository,
            activityRepository,
            workExperienceRepository,
            mapper,
            freshnessPort,
            profileFileService);
    }

    @Test
    void explicitlyNullSummaryClearsThePreviouslySavedValue() {
        StudentProfileUpdateRequest request = fromJson("{\"summary\": null}");

        service.updateMyProfile(request);

        assertThat(existingProfile.getSummary()).isNull();
    }

    @Test
    void omittingSummaryLeavesThePreviouslySavedValueUntouched() {
        StudentProfileUpdateRequest request = fromJson("{\"headline\": \"Updated headline\"}");

        service.updateMyProfile(request);

        assertThat(existingProfile.getSummary()).isEqualTo("Existing summary text");
        assertThat(existingProfile.getHeadline()).isEqualTo("Updated headline");
    }

    @Test
    void emptyRequestBodyIsRejected() {
        StudentProfileUpdateRequest request = fromJson("{}");

        assertThatThrownBy(() -> service.updateMyProfile(request))
            .isInstanceOf(ValidationException.class);
    }

    private StudentProfileUpdateRequest fromJson(String json) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(json, StudentProfileUpdateRequest.class);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
