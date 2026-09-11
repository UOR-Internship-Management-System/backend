ALTER TABLE public.audit_events
    ADD COLUMN outcome VARCHAR(20),
    ADD COLUMN severity VARCHAR(20);

UPDATE public.audit_events
SET outcome = CASE
    WHEN event_type LIKE '%\_FAILED' ESCAPE '\'
         OR event_type LIKE '%\_FAILURE' ESCAPE '\'
         OR event_type LIKE '%\_UNAVAILABLE' ESCAPE '\' THEN 'FAILED'
    WHEN event_type LIKE '%\_DENIED' ESCAPE '\'
         OR event_type LIKE '%\_REJECTED' ESCAPE '\'
         OR event_type LIKE '%\_NON\_ELIGIBLE' ESCAPE '\' THEN 'DENIED'
    WHEN event_type LIKE '%\_COMPLETED' ESCAPE '\'
         OR event_type LIKE '%\_SUCCEEDED' ESCAPE '\'
         OR event_type LIKE '%\_SUCCESS' ESCAPE '\'
         OR event_type LIKE '%\_CREATED' ESCAPE '\'
         OR event_type LIKE '%\_UPDATED' ESCAPE '\'
         OR event_type LIKE '%\_DELETED' ESCAPE '\'
         OR event_type LIKE '%\_SAVED' ESCAPE '\'
         OR event_type LIKE '%\_FINALIZED' ESCAPE '\'
         OR event_type LIKE '%\_DOWNLOADED%' ESCAPE '\'
         OR event_type LIKE '%\_VERIFIED' ESCAPE '\' THEN 'SUCCEEDED'
    ELSE 'ATTEMPTED'
END;

UPDATE public.audit_events
SET severity = CASE
    WHEN event_category <> 'SECURITY' THEN NULL
    WHEN event_type LIKE '%MAX%ATTEMPT%' OR event_type LIKE '%AUTHORIZATION%DENIED%' THEN 'HIGH'
    WHEN outcome IN ('FAILED', 'DENIED') THEN 'WARN'
    ELSE 'INFO'
END;

UPDATE public.audit_events
SET metadata = '{}'::jsonb
WHERE metadata IS NULL;

ALTER TABLE public.audit_events
    ALTER COLUMN outcome SET NOT NULL,
    ALTER COLUMN outcome SET DEFAULT 'ATTEMPTED',
    ALTER COLUMN metadata SET NOT NULL,
    ALTER COLUMN metadata SET DEFAULT '{}'::jsonb;

ALTER TABLE public.audit_events
    ADD CONSTRAINT chk_audit_events_outcome
        CHECK (outcome IN ('SUCCEEDED', 'FAILED', 'DENIED', 'ATTEMPTED')),
    ADD CONSTRAINT chk_audit_events_severity
        CHECK (severity IS NULL OR severity IN ('INFO', 'WARN', 'HIGH', 'CRITICAL')),
    ADD CONSTRAINT chk_audit_events_security_severity
        CHECK (event_category <> 'SECURITY' OR severity IS NOT NULL),
    ADD CONSTRAINT chk_audit_events_metadata_object
        CHECK (jsonb_typeof(metadata) = 'object');

CREATE INDEX idx_audit_events_category_occurred_at_id
    ON public.audit_events(event_category, occurred_at DESC, id);

CREATE INDEX idx_audit_events_resource_occurred_at_id
    ON public.audit_events(resource_type, resource_id, occurred_at DESC, id)
    WHERE resource_type IS NOT NULL AND resource_id IS NOT NULL;

CREATE INDEX idx_audit_events_actor_occurred_at_id
    ON public.audit_events(actor_user_id, occurred_at DESC, id)
    WHERE actor_user_id IS NOT NULL;
