# BMD-011 Shortlist Management Release Evidence

Do not commit credentials, JWTs, authorization headers, personal Student data, generated CSV/ZIP contents, or private storage paths.

## Environment

- Date/time:
- Backend commit:
- Frontend commit:
- Java version:
- PostgreSQL version:
- Flyway max version: **V090**
- Newman version: **6.2.2**

## Automated backend gates

- [x] Export-focused and architecture tests: **11 passed**
- [x] Complete backend suite: **356 tests, 0 failures, 0 errors, 60 skipped**
- [ ] PostgreSQL/Testcontainers suite passes with Docker available
- [x] Flyway inventory contains **54 migrations** through V090

## Frontend gates

- [x] Formatting, typecheck and lint pass
- [x] Frontend unit suite: **83 files, 368 tests passed**
- [ ] Live shortlist/export Playwright suite: **3 passed**
- Evidence location:

## Postman/Newman

- [ ] Admin and Student authorization checks pass
- [ ] Shortlist create/detail/mutation/finalization flow passes
- [ ] ETag, duplicate, stale and immutable-finalized checks pass
- [ ] CSV export completes and downloads with safe headers
- [ ] Bulk latest-CV ZIP completes and downloads with safe headers
- [ ] 0 failed requests
- [ ] 0 failed assertions
- JSON report:
- JUnit report:

## Remaining acceptance note

The live Playwright and Newman gates require ignored local credentials, an active Skill fixture, an active eligible Student with an available latest saved CV, a running PostgreSQL service and a running local backend. They were not simulated or marked as passed during implementation.
