package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.CandidateFilteringController;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.request.CandidateFilteringRunRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringCandidateResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringCriteriaResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringRequestSummaryResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringRunResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.CandidateFilteringQueryService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.CandidateFilteringService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillMatchMode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PageMetadata;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

class CandidateFilteringControllerTest {

    private final CandidateFilteringService filteringService = mock(CandidateFilteringService.class);
    private final CandidateFilteringQueryService queryService = mock(CandidateFilteringQueryService.class);
    private final CandidateFilteringController controller =
            new CandidateFilteringController(filteringService, queryService);

    private final UUID requestId = UUID.fromString("95000000-0000-4000-8000-000000000001");
    private final UUID companyId = UUID.fromString("95000000-0000-4000-8000-000000000002");
    private final UUID runId = UUID.fromString("95000000-0000-4000-8000-000000000003");

    @Test
    void createReturns201AndCanonicalRunLocation() {
        CandidateFilteringRunRequest request = new CandidateFilteringRunRequest(
                requestId, new BigDecimal("3.00"), null, List.of(), List.of(), FilterSkillMatchMode.AND);
        CandidateFilteringRunResponse response = runResponse();
        when(filteringService.createRun(request)).thenReturn(response);

        var actual = controller.createRun(request);

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(actual.getHeaders().getFirst(HttpHeaders.LOCATION))
                .isEqualTo("/api/v1/admin/candidate-filtering/runs/" + runId);
        assertThat(actual.getBody()).isEqualTo(response);
        verify(filteringService).createRun(request);
    }

    @Test
    void getDelegatesToQueryService() {
        CandidateFilteringRunResponse response = runResponse();
        when(queryService.getRun(runId)).thenReturn(response);

        assertThat(controller.getRun(runId)).isEqualTo(response);
        verify(queryService).getRun(runId);
    }

    @Test
    void listCandidatesDelegatesAllServerSideQueryParameters() {
        PagedResponse<CandidateFilteringCandidateResponse> response = new PagedResponse<>(
                List.of(),
                new PageMetadata(2, 20, 0, 0, "fullName,asc"));
        when(queryService.listCandidates(runId, 2, 20, "ann", "fullName,asc")).thenReturn(response);

        assertThat(controller.listCandidates(runId, 2, 20, "ann", "fullName,asc"))
                .isSameAs(response);
        verify(queryService).listCandidates(runId, 2, 20, "ann", "fullName,asc");
    }

    private CandidateFilteringRunResponse runResponse() {
        return new CandidateFilteringRunResponse(
                runId,
                new CandidateFilteringRequestSummaryResponse(
                        requestId, companyId, "Example Company", "Backend Intern", 6),
                new CandidateFilteringCriteriaResponse(
                        requestId,
                        new BigDecimal("3.00"),
                        null,
                        List.of(),
                        List.of(),
                        FilterSkillMatchMode.AND),
                4,
                OffsetDateTime.parse("2026-08-17T12:00:00Z"));
    }
}
