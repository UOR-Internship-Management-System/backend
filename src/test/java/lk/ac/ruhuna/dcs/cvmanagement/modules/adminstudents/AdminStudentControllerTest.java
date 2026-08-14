package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.AdminStudentController;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.request.AdminStudentSearchCriteria;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentListItemResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.application.RegisteredStudentQueryService;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PageMetadata;
import lk.ac.ruhuna.dcs.cvmanagement.shared.pagination.dto.PagedResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AdminStudentControllerTest {

    @Test
    void delegatesRosterParametersWithoutChangingTheWireContract() {
        RegisteredStudentQueryService service = mock(RegisteredStudentQueryService.class);
        AdminStudentController controller = new AdminStudentController(service);
        PagedResponse<AdminStudentListItemResponse> expected =
                new PagedResponse<>(List.of(), new PageMetadata(2, 50, 0, 0, "indexNumber,asc"));
        when(service.list(any())).thenReturn(expected);

        var actual = controller.listRegisteredStudents(2, 50, "indexNumber,asc", "Perera", 3);

        assertThat(actual).isSameAs(expected);
        ArgumentCaptor<AdminStudentSearchCriteria> captor = ArgumentCaptor.forClass(AdminStudentSearchCriteria.class);
        verify(service).list(captor.capture());
        assertThat(captor.getValue()).isEqualTo(
                new AdminStudentSearchCriteria(2, 50, "indexNumber,asc", "Perera", 3));
    }
}
