# Testing Strategy

Sprint 1 tests prove the backend foundation is safe to build on.

## Current Coverage

- Application context startup.
- Public health endpoint.
- Security smoke checks for public and protected paths.
- Architecture/package structure checks.
- Flyway migration behavior from an empty PostgreSQL database where Testcontainers is available.
- Removed-scope guardrail scanning.

## Rules

- Add tests with meaningful implementation changes.
- Do not write tests for removed workflows.
- Do not weaken security or migration checks to make tests pass.
- Prefer lightweight source scanning for Sprint 1 architecture rules instead of adding heavy dependencies.

## Future Sprints

Sprint 2 adds onboarding/authentication service and controller tests. Later sprints add module-specific unit, integration, API, security, and repository tests as business behavior is implemented.
