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
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.Test;

class AdminStudentInspectionServiceTest {

    private final RegisteredStudentReadRepository registeredRepository = mock(RegisteredStudentReadRepository.class);
    private final AdminStudentDetailReadRepository detailRepository = mock(AdminStudentDetailReadRepository.class);
    private final CurrentActorProvider currentActorProvider = mock(CurrentActorProvider.class);
    private final AdminStudentInspectionService service = new AdminStudentInspectionService(
            registeredRepository,
            detailRepository,
            new AdminStudentMapper(),
            currentActorProvider);

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

    private CurrentActor adminActor() {
        return new CurrentActor(
                UUID.randomUUID(),
                "admin@dcs.ruh.ac.lk",
                Set.of(RoleName.ADMIN));
    }
}
