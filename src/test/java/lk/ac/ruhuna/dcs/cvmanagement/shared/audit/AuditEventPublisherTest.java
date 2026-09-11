package lk.ac.ruhuna.dcs.cvmanagement.shared.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void requiredAuditFailurePropagatesToProtectTransactionalWorkflow() {
        AuditEventPublisher publisher = new AuditEventPublisher(event -> {
            throw new IllegalStateException("audit store unavailable");
        });

        assertThatThrownBy(() -> publisher.recordSecurityRequired(
                        UUID.randomUUID(),
                        "ADMIN",
                        AuditEventType.AUTH_ADMIN_LOGIN_SUCCEEDED,
                        AuditEventOutcome.SUCCEEDED,
                        AuditEventSeverity.INFO,
                        "user_account",
                        UUID.randomUUID().toString(),
                        Map.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("audit store unavailable");
    }

    @Test
    void bestEffortAuditFailureDoesNotReplacePublicWorkflowResult() {
        AuditEventPublisher publisher = new AuditEventPublisher(event -> {
            throw new IllegalStateException("audit store unavailable");
        });

        assertThatCode(() -> publisher.recordSecurityBestEffort(
                        null,
                        "ANONYMOUS",
                        AuditEventType.AUTH_LOGIN_FAILED,
                        AuditEventOutcome.FAILED,
                        AuditEventSeverity.WARN,
                        "user_account",
                        null,
                        Map.of("accountType", "ADMIN")))
                .doesNotThrowAnyException();
    }

    private static final class CapturingAuditEventSink implements AuditEventSink {
        private AuditEvent event;

        @Override
        public void persist(AuditEvent event) {
            this.event = event;
        }
    }
}
