package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerApiException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicRecordSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.AcademicRecordQueryRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

class AcademicRecordQueryServiceTest {
    private final AcademicRecordQueryRepository repository = mock(AcademicRecordQueryRepository.class);
    private final CurrentActorProvider actorProvider = mock(CurrentActorProvider.class);
    private final AcademicRecordQueryService service = new AcademicRecordQueryService(repository, actorProvider);

    @Test
    void normalizesAndForwardsApprovedQueryParameters() {
        UUID studentId = UUID.randomUUID();
        when(actorProvider.currentActor()).thenReturn(Optional.of(adminActor()));
        when(repository.search(any(), any(), any(), anyInt(), anyInt(), any())).thenReturn(new PageImpl<>(List.of()));

        service.listAdminRecords(1, 25, "courseCode,asc", "  Network  ", "CSC2113", studentId.toString());

        verify(repository).search(
                eq("network"), eq("CSC2113"), eq(studentId), eq(1), eq(25), eq(AcademicRecordSort.COURSE_CODE_ASC));
    }

    @Test
    void rejectsInvalidBoundsFiltersAndSortBeforeQuerying() {
        when(actorProvider.currentActor()).thenReturn(Optional.of(adminActor()));
        assertThatThrownBy(() -> service.listAdminRecords(-1, 20, null, null, null, null)).isInstanceOf(AcademicLedgerApiException.class);
        assertThatThrownBy(() -> service.listAdminRecords(0, 101, null, null, null, null)).isInstanceOf(AcademicLedgerApiException.class);
        assertThatThrownBy(() -> service.listAdminRecords(0, 20, "courseTitle,desc", null, null, null)).isInstanceOf(AcademicLedgerApiException.class);
        assertThatThrownBy(() -> service.listAdminRecords(0, 20, null, "   ", null, null)).isInstanceOf(AcademicLedgerApiException.class);
        assertThatThrownBy(() -> service.listAdminRecords(0, 20, null, null, "CSC 2113", null)).isInstanceOf(AcademicLedgerApiException.class);
        assertThatThrownBy(() -> service.listAdminRecords(0, 20, null, null, null, "not-a-uuid")).isInstanceOf(AcademicLedgerApiException.class);
    }

    @Test
    void mapsDatabaseReadFailureToCanonicalAcademicDataUnavailableResponse() {
        when(actorProvider.currentActor()).thenReturn(Optional.of(adminActor()));
        when(repository.search(any(), any(), any(), anyInt(), anyInt(), any()))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        var thrown = org.assertj.core.api.Assertions.catchThrowableOfType(
                AcademicLedgerApiException.class,
                () -> service.listAdminRecords(null, null, null, null, null, null));

        assertThat(thrown.status()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(thrown.code()).isEqualTo("ACADEMIC_DATA_UNAVAILABLE");
    }

    @Test
    void requiresAnAdminActorEvenBehindTheAdminRoute() {
        when(actorProvider.currentActor()).thenReturn(Optional.of(new CurrentActor(
                UUID.randomUUID(), "student@dcs.ruh.ac.lk", Set.of(RoleName.STUDENT))));
        assertThatThrownBy(() -> service.listAdminRecords(null, null, null, null, null, null))
                .isInstanceOf(AcademicLedgerApiException.class);
    }

    private CurrentActor adminActor() {
        return new CurrentActor(UUID.randomUUID(), "admin@dcs.ruh.ac.lk", Set.of(RoleName.ADMIN));
    }
}
