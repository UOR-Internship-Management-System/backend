package lk.ac.ruhuna.dcs.cvmanagement.modules.auditlog.application;

import java.util.Map;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class SecurityEventService {

    private final AuditEventPublisher auditEventPublisher;

    public SecurityEventService(AuditEventPublisher auditEventPublisher) {
        this.auditEventPublisher = auditEventPublisher;
    }

    public void record(UUID actorUserId, String actorRole, String eventType, String resourceType, String resourceId) {
        auditEventPublisher.record(actorUserId, actorRole, eventType, resourceType, resourceId);
    }

    public void record(UUID actorUserId, String actorRole, String eventType, Map<String, String> metadata) {
        auditEventPublisher.record(actorUserId, actorRole, eventType, metadata);
    }
}
