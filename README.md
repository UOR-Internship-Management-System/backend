# CV Management Backend

Spring Boot backend for the CV Management and Deterministic Internship Candidate Filtering System.

## Sprint 1 Status

This branch is Sprint 1 foundation only. It provides the backend runtime, local PostgreSQL setup, Flyway baseline migrations, health checks, Spring Security/JWT scaffolding, global API error handling, tests, CI, and OpenAPI contract placement. It does not implement full student onboarding, OTP delivery, login, profile, skills, projects, CV generation, academic ledger, company, internship request, filtering, shortlist, or export business workflows.

## Requirements

- Java 21
- Maven Wrapper (`./mvnw` or `mvnw.cmd`)
- Docker and Docker Compose

## Environment

Copy `.env.example` values into your local shell or a local `.env` file used by Docker Compose. The checked-in values are safe local placeholders only.

Important variables:

- `CV_DB_NAME=cv_management`
- `CV_DB_USERNAME=cv_user`
- `CV_DB_PASSWORD=cv_local_password`
- `CV_DB_PORT=5432`
- `JWT_SECRET=change-this-local-development-secret-at-least-32-characters`
- `FRONTEND_ORIGIN=http://localhost:5173`

Do not commit real credentials or production secrets.

## Start PostgreSQL

```bash
docker compose -f docker/docker-compose.dev.yml up -d
docker compose -f docker/docker-compose.dev.yml ps
```

The PostgreSQL service uses database `cv_management`, local user `cv_user`, port `5432`, a persistent named volume, and a `pg_isready` health check.

## Run The Backend Locally

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Health endpoints:

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/api/v1/health`

## Tests And Build

```bash
./mvnw test
./mvnw package
```

The test suite covers application context startup, health endpoint access, security smoke behavior, PostgreSQL Flyway migration application from an empty database, and removed-scope guardrails.

## Database Migrations

Flyway migrations live in:

- `src/main/resources/db/migration`

Sprint 1 baseline includes:

- PostgreSQL bootstrap extension.
- `roles`, `user_accounts`, and `user_roles`.
- `eligible_students` for Index Number + University Email verification readiness.
- `verification_sessions` with OTP hash metadata only.
- `audit_events`.
- Safe local seed data for roles, one predefined local admin placeholder account, and eligible Level 3/Level 4 students.

Hibernate `ddl-auto` is set to `validate` for local runtime. Flyway is the schema authority.

## OpenAPI Contract

The approved API contract is placed at:

- `docs/api/CV_Management_API_OpenAPI_v1.6.0.yaml`
- `src/main/resources/openapi/CV_Management_API_OpenAPI_v1.6.0.yaml`

Older versioned contract files are retained only as historical artifacts.

## Scripts

- `scripts/dev-start.sh` starts PostgreSQL and runs the backend with the local profile.
- `scripts/dev-stop.sh` stops the local PostgreSQL Compose stack.
- `scripts/run-tests.sh` runs backend tests.
- `scripts/migrate-local.sh` starts PostgreSQL and runs the app so Flyway applies migrations.

## Removed-Scope Warning

Do not add Admin student approval, pending/rejected registration lifecycle, temporary passwords, Admin skill management, skill verification, estimated GPA planning, CV submission/review/approval, company login/API role, AI scoring/ranking, match percentage, automated final selection, project approval, hard shortlist blocking, or GPA stored in internship request records.
