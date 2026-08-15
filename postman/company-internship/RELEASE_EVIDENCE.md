# Company & Internship Release Acceptance Evidence

Use this checklist only for a real acceptance run against the intended local/QA PostgreSQL-backed backend.

## Baseline

- Backend branch: `feature/company-internship-requests`
- Backend commit under test: ____________________
- Frontend branch: `feature/company-internship-requests`
- Frontend commit under test: ____________________
- Database/Flyway version: `V059`
- Newman version: ____________________
- Test date/time: ____________________
- Tester: ____________________

## Preconditions

- [ ] Backend health is `UP`.
- [ ] PostgreSQL is the real persistence store; no mock Company/Internship state is active.
- [ ] Local Admin credentials are configured outside Git.
- [ ] Local Student credentials are configured outside Git.
- [ ] Two distinct existing `ACTIVE` taxonomy Skill UUIDs are configured.
- [ ] Patch 8 live Playwright E2E has been executed successfully, or is explicitly recorded as pending.

## Newman acceptance

- [ ] `00 - Authentication` passed.
- [ ] `01 - Company CRUD` passed.
- [ ] `02 - Company Validation and Concurrency` passed.
- [ ] `03 - Internship Request CRUD` passed.
- [ ] `04 - Required Skills` passed.
- [ ] `05 - Internship Validation and Concurrency` passed.
- [ ] `06 - Cascade Delete` passed.
- [ ] `07 - Security` passed.
- [ ] `08 - Cleanup` passed.
- [ ] Newman completed with zero failed requests/assertions.

JSON report: ____________________

JUnit report: ____________________

## Required acceptance observations

- [ ] Duplicate normalized Company -> `409 DUPLICATE_COMPANY`.
- [ ] Missing Company -> `404 COMPANY_NOT_FOUND`.
- [ ] Invalid taxonomy Skill -> `422 INVALID_TAXONOMY_SKILL`.
- [ ] Duplicate required Skill -> `409 DUPLICATE_REQUIRED_SKILL`.
- [ ] Missing `If-Match` -> `428 IF_MATCH_REQUIRED`.
- [ ] Stale `If-Match` -> `412 PRECONDITION_FAILED`.
- [ ] No token -> `401 UNAUTHORIZED`.
- [ ] Student token on Admin endpoint -> `403 FORBIDDEN`.
- [ ] Removed Company `active` field is rejected.
- [ ] Removed Internship Request status/GPA fields are rejected.
- [ ] Company hard delete removes linked Requests.
- [ ] Canonical Skill remains usable after Company cascade deletion.
- [ ] Cleanup leaves no Patch 9 Company/Request business rows.

## Final closure

- [ ] Backend regression suite completed with no new Company/Internship failures.
- [ ] Known unrelated Projects architecture failures, if still present, are recorded separately and not hidden.
- [ ] Patch 8 live frontend-backend E2E: `3 passed`.
- [ ] Patch 9 Newman acceptance: passed.
- [ ] No credentials, tokens, local environments, or reports were committed.

Release acceptance decision: **PASS / FAIL / PENDING**

Notes:

______________________________________________________________________________
