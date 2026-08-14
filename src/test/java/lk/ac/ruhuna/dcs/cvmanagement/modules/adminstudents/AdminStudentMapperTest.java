package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminLatestCvResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.mapper.AdminStudentMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminActivityRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminAwardRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminCertificateRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminExperienceRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.AdminStudentProfileRow;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.RegisteredStudentRow;
import org.junit.jupiter.api.Test;

class AdminStudentMapperTest {

    private final AdminStudentMapper mapper = new AdminStudentMapper();

    @Test
    void mapsRosterProjectionToCanonicalFrontendShape() {
        UUID studentId = UUID.randomUUID();
        var response = mapper.toListItem(new RegisteredStudentRow(
                studentId,
                "SC/2022/12865",
                "K. Kavindu Lakshan",
                "sc202212865@dcs.ruh.ac.lk",
                "2022",
                3,
                new BigDecimal("3.70")));

        assertThat(response.studentId()).isEqualTo(studentId);
        assertThat(response.indexNumber()).isEqualTo("SC/2022/12865");
        assertThat(response.fullName()).isEqualTo("K. Kavindu Lakshan");
        assertThat(response.universityEmail()).isEqualTo("sc202212865@dcs.ruh.ac.lk");
        assertThat(response.degreeProgram()).isEqualTo("BSc Honours in Computer Science");
        assertThat(response.academicBatch()).isEqualTo("2022");
        assertThat(response.currentLevel()).isEqualTo(3);
        assertThat(response.officialGpa()).isEqualByComparingTo("3.70");
    }

    @Test
    void mapsDeepDiveProfileAndSupportingDataWithoutExposingInternalFileIdentifiers() {
        UUID studentId = UUID.randomUUID();
        UUID experienceId = UUID.randomUUID();
        UUID certificateId = UUID.randomUUID();
        UUID awardId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-08-13T09:00:00Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-08-14T09:00:00Z");

        var profileRow = new AdminStudentProfileRow(
                studentId,
                "Asha Silva",
                "SC/2022/12345",
                "asha@dcs.ruh.ac.lk",
                3,
                2022,
                "asha@example.com",
                "Software engineering undergraduate",
                "Interested in dependable systems.",
                "+94 77 123 4567",
                "Matara",
                2,
                updatedAt,
                updatedAt.plusHours(1));
        var experiences = List.of(new AdminExperienceRow(
                experienceId,
                "Example Labs",
                "Engineering Intern",
                "Colombo",
                LocalDate.parse("2026-01-01"),
                null,
                true,
                "Built administrative interfaces.",
                true,
                1,
                createdAt,
                updatedAt));
        var certificates = List.of(new AdminCertificateRow(
                certificateId,
                "Web Accessibility Foundations",
                "Open Learning",
                LocalDate.parse("2025-10-10"),
                "https://example.com/credentials/asha",
                true,
                1,
                createdAt,
                updatedAt));
        var awards = List.of(new AdminAwardRow(
                awardId,
                "Faculty Project Award",
                "University of Ruhuna",
                LocalDate.parse("2025-11-15"),
                "Recognized for a dependable design.",
                true,
                1,
                createdAt,
                updatedAt));
        var activities = List.of(new AdminActivityRow(
                activityId,
                "Computer Science Society",
                "Committee Member",
                LocalDate.parse("2024-01-01"),
                LocalDate.parse("2025-12-31"),
                "Supported technical sessions.",
                true,
                1,
                createdAt,
                updatedAt));

        var profile = mapper.toProfile(profileRow);
        var supporting = mapper.toSupportingData(experiences, certificates, awards, activities);

        assertThat(profile.studentId()).isEqualTo(studentId);
        assertThat(profile.degreeProgramme()).isEqualTo("BSc Honours in Computer Science");
        assertThat(profile.profilePhoto()).isNull();
        assertThat(profile.version()).isEqualTo(2);
        assertThat(profile.cvSourceUpdatedAt()).isEqualTo(updatedAt.plusHours(1));
        assertThat(supporting.experiences()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(experienceId);
            assertThat(item.currentRole()).isTrue();
            assertThat(item.endDate()).isNull();
        });
        assertThat(supporting.certificates()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(certificateId);
            assertThat(item.evidence()).isNull();
        });
        assertThat(supporting.awards()).singleElement().satisfies(item -> assertThat(item.id()).isEqualTo(awardId));
        assertThat(supporting.activities()).singleElement().satisfies(item -> assertThat(item.id()).isEqualTo(activityId));
    }

    @Test
    void composesCanonicalDeepDiveResponse() {
        UUID studentId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-08-14T09:00:00Z");
        var student = new RegisteredStudentRow(
                studentId,
                "SC/2022/12345",
                "Asha Silva",
                "asha@dcs.ruh.ac.lk",
                "2022",
                3,
                null);
        var profile = new AdminStudentProfileRow(
                studentId,
                "Asha Silva",
                "SC/2022/12345",
                "asha@dcs.ruh.ac.lk",
                3,
                2022,
                null,
                null,
                null,
                null,
                null,
                0,
                now,
                now);
        var supporting = mapper.toSupportingData(List.of(), List.of(), List.of(), List.of());

        var response = mapper.toDetail(student, profile, supporting, AdminLatestCvResponse.notSaved());

        assertThat(response.student().studentId()).isEqualTo(studentId);
        assertThat(response.profile().studentId()).isEqualTo(studentId);
        assertThat(response.cvSupportingData().experiences()).isEmpty();
        assertThat(response.latestCv().availability().name()).isEqualTo("NOT_SAVED");
    }
}
