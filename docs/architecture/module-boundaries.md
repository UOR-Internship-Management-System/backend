# Module Boundaries

Each module owns its API layer, application services, domain policies, mappers, and persistence types. Module code must not directly depend on unrelated module implementation packages.

## Allowed Dependencies

Module code may import:

- Java, Jakarta, Spring, Flyway, validation, and test-supported framework packages.
- Its own module package.
- `shared` package types.
- `infrastructure` package types where an adapter contract is intentionally reused.
- `config` only for framework configuration boundaries.

## Disallowed Dependencies

Module code must not import another module directly unless a later approved implementation introduces a deliberate port or query contract. Cross-module behavior should be coordinated through application-level contracts or shared abstractions, not by reaching into another module's entities or services.

## Sprint 1 Rule

Only the health module exposes a working endpoint in Sprint 1. Future module controllers remain inactive boundaries until their planned sprint.
