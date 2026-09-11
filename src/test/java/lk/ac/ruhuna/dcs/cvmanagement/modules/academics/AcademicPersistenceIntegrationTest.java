package lk.ac.ruhuna.dcs.cvmanagement.modules.academics;

import static org.assertj.core.api.Assertions.assertThat;

import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.GradeScaleRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.SubjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Verifies Flyway + Hibernate schema compatibility for the Academic Ledger persistence foundation. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AcademicPersistenceIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("cv_management_academic_test")
                    .withUsername("cv_user")
                    .withPassword("cv_local_password");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private GradeScaleRepository gradeScaleRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Test
    void academicMappingsValidateAndAuthoritativeGradeScaleIsAvailable() {
        assertThat(gradeScaleRepository.count()).isEqualTo(13);
        var grade = gradeScaleRepository.findByGradeCodeIgnoreCaseAndActiveTrue("a-").orElseThrow();
        assertThat(grade.getGradePoint()).isEqualByComparingTo("3.70");
        assertThat(grade.isPassing()).isTrue();

        var absent = gradeScaleRepository.findByGradeCodeIgnoreCaseAndActiveTrue("e*").orElseThrow();
        assertThat(absent.getGradePoint()).isEqualByComparingTo("0.00");
        assertThat(absent.isPassing()).isFalse();

        assertThat(subjectRepository.count()).isEqualTo(44);
        var databaseManagement = subjectRepository.findByCourseCodeInAndActiveTrue(java.util.Set.of("CSC1213"))
                .getFirst();
        assertThat(databaseManagement.getCredits()).isEqualByComparingTo("3.0");
        assertThat(databaseManagement.getCatalogVersion()).isEqualTo("CSC-UNIFIED-V1");
        assertThat(databaseManagement.getCohortStartYear()).isEqualTo((short) 2019);
        assertThat(databaseManagement.getCohortEndYear()).isNull();
    }
}
