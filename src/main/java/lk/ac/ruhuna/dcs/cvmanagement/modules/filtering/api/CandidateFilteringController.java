package lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.request.CandidateFilteringRunRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringCandidateResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.api.dto.response.CandidateFilteringRunResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.CandidateFilteringQueryService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.filtering.application.CandidateFilteringService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only HTTP boundary for deterministic Candidate Filtering runs and current results. */
@RestController
@RequestMapping(ApiPaths.ADMIN_CANDIDATE_FILTERING_RUNS)
public class CandidateFilteringController {

    private final CandidateFilteringService filteringService;
    private final CandidateFilteringQueryService queryService;

    public CandidateFilteringController(
            CandidateFilteringService filteringService,
            CandidateFilteringQueryService queryService) {
        this.filteringService = filteringService;
        this.queryService = queryService;
    }

    /** Creates and audits one immutable filtering run from runtime-only deterministic criteria. */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CandidateFilteringRunResponse> createRun(
            @Valid @RequestBody CandidateFilteringRunRequest request) {
        CandidateFilteringRunResponse response = filteringService.createRun(request);
        URI location = URI.create(ApiPaths.ADMIN_CANDIDATE_FILTERING_RUNS + "/" + response.filterRunId());
        return ResponseEntity.created(location).body(response);
    }

    /** Returns persisted run criteria and a candidate count recomputed from current committed data. */
    @GetMapping(path = "/{filterRunId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CandidateFilteringRunResponse getRun(@PathVariable UUID filterRunId) {
        return queryService.getRun(filterRunId);
    }

    /**
     * Lists deterministic candidates using server-side pagination, search, and constrained sorting.
     *
     * <p>The application service currently fails closed with the public 503 filtering-dependency
     * error after validating the request/run because the approved public response also requires CV
     * and cross-shortlist facts whose authoritative persistence is not implemented yet. A factual
     * downstream enrichment implementation can replace this gate without changing the HTTP contract.
     */
    @GetMapping(path = "/{filterRunId}/candidates", produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<CandidateFilteringCandidateResponse> listCandidates(
            @PathVariable UUID filterRunId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort) {
        return queryService.listCandidates(filterRunId, page, size, search, sort);
    }
}
