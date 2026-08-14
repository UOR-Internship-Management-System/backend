package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.AdminStudentController;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request.AdminAcademicRecordCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request.AdminStudentCollectionCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminAcademicRecordResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminDeclaredSkillResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminProjectResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request.AdminStudentSearchCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminLatestCvResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentCvSupportingDataResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentDetailResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentListItemResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentProfileResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.application.AdminStudentInspectionService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.application.RegisteredStudentQueryService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PageMetadata;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AdminStudentControllerTest {

    @Test
    void delegatesRosterParametersWithoutChangingTheWireContract() {
        RegisteredStudentQueryService rosterService = mock(RegisteredStudentQueryService.class);
        AdminStudentInspectionService inspectionService = mock(AdminStudentInspectionService.class);
        AdminStudentController controller = new AdminStudentController(rosterService, inspectionService);
        PagedResponse<AdminStudentListItemResponse> expected =
                new PagedResponse<>(List.of(), new PageMetadata(2, 50, 0, 0, "indexNumber,asc"));
        when(rosterService.list(any())).thenReturn(expected);

        var actual = controller.listRegisteredStudents(2, 50, "indexNumber,asc", "Perera", 3);

        assertThat(actual).isSameAs(expected);
        ArgumentCaptor<AdminStudentSearchCriteria> captor = ArgumentCaptor.forClass(AdminStudentSearchCriteria.class);
        verify(rosterService).list(captor.capture());
        assertThat(captor.getValue()).isEqualTo(
                new AdminStudentSearchCriteria(2, 50, "indexNumber,asc", "Perera", 3));
    }

    @Test
    void delegatesDeepDiveStudentIdentifierWithoutMutationSemantics() {
        RegisteredStudentQueryService rosterService = mock(RegisteredStudentQueryService.class);
        AdminStudentInspectionService inspectionService = mock(AdminStudentInspectionService.class);
        AdminStudentController controller = new AdminStudentController(rosterService, inspectionService);
        UUID studentId = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.parse("2026-08-14T10:00:00Z");
        var summary = new AdminStudentListItemResponse(
                studentId,
                "SC/2022/12345",
                "Asha Silva",
                "asha@dcs.ruh.ac.lk",
                "BSc Honours in Computer Science",
                "2022",
                3,
                null);
        var profile = new AdminStudentProfileResponse(
                studentId,
                "Asha Silva",
                "SC/2022/12345",
                "asha@dcs.ruh.ac.lk",
                "BSc Honours in Computer Science",
                3,
                2022,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                timestamp,
                timestamp);
        var expected = new AdminStudentDetailResponse(
                summary,
                profile,
                new AdminStudentCvSupportingDataResponse(List.of(), List.of(), List.of(), List.of()),
                AdminLatestCvResponse.notSaved());
        when(inspectionService.getDetail(studentId)).thenReturn(expected);

        assertThat(controller.getStudentDetail(studentId)).isSameAs(expected);
        verify(inspectionService).getDetail(studentId);
    }

    @Test
    void delegatesChildCollectionQueryControlsWithoutMutationSemantics() {
        RegisteredStudentQueryService rosterService = mock(RegisteredStudentQueryService.class);
        AdminStudentInspectionService inspectionService = mock(AdminStudentInspectionService.class);
        AdminStudentController controller = new AdminStudentController(rosterService, inspectionService);
        UUID studentId = UUID.randomUUID();

        PagedResponse<AdminDeclaredSkillResponse> skills =
                new PagedResponse<>(List.of(), new PageMetadata(0, 20, 0, 0, "skillName,asc"));
        PagedResponse<AdminProjectResponse> projects =
                new PagedResponse<>(List.of(), new PageMetadata(1, 50, 0, 0, "updatedAt,desc"));
        PagedResponse<AdminAcademicRecordResponse> academics =
                new PagedResponse<>(List.of(), new PageMetadata(0, 5, 0, 0, "academicYear,desc"));

        when(inspectionService.getDeclaredSkills(any(), any())).thenReturn(skills);
        when(inspectionService.getProjects(any(), any())).thenReturn(projects);
        when(inspectionService.getAcademicRecords(any(), any())).thenReturn(academics);

        assertThat(controller.getDeclaredSkills(studentId, 0, 20, "java")).isSameAs(skills);
        assertThat(controller.getProjects(studentId, 1, 50, "portal")).isSameAs(projects);
        assertThat(controller.getAcademicRecords(
                        studentId, 0, 5, "academicYear,desc", "web", "CSC3112"))
                .isSameAs(academics);

        verify(inspectionService).getDeclaredSkills(
                studentId, new AdminStudentCollectionCriteria(0, 20, "java"));
        verify(inspectionService).getProjects(
                studentId, new AdminStudentCollectionCriteria(1, 50, "portal"));
        verify(inspectionService).getAcademicRecords(
                studentId,
                new AdminAcademicRecordCriteria(0, 5, "academicYear,desc", "web", "CSC3112"));
    }

}
