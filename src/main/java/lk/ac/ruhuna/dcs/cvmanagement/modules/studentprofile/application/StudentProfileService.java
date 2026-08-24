package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.application;

import java.time.OffsetDateTime;
import java.util.Map;
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
import lk.ac.ruhuna.dcs.cvmanagement.shared.cv.CvSourceArea;
import lk.ac.ruhuna.dcs.cvmanagement.shared.cv.CvSourceFreshnessUpdatePort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.mapper.StudentProfileMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.ActivityEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.AwardEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.CertificateEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.ContactLinkEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentProfileEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.WorkExperienceEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.ActivityRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.AwardRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.CertificateRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.ContactLinkRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentProfileRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.StudentRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.WorkExperienceRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.api.dto.FileAssetResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.PreconditionFailedException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.files.ProfileFileService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.PageRequestFactory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentProfileService {

    private final CurrentActorProvider currentActorProvider;
    private final StudentRepository studentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ContactLinkRepository contactLinkRepository;
    private final CertificateRepository certificateRepository;
    private final AwardRepository awardRepository;
    private final ActivityRepository activityRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final StudentProfileMapper mapper;
    private final CvSourceFreshnessUpdatePort cvFreshnessUpdatePort;
    private final ProfileFileService profileFileService;

    public StudentProfileService(
        CurrentActorProvider currentActorProvider,
        StudentRepository studentRepository,
        StudentProfileRepository studentProfileRepository,
        ContactLinkRepository contactLinkRepository,
        CertificateRepository certificateRepository,
        AwardRepository awardRepository,
        ActivityRepository activityRepository,
        WorkExperienceRepository workExperienceRepository,
        StudentProfileMapper mapper,
        CvSourceFreshnessUpdatePort cvFreshnessUpdatePort,
        ProfileFileService profileFileService) {
        this.currentActorProvider = currentActorProvider;
        this.studentRepository = studentRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.contactLinkRepository = contactLinkRepository;
        this.certificateRepository = certificateRepository;
        this.awardRepository = awardRepository;
        this.activityRepository = activityRepository;
        this.workExperienceRepository = workExperienceRepository;
        this.mapper = mapper;
        this.cvFreshnessUpdatePort = cvFreshnessUpdatePort;
        this.profileFileService = profileFileService;
    }

    // ---- current student resolution ----

    private StudentEntity currentStudent() {
        CurrentActor actor = currentActorProvider.currentActor()
            .orElseThrow(() -> new ForbiddenException("No authenticated Student context."));
        return studentRepository.findByUserAccountId(actor.userId())
            .orElseThrow(() -> new NotFoundException("Student record not found for the authenticated account."));
    }

    private StudentProfileEntity getOrCreateProfile(UUID studentId) {
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

    // ---- core profile ----

    @Transactional(readOnly = true)
    public StudentProfileResponse getMyProfile() {
        StudentEntity student = currentStudent();
        StudentProfileEntity profile = getOrCreateProfile(student.getId());
        return mapper.toResponse(student, profile,
            profileFileService.resolve(profile.getProfilePhotoFileId()));
    }

    @Transactional
    public StudentProfileResponse updateMyProfile(StudentProfileUpdateRequest request) {
        StudentEntity student = currentStudent();
        StudentProfileEntity profile = getOrCreateProfile(student.getId());

        if (request.fullName() != null) profile.setDisplayName(request.fullName());
        if (request.personalEmail() != null) profile.setPersonalEmail(request.personalEmail());
        if (request.headline() != null) profile.setHeadline(request.headline());
        if (request.summary() != null) profile.setSummary(request.summary());
        if (request.phone() != null) profile.setPhone(request.phone());
        if (request.location() != null) profile.setLocation(request.location());
        profile.setUpdatedAt(OffsetDateTime.now());

        StudentProfileEntity saved = studentProfileRepository.save(profile);
        cvFreshnessUpdatePort.markChanged(student.getId(), CvSourceArea.PROFILE);
        return mapper.toResponse(student, saved,
            profileFileService.resolve(saved.getProfilePhotoFileId()));
    }

    // ---- contact links ----

    @Transactional(readOnly = true)
    public PagedResponse<ContactLinkResponse> listContactLinks(String search, Integer page, Integer size, String sort) {
        UUID studentId = currentStudent().getId();
        Pageable pageable = PageRequestFactory.build(page, size, sort);
        String searchPattern = "%" + (search == null ? "" : search.toLowerCase()) + "%";
        Page<ContactLinkEntity> result = contactLinkRepository.search(studentId, searchPattern, pageable);
        return PagedResponse.of(result.map(mapper::toResponse), PageRequestFactory.describeSort(sort));
    }

    @Transactional
    public ContactLinkResponse createContactLink(ContactLinkRequest request) {
        UUID studentId = currentStudent().getId();
        ContactLinkEntity entity = new ContactLinkEntity();
        entity.setId(UUID.randomUUID());
        entity.setStudentId(studentId);
        entity.setLabel(request.label());
        entity.setUrl(request.url());
        entity.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
        entity.setCvInclude(request.cvInclude() == null || request.cvInclude());
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        ContactLinkEntity saved = contactLinkRepository.save(entity);
        cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.PROFILE);
        return mapper.toResponse(saved);
    }

    @Transactional
    public ContactLinkResponse updateContactLink(UUID contactLinkId, ContactLinkRequest request, long ifMatchVersion) {
        UUID studentId = currentStudent().getId();
        ContactLinkEntity entity = contactLinkRepository.findById(contactLinkId)
            .orElseThrow(() -> new NotFoundException("Contact link not found."));
        assertOwnership(entity.getStudentId(), studentId);
        if (!entity.getVersion().equals(ifMatchVersion)) {
            throw new PreconditionFailedException("Contact link has been modified since it was last read.");
        }

        entity.setLabel(request.label());
        entity.setUrl(request.url());
        if (request.displayOrder() != null) entity.setDisplayOrder(request.displayOrder());
        if (request.cvInclude() != null) entity.setCvInclude(request.cvInclude());
        entity.setUpdatedAt(OffsetDateTime.now());
        ContactLinkEntity saved = contactLinkRepository.save(entity);
        cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.PROFILE);
        return mapper.toResponse(saved);
    }

    @Transactional
    public void deleteContactLink(UUID contactLinkId, long ifMatchVersion) {
        UUID studentId = currentStudent().getId();
        ContactLinkEntity entity = contactLinkRepository.findById(contactLinkId)
            .orElseThrow(() -> new NotFoundException("Contact link not found."));
        assertOwnership(entity.getStudentId(), studentId);
        if (!entity.getVersion().equals(ifMatchVersion)) {
            throw new PreconditionFailedException("Contact link has been modified since it was last read.");
        }
        contactLinkRepository.delete(entity);
        cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.PROFILE);
    }

    // ---- certificates ----

    @Transactional(readOnly = true)
    public PagedResponse<CertificateResponse> listCertificates(String search, Integer page, Integer size, String sort) {
        UUID studentId = currentStudent().getId();
        Pageable pageable = PageRequestFactory.build(page, size, sort);
        String searchPattern = "%" + (search == null ? "" : search.toLowerCase()) + "%";
        Page<CertificateEntity> result = certificateRepository.search(studentId, searchPattern, pageable);

        // One query for every evidence file on this page. Resolving inside the map below would
        // issue one query per certificate.
        Map<UUID, FileAssetResponse> evidence = profileFileService.resolveAll(
            result.getContent().stream().map(CertificateEntity::getEvidenceFileId).toList());

        return PagedResponse.of(
            result.map(entity -> mapper.toResponse(entity, evidence.get(entity.getEvidenceFileId()))),
            PageRequestFactory.describeSort(sort));
    }

    @Transactional
    public CertificateResponse createCertificate(CertificateRequest request) {
        UUID studentId = currentStudent().getId();
        CertificateEntity entity = new CertificateEntity();
        entity.setId(UUID.randomUUID());
        entity.setStudentId(studentId);
        entity.setTitle(request.title());
        entity.setIssuer(request.issuer());
        entity.setIssueDate(request.issueDate());
        entity.setCredentialUrl(request.credentialUrl());
        entity.setCvInclude(request.cvInclude() == null || request.cvInclude());
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        CertificateEntity saved = certificateRepository.save(entity);
        cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.PROFILE);
        // A newly created certificate never has evidence yet; it is attached separately.
        return mapper.toResponse(saved, null);
    }

    @Transactional
    public CertificateResponse updateCertificate(UUID certificateId, CertificateRequest request, long ifMatchVersion) {
        UUID studentId = currentStudent().getId();
        CertificateEntity entity = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new NotFoundException("Certificate not found."));
        assertOwnership(entity.getStudentId(), studentId);
        if (!entity.getVersion().equals(ifMatchVersion)) {
            throw new PreconditionFailedException("Certificate has been modified since it was last read.");
        }

        entity.setTitle(request.title());
        entity.setIssuer(request.issuer());
        entity.setIssueDate(request.issueDate());
        entity.setCredentialUrl(request.credentialUrl());
        if (request.cvInclude() != null) entity.setCvInclude(request.cvInclude());
        entity.setUpdatedAt(OffsetDateTime.now());
        CertificateEntity saved = certificateRepository.save(entity);
        cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.PROFILE);
        // Editing fields must not drop an already-attached evidence file from the response.
        return mapper.toResponse(saved, profileFileService.resolve(saved.getEvidenceFileId()));
    }

    @Transactional
    public void deleteCertificate(UUID certificateId, long ifMatchVersion) {
        UUID studentId = currentStudent().getId();
        CertificateEntity entity = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new NotFoundException("Certificate not found."));
        assertOwnership(entity.getStudentId(), studentId);
        if (!entity.getVersion().equals(ifMatchVersion)) {
            throw new PreconditionFailedException("Certificate has been modified since it was last read.");
        }
        // Read the file id before the delete; afterwards the entity is detached.
        UUID evidenceFileId = entity.getEvidenceFileId();
        certificateRepository.delete(entity);
        profileFileService.delete(evidenceFileId);
        cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.PROFILE);
    }

    // ---- awards ----

    @Transactional(readOnly = true)
    public PagedResponse<AwardResponse> listAwards(String search, Integer page, Integer size, String sort) {
        UUID studentId = currentStudent().getId();
        Pageable pageable = PageRequestFactory.build(page, size, sort);
        String searchPattern = "%" + (search == null ? "" : search.toLowerCase()) + "%";
        Page<AwardEntity> result = awardRepository.search(studentId, searchPattern, pageable);
        return PagedResponse.of(result.map(mapper::toResponse), PageRequestFactory.describeSort(sort));
    }

    @Transactional
    public AwardResponse createAward(AwardRequest request) {
        UUID studentId = currentStudent().getId();
        AwardEntity entity = new AwardEntity();
        entity.setId(UUID.randomUUID());
        entity.setStudentId(studentId);
        entity.setTitle(request.title());
        entity.setIssuer(request.issuer());
        entity.setAwardDate(request.awardDate());
        entity.setDescription(request.description());
        entity.setCvInclude(request.cvInclude() == null || request.cvInclude());
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        AwardEntity saved = awardRepository.save(entity);
        cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.PROFILE);
        return mapper.toResponse(saved);
    }

    @Transactional
    public AwardResponse updateAward(UUID awardId, AwardRequest request, long ifMatchVersion) {
        UUID studentId = currentStudent().getId();
        AwardEntity entity = awardRepository.findById(awardId)
            .orElseThrow(() -> new NotFoundException("Award not found."));
        assertOwnership(entity.getStudentId(), studentId);
        if (!entity.getVersion().equals(ifMatchVersion)) {
            throw new PreconditionFailedException("Award has been modified since it was last read.");
        }

        entity.setTitle(request.title());
        entity.setIssuer(request.issuer());
        entity.setAwardDate(request.awardDate());
        entity.setDescription(request.description());
        if (request.cvInclude() != null) entity.setCvInclude(request.cvInclude());
        entity.setUpdatedAt(OffsetDateTime.now());
        AwardEntity saved = awardRepository.save(entity);
        cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.PROFILE);
        return mapper.toResponse(saved);
    }

    @Transactional
    public void deleteAward(UUID awardId, long ifMatchVersion) {
        UUID studentId = currentStudent().getId();
        AwardEntity entity = awardRepository.findById(awardId)
            .orElseThrow(() -> new NotFoundException("Award not found."));
        assertOwnership(entity.getStudentId(), studentId);
        if (!entity.getVersion().equals(ifMatchVersion)) {
            throw new PreconditionFailedException("Award has been modified since it was last read.");
        }
        awardRepository.delete(entity);
        cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.PROFILE);
    }

    // ---- activities ----

    @Transactional(readOnly = true)
    public PagedResponse<ActivityResponse> listActivities(String search, Integer page, Integer size, String sort) {
        UUID studentId = currentStudent().getId();
        Pageable pageable = PageRequestFactory.build(page, size, sort);
        String searchPattern = "%" + (search == null ? "" : search.toLowerCase()) + "%";
        Page<ActivityEntity> result = activityRepository.search(studentId, searchPattern, pageable);
        return PagedResponse.of(result.map(mapper::toResponse), PageRequestFactory.describeSort(sort));
    }

    @Transactional
    public ActivityResponse createActivity(ActivityRequest request) {
        UUID studentId = currentStudent().getId();
        ActivityEntity entity = new ActivityEntity();
        entity.setId(UUID.randomUUID());
        entity.setStudentId(studentId);
        entity.setActivityName(request.activityName());
        entity.setRoleTitle(request.roleTitle());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setDescription(request.description());
        entity.setCvInclude(request.cvInclude() == null || request.cvInclude());
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        ActivityEntity saved = activityRepository.save(entity);
        cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.PROFILE);
        return mapper.toResponse(saved);
    }

    @Transactional
    public ActivityResponse updateActivity(UUID activityId, ActivityRequest request, long ifMatchVersion) {
        UUID studentId = currentStudent().getId();
        ActivityEntity entity = activityRepository.findById(activityId)
            .orElseThrow(() -> new NotFoundException("Activity not found."));
        assertOwnership(entity.getStudentId(), studentId);
        if (!entity.getVersion().equals(ifMatchVersion)) {
            throw new PreconditionFailedException("Activity has been modified since it was last read.");
        }

        entity.setActivityName(request.activityName());
        entity.setRoleTitle(request.roleTitle());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setDescription(request.description());
        if (request.cvInclude() != null) entity.setCvInclude(request.cvInclude());
        entity.setUpdatedAt(OffsetDateTime.now());
        ActivityEntity saved = activityRepository.save(entity);
        cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.PROFILE);
        return mapper.toResponse(saved);
    }

    @Transactional
    public void deleteActivity(UUID activityId, long ifMatchVersion) {
        UUID studentId = currentStudent().getId();
        ActivityEntity entity = activityRepository.findById(activityId)
            .orElseThrow(() -> new NotFoundException("Activity not found."));
        assertOwnership(entity.getStudentId(), studentId);
        if (!entity.getVersion().equals(ifMatchVersion)) {
            throw new PreconditionFailedException("Activity has been modified since it was last read.");
        }
        activityRepository.delete(entity);
        cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.PROFILE);
    }

    // ---- work experience ----

    @Transactional(readOnly = true)
    public PagedResponse<WorkExperienceResponse> listExperience(String search, Integer page, Integer size, String sort) {
        UUID studentId = currentStudent().getId();
        Pageable pageable = PageRequestFactory.build(page, size, sort);
        String searchPattern = "%" + (search == null ? "" : search.toLowerCase()) + "%";
        Page<WorkExperienceEntity> result = workExperienceRepository.search(studentId, searchPattern, pageable);
        return PagedResponse.of(result.map(mapper::toResponse), PageRequestFactory.describeSort(sort));
    }

    @Transactional
    public WorkExperienceResponse createExperience(WorkExperienceRequest request) {
        UUID studentId = currentStudent().getId();
        WorkExperienceEntity entity = new WorkExperienceEntity();
        entity.setId(UUID.randomUUID());
        entity.setStudentId(studentId);
        entity.setOrganization(request.organization());
        entity.setPositionTitle(request.positionTitle());
        entity.setLocation(request.location());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setCurrentRole(request.currentRole());
        entity.setDescription(request.description());
        entity.setCvInclude(request.cvInclude() == null || request.cvInclude());
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        WorkExperienceEntity saved = workExperienceRepository.save(entity);
        cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.PROFILE);
        return mapper.toResponse(saved);
    }

    @Transactional
    public WorkExperienceResponse updateExperience(UUID experienceId, WorkExperienceRequest request, long ifMatchVersion) {
        UUID studentId = currentStudent().getId();
        WorkExperienceEntity entity = workExperienceRepository.findById(experienceId)
            .orElseThrow(() -> new NotFoundException("Work experience not found."));
        assertOwnership(entity.getStudentId(), studentId);
        if (!entity.getVersion().equals(ifMatchVersion)) {
            throw new PreconditionFailedException("Work experience has been modified since it was last read.");
        }

        entity.setOrganization(request.organization());
        entity.setPositionTitle(request.positionTitle());
        entity.setLocation(request.location());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setCurrentRole(request.currentRole());
        entity.setDescription(request.description());
        if (request.cvInclude() != null) entity.setCvInclude(request.cvInclude());
        entity.setUpdatedAt(OffsetDateTime.now());
        WorkExperienceEntity saved = workExperienceRepository.save(entity);
        cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.PROFILE);
        return mapper.toResponse(saved);
    }

    @Transactional
    public void deleteExperience(UUID experienceId, long ifMatchVersion) {
        UUID studentId = currentStudent().getId();
        WorkExperienceEntity entity = workExperienceRepository.findById(experienceId)
            .orElseThrow(() -> new NotFoundException("Work experience not found."));
        assertOwnership(entity.getStudentId(), studentId);
        if (!entity.getVersion().equals(ifMatchVersion)) {
            throw new PreconditionFailedException("Work experience has been modified since it was last read.");
        }
        workExperienceRepository.delete(entity);
        cvFreshnessUpdatePort.markChanged(studentId, CvSourceArea.PROFILE);
    }

    private void assertOwnership(UUID resourceStudentId, UUID currentStudentId) {
        if (!resourceStudentId.equals(currentStudentId)) {
            throw new ForbiddenException("This record does not belong to the authenticated Student.");
        }
    }
}
