package lk.ac.ruhuna.dcs.cvmanagement.shared.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.CorrelationIdContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class AuditEventPublisherTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void requiredAuditEventCarriesCurrentCorrelationId() {
        CapturingJdbcTemplate jdbcTemplate = new CapturingJdbcTemplate();
        AuditEventPublisher publisher = new AuditEventPublisher(jdbcTemplate, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdContext.CORRELATION_ID_HEADER, "audit-correlation-123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        UUID actorId = UUID.randomUUID();

        publisher.recordRequired(
                actorId,
                "ADMIN",
                AuditEventType.COMPANY_CREATED.name(),
                AuditEventCategory.INTERNSHIP_MANAGEMENT,
                "COMPANY",
                UUID.randomUUID().toString(),
                Map.of("source", "test"));

        assertThat(jdbcTemplate.sql).contains("correlation_id");
        assertThat(jdbcTemplate.arguments).hasSize(8);
        assertThat(jdbcTemplate.arguments[0]).isEqualTo(actorId);
        assertThat(jdbcTemplate.arguments[2]).isEqualTo("COMPANY_CREATED");
        assertThat(jdbcTemplate.arguments[3]).isEqualTo("INTERNSHIP_MANAGEMENT");
        assertThat(jdbcTemplate.arguments[7]).isEqualTo("audit-correlation-123");
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
