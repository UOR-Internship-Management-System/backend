package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Immutable canonical source snapshot shared by HTML preview, fingerprinting, and later LaTeX/PDF rendering. */
public record CvDocumentModel(
        Identity identity,
        Profile profile,
        List<ContactLink> contactLinks,
        List<DeclaredSkill> declaredSkills,
        List<Experience> experiences,
        List<Project> projects,
        List<Certificate> certificates,
        List<Award> awards,
        List<Activity> activities,
        AcademicSummary academicSummary,
        CvConfiguration configuration) {

    public CvDocumentModel {
        contactLinks = List.copyOf(contactLinks);
        declaredSkills = List.copyOf(declaredSkills);
        experiences = List.copyOf(experiences);
        projects = List.copyOf(projects);
        certificates = List.copyOf(certificates);
        awards = List.copyOf(awards);
        activities = List.copyOf(activities);
    }

    public record Identity(UUID studentId, String fullName, String universityEmail, OffsetDateTime studentUpdatedAt) {}

    public record Profile(
            UUID profileId,
            String displayName,
            String personalEmail,
            String headline,
            String summary,
            String phone,
            String location,
            Long version,
            OffsetDateTime updatedAt) {}

    public record ContactLink(
            UUID id, String label, String url, Integer displayOrder, Long version, OffsetDateTime updatedAt) {}

    public record DeclaredSkill(
            UUID declaredSkillId,
            UUID skillId,
            String skillName,
            String competencyLevel,
            Integer displayOrder,
            Long version,
            OffsetDateTime updatedAt) {}

    public record Experience(
            UUID id,
            String organization,
            String positionTitle,
            String location,
            LocalDate startDate,
            LocalDate endDate,
            boolean currentRole,
            String description,
            Long version,
            OffsetDateTime updatedAt) {}

    public record ProjectSkill(UUID skillId, String skillName, Integer displayOrder) {}

    public record Project(
            UUID id,
            String title,
            String description,
            String repositoryUrl,
            String demoUrl,
            LocalDate startDate,
            LocalDate endDate,
            Long version,
            OffsetDateTime updatedAt,
            List<ProjectSkill> skills) {
        public Project {
            skills = List.copyOf(skills);
        }
    }

    public record Certificate(
            UUID id,
            String title,
            String issuer,
            LocalDate issueDate,
            String credentialUrl,
            Long version,
            OffsetDateTime updatedAt) {}

    public record Award(
            UUID id,
            String title,
            String issuer,
            LocalDate awardDate,
            String description,
            Long version,
            OffsetDateTime updatedAt) {}

    public record Activity(
            UUID id,
            String activityName,
            String roleTitle,
            LocalDate startDate,
            LocalDate endDate,
            String description,
            Long version,
            OffsetDateTime updatedAt) {}

    public record AcademicSummary(
            BigDecimal computerScienceGpa,
            BigDecimal totalCredits,
            OffsetDateTime calculatedAt,
            UUID sourceUploadId) {}
}
