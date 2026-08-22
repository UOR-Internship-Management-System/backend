package lk.ac.ruhuna.dcs.cvmanagement.shared.audit;

import java.util.Map;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.shared.http.CorrelationIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditEventPublisher.class);

    private final AuditEventSink auditEventSink;

    public AuditEventPublisher(AuditEventSink auditEventSink) {
        this.auditEventSink = auditEventSink;
    }

    public void record(UUID actorUserId, String actorRole, String eventType, String resourceType, String resourceId) {
        recordBestEffort(
                actorUserId,
                actorRole,
                eventType,
                AuditEventCategory.SECURITY,
                resourceType,
                resourceId,
                Map.of());
    }

    public void record(UUID actorUserId, String actorRole, String eventType, Map<String, String> metadata) {
        recordBestEffort(
                actorUserId,
                actorRole,
                eventType,
                AuditEventCategory.SECURITY,
                null,
                null,
                metadata == null ? Map.of() : metadata);
    }

    /**
     * Persists an audit event without swallowing persistence failures.
     *
     * <p>Use this method when the audit record is part of a transactional business invariant. Callers
     * should invoke it inside the same database transaction as the protected mutation.
     */
    public void recordRequired(
            UUID actorUserId,
            String actorRole,
            String eventType,
            AuditEventCategory category,
            String resourceType,
            String resourceId,
            Map<String, ?> metadata) {
        AuditEventOutcome outcome = outcomeFor(eventType);
        auditEventSink.persist(new AuditEvent(
                actorUserId,
                actorRole,
                eventType,
                category,
                outcome,
                category == AuditEventCategory.SECURITY ? severityFor(outcome) : null,
                resourceType,
                resourceId,
                metadata == null ? Map.of() : metadata,
                CorrelationIdContext.current().orElse(null)));
    }

    public void recordBestEffort(
            UUID actorUserId,
            String actorRole,
            String eventType,
            AuditEventCategory category,
            String resourceType,
            String resourceId,
            Map<String, ?> metadata) {
        try {
            recordRequired(actorUserId, actorRole, eventType, category, resourceType, resourceId, metadata);
        } catch (RuntimeException exception) {
            LOGGER.warn("Audit event could not be persisted: {}", eventType);
        }
    }

    public void recordSecurityRequired(
            UUID actorUserId,
            String actorRole,
            AuditEventType eventType,
            AuditEventOutcome outcome,
            AuditEventSeverity severity,
            String resourceType,
            String resourceId,
            Map<String, ?> metadata) {
        persist(
                actorUserId,
                actorRole,
                eventType.name(),
                AuditEventCategory.SECURITY,
                outcome,
                severity,
                resourceType,
                resourceId,
                metadata);
    }

    public void recordSecurityBestEffort(
            UUID actorUserId,
            String actorRole,
            AuditEventType eventType,
            AuditEventOutcome outcome,
            AuditEventSeverity severity,
            String resourceType,
            String resourceId,
            Map<String, ?> metadata) {
        try {
            recordSecurityRequired(
                    actorUserId,
                    actorRole,
                    eventType,
                    outcome,
                    severity,
                    resourceType,
                    resourceId,
                    metadata);
        } catch (RuntimeException exception) {
            LOGGER.warn("Audit event could not be persisted: {}", eventType.name());
        }
    }

    private void persist(
            UUID actorUserId,
            String actorRole,
            String eventType,
            AuditEventCategory category,
            AuditEventOutcome outcome,
            AuditEventSeverity severity,
            String resourceType,
            String resourceId,
            Map<String, ?> metadata) {
        auditEventSink.persist(new AuditEvent(
                actorUserId,
                actorRole,
                eventType,
                category,
                outcome,
                severity,
                resourceType,
                resourceId,
                metadata == null ? Map.of() : metadata,
                CorrelationIdContext.current().orElse(null)));
    }

    private AuditEventOutcome outcomeFor(String eventType) {
        String normalized = eventType == null ? "" : eventType.toUpperCase(java.util.Locale.ROOT);
        if (normalized.contains("FAILED") || normalized.contains("FAILURE") || normalized.contains("UNAVAILABLE")) {
            return AuditEventOutcome.FAILED;
        }
        if (normalized.contains("DENIED") || normalized.contains("REJECTED")
                || normalized.contains("NON_ELIGIBLE")) {
            return AuditEventOutcome.DENIED;
        }
        if (normalized.endsWith("_STARTED") || normalized.endsWith("_SENT")
                || normalized.endsWith("_REQUESTED")) {
            return AuditEventOutcome.ATTEMPTED;
        }
        return AuditEventOutcome.SUCCEEDED;
    }

    private AuditEventSeverity severityFor(AuditEventOutcome outcome) {
        return switch (outcome) {
            case FAILED, DENIED -> AuditEventSeverity.WARN;
            case SUCCEEDED, ATTEMPTED -> AuditEventSeverity.INFO;
        };
    }
}
