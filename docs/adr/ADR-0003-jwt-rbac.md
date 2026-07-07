# Adr 0003 Jwt Rbac

## Status

Accepted for Version 1.

## Decision

Use Spring Security with JWT/RBAC for protected APIs.

## Context

The system has student and department admin actors with strict ownership and read/write boundaries. Backend authorization is the security authority; frontend route guards only improve user experience.

## Consequences

- Student-owned resources require ownership checks.
- Admin APIs require admin role enforcement.
- Public paths are limited to health and OpenAPI-declared auth/onboarding/password-reset paths.
- JWT issuing and validation business behavior starts in Sprint 2, not Sprint 1 closure hardening.
