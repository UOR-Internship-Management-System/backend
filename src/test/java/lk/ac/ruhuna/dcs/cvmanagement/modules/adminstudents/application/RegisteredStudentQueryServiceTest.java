package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request.AdminStudentSearchCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception.AdminStudentApiException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.RegisteredStudentSort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.mapper.AdminStudentMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.RegisteredStudentRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.query.RegisteredStudentReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

class RegisteredStudentQueryServiceTest {

    private final RegisteredStudentReadRepository repository = mock(RegisteredStudentReadRepository.class);
    private final CurrentActorProvider actorProvider = mock(CurrentActorProvider.class);
    private final AdminStudentMapper mapper = new AdminStudentMapper();
    private final RegisteredStudentQueryService service =
            new RegisteredStudentQueryService(repository, mapper, actorProvider);

    @Test
    void normalizesAndForwardsApprovedRosterCriteria() {
        when(actorProvider.currentActor()).thenReturn(Optional.of(adminActor()));
        var row = new RegisteredStudentRow(
                UUID.randomUUID(),
                "SC/2022/12345",
                "Kavindu Lakshan",
                "sc202212345@dcs.ruh.ac.lk",
                "2022",
                4,
                new BigDecimal("3.70"));
        when(repository.search(any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(new PageImpl<>(List.of(row), PageRequest.of(1, 50), 51));

        var response = service.list(new AdminStudentSearchCriteria(
                1, 50, "officialGpa,desc", "  KAVINDU  ", 4));

        verify(repository).search("kavindu", 4, 1, 50, RegisteredStudentSort.GPA_DESC);
        assertThat(response.page().page()).isEqualTo(1);
        assertThat(response.page().size()).isEqualTo(50);
        assertThat(response.page().totalElements()).isEqualTo(51);
        assertThat(response.page().sort()).isEqualTo("officialGpa,desc");
        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.fullName()).isEqualTo("Kavindu Lakshan");
            assertThat(item.degreeProgram()).isEqualTo("BSc Honours in Computer Science");
            assertThat(item.officialGpa()).isEqualByComparingTo("3.70");
        });
    }

    @Test
    void blankSearchUsesDefaultPagingAndDoesNotCreateASearchPredicate() {
        when(actorProvider.currentActor()).thenReturn(Optional.of(adminActor()));
        when(repository.search(any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        service.list(new AdminStudentSearchCriteria(null, null, null, "   ", null));

        verify(repository).search(null, null, 0, 20, RegisteredStudentSort.FULL_NAME_ASC);
    }

    @Test
    void rejectsInvalidBoundsLevelAndSortBeforeQuerying() {
        when(actorProvider.currentActor()).thenReturn(Optional.of(adminActor()));

        assertBadRequest(new AdminStudentSearchCriteria(-1, 20, null, null, null));
        assertBadRequest(new AdminStudentSearchCriteria(0, 0, null, null, null));
        assertBadRequest(new AdminStudentSearchCriteria(0, 101, null, null, null));
        assertBadRequest(new AdminStudentSearchCriteria(0, 20, null, null, 2));
        assertBadRequest(new AdminStudentSearchCriteria(0, 20, "fullName,desc", null, null));
        assertBadRequest(new AdminStudentSearchCriteria(0, 20, null, "x".repeat(121), null));

        verifyNoInteractions(repository);
    }

    @Test
    void mapsDatabaseReadFailureToAcademicDataUnavailable() {
        when(actorProvider.currentActor()).thenReturn(Optional.of(adminActor()));
        when(repository.search(any(), any(), anyInt(), anyInt(), any()))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        AdminStudentApiException thrown = org.assertj.core.api.Assertions.catchThrowableOfType(
                AdminStudentApiException.class,
                () -> service.list(new AdminStudentSearchCriteria(null, null, null, null, null)));

        assertThat(thrown.status()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(thrown.code()).isEqualTo("ACADEMIC_DATA_UNAVAILABLE");
    }

    @Test
    void requiresAnAuthenticatedAdminEvenBehindTheAdminRoute() {
        when(actorProvider.currentActor()).thenReturn(Optional.empty());
        AdminStudentApiException unauthorized = org.assertj.core.api.Assertions.catchThrowableOfType(
                AdminStudentApiException.class,
                () -> service.list(new AdminStudentSearchCriteria(null, null, null, null, null)));
        assertThat(unauthorized.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(unauthorized.code()).isEqualTo("UNAUTHORIZED");

        when(actorProvider.currentActor()).thenReturn(Optional.of(new CurrentActor(
                UUID.randomUUID(), "student@dcs.ruh.ac.lk", Set.of(RoleName.STUDENT))));
        AdminStudentApiException forbidden = org.assertj.core.api.Assertions.catchThrowableOfType(
                AdminStudentApiException.class,
                () -> service.list(new AdminStudentSearchCriteria(null, null, null, null, null)));
        assertThat(forbidden.status()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(forbidden.code()).isEqualTo("FORBIDDEN");

        verifyNoInteractions(repository);
    }

    private void assertBadRequest(AdminStudentSearchCriteria criteria) {
        assertThatThrownBy(() -> service.list(criteria))
                .isInstanceOfSatisfying(AdminStudentApiException.class, exception -> {
                    assertThat(exception.status()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    private CurrentActor adminActor() {
        return new CurrentActor(UUID.randomUUID(), "admin@dcs.ruh.ac.lk", Set.of(RoleName.ADMIN));
    }
}
