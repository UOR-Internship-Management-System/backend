# Adr 0004 Backend Controlled Cv Generation

## Status

Accepted for Version 1; implementation is planned for a later sprint.

## Decision

Generate ATS-compliant CV output on the backend rather than relying on frontend-only rendering.

## Context

The approved scope requires saved CV versions, latest saved CV access by admin, and bulk export of latest saved CVs. Backend generation provides consistent output, persistence, authorization, and auditability.

## Consequences

- Sprint 1 may keep renderer/storage boundaries only.
- CV generation and download behavior must not be simulated during Sprint 1.
- No CV submission, review, approval, rejection, or correction workflow is allowed.
