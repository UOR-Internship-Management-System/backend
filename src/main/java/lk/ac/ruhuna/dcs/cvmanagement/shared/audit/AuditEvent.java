package lk.ac.ruhuna.dcs.cvmanagement.shared.audit;

import java.util.Map;
import java.util.UUID;

public record AuditEvent(
        UUID actorUserId,
        String actorRole,
        String eventType,
        AuditEventCategory category,
        AuditEventOutcome outcome,
        AuditEventSeverity severity,
        String resourceType,
        String resourceId,
        Map<String, ?> metadata,
        String correlationId) {
}

