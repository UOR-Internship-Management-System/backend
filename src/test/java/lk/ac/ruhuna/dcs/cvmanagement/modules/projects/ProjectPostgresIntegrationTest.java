package lk.ac.ruhuna.dcs.cvmanagement.modules.projects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.entity.ProjectEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.projects.persistence.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ProjectPostgresIntegrationTest {

    private static final UUID REACT_SKILL_ID =
            UUID.fromString("c0000000-0000-0000-0000-000000000001");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("cv_management_projects_test")
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
    private JdbcTemplate jdbc;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void clearProjects() {
        jdbc.update("DELETE FROM public.student_project_skills");
        jdbc.update("DELETE FROM public.student_projects");
    }

    @Test
    void projectDeletionCascadesSkillLinksButPreservesCanonicalSkill() {
        UUID studentId = seededStudentId();
        UUID projectId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO public.student_projects (id, student_id, title) VALUES (?, ?, 'Portfolio')",
                projectId,
                studentId);
        jdbc.update(
                "INSERT INTO public.student_project_skills (project_id, skill_id) VALUES (?, ?)",
                projectId,
                REACT_SKILL_ID);

        assertThatThrownBy(() -> jdbc.update("DELETE FROM public.skills WHERE id = ?", REACT_SKILL_ID))
                .isInstanceOf(DataIntegrityViolationException.class);

        jdbc.update("DELETE FROM public.student_projects WHERE id = ?", projectId);

        assertThat(count("SELECT COUNT(*) FROM public.student_project_skills WHERE project_id = ?", projectId))
                .isZero();
        assertThat(count("SELECT COUNT(*) FROM public.skills WHERE id = ?", REACT_SKILL_ID)).isEqualTo(1);
    }

    @Test
    void databaseRejectsUnknownStudentAndDuplicateProjectSkillLinks() {
        assertThatThrownBy(() -> jdbc.update(
                        "INSERT INTO public.student_projects (student_id, title) VALUES (?, 'Invalid owner')",
                        UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);

        UUID projectId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO public.student_projects (id, student_id, title) VALUES (?, ?, 'Portfolio')",
                projectId,
                seededStudentId());
        jdbc.update(
                "INSERT INTO public.student_project_skills (project_id, skill_id) VALUES (?, ?)",
                projectId,
                REACT_SKILL_ID);

        assertThatThrownBy(() -> jdbc.update(
                        "INSERT INTO public.student_project_skills (project_id, skill_id) VALUES (?, ?)",
                        projectId,
                        REACT_SKILL_ID))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void projectEntityUsesOptimisticVersioning() {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-16T06:00:00Z");
        ProjectEntity project = new ProjectEntity();
        project.setId(UUID.randomUUID());
        project.setStudentId(seededStudentId());
        project.setTitle("Versioned Portfolio");
        project.setIncludeInCv(true);
        project.setCreatedAt(now);
        project.setUpdatedAt(now);

        ProjectEntity created = projectRepository.saveAndFlush(project);
        assertThat(created.getVersion()).isZero();

        created.setTitle("Updated Portfolio");
        created.setUpdatedAt(now.plusMinutes(1));
        ProjectEntity updated = projectRepository.saveAndFlush(created);

        assertThat(updated.getVersion()).isEqualTo(1L);
    }

    private UUID seededStudentId() {
        return jdbc.queryForObject(
                "SELECT id FROM public.eligible_students ORDER BY index_number LIMIT 1", UUID.class);
    }

    private int count(String sql, Object... arguments) {
        Integer value = jdbc.queryForObject(sql, Integer.class, arguments);
        return value == null ? 0 : value;
    }
}
