# Security Policy

## Secret Handling

- **No production secrets are committed to the repository.** All sensitive values (database credentials, JWT secrets, SMTP credentials) are injected via environment variables at runtime.
- The `.env.example` file contains only safe local-development placeholder values.
- The `.gitignore` excludes `.env` files to prevent accidental secret commits.
- Docker images do not bake secrets into layers. Secrets are passed via environment variables or mounted secret files.
- The predefined admin seed password hash in `V005__seed_sprint_1_reference_data.sql` is a local-development-only placeholder. Production deployments must use a securely generated password hash.

## Vulnerability Reporting

If you discover a security vulnerability in this project:

1. **Do not** open a public GitHub issue.
2. Contact the project maintainers directly via the University of Ruhuna, Department of Computer Science.
3. Include a description of the vulnerability, steps to reproduce, and the potential impact.
4. Allow reasonable time for a fix before any public disclosure.

## Dependency Management

- Dependencies are managed via Maven and declared in `pom.xml`.
- The CI pipeline runs `dependency:resolve` and `dependency:tree` checks on every push.
- Regularly review dependencies for known vulnerabilities using `mvn dependency-check:check` or equivalent tooling.

## Security Configuration

- Spring Security is configured in `SecurityConfig.java` with stateless session management.
- CSRF is disabled (stateless JWT API).
- CORS is restricted to configured allowed origins.
- Public endpoints are explicitly whitelisted; all other endpoints require authentication.
- Role-based access control (RBAC) restricts `STUDENT` and `ADMIN` paths.

## Removed-Scope Security Rules

The following features are **explicitly removed from scope** and must never be implemented. Their presence in the codebase would constitute a security and scope violation:

- Temporary password generation or distribution
- Admin approval/rejection of student registrations
- Company login or company API role
- AI-based scoring, ranking, or automated selection
- CV review/approval workflows
- Skill verification or verified skill status

The `RemovedScopeGuardrailTest` automatically scans implementation artifacts for forbidden tokens and will fail the build if any are found. See `docs/architecture/removed-scope-guardrails.md` for the complete list.
