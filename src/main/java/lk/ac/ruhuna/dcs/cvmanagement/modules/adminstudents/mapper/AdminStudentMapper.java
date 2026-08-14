package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.mapper;

import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response.AdminStudentListItemResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.persistence.projection.RegisteredStudentRow;
import org.springframework.stereotype.Component;

/** Maps Admin Student read projections to the stable OpenAPI response contract. */
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
}
