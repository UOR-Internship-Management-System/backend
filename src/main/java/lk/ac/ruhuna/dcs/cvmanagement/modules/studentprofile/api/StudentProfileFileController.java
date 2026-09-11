package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.CertificateResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.ProfileUploadPolicyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.StudentProfileResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.application.StudentProfileFileService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.ApiPaths;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.IfMatchSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Multipart file endpoints for the student's own profile.
 *
 * <p>Separate from {@link StudentProfileController} to keep multipart handling out of the JSON
 * controller. Both are mapped under {@code /api/v1/me/profile}, so the existing {@code ROLE_STUDENT}
 * rule in {@code SecurityConfig} already covers these routes.
 */
@RestController
@RequestMapping(ApiPaths.ME_PROFILE)
@Validated
public class StudentProfileFileController {

    private final StudentProfileFileService service;

    public StudentProfileFileController(StudentProfileFileService service) {
        this.service = service;
    }

    @GetMapping("/upload-policy")
    public ProfileUploadPolicyResponse uploadPolicy() {
        return service.uploadPolicy();
    }

    @PutMapping(
        path = "/photo",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public StudentProfileResponse uploadPhoto(
        @RequestParam("file") MultipartFile file,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        return service.replacePhoto(file, IfMatchSupport.parseVersion(ifMatch));
    }

    @DeleteMapping(path = "/photo", produces = MediaType.APPLICATION_JSON_VALUE)
    public StudentProfileResponse deletePhoto(
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        return service.removePhoto(IfMatchSupport.parseVersion(ifMatch));
    }

    @PutMapping(
        path = "/certificates/{certificateId}/evidence",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public CertificateResponse uploadEvidence(
        @PathVariable UUID certificateId,
        @RequestParam("file") MultipartFile file,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        return service.replaceEvidence(certificateId, file, IfMatchSupport.parseVersion(ifMatch));
    }

    @DeleteMapping(
        path = "/certificates/{certificateId}/evidence",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public CertificateResponse deleteEvidence(
        @PathVariable UUID certificateId,
        @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        return service.removeEvidence(certificateId, IfMatchSupport.parseVersion(ifMatch));
    }
}
