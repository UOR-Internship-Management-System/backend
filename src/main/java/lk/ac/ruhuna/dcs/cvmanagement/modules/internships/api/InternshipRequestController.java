package lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequestCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequestSearchCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequestUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.request.InternshipRequiredSkillRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.response.InternshipRequestResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.api.dto.response.InternshipRequiredSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.internships.application.InternshipRequestService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.IfMatchSupport;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only HTTP boundary for Internship Request management. */
@RestController
@RequestMapping(ApiPaths.ADMIN_INTERNSHIP_REQUESTS)
public class InternshipRequestController {

    private final InternshipRequestService service;

    public InternshipRequestController(InternshipRequestService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<InternshipRequestResponse> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID companyId) {
        return service.list(new InternshipRequestSearchCriteria(page, size, sort, search, companyId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InternshipRequestResponse> create(@Valid @RequestBody InternshipRequestCreateRequest request) {
        InternshipRequestResponse response = service.create(request);
        return ResponseEntity.created(URI.create(ApiPaths.ADMIN_INTERNSHIP_REQUESTS + "/" + response.requestId()))
                .eTag(IfMatchSupport.formatVersion(response.version()))
                .body(response);
    }

    @GetMapping(path = "/{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InternshipRequestResponse> get(@PathVariable UUID requestId) {
        InternshipRequestResponse response = service.get(requestId);
        return ResponseEntity.ok().eTag(IfMatchSupport.formatVersion(response.version())).body(response);
    }

    @PatchMapping(path = "/{requestId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InternshipRequestResponse> update(
            @PathVariable UUID requestId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestBody InternshipRequestUpdateRequest request) {
        InternshipRequestResponse response = service.update(requestId, request, IfMatchSupport.parseVersion(ifMatch));
        return ResponseEntity.ok().eTag(IfMatchSupport.formatVersion(response.version())).body(response);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID requestId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch) {
        service.delete(requestId, IfMatchSupport.parseVersion(ifMatch));
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/{requestId}/required-skills", produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<InternshipRequiredSkillResponse> listRequiredSkills(
            @PathVariable UUID requestId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return service.listRequiredSkills(requestId, page, size);
    }

    @PostMapping(path = "/{requestId}/required-skills", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InternshipRequiredSkillResponse> addRequiredSkill(
            @PathVariable UUID requestId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @Valid @RequestBody InternshipRequiredSkillRequest request) {
        var result = service.addRequiredSkill(requestId, request, IfMatchSupport.parseVersion(ifMatch));
        return ResponseEntity.status(201)
                .eTag(IfMatchSupport.formatVersion(result.requestVersion()))
                .body(result.skill());
    }

    @DeleteMapping("/{requestId}/required-skills/{requiredSkillId}")
    public ResponseEntity<Void> removeRequiredSkill(
            @PathVariable UUID requestId,
            @PathVariable UUID requiredSkillId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch) {
        service.removeRequiredSkill(requestId, requiredSkillId, IfMatchSupport.parseVersion(ifMatch));
        return ResponseEntity.noContent().build();
    }
}
