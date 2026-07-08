# Dependency Rules

Dependency rules protect the modular monolith from collapsing into a flat controller/service/repository structure.

## Source Rules

- Main Java files must stay under `lk.ac.ruhuna.dcs.cvmanagement`.
- Controllers belong under `modules/<module>/api`.
- Services belong under `modules/<module>/application`.
- Domain policies and ports belong under `modules/<module>/domain`.
- Repositories belong under `modules/<module>/persistence/repository`.

## Runtime Rules

- Do not add new dependencies unless they are required for Sprint 1 hardening or an approved sprint task.
- Do not bypass Flyway by using Hibernate to create runtime schema.
- Do not add cloud, Kubernetes, or production deployment dependencies in Sprint 1.

## Test Enforcement

Architecture tests use lightweight filesystem and source scanning. They check root package placement, required package directories, cross-module imports, removed-scope names, and accidental future endpoint exposure.
