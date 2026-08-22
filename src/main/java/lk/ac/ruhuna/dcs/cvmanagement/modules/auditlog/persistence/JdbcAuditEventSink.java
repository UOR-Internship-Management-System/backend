package lk.ac.ruhuna.dcs.cvmanagement.modules.auditlog.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.auditlog.application.AuditMetadataPolicy;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEvent;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventSink;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcAuditEventSink implements AuditEventSink {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final AuditMetadataPolicy metadataPolicy;

    public JdbcAuditEventSink(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper,
            AuditMetadataPolicy metadataPolicy) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.metadataPolicy = metadataPolicy;
    }

    @Override
    public void persist(AuditEvent event) {
        metadataPolicy.validate(event.metadata());
        jdbcTemplate.update(
                """
                INSERT INTO public.audit_events (
                    actor_user_id, actor_role, event_type, event_category, outcome, severity,
                    resource_type, resource_id, metadata, correlation_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?)
                """,
                event.actorUserId(),
                event.actorRole(),
                event.eventType(),
                event.category().name(),
                event.outcome().name(),
                event.severity() == null ? null : event.severity().name(),
                event.resourceType(),
                event.resourceId(),
                serialize(event),
                event.correlationId());
    }

    private String serialize(AuditEvent event) {
        try {
            return objectMapper.writeValueAsString(event.metadata());
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Audit metadata could not be serialized.", exception);
        }
    }
}
