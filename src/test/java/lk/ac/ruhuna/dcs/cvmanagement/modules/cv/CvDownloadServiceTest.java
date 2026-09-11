package lk.ac.ruhuna.dcs.cvmanagement.modules.cv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.CvDownloadService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.port.ActiveCvFileResolver;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvFileUnavailableException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventType;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.Test;

class CvDownloadServiceTest {

    @Test
    void successfulStudentDownloadIsAuditedWithCvRevisionOnly() {
        UUID accountId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID cvId = UUID.randomUUID();
        CurrentActorProvider actors = mock(CurrentActorProvider.class);
        StudentRepository students = mock(StudentRepository.class);
        ActiveCvFileResolver resolver = mock(ActiveCvFileResolver.class);
        AuditEventPublisher audit = mock(AuditEventPublisher.class);
        StudentEntity student = new StudentEntity();
        student.setId(studentId);
        when(actors.currentActor()).thenReturn(Optional.of(
                new CurrentActor(accountId, "student@dcs.ruh.ac.lk", Set.of(RoleName.STUDENT))));
        when(students.findByUserAccountId(accountId)).thenReturn(Optional.of(student));
        var expected = new ActiveCvFileResolver.ResolvedCvFile(
                cvId, 7, "cv-safe.pdf", 9, "%PDF-1.7\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        when(resolver.resolve(studentId)).thenReturn(expected);

        var service = new CvDownloadService(actors, students, resolver, audit);
        var actual = service.downloadCurrent();

        assertThat(actual.cvId()).isEqualTo(cvId);
        verify(audit).recordRequired(
                eq(accountId), eq("STUDENT"), eq(AuditEventType.CV_DOWNLOADED_BY_STUDENT.name()),
                eq(AuditEventCategory.CV_MANAGEMENT), eq("CV"), eq(cvId.toString()),
                eq(Map.of("revision", 7, "fileSizeBytes", 9L)));
    }

    @Test
    void unavailableStudentFileIsAuditedBestEffortAndFailsClosed() {
        UUID accountId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        CurrentActorProvider actors = mock(CurrentActorProvider.class);
        StudentRepository students = mock(StudentRepository.class);
        ActiveCvFileResolver resolver = mock(ActiveCvFileResolver.class);
        AuditEventPublisher audit = mock(AuditEventPublisher.class);
        StudentEntity student = new StudentEntity();
        student.setId(studentId);
        when(actors.currentActor()).thenReturn(Optional.of(
                new CurrentActor(accountId, "student@dcs.ruh.ac.lk", Set.of(RoleName.STUDENT))));
        when(students.findByUserAccountId(accountId)).thenReturn(Optional.of(student));
        when(resolver.resolve(studentId)).thenThrow(new CvFileUnavailableException());

        var service = new CvDownloadService(actors, students, resolver, audit);

        assertThatThrownBy(service::downloadCurrent).isInstanceOf(CvFileUnavailableException.class);
        verify(audit).recordBestEffort(
                eq(accountId), eq("STUDENT"), eq(AuditEventType.CV_FILE_UNAVAILABLE.name()),
                eq(AuditEventCategory.CV_MANAGEMENT), eq("STUDENT_CV"), eq(studentId.toString()), any());
    }
}
