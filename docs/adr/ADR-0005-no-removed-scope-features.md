# Adr 0005 No Removed Scope Features

## Status

Accepted for Version 1.

## Decision

Removed-scope features must not be implemented, scaffolded as active behavior, documented as supported, tested as supported, or implied by database/API names.

## Context

The reduced-scope baseline and SRS explicitly removed legacy workflows such as admin approval, temporary passwords, admin skill management, verified skills, estimated GPA planning, CV review, company login, AI ranking, hard shortlist blocking, and GPA persistence inside internship requests.

## Consequences

- Guardrail tests and scans are part of closure hardening.
- Documentation may mention removed features only to state they are forbidden.
- Existing artifacts that imply removed behavior must not be extended.
