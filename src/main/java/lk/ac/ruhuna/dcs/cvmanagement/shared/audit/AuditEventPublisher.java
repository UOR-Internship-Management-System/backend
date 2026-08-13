package lk.ac.ruhuna.dcs.cvmanagement.shared.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public AuditEventPublisher(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
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
        jdbcTemplate.update(
                """
                INSERT INTO audit_events (
                    actor_user_id, actor_role, event_type, event_category,
                    resource_type, resource_id, metadata
                )
                VALUES (?, ?, ?, ?, ?, ?, ?::jsonb)
                """,
                actorUserId,
                actorRole,
                eventType,
                category.name(),
                resourceType,
                resourceId,
                serialize(metadata));
    }

    private void recordBestEffort(
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

    private String serialize(Map<String, ?> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata == null ? Map.of() : metadata);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Audit metadata could not be serialized.", exception);
        }
    }
}
