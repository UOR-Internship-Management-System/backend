package lk.ac.ruhuna.dcs.cvmanagement.modules.admindashboard.persistence;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.DependencyUnavailableException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminDashboardMetricsQuery {

    private static final String INTERNSHIP_REQUESTS_TABLE = "internship_requests";

    private final JdbcTemplate jdbcTemplate;

    public AdminDashboardMetricsQuery(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long countTotalStudents() {
        return count("SELECT COUNT(*) FROM eligible_students");
    }

    public long countRegisteredStudents() {
        return count("""
                SELECT COUNT(DISTINCT es.id)
                FROM eligible_students es
                JOIN user_accounts ua ON ua.id = es.user_account_id
                JOIN user_roles ur ON ur.user_id = ua.id
                JOIN roles r ON r.id = ur.role_id
                WHERE es.is_active = TRUE
                  AND ua.account_status = 'ACTIVE'
                  AND r.name = 'ROLE_STUDENT'
                """);
    }

    public long countInternshipRequests() {
        if (!tableExists(INTERNSHIP_REQUESTS_TABLE)) {
            return 0L;
        }
        return count("SELECT COUNT(*) FROM " + INTERNSHIP_REQUESTS_TABLE);
    }

    private long count(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }

    private boolean tableExists(String expectedTableName) {
        Boolean exists = jdbcTemplate.execute((ConnectionCallback<Boolean>) connection -> {
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet tables = metadata.getTables(connection.getCatalog(), null, null, new String[] {"TABLE"})) {
                while (tables.next()) {
                    if (expectedTableName.equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
                        return true;
                    }
                }
                return false;
            } catch (SQLException exception) {
                throw new DependencyUnavailableException("Admin dashboard metrics cannot be loaded at this time.");
            }
        });
        return Boolean.TRUE.equals(exists);
    }
}
