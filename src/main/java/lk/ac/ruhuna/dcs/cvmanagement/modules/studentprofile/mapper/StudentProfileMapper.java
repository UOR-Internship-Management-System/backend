package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.mapper;

import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.ActivityResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.AwardResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.CertificateResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.ContactLinkResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.StudentProfileResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response.WorkExperienceResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.ActivityEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.AwardEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.CertificateEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.ContactLinkEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.StudentProfileEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.persistence.entity.WorkExperienceEntity;
import org.springframework.stereotype.Component;

@Component
public class StudentProfileMapper {

    public StudentProfileResponse toResponse(StudentEntity student, StudentProfileEntity profile) {
        String fullName = (profile.getDisplayName() != null && !profile.getDisplayName().isBlank())
            ? profile.getDisplayName()
            : student.getFullName();
        return new StudentProfileResponse(
            student.getId(),
            fullName,
            student.getIndexNumber(),
            student.getUniversityEmail(),
            "BSc Honours in Computer Science",
            student.getAcademicLevel(),
            parseCohortYear(student.getIndexNumber()),
            profile.getPersonalEmail(),
            profile.getHeadline(),
            profile.getSummary(),
            profile.getPhone(),
            profile.getLocation(),
            null,
            profile.getVersion() != null ? profile.getVersion() : 0L,
            profile.getUpdatedAt(),
            profile.getUpdatedAt());
    }

    private Integer parseCohortYear(String indexNumber) {
        if (indexNumber == null) return null;
        String[] parts = indexNumber.split("/");
        if (parts.length < 2) return null;
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public ContactLinkResponse toResponse(ContactLinkEntity entity) {
        return new ContactLinkResponse(
            entity.getId(),
            entity.getLabel(),
            entity.getUrl(),
            entity.getDisplayOrder(),
            entity.isCvInclude(),
            entity.getVersion() != null ? entity.getVersion() : 0L,
            entity.getCreatedAt(),
            entity.getUpdatedAt());
    }

    public CertificateResponse toResponse(CertificateEntity entity) {
        return new CertificateResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getIssuer(),
            entity.getIssueDate(),
            entity.getCredentialUrl(),
            null, // evidence — stubbed until file upload is built
            entity.isCvInclude(),
            entity.getVersion() != null ? entity.getVersion() : 0L,
            entity.getCreatedAt(),
            entity.getUpdatedAt());
    }

    public AwardResponse toResponse(AwardEntity entity) {
        return new AwardResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getIssuer(),
            entity.getAwardDate(),
            entity.getDescription(),
            entity.isCvInclude(),
            entity.getVersion() != null ? entity.getVersion() : 0L,
            entity.getCreatedAt(),
            entity.getUpdatedAt());
    }

    public ActivityResponse toResponse(ActivityEntity entity) {
        return new ActivityResponse(
            entity.getId(),
            entity.getActivityName(),
            entity.getRoleTitle(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getDescription(),
            entity.isCvInclude(),
            entity.getVersion() != null ? entity.getVersion() : 0L,
            entity.getCreatedAt(),
            entity.getUpdatedAt());
    }

    public WorkExperienceResponse toResponse(WorkExperienceEntity entity) {
        return new WorkExperienceResponse(
            entity.getId(),
            entity.getOrganization(),
            entity.getPositionTitle(),
            entity.getLocation(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.isCurrentRole(),
            entity.getDescription(),
            entity.isCvInclude(),
            entity.getVersion() != null ? entity.getVersion() : 0L,
            entity.getCreatedAt(),
            entity.getUpdatedAt());
    }
}
