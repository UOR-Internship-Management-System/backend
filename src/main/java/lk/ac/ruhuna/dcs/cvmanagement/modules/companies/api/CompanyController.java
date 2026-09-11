package lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanyRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanySearchCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.request.CompanyUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.api.dto.response.CompanyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.companies.application.CompanyService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.IfMatchSupport;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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

/** Admin-only HTTP boundary for Company metadata management. */
@RestController
@RequestMapping(ApiPaths.ADMIN_COMPANIES)
@Validated
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResponse<CompanyResponse> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search) {
        return service.list(new CompanySearchCriteria(page, size, sort, search));
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = service.create(request);
        return ResponseEntity
                .created(URI.create(ApiPaths.ADMIN_COMPANIES + "/" + response.companyId()))
                .eTag(IfMatchSupport.formatVersion(response.version()))
                .body(response);
    }

    @GetMapping(path = "/{companyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompanyResponse> get(@PathVariable UUID companyId) {
        CompanyResponse response = service.get(companyId);
        return ResponseEntity
                .ok()
                .eTag(IfMatchSupport.formatVersion(response.version()))
                .body(response);
    }

    @PatchMapping(
            path = "/{companyId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompanyResponse> update(
            @PathVariable UUID companyId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestBody CompanyUpdateRequest request) {
        CompanyResponse response =
                service.update(companyId, request, IfMatchSupport.parseVersion(ifMatch));
        return ResponseEntity
                .ok()
                .eTag(IfMatchSupport.formatVersion(response.version()))
                .body(response);
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID companyId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch) {
        service.delete(companyId, IfMatchSupport.parseVersion(ifMatch));
        return ResponseEntity.noContent().build();
    }
}
