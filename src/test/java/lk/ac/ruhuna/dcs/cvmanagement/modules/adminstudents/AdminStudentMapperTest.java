package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.mapper.AdminStudentMapper;
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
}
