package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.StudentAcademicSummaryEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.StudentAcademicSummaryRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.request.CvPreviewRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvPreviewConfigurationResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvPreviewResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.CvPreviewCache;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.HtmlEscaper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.entity.ProjectEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.entity.ProjectSkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.repository.ProjectRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.repository.ProjectSkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity.DeclaredSkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.entity.SkillEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository.DeclaredSkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.skills.persistence.repository.SkillRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.*;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.repository.*;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ForbiddenException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.NotFoundException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CvPreviewService {

    private static final Duration PREVIEW_TTL = Duration.ofMinutes(15);

    private final CurrentActorProvider currentActorProvider;
    private final StudentRepository studentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ContactLinkRepository contactLinkRepository;
    private final CertificateRepository certificateRepository;
    private final AwardRepository awardRepository;
    private final ActivityRepository activityRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final DeclaredSkillRepository declaredSkillRepository;
    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;
    private final ProjectSkillRepository projectSkillRepository;
    private final StudentAcademicSummaryRepository academicSummaryRepository;
    private final CvFreshnessService freshnessService;
    private final CvPreviewCache previewCache;

    public CvPreviewService(
        CurrentActorProvider currentActorProvider,
        StudentRepository studentRepository,
        StudentProfileRepository studentProfileRepository,
        ContactLinkRepository contactLinkRepository,
        CertificateRepository certificateRepository,
        AwardRepository awardRepository,
        ActivityRepository activityRepository,
        WorkExperienceRepository workExperienceRepository,
        DeclaredSkillRepository declaredSkillRepository,
        SkillRepository skillRepository,
        ProjectRepository projectRepository,
        ProjectSkillRepository projectSkillRepository,
        StudentAcademicSummaryRepository academicSummaryRepository,
        CvFreshnessService freshnessService,
        CvPreviewCache previewCache) {
        this.currentActorProvider = currentActorProvider;
        this.studentRepository = studentRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.contactLinkRepository = contactLinkRepository;
        this.certificateRepository = certificateRepository;
        this.awardRepository = awardRepository;
        this.activityRepository = activityRepository;
        this.workExperienceRepository = workExperienceRepository;
        this.declaredSkillRepository = declaredSkillRepository;
        this.skillRepository = skillRepository;
        this.projectRepository = projectRepository;
        this.projectSkillRepository = projectSkillRepository;
        this.academicSummaryRepository = academicSummaryRepository;
        this.freshnessService = freshnessService;
        this.previewCache = previewCache;
    }

    public CvPreviewResponse createPreview(CvPreviewRequest request) {
        StudentEntity student = currentStudent();
        UUID studentId = student.getId();

        List<UUID> experienceIds = safe(request.includedExperienceIds());
        List<UUID> projectIds = safe(request.includedProjectIds());
        List<UUID> certificateIds = safe(request.includedCertificateIds());
        List<UUID> awardIds = safe(request.includedAwardIds());
        List<UUID> activityIds = safe(request.includedActivityIds());

        String html = renderHtml(student, studentId, experienceIds, projectIds, certificateIds, awardIds, activityIds);

        UUID previewId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiresAt = now.plus(PREVIEW_TTL);

        previewCache.store(new CvPreviewCache.CachedPreview(
            studentId, html, experienceIds, projectIds, certificateIds, awardIds, activityIds, now, expiresAt), previewId);

        var configuration = new CvPreviewConfigurationResponse(
            experienceIds, projectIds, certificateIds, awardIds, activityIds);

        return new CvPreviewResponse(previewId, html, freshnessService.getFreshness(), configuration, now, expiresAt);
    }

    private String renderHtml(
        StudentEntity student,
        UUID studentId,
        List<UUID> experienceIds,
        List<UUID> projectIds,
        List<UUID> certificateIds,
        List<UUID> awardIds,
        List<UUID> activityIds) {

        StudentProfileEntity profile = studentProfileRepository.findByStudentId(studentId).orElse(null);
        String fullName = profile != null && profile.getDisplayName() != null && !profile.getDisplayName().isBlank()
            ? profile.getDisplayName() : student.getFullName();

        StringBuilder html = new StringBuilder();
        html.append("<div class=\"cv-document\">");

        // Header
        html.append("<header class=\"cv-header\">");
        html.append("<h1>").append(HtmlEscaper.escape(fullName)).append("</h1>");
        if (profile != null && profile.getHeadline() != null) {
            html.append("<p class=\"cv-headline\">").append(HtmlEscaper.escape(profile.getHeadline())).append("</p>");
        }
        html.append("<p class=\"cv-contact\">")
            .append(HtmlEscaper.escape(student.getUniversityEmail()));
        if (profile != null && profile.getPhone() != null) {
            html.append(" &middot; ").append(HtmlEscaper.escape(profile.getPhone()));
        }
        if (profile != null && profile.getLocation() != null) {
            html.append(" &middot; ").append(HtmlEscaper.escape(profile.getLocation()));
        }
        html.append("</p>");

        List<ContactLinkEntity> links = contactLinkRepository
            .search(studentId, "%", org.springframework.data.domain.Pageable.unpaged()).getContent();
        if (!links.isEmpty()) {
            html.append("<p class=\"cv-links\">");
            for (ContactLinkEntity link : links) {
                if (!link.isCvInclude()) continue;
                html.append("<a href=\"").append(HtmlEscaper.escape(link.getUrl())).append("\">")
                    .append(HtmlEscaper.escape(link.getLabel())).append("</a> ");
            }
            html.append("</p>");
        }
        html.append("</header>");

        // Summary
        if (profile != null && profile.getSummary() != null && !profile.getSummary().isBlank()) {
            html.append("<section><h2>Summary</h2><p>")
                .append(HtmlEscaper.escape(profile.getSummary())).append("</p></section>");
        }

        // Academic summary (always included)
        Optional<StudentAcademicSummaryEntity> summary = academicSummaryRepository.findById(studentId);
        html.append("<section><h2>Academic Summary</h2>");
        if (summary.isPresent()) {
            html.append("<p>Computer Science GPA: ")
                .append(summary.get().getComputerScienceGpa()).append("</p>");
        } else {
            html.append("<p>Official academic results not yet committed.</p>");
        }
        html.append("</section>");

        // Declared skills (always included)
        List<DeclaredSkillEntity> declaredSkills = declaredSkillRepository.search(studentId, "%", org.springframework.data.domain.Pageable.unpaged()).getContent();
        if (!declaredSkills.isEmpty()) {
            html.append("<section><h2>Skills</h2><ul>");
            for (DeclaredSkillEntity ds : declaredSkills) {
                String skillName = skillRepository.findById(ds.getSkillId()).map(SkillEntity::getSkillName).orElse("Unknown");
                html.append("<li>").append(HtmlEscaper.escape(skillName))
                    .append(" (").append(ds.getCompetencyLevel()).append(")</li>");
            }
            html.append("</ul></section>");
        }

        // Work experience (filtered by selection)
        appendFilteredSection(html, "Professional Experience",
            workExperienceRepository.search(studentId, "%", org.springframework.data.domain.Pageable.unpaged()).getContent(), experienceIds,
            WorkExperienceEntity::getId, exp -> {
                StringBuilder item = new StringBuilder("<div><strong>")
                    .append(HtmlEscaper.escape(exp.getPositionTitle() != null ? exp.getPositionTitle() : ""))
                    .append("</strong> &mdash; ").append(HtmlEscaper.escape(exp.getOrganization())).append("<br/>");
                item.append(formatDateRange(exp.getStartDate(), exp.getEndDate(), exp.isCurrentRole()));
                if (exp.getDescription() != null) {
                    item.append("<p>").append(HtmlEscaper.escape(exp.getDescription())).append("</p>");
                }
                item.append("</div>");
                return item.toString();
            });

        // Projects (filtered by selection)
        List<ProjectEntity> allProjects = projectRepository.search(studentId, "%", org.springframework.data.domain.Pageable.unpaged()).getContent();
        appendFilteredSection(html, "Projects", allProjects, projectIds,
            ProjectEntity::getId, proj -> {
                StringBuilder item = new StringBuilder("<div><strong>")
                    .append(HtmlEscaper.escape(proj.getTitle())).append("</strong><br/>");
                if (proj.getDescription() != null) {
                    item.append("<p>").append(HtmlEscaper.escape(proj.getDescription())).append("</p>");
                }
                List<ProjectSkillEntity> mappings = projectSkillRepository.findByIdProjectId(proj.getId());
                if (!mappings.isEmpty()) {
                    item.append("<p class=\"cv-project-skills\">");
                    for (ProjectSkillEntity mapping : mappings) {
                        String name = skillRepository.findById(mapping.getId().getSkillId())
                            .map(SkillEntity::getSkillName).orElse("");
                        item.append(HtmlEscaper.escape(name)).append(" ");
                    }
                    item.append("</p>");
                }
                item.append("</div>");
                return item.toString();
            });

        // Certificates (filtered)
        appendFilteredSection(html, "Certificates",
            certificateRepository.search(studentId, "%", org.springframework.data.domain.Pageable.unpaged()).getContent(), certificateIds,
            CertificateEntity::getId, cert -> "<div><strong>" + HtmlEscaper.escape(cert.getTitle())
                + "</strong> &mdash; " + HtmlEscaper.escape(cert.getIssuer() != null ? cert.getIssuer() : "") + "</div>");

        // Awards (filtered)
        appendFilteredSection(html, "Awards and Honors",
            awardRepository.search(studentId, "%", org.springframework.data.domain.Pageable.unpaged()).getContent(), awardIds,
            AwardEntity::getId, award -> "<div><strong>" + HtmlEscaper.escape(award.getTitle())
                + "</strong> &mdash; " + HtmlEscaper.escape(award.getIssuer() != null ? award.getIssuer() : "") + "</div>");

        // Activities (filtered)
        appendFilteredSection(html, "Extracurricular Activities",
            activityRepository.search(studentId, "%", org.springframework.data.domain.Pageable.unpaged()).getContent(), activityIds,
            ActivityEntity::getId, act -> "<div><strong>" + HtmlEscaper.escape(act.getActivityName())
                + "</strong> &mdash; " + HtmlEscaper.escape(act.getRoleTitle() != null ? act.getRoleTitle() : "") + "</div>");

        html.append("</div>");
        return html.toString();
    }

    private <T> void appendFilteredSection(
        StringBuilder html, String title, List<T> all, List<UUID> selectedIds,
        java.util.function.Function<T, UUID> idExtractor, java.util.function.Function<T, String> renderer) {
        List<T> selected = all.stream().filter(item -> selectedIds.contains(idExtractor.apply(item))).toList();
        if (selected.isEmpty()) return;
        html.append("<section><h2>").append(HtmlEscaper.escape(title)).append("</h2>");
        for (T item : selected) {
            html.append(renderer.apply(item));
        }
        html.append("</section>");
    }

    private String formatDateRange(java.time.LocalDate start, java.time.LocalDate end, boolean current) {
        String startStr = start != null ? start.toString() : "";
        String endStr = current ? "Present" : (end != null ? end.toString() : "");
        return "<span class=\"cv-date-range\">" + startStr + " &ndash; " + endStr + "</span>";
    }

    private List<UUID> safe(List<UUID> input) {
        return input == null ? List.of() : input;
    }

    private StudentEntity currentStudent() {
        var actor = currentActorProvider.currentActor()
            .orElseThrow(() -> new ForbiddenException("No authenticated Student context."));
        return studentRepository.findByUserAccountId(actor.userId())
            .orElseThrow(() -> new NotFoundException("Student record not found for the authenticated account."));
    }
}
