package lk.ac.ruhuna.dcs.cvmanagement.shared.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.CorrelationIdContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class AuditEventPublisherTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void requiredAuditEventCarriesCurrentCorrelationIdAndExplicitClassification() {
        CapturingAuditEventSink sink = new CapturingAuditEventSink();
        AuditEventPublisher publisher = new AuditEventPublisher(sink);
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

        assertThat(sink.event.actorUserId()).isEqualTo(actorId);
        assertThat(sink.event.eventType()).isEqualTo("COMPANY_CREATED");
        assertThat(sink.event.category()).isEqualTo(AuditEventCategory.INTERNSHIP_MANAGEMENT);
        assertThat(sink.event.outcome()).isEqualTo(AuditEventOutcome.SUCCEEDED);
        assertThat(sink.event.severity()).isNull();
        assertThat(sink.event.correlationId()).isEqualTo("audit-correlation-123");
    }

    @Test
    void failedSecurityEventIsClassifiedAsWarning() {
        CapturingAuditEventSink sink = new CapturingAuditEventSink();
        AuditEventPublisher publisher = new AuditEventPublisher(sink);

        publisher.record(null, "ANONYMOUS", "AUTH_LOGIN_FAILURE", Map.of());

        assertThat(sink.event.outcome()).isEqualTo(AuditEventOutcome.FAILED);
        assertThat(sink.event.severity()).isEqualTo(AuditEventSeverity.WARN);
    }

    private static final class CapturingAuditEventSink implements AuditEventSink {
        private AuditEvent event;

        @Override
        public void persist(AuditEvent event) {
            this.event = event;
        }
    }
}
