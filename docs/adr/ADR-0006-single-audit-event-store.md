# ADR-0006: Continue with one authoritative audit event store

## Status

Accepted for BMD-012 implementation.

## Context

The conceptual database design describes separate `audit.audit_log` and
`audit.security_event` relations. The deployed application already has a
forward-migrated `public.audit_events` table from `V004`, indexes from `V006`,
and production code in multiple modules that persists security and business
events into that table.

Creating the two conceptual relations now would leave parallel stores with
unclear ownership, incomplete history, and inconsistent transaction behavior.
Rewriting `V004` would also invalidate Flyway history in existing databases.

## Decision

`public.audit_events` remains the only authoritative physical event store.

- Business audit events use their controlled business category.
- Security events use the `SECURITY` category and an explicit severity.
- Event outcome is stored explicitly rather than inferred by readers.
- Existing rows are preserved and classified by a forward-only migration.
- `shared.audit` contains the stable cross-module contract.
- `modules.auditlog` owns the persistence adapter and any approved read model.
- Required events participate in the protected business transaction.
- Best-effort events are restricted to non-critical diagnostics and failed
  attempts where an audit-store failure must not change the user-facing result.
- No audit mutation or deletion API is provided.

The optional `/api/v1/admin/audit-events` endpoint remains inactive until an
Admin viewer, security-event visibility, and retention policy are approved and
the underspecified OpenAPI response is corrected.

## Consequences

### Positive

- Existing history and Flyway checksums are preserved.
- All modules retain one transactional write path.
- Security and business events can still be queried independently.
- The project avoids a second parallel persistence model.

### Trade-offs

- The physical table does not mirror the conceptual schema names.
- Database-enforced append-only access requires separate Flyway-owner and
  runtime application roles; the current single-role local setup cannot yet
  make that production claim.
- Retention remains disabled until a written retention and archival policy is
  approved.

## Security constraints

Audit metadata is an allow-listed, bounded JSON object. It must never contain
passwords, OTPs, JWTs, authorization headers, cookies, reset tokens, SQL,
stack traces, CV/file contents, or filesystem paths.

