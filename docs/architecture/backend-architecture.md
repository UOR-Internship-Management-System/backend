# Backend Architecture

The backend is a Spring Boot modular monolith for the approved Version 1 system. Sprint 1 provides only the foundation: application bootstrap, package structure, health checks, security scaffolding, OpenAPI placement, Flyway baseline migrations, local Docker support, and tests that protect the reduced scope.

## Architecture Style

- Root package: `lk.ac.ruhuna.dcs.cvmanagement`.
- Domain modules live under `modules/<module>`.
- Shared primitives live under `shared`.
- Infrastructure adapters live under `infrastructure`.
- Cross-cutting Spring configuration lives under `config`.

Modules keep controllers in `api`, use-case orchestration in `application`, business rules in `domain`, mapping in `mapper`, and persistence adapters/entities/repositories in `persistence` when that layer is needed.

## Sprint 1 Boundary

Sprint 1 does not implement student onboarding, login, profile, skills, projects, CV generation, academic ledger, companies, internship requests, filtering, shortlists, or exports. Those packages may exist as intentional boundaries, but they must not expose active request mappings or simulated business behavior before their planned sprint.

## Persistence

PostgreSQL is the target database and Flyway is the schema authority. Hibernate `ddl-auto` must remain validation-only for non-test runtime profiles.

## Security

Spring Security is enabled from Sprint 1. Public paths are limited to health and OpenAPI-declared onboarding/authentication paths. Protected student and admin paths must remain backend-enforced; frontend route guards are only a UX aid.
