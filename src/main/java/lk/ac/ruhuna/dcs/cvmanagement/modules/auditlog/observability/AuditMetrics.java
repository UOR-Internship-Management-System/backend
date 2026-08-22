package lk.ac.ruhuna.dcs.cvmanagement.modules.auditlog.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEvent;
import org.springframework.stereotype.Component;

/** Low-cardinality operational signals for audit persistence. */
@Component
public class AuditMetrics {

    private static final String PERSISTED_COUNTER = "cv.audit.events.persisted";
    private static final String FAILURE_COUNTER = "cv.audit.persistence.failures";

    private final MeterRegistry meterRegistry;

    public AuditMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordPersisted(AuditEvent event) {
        Counter.builder(PERSISTED_COUNTER)
                .description("Audit events persisted successfully")
                .tag("category", event.category().name())
                .tag("outcome", event.outcome().name())
                .tag("criticality", criticality(event))
                .register(meterRegistry)
                .increment();
    }

    public void recordPersistenceFailure(AuditEvent event) {
        Counter.builder(FAILURE_COUNTER)
                .description("Audit event persistence failures")
                .tag("category", event.category().name())
                .tag("criticality", criticality(event))
                .register(meterRegistry)
                .increment();
    }

    private String criticality(AuditEvent event) {
        return event.severity() == null ? "STANDARD" : event.severity().name();
    }
}
