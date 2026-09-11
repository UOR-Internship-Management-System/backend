package lk.ac.ruhuna.dcs.cvmanagement.modules.internships;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.InternshipRequestController;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequestCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequestUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequiredSkillRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.response.InternshipCompanyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.response.InternshipRequestResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.response.InternshipRequiredSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.application.InternshipRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

class InternshipRequestControllerTest {

    private final InternshipRequestService service = mock(InternshipRequestService.class);
    private final InternshipRequestController controller = new InternshipRequestController(service);

    @Test
    void createReturnsLocationAndStrongEtag() {
        UUID companyId = UUID.randomUUID();
        InternshipRequestCreateRequest request = new InternshipRequestCreateRequest(
                companyId, "Intern", null, null, List.of());
        InternshipRequestResponse response = response(0L, companyId);
        when(service.create(request)).thenReturn(response);

        var actual = controller.create(request);
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(actual.getHeaders().getFirst(HttpHeaders.LOCATION))
                .isEqualTo("/api/v1/admin/internship-requests/" + response.requestId());
        assertThat(actual.getHeaders().getETag()).isEqualTo("\"0\"");
    }

    @Test
    void updateParsesQuotedIfMatchAndReturnsNewEtag() {
        UUID companyId = UUID.randomUUID();
        InternshipRequestResponse response = response(5L, companyId);
        InternshipRequestUpdateRequest request = new InternshipRequestUpdateRequest();
        request.setTitle("Updated Intern");
        when(service.update(eq(response.requestId()), eq(request), eq(4L))).thenReturn(response);

        var actual = controller.update(response.requestId(), "\"4\"", request);
        verify(service).update(response.requestId(), request, 4L);
        assertThat(actual.getHeaders().getETag()).isEqualTo("\"5\"");
    }

    @Test
    void addSkillReturnsUpdatedParentEtag() {
        UUID requestId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        var body = new InternshipRequiredSkillRequest(skillId);
        var skill = new InternshipRequiredSkillResponse(UUID.randomUUID(), skillId, "Java");
        when(service.addRequiredSkill(requestId, body, 2L))
                .thenReturn(new InternshipRequestService.RequiredSkillMutationResult(skill, 3L));

        var actual = controller.addRequiredSkill(requestId, "\"2\"", body);
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(actual.getHeaders().getETag()).isEqualTo("\"3\"");
        assertThat(actual.getBody()).isSameAs(skill);
    }

    private InternshipRequestResponse response(long version, UUID companyId) {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-15T15:00:00Z");
        return new InternshipRequestResponse(UUID.randomUUID(),
                new InternshipCompanyResponse(companyId, "Example", null, null, null, null, null, 0L, now, now),
                "Intern", null, null, List.of(), version, now, now);
    }
}
