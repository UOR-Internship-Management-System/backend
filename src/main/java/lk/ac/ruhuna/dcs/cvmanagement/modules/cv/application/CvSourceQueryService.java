package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.StudentAcademicSummaryRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvConfigurationInvalidException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvConfiguration;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model.CvDocumentModel;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.entity.ProjectEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.repository.ProjectRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.repository.ProjectSkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository.DeclaredSkillRepository;
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
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.WorkExperienceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Collects and canonicalizes the Student-owned structured source snapshot used by every CV renderer.
 * Selected UUID ownership is validated with set-based owner-scoped queries; foreign and missing IDs
 * deliberately collapse to the same generic configuration error.
 */
@Service
@Transactional(readOnly = true)
public class CvSourceQueryService {

    private static final Comparator<String> TEXT_ORDER = Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER);
    private static final Comparator<LocalDate> DATE_DESC = Comparator.nullsLast(Comparator.reverseOrder());

    private final StudentProfileRepository profileRepository;
    private final ContactLinkRepository contactLinkRepository;
    private final WorkExperienceRepository experienceRepository;
    private final ProjectRepository projectRepository;
    private final ProjectSkillRepository projectSkillRepository;
    private final CertificateRepository certificateRepository;
    private final AwardRepository awardRepository;
    private final ActivityRepository activityRepository;
    private final DeclaredSkillRepository declaredSkillRepository;
    private final StudentAcademicSummaryRepository academicSummaryRepository;

    public CvSourceQueryService(
            StudentProfileRepository profileRepository,
            ContactLinkRepository contactLinkRepository,
            WorkExperienceRepository experienceRepository,
            ProjectRepository projectRepository,
            ProjectSkillRepository projectSkillRepository,
            CertificateRepository certificateRepository,
            AwardRepository awardRepository,
            ActivityRepository activityRepository,
            DeclaredSkillRepository declaredSkillRepository,
            StudentAcademicSummaryRepository academicSummaryRepository) {
        this.profileRepository = profileRepository;
        this.contactLinkRepository = contactLinkRepository;
        this.experienceRepository = experienceRepository;
        this.projectRepository = projectRepository;
        this.projectSkillRepository = projectSkillRepository;
        this.certificateRepository = certificateRepository;
        this.awardRepository = awardRepository;
        this.activityRepository = activityRepository;
        this.declaredSkillRepository = declaredSkillRepository;
        this.academicSummaryRepository = academicSummaryRepository;
    }

    public CvDocumentModel load(StudentEntity student, CvConfiguration configuration) {
        UUID studentId = student.getId();
        StudentProfileEntity profile = profileRepository.findByStudentId(studentId).orElse(null);

        List<ContactLinkEntity> contactLinks = contactLinkRepository
                .findAllByStudentIdAndCvIncludeTrueOrderByDisplayOrderAscLabelAscIdAsc(studentId);
        List<WorkExperienceEntity> experiences = validatedSelection(
                configuration.includedExperienceIds(),
                ids -> experienceRepository.findAllByStudentIdAndIdIn(studentId, ids),
                WorkExperienceEntity::getId);
        List<ProjectEntity> projects = validatedSelection(
                configuration.includedProjectIds(),
                ids -> projectRepository.findAllByStudentIdAndIdIn(studentId, ids),
                ProjectEntity::getId);
        List<CertificateEntity> certificates = validatedSelection(
                configuration.includedCertificateIds(),
                ids -> certificateRepository.findAllByStudentIdAndIdIn(studentId, ids),
                CertificateEntity::getId);
        List<AwardEntity> awards = validatedSelection(
                configuration.includedAwardIds(),
                ids -> awardRepository.findAllByStudentIdAndIdIn(studentId, ids),
                AwardEntity::getId);
        List<ActivityEntity> activities = validatedSelection(
                configuration.includedActivityIds(),
                ids -> activityRepository.findAllByStudentIdAndIdIn(studentId, ids),
                ActivityEntity::getId);

        experiences.sort(Comparator
                .comparing(WorkExperienceEntity::isCurrentRole).reversed()
                .thenComparing(WorkExperienceEntity::getStartDate, DATE_DESC)
                .thenComparing(WorkExperienceEntity::getPositionTitle, TEXT_ORDER)
                .thenComparing(WorkExperienceEntity::getOrganization, TEXT_ORDER)
                .thenComparing(item -> item.getId().toString()));
        projects.sort(Comparator
                .comparing(this::projectSortDate, DATE_DESC)
                .thenComparing(ProjectEntity::getTitle, TEXT_ORDER)
                .thenComparing(item -> item.getId().toString()));
        certificates.sort(Comparator
                .comparing(CertificateEntity::getIssueDate, DATE_DESC)
                .thenComparing(CertificateEntity::getTitle, TEXT_ORDER)
                .thenComparing(item -> item.getId().toString()));
        awards.sort(Comparator
                .comparing(AwardEntity::getAwardDate, DATE_DESC)
                .thenComparing(AwardEntity::getTitle, TEXT_ORDER)
                .thenComparing(item -> item.getId().toString()));
        activities.sort(Comparator
                .comparing((ActivityEntity item) -> item.getEndDate() == null && item.getStartDate() != null).reversed()
                .thenComparing(ActivityEntity::getStartDate, DATE_DESC)
                .thenComparing(ActivityEntity::getActivityName, TEXT_ORDER)
                .thenComparing(item -> item.getId().toString()));

        Map<UUID, List<CvDocumentModel.ProjectSkill>> projectSkills = loadProjectSkills(projects);

        List<CvDocumentModel.DeclaredSkill> skills = declaredSkillRepository.findCvSkills(studentId).stream()
                .map(skill -> new CvDocumentModel.DeclaredSkill(
                        skill.getDeclaredSkillId(),
                        skill.getSkillId(),
                        skill.getSkillName(),
                        skill.getCompetencyLevel().name(),
                        skill.getDisplayOrder(),
                        skill.getVersion(),
                        skill.getUpdatedAt()))
                .toList();

        CvDocumentModel.AcademicSummary academicSummary = academicSummaryRepository.findById(studentId)
                .map(summary -> new CvDocumentModel.AcademicSummary(
                        summary.getComputerScienceGpa(),
                        summary.getTotalCredits(),
                        summary.getCalculatedAt(),
                        summary.getSourceUploadId()))
                .orElse(null);

        return new CvDocumentModel(
                new CvDocumentModel.Identity(
                        studentId, student.getFullName(), student.getUniversityEmail(), student.getUpdatedAt()),
                toProfile(profile),
                contactLinks.stream().map(this::toContactLink).toList(),
                skills,
                experiences.stream().map(this::toExperience).toList(),
                projects.stream().map(project -> toProject(project, projectSkills.getOrDefault(project.getId(), List.of()))).toList(),
                certificates.stream().map(this::toCertificate).toList(),
                awards.stream().map(this::toAward).toList(),
                activities.stream().map(this::toActivity).toList(),
                academicSummary,
                configuration);
    }

    private Map<UUID, List<CvDocumentModel.ProjectSkill>> loadProjectSkills(List<ProjectEntity> projects) {
        if (projects.isEmpty()) return Map.of();
        List<UUID> projectIds = projects.stream().map(ProjectEntity::getId).toList();
        Map<UUID, List<CvDocumentModel.ProjectSkill>> grouped = new HashMap<>();
        for (var projection : projectSkillRepository.findCvSkillsByProjectIds(projectIds)) {
            grouped.computeIfAbsent(projection.getProjectId(), ignored -> new ArrayList<>())
                    .add(new CvDocumentModel.ProjectSkill(
                            projection.getSkillId(), projection.getSkillName(), projection.getDisplayOrder()));
        }
        grouped.replaceAll((ignored, values) -> List.copyOf(values));
        return Map.copyOf(grouped);
    }

    private <T> List<T> validatedSelection(
            List<UUID> requestedIds,
            Function<Collection<UUID>, List<T>> loader,
            Function<T, UUID> idExtractor) {
        if (requestedIds.isEmpty()) return new ArrayList<>();
        List<T> loaded = new ArrayList<>(loader.apply(requestedIds));
        Set<UUID> loadedIds = new HashSet<>();
        for (T item : loaded) loadedIds.add(idExtractor.apply(item));
        if (loadedIds.size() != requestedIds.size() || !loadedIds.containsAll(requestedIds)) {
            throw new CvConfigurationInvalidException();
        }
        return loaded;
    }

    private LocalDate projectSortDate(ProjectEntity project) {
        return project.getEndDate() != null ? project.getEndDate() : project.getStartDate();
    }

    private CvDocumentModel.Profile toProfile(StudentProfileEntity profile) {
        if (profile == null) return null;
        return new CvDocumentModel.Profile(
                profile.getId(), profile.getDisplayName(), profile.getPersonalEmail(), profile.getHeadline(),
                profile.getSummary(), profile.getPhone(), profile.getLocation(), profile.getVersion(), profile.getUpdatedAt());
    }

    private CvDocumentModel.ContactLink toContactLink(ContactLinkEntity item) {
        return new CvDocumentModel.ContactLink(
                item.getId(), item.getLabel(), item.getUrl(), item.getDisplayOrder(), item.getVersion(), item.getUpdatedAt());
    }

    private CvDocumentModel.Experience toExperience(WorkExperienceEntity item) {
        return new CvDocumentModel.Experience(
                item.getId(), item.getOrganization(), item.getPositionTitle(), item.getLocation(), item.getStartDate(),
                item.getEndDate(), item.isCurrentRole(), item.getDescription(), item.getVersion(), item.getUpdatedAt());
    }

    private CvDocumentModel.Project toProject(ProjectEntity item, List<CvDocumentModel.ProjectSkill> skills) {
        return new CvDocumentModel.Project(
                item.getId(), item.getTitle(), item.getDescription(), item.getRepositoryUrl(), item.getDemoUrl(),
                item.getStartDate(), item.getEndDate(), item.getVersion(), item.getUpdatedAt(), skills);
    }

    private CvDocumentModel.Certificate toCertificate(CertificateEntity item) {
        return new CvDocumentModel.Certificate(
                item.getId(), item.getTitle(), item.getIssuer(), item.getIssueDate(), item.getCredentialUrl(),
                item.getVersion(), item.getUpdatedAt());
    }

    private CvDocumentModel.Award toAward(AwardEntity item) {
        return new CvDocumentModel.Award(
                item.getId(), item.getTitle(), item.getIssuer(), item.getAwardDate(), item.getDescription(),
                item.getVersion(), item.getUpdatedAt());
    }

    private CvDocumentModel.Activity toActivity(ActivityEntity item) {
        return new CvDocumentModel.Activity(
                item.getId(), item.getActivityName(), item.getRoleTitle(), item.getStartDate(), item.getEndDate(),
                item.getDescription(), item.getVersion(), item.getUpdatedAt());
    }
}
