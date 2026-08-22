# BMD-010 Candidate Filtering Release Evidence

Do not commit credentials, JWTs, authorization headers, personal Student data or private database/storage details.

## Environment

- Date/time:
- Backend commit:
- Frontend commit:
- Java version:
- PostgreSQL version:
- Flyway max version: **V090**
- Newman version: **6.2.2**

## Backend gates

- [x] Candidate enrichment uses authoritative CV and shortlist persistence
- [ ] Complete backend suite passes
- [ ] PostgreSQL/Testcontainers filtering matrix passes
- [ ] Real PostgreSQL/Flyway startup succeeds
- [ ] Representative filtering query plans reviewed

## Frontend gates

- [x] Candidate Filtering typecheck and lint pass
- [x] Focused frontend suite: **6 files, 19 tests passed**
- [x] Three live Candidate Filtering Playwright tests discovered
- [ ] Live Playwright suite: **3 passed**
- Evidence location:

## Postman/Newman

- [ ] Basic immutable run lifecycle passes
- [ ] Inclusive GPA minimum/maximum/range/boundary checks pass
- [ ] AND/OR declared-skill checks pass
- [ ] Search, pagination and stable sorting pass
- [ ] Anonymous 401 and Student 403 checks pass
- [ ] Validation and unknown-resource errors pass
- [ ] Removed score/rank/recommendation fields remain absent
- [ ] 0 failed requests
- [ ] 0 failed assertions
- JSON report:
- JUnit report:

## Pending acceptance note

Live Playwright and Newman execution require ignored local credentials and deterministic PostgreSQL fixtures. Docker-dependent PostgreSQL integration tests also require Docker. These checks must not be simulated or marked complete without real evidence.
