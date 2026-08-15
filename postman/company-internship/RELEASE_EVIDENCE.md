# Company & Internship Release Acceptance Evidence

Use this checklist only for a real acceptance run against the intended local/QA PostgreSQL-backed backend.

## Baseline

- Backend branch: `feature/company-internship-requests`
- Backend commit under test: `b141b79` plus the acceptance corrections recorded below
- Frontend branch: `feature/company-internship-requests`
- Frontend commit under test: `88d3b5b` plus the Patch 8 harness corrections recorded below
- Database/Flyway version: `V059`
- Newman version: `6.2.2`
- Test date/time: `2026-08-16 02:12 Asia/Colombo`
- Tester: `Kavin (Codex-assisted local acceptance)`

## Preconditions

- [x] Backend health is `UP`.
- [x] PostgreSQL is the real persistence store; no mock Company/Internship state is active.
- [x] Local Admin credentials are configured outside Git.
- [x] Local Student credentials are configured outside Git.
- [x] Two distinct existing `ACTIVE` taxonomy Skill UUIDs are configured.
- [x] Patch 8 live Playwright E2E has been executed successfully.

## Newman acceptance

- [x] `00 - Authentication` passed.
- [x] `01 - Company CRUD` passed.
- [x] `02 - Company Validation and Concurrency` passed.
- [x] `03 - Internship Request CRUD` passed.
- [x] `04 - Required Skills` passed.
- [x] `05 - Internship Validation and Concurrency` passed.
- [x] `06 - Cascade Delete` passed.
- [x] `07 - Security` passed.
- [x] `08 - Cleanup` passed.
- [x] Newman completed with zero failed requests/assertions.

JSON report: `reports/company-internship-20260816-021226.json` (local, ignored)

JUnit report: `reports/company-internship-20260816-021226.xml` (local, ignored)

## Required acceptance observations

- [x] Duplicate normalized Company -> `409 DUPLICATE_COMPANY`.
- [x] Missing Company -> `404 COMPANY_NOT_FOUND`.
- [x] Invalid taxonomy Skill -> `422 INVALID_TAXONOMY_SKILL`.
- [x] Duplicate required Skill -> `409 DUPLICATE_REQUIRED_SKILL`.
- [x] Missing `If-Match` -> `428 IF_MATCH_REQUIRED`.
- [x] Stale `If-Match` -> `412 PRECONDITION_FAILED`.
- [x] No token -> `401 UNAUTHORIZED`.
- [x] Student token on Admin endpoint -> `403 FORBIDDEN`.
- [x] Removed Company `active` field is rejected with `400 BAD_REQUEST`.
- [x] Removed Internship Request status/GPA fields are rejected with `400 BAD_REQUEST`.
- [x] Company hard delete removes linked Requests.
- [x] Canonical Skill remains usable after Company cascade deletion.
- [x] Cleanup leaves no Patch 9 Company/Request business rows.

## Final closure

- [x] Backend regression suite completed with no new Company/Internship failures.
- [x] Known unrelated Projects architecture failures are recorded below and not hidden.
- [x] Patch 8 live frontend-backend E2E: `3 passed` in `20.1s`.
- [x] Patch 9 Newman acceptance: `39` requests, `39` test scripts, `85` assertions, `0` failures.
- [x] No credentials, tokens, local environments, or reports were committed.

Release acceptance decision: **PASS** for Company Management and Internship Request Management.

Notes:

- Acceptance database: fresh local PostgreSQL database migrated through `V059`; no Flyway repair was used.
- Playwright used local Chrome and the configured real backend; mocks were disabled.
- Patch 8 harness corrections made during acceptance: exact dialog-button matching, pathname-based response predicates, success-toast dismissal, Company-search isolation, and a 60-second live-test timeout.
- Patch 9 corrections made during acceptance: quoted the PowerShell reporter list, accepted the project's fixed UUID seed syntax, and enabled strict unknown-property rejection in Spring MVC's Jackson 3 mapper.
- Full backend regression result: `186` tests, `2` failures, `0` errors, `33` skipped. Both failures are pre-existing and unrelated to Company/Internship work:
  - `ModuleDependencyRulesTest.moduleCodeDoesNotImportFromOtherModules` — Projects imports Skills and Student Profile internals.
  - `ModuleDependencyRulesTest.futureModuleControllersDoNotExposeEndpoints` — Projects controller is active while still classified as a future module.
