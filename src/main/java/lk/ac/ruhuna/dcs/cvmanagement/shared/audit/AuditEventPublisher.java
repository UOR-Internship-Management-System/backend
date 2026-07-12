package lk.ac.ruhuna.dcs.cvmanagement.shared.audit;

import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuditEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditEventPublisher.class);

    private final JdbcTemplate jdbcTemplate;

    public AuditEventPublisher(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void record(UUID actorUserId, String actorRole, String eventType, String resourceType, String resourceId) {
        try {
            jdbcTemplate.update(
                    """
                    INSERT INTO audit_events (
                        actor_user_id, actor_role, event_type, event_category,
                        resource_type, resource_id, metadata
                    )
                    VALUES (?, ?, ?, 'SECURITY', ?, ?, ?::jsonb)
                    """,
                    actorUserId,
                    actorRole,
                    eventType,
                    resourceType,
                    resourceId,
                    "{}");
        } catch (RuntimeException exception) {
            recordWithoutJsonCast(actorUserId, actorRole, eventType, resourceType, resourceId, "{}");
        }
    }

    public void record(UUID actorUserId, String actorRole, String eventType, Map<String, String> metadata) {
        String json = metadata == null || metadata.isEmpty()
                ? "{}"
                : metadata.entrySet().stream()
                        .map(entry -> "\"" + sanitize(entry.getKey()) + "\":\"" + sanitize(entry.getValue()) + "\"")
                        .reduce("{", (left, right) -> left.equals("{") ? left + right : left + "," + right)
                        + "}";
        try {
            jdbcTemplate.update(
                    """
                    INSERT INTO audit_events (
                        actor_user_id, actor_role, event_type, event_category, metadata
                    )
                    VALUES (?, ?, ?, 'SECURITY', ?::jsonb)
                    """,
                    actorUserId,
                    actorRole,
                    eventType,
                    json);
        } catch (RuntimeException exception) {
            recordWithoutJsonCast(actorUserId, actorRole, eventType, null, null, json);
        }
    }

    private void recordWithoutJsonCast(
            UUID actorUserId,
            String actorRole,
            String eventType,
            String resourceType,
            String resourceId,
            String json) {
        try {
            jdbcTemplate.update(
                    """
                    INSERT INTO audit_events (
                        actor_user_id, actor_role, event_type, event_category,
                        resource_type, resource_id, metadata
                    )
                    VALUES (?, ?, ?, 'SECURITY', ?, ?, ?)
                    """,
                    actorUserId,
                    actorRole,
                    eventType,
                    resourceType,
                    resourceId,
                    json);
        } catch (RuntimeException exception) {
            LOGGER.warn("Security audit event could not be persisted: {}", eventType);
        }
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
