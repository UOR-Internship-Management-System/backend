# Contributing

Thank you for contributing to the CV Management Backend. This document describes the expectations for code contributions.

## Branch Naming

| Pattern | Use |
|---------|-----|
| `feature/<module>-<short-description>` | New feature work |
| `fix/<short-description>` | Bug fixes |
| `chore/<short-description>` | Maintenance, refactoring, documentation |
| `hotfix/<short-description>` | Urgent production fixes |

All work branches are created from `develop` and merged back via pull request.

## Commit Message Expectations

Use conventional-style commit messages:

```
<type>(<scope>): <short summary>

<optional body>
```

Types: `feat`, `fix`, `chore`, `docs`, `test`, `refactor`, `ci`.

Examples:

- `feat(auth): add student login endpoint`
- `fix(health): correct database status field name`
- `chore(docker): update Dockerfile to Java 21`
- `docs(adr): add ADR-0006 for email provider`

## Local Checks Before Pushing

Run the following commands locally before pushing:

```bash
./mvnw -B test                                       # All tests must pass
./mvnw -B package -DskipTests                        # Clean build
docker compose -f docker/docker-compose.dev.yml config  # Valid compose
docker compose -f docker/docker-compose.test.yml config # Valid compose
```

## Scope Guardrails

This project enforces strict scope boundaries. The `RemovedScopeGuardrailTest` will fail CI if forbidden feature tokens appear in implementation code, migrations, scripts, or Docker files.

Before contributing, review `docs/architecture/removed-scope-guardrails.md` for the full list of forbidden features.

Do **not** introduce:

- Admin student approval workflows
- Temporary passwords
- Admin skill management or skill verification
- CV submission/review/approval workflows
- Company login or company API role
- AI scoring, ranking, or match percentage
- Project approval or verification
- Hard shortlist blocking
- GPA stored inside internship request records

## Pull Request Checklist

- [ ] Tests pass locally (`./mvnw -B test`)
- [ ] Build succeeds (`./mvnw -B package -DskipTests`)
- [ ] No raw placeholder comments in Sprint-owned files
- [ ] No removed-scope features introduced
- [ ] OpenAPI files remain synchronised (`./scripts/validate-openapi.sh`)
- [ ] Documentation updated if behaviour changes
