package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.application;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.CertificateResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.ProfileUploadPolicyResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.ProfileUploadPolicyResponse.FileUploadConstraintResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.StudentProfileResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.mapper.StudentProfileMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.CertificateEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentProfileEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.CertificateRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentProfileRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.cv.CvSourceArea;
import lk.ac.ruhuna.dcs.cvmanagement.shared.cv.CvSourceFreshnessUpdatePort;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.PreconditionFailedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.files.ProfileFileProperties;
import lk.ac.ruhuna.dcs.cvmanagement.shared.files.ProfileFileService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Owns profile photo and certificate evidence file lifecycle.
 *
 * <p>Kept separate from {@link StudentProfileService} because these operations are multipart rather
 * than JSON and pull in the storage layer, which the field-editing service has no reason to know
 * about.
 */
@Service
public class StudentProfileFileService {

    private static final String PHOTO_KEY_PREFIX = "profile-photo";
    private static final String EVIDENCE_KEY_PREFIX = "certificate-evidence";

    private final CurrentActorProvider currentActorProvider;
    private final StudentRepository studentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CertificateRepository certificateRepository;
    private final ProfileFileService profileFileService;
    private final StudentProfileMapper mapper;
    private final CvSourceFreshnessUpdatePort cvFreshnessUpdatePort;

    public StudentProfileFileService(
        CurrentActorProvider currentActorProvider,
        StudentRepository studentRepository,
        StudentProfileRepository studentProfileRepository,
        CertificateRepository certificateRepository,
        ProfileFileService profileFileService,
        StudentProfileMapper mapper,
        CvSourceFreshnessUpdatePort cvFreshnessUpdatePort) {
        this.currentActorProvider = currentActorProvider;
        this.studentRepository = studentRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.certificateRepository = certificateRepository;
        this.profileFileService = profileFileService;
        this.mapper = mapper;
        this.cvFreshnessUpdatePort = cvFreshnessUpdatePort;
    }

    public ProfileUploadPolicyResponse uploadPolicy() {
        ProfileFileProperties properties = profileFileService.properties();
        return new ProfileUploadPolicyResponse(
            describe(properties.profilePhoto()),
            describe(properties.certificateEvidence()));
    }

    // ---- profile photo ----

    @Transactional
    public StudentProfileResponse replacePhoto(MultipartFile file, long ifMatchVersion) {
        StudentEntity student = currentStudent();
        StudentProfileEntity profile = requireProfile(student.getId());
        requireVersion(profile.getVersion(), ifMatchVersion);

        UUID previous = profile.getProfilePhotoFileId();
        FileAssetEntity stored = profileFileService.store(
            file,
            profileFileService.properties().profilePhoto(),
            PHOTO_KEY_PREFIX,
            currentActor().userId());

        profile.setProfilePhotoFileId(stored.getId());
        profile.setUpdatedAt(OffsetDateTime.now());
        StudentProfileEntity saved = studentProfileRepository.saveAndFlush(profile);

        // Only after the new reference is durably persisted is the old asset safe to remove.
        profileFileService.delete(previous);

        cvFreshnessUpdatePort.markChanged(student.getId(), CvSourceArea.PROFILE);
        return mapper.toResponse(student, saved, profileFileService.resolve(saved.getProfilePhotoFileId()));
    }

    @Transactional
    public StudentProfileResponse removePhoto(long ifMatchVersion) {
        StudentEntity student = currentStudent();
        StudentProfileEntity profile = requireProfile(student.getId());
        requireVersion(profile.getVersion(), ifMatchVersion);

        UUID previous = profile.getProfilePhotoFileId();
        if (previous == null) {
            throw new NotFoundException("No profile photo is currently set.");
        }

        profile.setProfilePhotoFileId(null);
        profile.setUpdatedAt(OffsetDateTime.now());
        StudentProfileEntity saved = studentProfileRepository.saveAndFlush(profile);
        profileFileService.delete(previous);

        cvFreshnessUpdatePort.markChanged(student.getId(), CvSourceArea.PROFILE);
        return mapper.toResponse(student, saved, null);
    }

