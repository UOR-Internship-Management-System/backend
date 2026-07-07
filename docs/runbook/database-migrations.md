# Database Migrations

Flyway is the schema authority for PostgreSQL.

## Location

Migrations live in `src/main/resources/db/migration` and use versioned names such as `V001__create_schemas.sql`.

## Sprint 1 Baseline

The current baseline is limited to:

- PostgreSQL bootstrap extension support.
- Roles and user account scaffolding.
- Eligible students for future verification readiness.
- Verification session metadata with OTP hash fields only.
- Audit events.
- Safe local seed data and Sprint 1 indexes.

Do not add future-sprint tables for profile, skills, projects, CV, academic ledger, companies, internships, filtering, shortlists, or exports as part of Sprint 1 hardening.

## Rules

- Do not edit already-applied migrations after a shared baseline is established.
- Keep migrations repeatable from an empty local database.
- Use constraints and indexes deliberately.
- Never store raw OTP values or real secrets.
