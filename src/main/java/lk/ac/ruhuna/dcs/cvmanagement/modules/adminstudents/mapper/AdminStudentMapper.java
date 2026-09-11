package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.mapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminAcademicRecordResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminActivityResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminAwardResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminCertificateResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminDeclaredSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminExperienceResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminLatestCvResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminProjectResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminProjectSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentCvSupportingDataResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentDetailResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentListItemResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentProfileResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminAcademicRecordRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminActivityRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminAwardRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminCertificateRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminDeclaredSkillRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminExperienceRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminProjectRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminProjectSkillRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminStudentProfileRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.RegisteredStudentRow;
import org.springframework.stereotype.Component;

/** Maps Admin Student read projections to the stable OpenAPI v1.6 response contract. */
@Component
public class AdminStudentMapper {

    private static final String DEGREE_PROGRAM = "BSc Honours in Computer Science";

    public AdminStudentListItemResponse toListItem(RegisteredStudentRow row) {
        return new AdminStudentListItemResponse(
                row.studentId(),
                row.indexNumber(),
                row.fullName(),
                row.universityEmail(),
                DEGREE_PROGRAM,
                row.academicBatch(),
                row.currentLevel(),
                row.officialGpa());
    }

    public AdminStudentProfileResponse toProfile(AdminStudentProfileRow row) {
        return new AdminStudentProfileResponse(
                row.studentId(),
                row.fullName(),
                row.indexNumber(),
                row.universityEmail(),
                DEGREE_PROGRAM,
                row.studentLevel(),
                row.cohortYear(),
                row.personalEmail(),
                row.headline(),
                row.summary(),
                row.phone(),
                row.location(),
                // The current Student profile module does not expose a resolvable profile-photo
                // asset yet. Do not leak the internal profile_photo_file_id as a substitute URL.
                null,
                row.version(),
                row.updatedAt(),
                row.cvSourceUpdatedAt());
    }

    public AdminStudentCvSupportingDataResponse toSupportingData(
            List<AdminExperienceRow> experiences,
            List<AdminCertificateRow> certificates,
            List<AdminAwardRow> awards,
            List<AdminActivityRow> activities) {
        return new AdminStudentCvSupportingDataResponse(
                experiences.stream().map(this::toExperience).toList(),
                certificates.stream().map(this::toCertificate).toList(),
                awards.stream().map(this::toAward).toList(),
                activities.stream().map(this::toActivity).toList());
    }

    public AdminStudentDetailResponse toDetail(
            RegisteredStudentRow student,
            AdminStudentProfileRow profile,
            AdminStudentCvSupportingDataResponse supportingData,
            AdminLatestCvResponse latestCv) {
        return new AdminStudentDetailResponse(
                toListItem(student),
                toProfile(profile),
                supportingData,
                latestCv);
    }

    private AdminExperienceResponse toExperience(AdminExperienceRow row) {
        return new AdminExperienceResponse(
                row.id(),
                row.organization(),
                row.positionTitle(),
                row.location(),
                row.startDate(),
                row.endDate(),
                row.currentRole(),
                row.description(),
                row.cvInclude(),
                row.version(),
                row.createdAt(),
                row.updatedAt());
    }

    private AdminCertificateResponse toCertificate(AdminCertificateRow row) {
        return new AdminCertificateResponse(
                row.id(),
                row.title(),
                row.issuer(),
                row.issueDate(),
                row.credentialUrl(),
                row.cvInclude(),
                // Certificate evidence upload/linkage is not implemented in the current backend.
                null,
                row.version(),
                row.createdAt(),
                row.updatedAt());
    }

    private AdminAwardResponse toAward(AdminAwardRow row) {
        return new AdminAwardResponse(
                row.id(),
                row.title(),
                row.issuer(),
                row.awardDate(),
                row.description(),
                row.cvInclude(),
                row.version(),
                row.createdAt(),
                row.updatedAt());
    }

    private AdminActivityResponse toActivity(AdminActivityRow row) {
        return new AdminActivityResponse(
                row.id(),
                row.activityName(),
                row.roleTitle(),
                row.startDate(),
                row.endDate(),
                row.description(),
                row.cvInclude(),
                row.version(),
                row.createdAt(),
                row.updatedAt());
    }

    public AdminDeclaredSkillResponse toDeclaredSkill(AdminDeclaredSkillRow row) {
        return new AdminDeclaredSkillResponse(
                row.declaredSkillId(),
                row.skillId(),
                row.skillName(),
                row.competencyLevel(),
                row.version(),
                row.createdAt(),
                row.updatedAt());
    }

    public AdminProjectResponse toProject(
            AdminProjectRow row,
            Map<UUID, List<AdminProjectSkillRow>> skillsByProject) {
        List<AdminProjectSkillResponse> skills = skillsByProject
                .getOrDefault(row.projectId(), List.of())
                .stream()
                .map(this::toProjectSkill)
                .toList();
        return new AdminProjectResponse(
                row.projectId(),
                row.title(),
                row.description(),
                row.repositoryUrl(),
                row.demoUrl(),
                row.startDate(),
                row.endDate(),
                skills,
                row.includeInCv(),
                row.version(),
                row.createdAt(),
                row.updatedAt());
    }

    public AdminAcademicRecordResponse toAcademicRecord(AdminAcademicRecordRow row) {
        return new AdminAcademicRecordResponse(
                row.academicRecordId(),
                row.subjectId(),
                row.courseCode(),
                row.courseTitle(),
                row.credits(),
                row.letterGrade(),
                row.gradePoint(),
                row.semester(),
                row.academicYear(),
                row.attemptNumber(),
                row.resultStatus(),
                row.committedAt());
    }

    private AdminProjectSkillResponse toProjectSkill(AdminProjectSkillRow row) {
        return new AdminProjectSkillResponse(row.skillId(), row.name(), row.description());
    }

}
