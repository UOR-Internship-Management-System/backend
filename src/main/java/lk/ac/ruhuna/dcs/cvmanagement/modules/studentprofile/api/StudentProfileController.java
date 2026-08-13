package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api;

import jakarta.validation.Valid;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request.ActivityRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request.AwardRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request.CertificateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request.ContactLinkRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request.StudentProfileUpdateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.request.WorkExperienceRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.ActivityResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.AwardResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.CertificateResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.ContactLinkResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.StudentProfileResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.WorkExperienceResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.application.StudentProfileService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.IfMatchSupport;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.ME_PROFILE)
@Validated
public class StudentProfileController {

    private final StudentProfileService service;

    public StudentProfileController(StudentProfileService service) {
        this.service = service;
    }

    @GetMapping
    public StudentProfileResponse getProfile() {
        return service.getMyProfile();
    }

    @PatchMapping
    public StudentProfileResponse patchProfile(@Valid @RequestBody StudentProfileUpdateRequest request) {
        return service.updateMyProfile(request);
    }

    // contact links
    @GetMapping("/contact-links")
    public PagedResponse<ContactLinkResponse> listContactLinks(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort) {
        return service.listContactLinks(search, page, size, sort);
    }

    @PostMapping("/contact-links")
    @ResponseStatus(HttpStatus.CREATED)
    public ContactLinkResponse createContactLink(@Valid @RequestBody ContactLinkRequest request) {
        return service.createContactLink(request);
    }

    @PatchMapping("/contact-links/{contactLinkId}")
    public ContactLinkResponse updateContactLink(
        @PathVariable UUID contactLinkId,
        @Valid @RequestBody ContactLinkRequest request,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        return service.updateContactLink(contactLinkId, request, IfMatchSupport.parseVersion(ifMatch));
    }

    @DeleteMapping("/contact-links/{contactLinkId}")
    public ResponseEntity<Void> deleteContactLink(
        @PathVariable UUID contactLinkId,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        service.deleteContactLink(contactLinkId, IfMatchSupport.parseVersion(ifMatch));
        return ResponseEntity.noContent().build();
    }

    // certificates

    @GetMapping("/certificates")
    public PagedResponse<CertificateResponse> listCertificates(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort) {
        return service.listCertificates(search, page, size, sort);
    }

    @PostMapping("/certificates")
    @ResponseStatus(HttpStatus.CREATED)
    public CertificateResponse createCertificate(@Valid @RequestBody CertificateRequest request) {
        return service.createCertificate(request);
    }

    @PatchMapping("/certificates/{certificateId}")
    public CertificateResponse updateCertificate(
        @PathVariable UUID certificateId,
        @Valid @RequestBody CertificateRequest request,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        return service.updateCertificate(certificateId, request, IfMatchSupport.parseVersion(ifMatch));
    }

    @DeleteMapping("/certificates/{certificateId}")
    public ResponseEntity<Void> deleteCertificate(
        @PathVariable UUID certificateId,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        service.deleteCertificate(certificateId, IfMatchSupport.parseVersion(ifMatch));
        return ResponseEntity.noContent().build();
    }

    // awards

    @GetMapping("/awards")
    public PagedResponse<AwardResponse> listAwards(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort) {
        return service.listAwards(search, page, size, sort);
    }

    @PostMapping("/awards")
    @ResponseStatus(HttpStatus.CREATED)
    public AwardResponse createAward(@Valid @RequestBody AwardRequest request) {
        return service.createAward(request);
    }

    @PatchMapping("/awards/{awardId}")
    public AwardResponse updateAward(
        @PathVariable UUID awardId,
        @Valid @RequestBody AwardRequest request,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        return service.updateAward(awardId, request, IfMatchSupport.parseVersion(ifMatch));
    }

    @DeleteMapping("/awards/{awardId}")
    public ResponseEntity<Void> deleteAward(
        @PathVariable UUID awardId,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        service.deleteAward(awardId, IfMatchSupport.parseVersion(ifMatch));
        return ResponseEntity.noContent().build();
    }

    // activities

    @GetMapping("/activities")
    public PagedResponse<ActivityResponse> listActivities(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort) {
        return service.listActivities(search, page, size, sort);
    }

    @PostMapping("/activities")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityResponse createActivity(@Valid @RequestBody ActivityRequest request) {
        return service.createActivity(request);
    }

    @PatchMapping("/activities/{activityId}")
    public ActivityResponse updateActivity(
        @PathVariable UUID activityId,
        @Valid @RequestBody ActivityRequest request,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        return service.updateActivity(activityId, request, IfMatchSupport.parseVersion(ifMatch));
    }

    @DeleteMapping("/activities/{activityId}")
    public ResponseEntity<Void> deleteActivity(
        @PathVariable UUID activityId,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        service.deleteActivity(activityId, IfMatchSupport.parseVersion(ifMatch));
        return ResponseEntity.noContent().build();
    }

    // work experience
    @GetMapping("/experience")
    public PagedResponse<WorkExperienceResponse> listExperience(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort) {
        return service.listExperience(search, page, size, sort);
    }

    @PostMapping("/experience")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkExperienceResponse createExperience(@Valid @RequestBody WorkExperienceRequest request) {
        return service.createExperience(request);
    }

    @PatchMapping("/experience/{experienceId}")
    public WorkExperienceResponse updateExperience(
        @PathVariable UUID experienceId,
        @Valid @RequestBody WorkExperienceRequest request,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        return service.updateExperience(experienceId, request, IfMatchSupport.parseVersion(ifMatch));
    }

    @DeleteMapping("/experience/{experienceId}")
    public ResponseEntity<Void> deleteExperience(
        @PathVariable UUID experienceId,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        service.deleteExperience(experienceId, IfMatchSupport.parseVersion(ifMatch));
        return ResponseEntity.noContent().build();
    }
}
