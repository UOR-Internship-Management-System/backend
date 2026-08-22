# BMD-012 Audit Operations Runbook

## Scope

The authoritative store is `public.audit_events`. BMD-012 Version 1 is internal-only; it does not expose an Admin HTTP viewer. Audit review is performed through restricted database access and release evidence.

## Operational signals

- `cv.audit.events.persisted{category,outcome,criticality}` counts successful writes.
- `cv.audit.persistence.failures{category,criticality}` counts failed writes.
- Labels are deliberately low-cardinality and never contain user IDs, resource IDs, correlation IDs, emails, index numbers, tokens, passwords, or OTPs.

Alert when the failure counter increases in any production interval. For required business events, persistence failure rolls back the protected transaction. Best-effort events log a warning and increment the failure counter without failing the public request.

## Triage

1. Capture the application timestamp, deployment version, and correlation ID from the structured application log.
2. Confirm PostgreSQL availability and connection-pool health.
3. Inspect recent audit rows using a read-only database role:

```sql
SELECT occurred_at, event_type, event_category, outcome, severity,
       actor_user_id, resource_type, resource_id, correlation_id
FROM public.audit_events
ORDER BY occurred_at DESC, id DESC
LIMIT 100;
```

4. Check the failed workflow's business row. A required audit failure must not leave its protected mutation committed.
5. Never repair an incident by inserting fabricated audit events or editing existing rows.

## Append-only deployment policy

The application contains no update or delete operation for `public.audit_events`, and architecture tests enforce the single writer. Production database permissions must additionally grant the runtime role only `SELECT` and `INSERT` on this table. Apply grants through environment-specific database provisioning because the application may own its local development schema.

Example for a separately owned production table:

```sql
REVOKE UPDATE, DELETE, TRUNCATE ON public.audit_events FROM cv_runtime;
GRANT SELECT, INSERT ON public.audit_events TO cv_runtime;
```

Do not apply that example blindly when the runtime role owns the table; PostgreSQL owners retain implicit privileges. Use a separate migration-owner role for production.

## Retention and privacy

No automatic deletion is enabled. Retention duration and archival location remain a deployment-policy decision. Until approved, preserve rows and restrict database access. Metadata is validated centrally and must not contain secrets, credentials, OTPs, raw authorization headers, or unnecessary personal data.
