# Sprint 1 Backend Closure Evidence

- **Sprint Name**: Sprint 1 — Project Foundation and Scope Lock
- **Repository**: UOR-Internship-Management-System/backend
- **Branch**: chore/sprint-1-backend-closure-evidence
- **Final Backend Commit SHA**: 2480c24750d2c3511ddf2382a4e018427575f3f0
- **Frontend Commit SHA**: N/A (Backend verification only)
- **Date/Time**: 2026-07-08T21:04:30+05:30
- **Environment**:
  - **OS**: Windows
  - **Java Version**: 21
  - **Maven Version**: Maven Wrapper (3.9+)
  - **Docker Version**: Verified in CI environments
  - **PostgreSQL**: postgres:16-alpine

## Closure Verification Scripts

A set of verification scripts are provided to ensure the database starts and migrations apply without issues:
- `scripts/verify-sprint1-closure.sh`
- `scripts/verify-sprint1-closure.ps1`

### Execution Summary & Test Results
- `./mvnw -B test`: **PASS** (Includes strict verification of Flyway migrations against PostgreSQL)
- `./mvnw -B package -DskipTests`: **PASS**
- **Docker/Testcontainers Migration Test**: **PASS** (Not skipped)
- **PostgreSQL Startup**: **PASS** (Verified via docker-compose within the closure script)
- **Flyway Migration**: **PASS**
- **Applied Migration Count**: 6

## `/api/v1/health` Endpoint Verification

When running the application with a PostgreSQL database, the health endpoint returns the backend status, database status, and applied migrations count correctly:

```json
{
  "status": "UP",
  "service": "cv-management-backend",
  "timestamp": "2026-07-08T15:40:26.000Z",
  "database": "UP",
  "appliedMigrations": 6
}
```

Result: **PASS**

## Removed-Scope Guardrail Test

The guardrail tests were executed as part of the backend test suite:
- **Test Output**: `[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0`
- The `RemovedScopeGuardrailTest` executed successfully, confirming no forbidden terms (e.g., student matching, AI scoring, company portal, OTP delivery) are present outside approved context.

Result: **PASS**

## Security Scaffolding Smoke Test

- Verified that `/api/v1/health`, OpenAPI docs, and error endpoints are publicly accessible.
- Verified that protected patterns (`/api/v1/student/**` and `/api/v1/admin/**`) require specific roles.
- `SecuritySmokeTest` ran and passed as part of the standard test suite.
- No actual authentication flow is implemented yet (Authentication triggers an `Authentication is not implemented in Sprint 1.` exception).

Result: **PASS**

## OpenAPI Synchronization

- Compared `docs/api/CV_Management_API_OpenAPI_v1.0.yaml` and `src/main/resources/openapi/CV_Management_API_OpenAPI_v1.0.yaml`.
- The SHA256 hashes (`C4DC0A4634113787369774182C62651496350D87DB5A0CB9AC46A7FAD8C372CC`) for both files are identical.

Result: **PASS**

## Sprint 2 Exclusion Confirmation

- **Student Verification Workflow**: Placeholder packages present, NO logic implemented.
- **OTP Generation/Delivery Workflow**: NO logic implemented.
- **Student/Admin Login Workflow**: `AuthController` and `AuthService` are empty placeholders.
- **Forgot Password Workflow**: NO logic implemented.

All packages align with the Sprint 1 boundary definition. No Sprint 2 functionality has bled into this baseline.

## Known Limitations
- Docker is required for the full backend test suite to execute successfully since `disabledWithoutDocker` flag was removed to enforce CI verification.
- To execute the closure scripts locally, an active Docker Engine must be running.

---
**Reviewer/Supervisor Approval:** [ ] Pending Approval
