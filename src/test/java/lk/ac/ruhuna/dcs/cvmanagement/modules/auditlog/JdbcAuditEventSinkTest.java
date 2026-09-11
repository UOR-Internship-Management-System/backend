package lk.ac.ruhuna.dcs.cvmanagement.modules.auditlog;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.auditlog.application.AuditMetadataPolicy;
import lk.ac.ruhuna.dcs.cvmanagement.modules.auditlog.persistence.JdbcAuditEventSink;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEvent;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventOutcome;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventSeverity;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class JdbcAuditEventSinkTest {

    @Test
    void persistsTheCompleteClassifiedEventContract() {
        CapturingJdbcTemplate jdbc = new CapturingJdbcTemplate();
        JdbcAuditEventSink sink = new JdbcAuditEventSink(jdbc, new ObjectMapper(), new AuditMetadataPolicy());
        UUID actorId = UUID.randomUUID();

        sink.persist(new AuditEvent(
                actorId,
                "ADMIN",
                "AUTHORIZATION_DENIED",
                AuditEventCategory.SECURITY,
                AuditEventOutcome.DENIED,
                AuditEventSeverity.HIGH,
                "ENDPOINT",
                "/api/v1/admin/example",
                Map.of("reasonClass", "ROLE_MISMATCH"),
                "correlation-123"));

        assertThat(jdbc.sql).contains("outcome", "severity", "metadata", "correlation_id");
        assertThat(jdbc.arguments).hasSize(10);
        assertThat(jdbc.arguments[0]).isEqualTo(actorId);
        assertThat(jdbc.arguments[4]).isEqualTo("DENIED");
        assertThat(jdbc.arguments[5]).isEqualTo("HIGH");
        assertThat(jdbc.arguments[8]).isEqualTo("{\"reasonClass\":\"ROLE_MISMATCH\"}");
        assertThat(jdbc.arguments[9]).isEqualTo("correlation-123");
    }

    private static final class CapturingJdbcTemplate extends JdbcTemplate {
        private String sql;
        private Object[] arguments;

        @Override
        public int update(String sql, Object... args) {
            this.sql = sql;
            this.arguments = args;
            return 1;
        }
    }
}
