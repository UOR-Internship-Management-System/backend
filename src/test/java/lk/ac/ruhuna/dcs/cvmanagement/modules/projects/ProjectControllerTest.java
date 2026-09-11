package lk.ac.ruhuna.dcs.cvmanagement.modules.projects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.ProjectController;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.request.ProjectCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.request.ProjectUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.api.dto.response.ProjectResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.application.ProjectService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.BadRequestException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PageMetadata;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ProjectControllerTest {

    private final ProjectService service = mock(ProjectService.class);
    private final ProjectController controller = new ProjectController(service);

    @Test
    void delegatesStudentProjectListingControls() {
        PagedResponse<ProjectResponse> expected =
                new PagedResponse<>(List.of(), new PageMetadata(0, 20, 0, 0, "updatedAt,desc"));
        when(service.list("portal", 0, 20, "updatedAt,desc")).thenReturn(expected);

        assertThat(controller.list("portal", 0, 20, "updatedAt,desc")).isSameAs(expected);
    }

    @Test
    void delegatesCreateWithoutChangingResponseContract() {
        ProjectCreateRequest request =
                new ProjectCreateRequest("Portfolio", null, null, null, null, null, List.of(), true);
        ProjectResponse expected = response(0L);
        when(service.create(request)).thenReturn(expected);

        assertThat(controller.create(request)).isSameAs(expected);
    }

    @Test
    void parsesQuotedIfMatchDuringUpdate() {
        UUID projectId = UUID.randomUUID();
        ProjectUpdateRequest request =
                new ProjectUpdateRequest("Updated", null, null, null, null, null, null, null);
        ProjectResponse expected = response(4L);
        when(service.update(eq(projectId), eq(request), eq(3L))).thenReturn(expected);

        assertThat(controller.update(projectId, request, "\"3\"")).isSameAs(expected);
        verify(service).update(projectId, request, 3L);
    }

    @Test
    void rejectsMalformedIfMatchBeforeCallingService() {
        UUID projectId = UUID.randomUUID();

        assertThatThrownBy(() -> controller.update(projectId, new ProjectUpdateRequest(
                null, null, null, null, null, null, null, null), "invalid"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deleteParsesIfMatchAndReturnsNoContent() {
        UUID projectId = UUID.randomUUID();

        var response = controller.delete(projectId, "\"7\"");

        verify(service).delete(projectId, 7L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private ProjectResponse response(long version) {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-16T06:00:00Z");
        return new ProjectResponse(
                UUID.randomUUID(), "Portfolio", null, null, null, null, null, List.of(), true, version, now, now);
    }
}
