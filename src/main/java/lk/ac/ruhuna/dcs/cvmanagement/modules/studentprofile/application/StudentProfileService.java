package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.application;

import java.time.OffsetDateTime;
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
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.domain.policy.CvFreshnessUpdatePort;
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
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
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
    private final CvFreshnessUpdatePort cvFreshnessUpdatePort;

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
        CvFreshnessUpdatePort cvFreshnessUpdatePort) {
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
        return mapper.toResponse(student, profile, null); // wire real photo URL once file module exists
    }

    @Transactional
    public StudentProfileResponse updateMyProfile(StudentProfileUpdateRequest request) {
        StudentEntity student = currentStudent();
        StudentProfileEntity profile = getOrCreateProfile(student.getId());

        if (request.fullName() != null) {
            profile.setDisplayName(request.fullName());
        }
        if (request.summary() != null) {
            profile.setSummary(request.summary());
        }
        if (request.phone() != null) {
            profile.setPhone(request.phone());
        }
        if (request.profilePhotoFileId() != null) {
            profile.setProfilePhotoFileId(request.profilePhotoFileId());
        }
        profile.setUpdatedAt(OffsetDateTime.now());
        StudentProfileEntity saved = studentProfileRepository.save(profile);

        cvFreshnessUpdatePort.markStale(student.getId(), "studentprofile");
        return mapper.toResponse(student, saved, null);
    }

    // ---- contact links ----

    @Transactional(readOnly = true)
    public List<ContactLinkResponse> listContactLinks() {
        UUID studentId = currentStudent().getId();
        return contactLinkRepository.findByStudentIdOrderByDisplayOrderAsc(studentId).stream()
            .map(mapper::toResponse)
            .toList();
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
        cvFreshnessUpdatePort.markStale(studentId, "studentprofile");
        return mapper.toResponse(saved);
    }

    @Transactional
    public ContactLinkResponse updateContactLink(UUID contactLinkId, ContactLinkRequest request) {
        UUID studentId = currentStudent().getId();
        ContactLinkEntity entity = contactLinkRepository.findById(contactLinkId)
            .orElseThrow(() -> new NotFoundException("Contact link not found."));
        assertOwnership(entity.getStudentId(), studentId);

        entity.setLabel(request.label());
        entity.setUrl(request.url());
        if (request.displayOrder() != null) {
            entity.setDisplayOrder(request.displayOrder());
        }
        if (request.cvInclude() != null) {
            entity.setCvInclude(request.cvInclude());
        }
        entity.setUpdatedAt(OffsetDateTime.now());
        ContactLinkEntity saved = contactLinkRepository.save(entity);
        cvFreshnessUpdatePort.markStale(studentId, "studentprofile");
        return mapper.toResponse(saved);
    }

    @Transactional
    public void deleteContactLink(UUID contactLinkId) {
        UUID studentId = currentStudent().getId();
        ContactLinkEntity entity = contactLinkRepository.findById(contactLinkId)
            .orElseThrow(() -> new NotFoundException("Contact link not found."));
        assertOwnership(entity.getStudentId(), studentId);
        contactLinkRepository.delete(entity);
        cvFreshnessUpdatePort.markStale(studentId, "studentprofile");
    }

    // ---- certificates ----

    @Transactional(readOnly = true)
    public List<CertificateResponse> listCertificates() {
        UUID studentId = currentStudent().getId();
        return certificateRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
            .map(mapper::toResponse)
            .toList();
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
        cvFreshnessUpdatePort.markStale(studentId, "studentprofile");
        return mapper.toResponse(saved);
    }

    @Transactional
    public CertificateResponse updateCertificate(UUID certificateId, CertificateRequest request) {
        UUID studentId = currentStudent().getId();
        CertificateEntity entity = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new NotFoundException("Certificate not found."));
        assertOwnership(entity.getStudentId(), studentId);

        entity.setTitle(request.title());
        entity.setIssuer(request.issuer());
        entity.setIssueDate(request.issueDate());
        entity.setCredentialUrl(request.credentialUrl());
        if (request.cvInclude() != null) {
            entity.setCvInclude(request.cvInclude());
        }
        entity.setUpdatedAt(OffsetDateTime.now());
        CertificateEntity saved = certificateRepository.save(entity);
        cvFreshnessUpdatePort.markStale(studentId, "studentprofile");
        return mapper.toResponse(saved);
    }

    @Transactional
    public void deleteCertificate(UUID certificateId) {
        UUID studentId = currentStudent().getId();
        CertificateEntity entity = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new NotFoundException("Certificate not found."));
        assertOwnership(entity.getStudentId(), studentId);
        certificateRepository.delete(entity);
        cvFreshnessUpdatePort.markStale(studentId, "studentprofile");
    }

    // ---- awards ----

    @Transactional(readOnly = true)
    public List<AwardResponse> listAwards() {
        UUID studentId = currentStudent().getId();
        return awardRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
            .map(mapper::toResponse)
            .toList();
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
        cvFreshnessUpdatePort.markStale(studentId, "studentprofile");
        return mapper.toResponse(saved);
    }

    @Transactional
    public AwardResponse updateAward(UUID awardId, AwardRequest request) {
        UUID studentId = currentStudent().getId();
        AwardEntity entity = awardRepository.findById(awardId)
            .orElseThrow(() -> new NotFoundException("Award not found."));
        assertOwnership(entity.getStudentId(), studentId);

        entity.setTitle(request.title());
        entity.setIssuer(request.issuer());
        entity.setAwardDate(request.awardDate());
        entity.setDescription(request.description());
        if (request.cvInclude() != null) {
            entity.setCvInclude(request.cvInclude());
        }
        entity.setUpdatedAt(OffsetDateTime.now());
        AwardEntity saved = awardRepository.save(entity);
        cvFreshnessUpdatePort.markStale(studentId, "studentprofile");
        return mapper.toResponse(saved);
    }

    @Transactional
    public void deleteAward(UUID awardId) {
        UUID studentId = currentStudent().getId();
        AwardEntity entity = awardRepository.findById(awardId)
            .orElseThrow(() -> new NotFoundException("Award not found."));
        assertOwnership(entity.getStudentId(), studentId);
        awardRepository.delete(entity);
        cvFreshnessUpdatePort.markStale(studentId, "studentprofile");
    }

    // ---- activities ----

    @Transactional(readOnly = true)
    public List<ActivityResponse> listActivities() {
        UUID studentId = currentStudent().getId();
        return activityRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
            .map(mapper::toResponse)
            .toList();
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
        cvFreshnessUpdatePort.markStale(studentId, "studentprofile");
        return mapper.toResponse(saved);
    }

    @Transactional
    public ActivityResponse updateActivity(UUID activityId, ActivityRequest request) {
        UUID studentId = currentStudent().getId();
        ActivityEntity entity = activityRepository.findById(activityId)
            .orElseThrow(() -> new NotFoundException("Activity not found."));
        assertOwnership(entity.getStudentId(), studentId);

        entity.setActivityName(request.activityName());
        entity.setRoleTitle(request.roleTitle());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setDescription(request.description());
        if (request.cvInclude() != null) {
            entity.setCvInclude(request.cvInclude());
        }
        entity.setUpdatedAt(OffsetDateTime.now());
        ActivityEntity saved = activityRepository.save(entity);
        cvFreshnessUpdatePort.markStale(studentId, "studentprofile");
        return mapper.toResponse(saved);
    }

    @Transactional
    public void deleteActivity(UUID activityId) {
        UUID studentId = currentStudent().getId();
        ActivityEntity entity = activityRepository.findById(activityId)
            .orElseThrow(() -> new NotFoundException("Activity not found."));
        assertOwnership(entity.getStudentId(), studentId);
        activityRepository.delete(entity);
        cvFreshnessUpdatePort.markStale(studentId, "studentprofile");
    }

    // ---- work experience ----

    @Transactional(readOnly = true)
    public List<WorkExperienceResponse> listExperience() {
        UUID studentId = currentStudent().getId();
        return workExperienceRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
            .map(mapper::toResponse)
            .toList();
    }

    @Transactional
    public WorkExperienceResponse createExperience(WorkExperienceRequest request) {
        UUID studentId = currentStudent().getId();
        WorkExperienceEntity entity = new WorkExperienceEntity();
        entity.setId(UUID.randomUUID());
        entity.setStudentId(studentId);
        entity.setOrganization(request.organization());
        entity.setPositionTitle(request.positionTitle());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setDescription(request.description());
        entity.setCvInclude(request.cvInclude() == null || request.cvInclude());
        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        WorkExperienceEntity saved = workExperienceRepository.save(entity);
        cvFreshnessUpdatePort.markStale(studentId, "studentprofile");
        return mapper.toResponse(saved);
    }

    @Transactional
    public WorkExperienceResponse updateExperience(UUID experienceId, WorkExperienceRequest request) {
        UUID studentId = currentStudent().getId();
        WorkExperienceEntity entity = workExperienceRepository.findById(experienceId)
            .orElseThrow(() -> new NotFoundException("Work experience not found."));
        assertOwnership(entity.getStudentId(), studentId);

        entity.setOrganization(request.organization());
        entity.setPositionTitle(request.positionTitle());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setDescription(request.description());
        if (request.cvInclude() != null) {
            entity.setCvInclude(request.cvInclude());
        }
        entity.setUpdatedAt(OffsetDateTime.now());
        WorkExperienceEntity saved = workExperienceRepository.save(entity);
        cvFreshnessUpdatePort.markStale(studentId, "studentprofile");
        return mapper.toResponse(saved);
    }

    @Transactional
    public void deleteExperience(UUID experienceId) {
        UUID studentId = currentStudent().getId();
        WorkExperienceEntity entity = workExperienceRepository.findById(experienceId)
            .orElseThrow(() -> new NotFoundException("Work experience not found."));
        assertOwnership(entity.getStudentId(), studentId);
        workExperienceRepository.delete(entity);
        cvFreshnessUpdatePort.markStale(studentId, "studentprofile");
    }

    private void assertOwnership(UUID resourceStudentId, UUID currentStudentId) {
        if (!resourceStudentId.equals(currentStudentId)) {
            throw new ForbiddenException("This record does not belong to the authenticated Student.");
        }
    }
}