    // ---- certificate evidence ----

    @Transactional
    public CertificateResponse replaceEvidence(
        UUID certificateId, MultipartFile file, long ifMatchVersion) {
        StudentEntity student = currentStudent();
        CertificateEntity certificate = requireOwnedCertificate(certificateId, student.getId());
        requireVersion(certificate.getVersion(), ifMatchVersion);

        UUID previous = certificate.getEvidenceFileId();
        FileAssetEntity stored = profileFileService.store(
            file,
            profileFileService.properties().certificateEvidence(),
            EVIDENCE_KEY_PREFIX,
            currentActor().userId());

        certificate.setEvidenceFileId(stored.getId());
        certificate.setUpdatedAt(OffsetDateTime.now());
        CertificateEntity saved = certificateRepository.saveAndFlush(certificate);
        profileFileService.delete(previous);

        cvFreshnessUpdatePort.markChanged(student.getId(), CvSourceArea.PROFILE);
        return mapper.toResponse(saved, profileFileService.resolve(saved.getEvidenceFileId()));
    }

    @Transactional
    public CertificateResponse removeEvidence(UUID certificateId, long ifMatchVersion) {
        StudentEntity student = currentStudent();
        CertificateEntity certificate = requireOwnedCertificate(certificateId, student.getId());
        requireVersion(certificate.getVersion(), ifMatchVersion);

        UUID previous = certificate.getEvidenceFileId();
        if (previous == null) {
            throw new NotFoundException("No evidence file is attached to this certificate.");
        }

        certificate.setEvidenceFileId(null);
        certificate.setUpdatedAt(OffsetDateTime.now());
        CertificateEntity saved = certificateRepository.saveAndFlush(certificate);
        profileFileService.delete(previous);

        cvFreshnessUpdatePort.markChanged(student.getId(), CvSourceArea.PROFILE);
        return mapper.toResponse(saved, null);
    }

    // ---- helpers ----

    private CurrentActor currentActor() {
        return currentActorProvider.currentActor()
            .orElseThrow(() -> new ForbiddenException("No authenticated Student context."));
    }

    private StudentEntity currentStudent() {
        return studentRepository.findByUserAccountId(currentActor().userId())
            .orElseThrow(() -> new NotFoundException(
                "Student record not found for the authenticated account."));
    }

    private StudentProfileEntity requireProfile(UUID studentId) {
        // Mirrors getOrCreateProfile in StudentProfileService: the row is created lazily, so a
        // student who has never edited their profile can still upload a photo.
        return studentProfileRepository.findByStudentId(studentId)
            .orElseGet(() -> {
                StudentProfileEntity profile = new StudentProfileEntity();
                profile.setId(UUID.randomUUID());
                profile.setStudentId(studentId);
                OffsetDateTime now = OffsetDateTime.now();
                profile.setCreatedAt(now);
                profile.setUpdatedAt(now);
                return studentProfileRepository.save(profile);
            });
    }

    private CertificateEntity requireOwnedCertificate(UUID certificateId, UUID studentId) {
        CertificateEntity certificate = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new NotFoundException("Certificate not found."));
        // 403 here matches assertOwnership in StudentProfileService. Consistency across the module
        // matters more than the marginal disclosure benefit of a 404.
        if (!studentId.equals(certificate.getStudentId())) {
            throw new ForbiddenException("This record does not belong to the authenticated Student.");
        }
        return certificate;
    }

    private void requireVersion(Long actual, long expected) {
        long current = actual == null ? 0L : actual;
        if (current != expected) {
            throw new PreconditionFailedException(
                "The resource was modified by another request. Reload and try again.");
        }
    }

    private FileUploadConstraintResponse describe(ProfileFileProperties.Constraint constraint) {
        return new FileUploadConstraintResponse(
            constraint.allowedMimeTypes(),
            constraint.allowedExtensions(),
            constraint.maxSizeBytes());
    }
}
