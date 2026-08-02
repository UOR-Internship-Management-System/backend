package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api;

import jakarta.validation.Valid;
import java.util.List;
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
    public List<ContactLinkResponse> listContactLinks() {
        return service.listContactLinks();
    }

    @PostMapping("/contact-links")
    @ResponseStatus(HttpStatus.CREATED)
    public ContactLinkResponse createContactLink(@Valid @RequestBody ContactLinkRequest request) {
        return service.createContactLink(request);
    }

    @PatchMapping("/contact-links/{contactLinkId}")
    public ContactLinkResponse updateContactLink(
        @PathVariable UUID contactLinkId, @Valid @RequestBody ContactLinkRequest request) {
        return service.updateContactLink(contactLinkId, request);
    }

    @DeleteMapping("/contact-links/{contactLinkId}")
    public ResponseEntity<Void> deleteContactLink(@PathVariable UUID contactLinkId) {
        service.deleteContactLink(contactLinkId);
        return ResponseEntity.noContent().build();
    }

    // certificates
    @GetMapping("/certificates")
    public List<CertificateResponse> listCertificates() {
        return service.listCertificates();
    }

    @PostMapping("/certificates")
    @ResponseStatus(HttpStatus.CREATED)
    public CertificateResponse createCertificate(@Valid @RequestBody CertificateRequest request) {
        return service.createCertificate(request);
    }

    @PatchMapping("/certificates/{certificateId}")
    public CertificateResponse updateCertificate(
        @PathVariable UUID certificateId, @Valid @RequestBody CertificateRequest request) {
        return service.updateCertificate(certificateId, request);
    }

    @DeleteMapping("/certificates/{certificateId}")
    public ResponseEntity<Void> deleteCertificate(@PathVariable UUID certificateId) {
        service.deleteCertificate(certificateId);
        return ResponseEntity.noContent().build();
    }

    // awards
    @GetMapping("/awards")
    public List<AwardResponse> listAwards() {
        return service.listAwards();
    }

    @PostMapping("/awards")
    @ResponseStatus(HttpStatus.CREATED)
    public AwardResponse createAward(@Valid @RequestBody AwardRequest request) {
        return service.createAward(request);
    }

    @PatchMapping("/awards/{awardId}")
    public AwardResponse updateAward(@PathVariable UUID awardId, @Valid @RequestBody AwardRequest request) {
        return service.updateAward(awardId, request);
    }

    @DeleteMapping("/awards/{awardId}")
    public ResponseEntity<Void> deleteAward(@PathVariable UUID awardId) {
        service.deleteAward(awardId);
        return ResponseEntity.noContent().build();
    }

    // activities
    @GetMapping("/activities")
    public List<ActivityResponse> listActivities() {
        return service.listActivities();
    }

    @PostMapping("/activities")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityResponse createActivity(@Valid @RequestBody ActivityRequest request) {
        return service.createActivity(request);
    }

    @PatchMapping("/activities/{activityId}")
    public ActivityResponse updateActivity(
        @PathVariable UUID activityId, @Valid @RequestBody ActivityRequest request) {
        return service.updateActivity(activityId, request);
    }

    @DeleteMapping("/activities/{activityId}")
    public ResponseEntity<Void> deleteActivity(@PathVariable UUID activityId) {
        service.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }

    // work experience
    @GetMapping("/experience")
    public List<WorkExperienceResponse> listExperience() {
        return service.listExperience();
    }

    @PostMapping("/experience")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkExperienceResponse createExperience(@Valid @RequestBody WorkExperienceRequest request) {
        return service.createExperience(request);
    }

    @PatchMapping("/experience/{experienceId}")
    public WorkExperienceResponse updateExperience(
        @PathVariable UUID experienceId, @Valid @RequestBody WorkExperienceRequest request) {
        return service.updateExperience(experienceId, request);
    }

    @DeleteMapping("/experience/{experienceId}")
    public ResponseEntity<Void> deleteExperience(@PathVariable UUID experienceId) {
        service.deleteExperience(experienceId);
        return ResponseEntity.noContent().build();
    }
}
