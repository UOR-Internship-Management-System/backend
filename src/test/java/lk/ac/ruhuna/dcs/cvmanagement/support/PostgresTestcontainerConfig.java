package lk.ac.ruhuna.dcs.cvmanagement.support;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Shared Testcontainers PostgreSQL configuration for integration tests
 * requiring a real database.
 * <p>Use with {@code @Testcontainers} and {@code @Container}.
 */
public final class PostgresTestcontainerConfig {

    public static final String POSTGRES_IMAGE = "postgres:16-alpine";
    public static final String DATABASE_NAME = "cv_management_test";
    public static final String USERNAME = "cv_user";
    public static final String PASSWORD = "cv_local_password";

    private PostgresTestcontainerConfig() {
    }

    /**
     * Creates a new PostgreSQL container configured for CV Management tests.
     */
    public static PostgreSQLContainer<?> createContainer() {
        return new PostgreSQLContainer<>(POSTGRES_IMAGE)
                .withDatabaseName(DATABASE_NAME)
                .withUsername(USERNAME)
                .withPassword(PASSWORD);
    }
}
