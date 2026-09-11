package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.domain.policy.ShortlistStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.persistence.query.ShortlistReadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** PostgreSQL regression coverage for optional Shortlist directory filters. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ShortlistReadRepositoryPostgresIntegrationTest {

    private static final UUID ADMIN_ACCOUNT = UUID.fromString("91000000-0000-4000-8000-000000000001");
    private static final UUID COMPANY_ID = UUID.fromString("91000000-0000-4000-8000-000000000002");
    private static final UUID REQUEST_ID = UUID.fromString("91000000-0000-4000-8000-000000000003");
    private static final UUID SHORTLIST_ID = UUID.fromString("91000000-0000-4000-8000-000000000004");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cv_management_shortlist_read_test")
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

    @Autowired private JdbcTemplate jdbc;
    @Autowired private ShortlistReadRepository repository;

    @BeforeEach
    void seedFinalizedShortlist() {
        jdbc.update("DELETE FROM public.shortlists WHERE id = ?", SHORTLIST_ID);
        jdbc.update("DELETE FROM public.internship_requests WHERE id = ?", REQUEST_ID);
        jdbc.update("DELETE FROM public.companies WHERE id = ?", COMPANY_ID);
        jdbc.update("DELETE FROM public.user_accounts WHERE id = ?", ADMIN_ACCOUNT);

        jdbc.update(
                "INSERT INTO public.user_accounts (id, university_email, account_status) VALUES (?, ?, 'ACTIVE')",
                ADMIN_ACCOUNT,
                "shortlist.read.admin@dcs.ruh.ac.lk");
        jdbc.update("INSERT INTO public.companies (id, name) VALUES (?, ?)", COMPANY_ID, "Shortlist Query Ltd");
        jdbc.update(
                """
                INSERT INTO public.internship_requests (
                    id, company_id, title, shortlist_guidance_value, created_by_account_id
                ) VALUES (?, ?, 'Platform Engineering Intern', 2, ?)
                """,
                REQUEST_ID,
                COMPANY_ID,
                ADMIN_ACCOUNT);
        jdbc.update(
                """
                INSERT INTO public.shortlists (
                    id, internship_request_id, status, guidance_value_snapshot,
                    guidance_warning_acknowledged, created_by_account_id,
                    finalized_by_account_id, finalized_at
                ) VALUES (?, ?, 'FINALIZED', 2, TRUE, ?, ?, NOW())
                """,
                SHORTLIST_ID,
                REQUEST_ID,
                ADMIN_ACCOUNT,
                ADMIN_ACCOUNT);
    }

    @Test
    void listsFinalizedShortlistsWhenCompanyFilterIsAbsent() {
        var page = repository.searchShortlists(
                "", ShortlistStatus.FINALIZED, null, 0, 5, "s.updated_at DESC, s.id ASC");

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent())
                .singleElement()
                .satisfies(row -> {
                    assertThat(row.shortlistId()).isEqualTo(SHORTLIST_ID);
                    assertThat(row.status()).isEqualTo(ShortlistStatus.FINALIZED);
                    assertThat(row.companyId()).isEqualTo(COMPANY_ID);
                });
    }

    @Test
    void listsShortlistsWhenAllOptionalFiltersAreAbsent() {
        var page = repository.searchShortlists("", null, null, 0, 5, "s.updated_at DESC, s.id ASC");

        assertThat(page.getContent())
                .extracting(row -> row.shortlistId())
                .containsExactly(SHORTLIST_ID);
    }
}
