package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.request.ShortlistCandidateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.request.ShortlistCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.request.ShortlistFinalizeRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.ShortlistCandidateMutationResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.ShortlistDetailResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.ShortlistFinalizeResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.ShortlistResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.application.ShortlistFinalizationService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.application.ShortlistService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.domain.policy.ShortlistStatus;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.IfMatchSupport;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only HTTP boundary for shortlist lifecycle management. */
@RestController
@RequestMapping(ApiPaths.ADMIN_SHORTLISTS)
public class ShortlistController {

    private final ShortlistService shortlistService;
    private final ShortlistFinalizationService finalizationService;

    public ShortlistController(
            ShortlistService shortlistService,
            ShortlistFinalizationService finalizationService) {
        this.shortlistService = shortlistService;
        this.finalizationService = finalizationService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<ShortlistResponse> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) ShortlistStatus status,
            @RequestParam(required = false) UUID companyId) {
        return shortlistService.list(page, size, search, sort, status, companyId);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ShortlistResponse> create(@Valid @RequestBody ShortlistCreateRequest request) {
        ShortlistResponse response = shortlistService.create(request);
        return ResponseEntity.created(URI.create(ApiPaths.ADMIN_SHORTLISTS + "/" + response.shortlistId()))
                .eTag(IfMatchSupport.formatVersion(response.version()))
                .body(response);
    }

    @GetMapping(path = "/{shortlistId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ShortlistDetailResponse> getDetail(
            @PathVariable UUID shortlistId,
            @RequestParam(required = false) Integer candidatePage,
            @RequestParam(required = false) Integer candidateSize,
            @RequestParam(required = false) String candidateSearch,
            @RequestParam(required = false, name = "sort") String candidateSort) {
        ShortlistDetailResponse response = shortlistService.getDetail(
                shortlistId, candidatePage, candidateSize, candidateSearch, candidateSort);
        return ResponseEntity.ok()
                .eTag(IfMatchSupport.formatVersion(response.shortlist().version()))
                .body(response);
    }

    @PostMapping(
            path = "/{shortlistId}/candidates",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ShortlistCandidateMutationResponse> addCandidates(
            @PathVariable UUID shortlistId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @Valid @RequestBody ShortlistCandidateRequest request) {
        ShortlistCandidateMutationResponse response = shortlistService.addCandidates(
                shortlistId, request, IfMatchSupport.parseVersion(ifMatch));
        return ResponseEntity.ok()
                .eTag(IfMatchSupport.formatVersion(response.version()))
                .body(response);
    }

    @DeleteMapping(path = "/{shortlistId}/candidates/{studentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ShortlistCandidateMutationResponse> removeCandidate(
            @PathVariable UUID shortlistId,
            @PathVariable UUID studentId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch) {
        ShortlistCandidateMutationResponse response = shortlistService.removeCandidate(
                shortlistId, studentId, IfMatchSupport.parseVersion(ifMatch));
        return ResponseEntity.ok()
                .eTag(IfMatchSupport.formatVersion(response.version()))
                .body(response);
    }

    @PostMapping(
            path = "/{shortlistId}/finalize",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ShortlistFinalizeResponse> finalizeShortlist(
            @PathVariable UUID shortlistId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @Valid @RequestBody ShortlistFinalizeRequest request) {
        ShortlistFinalizeResponse response = finalizationService.finalizeShortlist(
                shortlistId, request, IfMatchSupport.parseVersion(ifMatch));
        return ResponseEntity.ok()
                .eTag(IfMatchSupport.formatVersion(response.version()))
                .body(response);
    }
}
