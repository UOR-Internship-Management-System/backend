# Adr 0001 Modular Monolith

## Status

Accepted for Version 1.

## Decision

Use a single Spring Boot modular monolith with package-by-feature/domain organization under `lk.ac.ruhuna.dcs.cvmanagement`.

## Context

The project is a two-developer undergraduate capstone with a 45-day implementation plan. A modular monolith keeps deployment and local development simple while still preserving clear backend boundaries for auth, verification, student data, academic records, CV generation, admin operations, filtering, shortlists, exports, audit, shared code, and infrastructure.

## Consequences

- No microservices are introduced.
- Module boundaries are enforced by package structure and tests.
- Cross-module access must remain deliberate and minimal.
- Sprint 1 may contain reserved module packages, but future business behavior is not implemented until its sprint.
