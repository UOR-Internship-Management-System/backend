package lk.ac.ruhuna.dcs.cvmanagement.modules.auditlog;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Map;
import lk.ac.ruhuna.dcs.cvmanagement.modules.auditlog.observability.AuditMetrics;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEvent;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventCategory;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventOutcome;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventSeverity;
import org.junit.jupiter.api.Test;

class AuditMetricsTest {

    @Test
    void recordsOnlyLowCardinalityOperationalTags() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        AuditMetrics metrics = new AuditMetrics(registry);
        AuditEvent event = new AuditEvent(
                null,
                "ADMIN",
                "AUTH_LOGIN_FAILED",
                AuditEventCategory.SECURITY,
                AuditEventOutcome.FAILED,
                AuditEventSeverity.WARN,
                "user_account",
                "sensitive-resource-id",
                Map.of("email", "must-not-be-a-tag@example.test"),
                "request-id-must-not-be-a-tag");

        metrics.recordPersisted(event);
        metrics.recordPersistenceFailure(event);

        assertThat(registry.get("cv.audit.events.persisted")
                        .tags("category", "SECURITY", "outcome", "FAILED", "criticality", "WARN")
                        .counter()
                        .count())
                .isEqualTo(1.0);
        assertThat(registry.get("cv.audit.persistence.failures")
                        .tags("category", "SECURITY", "criticality", "WARN")
                        .counter()
                        .count())
                .isEqualTo(1.0);
        assertThat(registry.getMeters())
                .allSatisfy(meter -> assertThat(meter.getId().getTags())
                        .noneMatch(tag -> tag.getValue().contains("example.test")
                                || tag.getValue().contains("sensitive-resource-id")
                                || tag.getValue().contains("request-id")));
    }
}
