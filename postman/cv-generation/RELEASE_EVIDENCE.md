# BMD-007 Release Evidence

Complete this file during Patch 8 acceptance. Do not commit secrets, JWTs, private filesystem paths, PDF content, or compiler output.

## Environment

- Date/time:
- Git commit:
- Java version:
- PostgreSQL version:
- Flyway max version: **084**
- XeLaTeX version:
- Newman version: **6.2.2**
- Frontend commit:

## Backend gates

- [ ] `git diff --check`
- [ ] `./mvnw test`
- [ ] `./mvnw test -Dcv.latex.integration=true`
- [ ] PostgreSQL/Testcontainers CV acceptance passed (or local PostgreSQL equivalent recorded)
- [ ] Hibernate `validate` startup succeeded
- [ ] no migration above V084 was introduced by Batch 3

## Frontend gates

- [ ] `npm ci`
- [ ] `npm run openapi:check`
- [ ] `npm run verify:scope`
- [ ] `npm run typecheck`
- [ ] `npm run lint`
- [ ] `npm run test`
- [ ] `npm run build`
- [ ] `npm run e2e`
- [ ] `npm run e2e:cv-live`

## Postman/Newman

- [ ] 0 failed requests
- [ ] 0 failed assertions
- JSON report:
- JUnit report:

## Manual evidence

- [ ] Student preview generated without raw LaTeX exposure
- [ ] Student PDF begins with `%PDF-` and contains selectable text
- [ ] Student save/create returned 201 + `ETag: "1"`
- [ ] replacement returned 200 + next ETag
- [ ] source change returned OUTDATED/PROFILE
- [ ] stale preview returned 409 `CV_PREVIEW_EXPIRED`
- [ ] stale ETag returned 412 `STALE_VERSION`
- [ ] Admin downloaded the same latest active CV revision
- [ ] no CV review/approval/history behavior exists
