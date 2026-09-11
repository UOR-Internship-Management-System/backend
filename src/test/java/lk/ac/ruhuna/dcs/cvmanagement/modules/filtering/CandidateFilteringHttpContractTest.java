package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.CandidateFilteringController;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.request.CandidateFilteringRunRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringCriteriaResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringRequestSummaryResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringRunResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.CandidateFilteringQueryService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.CandidateFilteringService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.FilterDependencyUnavailableException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.FilterRunNotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.exception.InvalidFilterCriteriaException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.domain.policy.FilterSkillMatchMode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.GlobalExceptionHandler;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ProblemDetailsFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class CandidateFilteringHttpContractTest {

    private final CandidateFilteringService filteringService = mock(CandidateFilteringService.class);
    private final CandidateFilteringQueryService queryService = mock(CandidateFilteringQueryService.class);
    private MockMvc mockMvc;

    private final UUID requestId = UUID.fromString("95100000-0000-4000-8000-000000000001");
    private final UUID companyId = UUID.fromString("95100000-0000-4000-8000-000000000002");
    private final UUID runId = UUID.fromString("95100000-0000-4000-8000-000000000003");

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new CandidateFilteringController(filteringService, queryService))
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler(new ProblemDetailsFactory()))
                .build();
    }

    @Test
    void createMatchesRunContractAndReturnsLocation() throws Exception {
        when(filteringService.createRun(any(CandidateFilteringRunRequest.class))).thenReturn(runResponse());

        mockMvc.perform(post("/api/v1/admin/candidate-filtering/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId":"%s",
                                  "runtimeGpaLowerBound":3.00,
                                  "requestSkillIds":[],
                                  "additionalSkillIds":[],
                                  "skillMatchMode":"AND"
                                }
                                """.formatted(requestId)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        "/api/v1/admin/candidate-filtering/runs/" + runId))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.filterRunId").value(runId.toString()))
                .andExpect(jsonPath("$.request.requestId").value(requestId.toString()))
                .andExpect(jsonPath("$.request.companyId").value(companyId.toString()))
                .andExpect(jsonPath("$.criteria.runtimeGpaLowerBound").value(3.0))
                .andExpect(jsonPath("$.criteria.runtimeGpaUpperBound").value(nullValue()))
                .andExpect(jsonPath("$.criteria.skillMatchMode").value("AND"))
                .andExpect(jsonPath("$.candidateCount").value(4));
    }

    @Test
    void createRejectsMissingRequiredFieldsBeforeService() throws Exception {
        mockMvc.perform(post("/api/v1/admin/candidate-filtering/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestSkillIds\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void createMapsSemanticCriteriaFailureTo422() throws Exception {
        when(filteringService.createRun(any(CandidateFilteringRunRequest.class)))
                .thenThrow(new InvalidFilterCriteriaException("Minimum GPA cannot exceed maximum GPA."));

        mockMvc.perform(post("/api/v1/admin/candidate-filtering/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId":"%s",
                                  "runtimeGpaLowerBound":3.50,
                                  "runtimeGpaUpperBound":3.00,
                                  "skillMatchMode":"AND"
                                }
                                """.formatted(requestId)))
                .andExpect(status().is(422))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_FILTER_CRITERIA"));
    }

    @Test
    void getUnknownRunMapsToStable404() throws Exception {
        when(queryService.getRun(runId)).thenThrow(new FilterRunNotFoundException());

        mockMvc.perform(get("/api/v1/admin/candidate-filtering/runs/{filterRunId}", runId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("FILTER_RUN_NOT_FOUND"));
    }

    @Test
    void malformedRunIdMapsToValidationProblem() throws Exception {
        mockMvc.perform(get("/api/v1/admin/candidate-filtering/runs/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void candidateEndpointFailsClosedWithDocumented503UntilFactualEnrichmentExists() throws Exception {
        when(queryService.listCandidates(
                        eq(runId),
                        nullable(Integer.class),
                        nullable(Integer.class),
                        nullable(String.class),
                        nullable(String.class)))
                .thenThrow(new FilterDependencyUnavailableException(
                        "Candidate CV and shortlist enrichment data is not available from authoritative persistence."));

        mockMvc.perform(get("/api/v1/admin/candidate-filtering/runs/{filterRunId}/candidates", runId))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("FILTER_DEPENDENCY_UNAVAILABLE"))
                .andExpect(jsonPath("$.title").value("Filtering data unavailable"));
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
