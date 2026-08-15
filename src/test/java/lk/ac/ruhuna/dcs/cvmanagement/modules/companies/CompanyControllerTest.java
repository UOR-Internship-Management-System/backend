package lk.ac.ruhuna.dcs.cvmanagement.modules.companies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.CompanyController;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanyRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanySearchCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanyUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.response.CompanyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.application.CompanyService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PageMetadata;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

class CompanyControllerTest {

    private final CompanyService service = mock(CompanyService.class);
    private final CompanyController controller = new CompanyController(service);

    @Test
    void delegatesListControlsWithoutChangingContract() {
        PagedResponse<CompanyResponse> expected =
                new PagedResponse<>(List.of(), new PageMetadata(2, 50, 0, 0, "updatedAt,desc"));
        when(service.list(any())).thenReturn(expected);

        assertThat(controller.list(2, 50, "updatedAt,desc", "Example")).isSameAs(expected);

        ArgumentCaptor<CompanySearchCriteria> captor = ArgumentCaptor.forClass(CompanySearchCriteria.class);
        verify(service).list(captor.capture());
        assertThat(captor.getValue())
                .isEqualTo(new CompanySearchCriteria(2, 50, "updatedAt,desc", "Example"));
    }

    @Test
    void createReturnsLocationAndStrongEtag() {
        CompanyRequest request = new CompanyRequest(
                "Example Technologies", null, null, null, null, null);
        CompanyResponse response = response(0L);
        when(service.create(request)).thenReturn(response);

        var actual = controller.create(request);

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(actual.getHeaders().getFirst(HttpHeaders.LOCATION))
                .isEqualTo("/api/v1/admin/companies/" + response.companyId());
        assertThat(actual.getHeaders().getETag()).isEqualTo("\"0\"");
        assertThat(actual.getBody()).isSameAs(response);
    }

    @Test
    void detailReturnsCurrentStrongEtag() {
        CompanyResponse response = response(3L);
        when(service.get(response.companyId())).thenReturn(response);

        var actual = controller.get(response.companyId());

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getHeaders().getETag()).isEqualTo("\"3\"");
        assertThat(actual.getBody()).isSameAs(response);
    }

    @Test
    void updateParsesQuotedIfMatchAndReturnsNewEtag() {
        CompanyResponse response = response(5L);
        CompanyUpdateRequest request = new CompanyUpdateRequest();
        request.setNotes("updated");
        when(service.update(eq(response.companyId()), eq(request), eq(4L))).thenReturn(response);

        var actual = controller.update(response.companyId(), "\"4\"", request);

        verify(service).update(response.companyId(), request, 4L);
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getHeaders().getETag()).isEqualTo("\"5\"");
    }

    @Test
    void deleteParsesQuotedIfMatchBeforeDelegating() {
        UUID companyId = UUID.randomUUID();

        var actual = controller.delete(companyId, "\"7\"");

        verify(service).delete(companyId, 7L);
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private CompanyResponse response(long version) {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-15T12:00:00Z");
        return new CompanyResponse(
                UUID.randomUUID(),
                "Example Technologies",
                "https://example.com",
                "HR Coordinator",
                "hr@example.com",
                "+94 11 234 5678",
                null,
                version,
                now,
                now);
    }
}
