# Adr 0002 Postgresql Flyway

## Status

Accepted for Version 1.

## Decision

Use PostgreSQL as the target relational database and Flyway as the only runtime schema migration authority.

## Context

The approved documentation requires normalized relational persistence, auditability, official academic records, deterministic filtering, and safe schema review. Flyway gives explicit, reviewable migrations and avoids implicit production schema creation.

## Consequences

- Hibernate `ddl-auto` must not create or update production schema.
- All schema changes use versioned migrations.
- Sprint 1 migrations remain foundation-only.
- Raw OTPs and real secrets must never be stored.
