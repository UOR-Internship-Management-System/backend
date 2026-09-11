# Admin Student Inspection Release Evidence

Do not commit credentials, JWTs, authorization headers, PDF contents, or private storage paths.

## Environment

- Date/time:
- Backend commit:
- Frontend commit:
- Java version:
- PostgreSQL version:
- Flyway max version:
- Newman version: **6.2.2**

## Automated backend gates

- [ ] `git diff --check`
- [ ] focused Admin Student tests pass
- [ ] complete `./mvnw.cmd test` passes
- [ ] PostgreSQL/Testcontainers Admin Student tests pass
- [ ] OpenAPI repository/resource copies are identical
- [ ] no migration was added by Patch 5 or Patch 6

## Security and behavior

- [ ] anonymous representative requests return 401
- [ ] Student representative requests return 403
- [ ] malformed UUID returns `VALIDATION_FAILED`
- [ ] unknown Student returns `REGISTERED_STUDENT_NOT_FOUND`
- [ ] populated and empty child collections return stable page metadata
- [ ] latest CV covers `CURRENT`, `OUTDATED`, and `NOT_SAVED`
- [ ] successful PDF has attachment, `no-store`, and `nosniff` headers
- [ ] successful Admin PDF download created `CV_DOWNLOADED_BY_ADMIN` audit evidence

## Postman/Newman

- [ ] 0 failed requests
- [ ] 0 failed assertions
- JSON report:
- JUnit report:

## Frontend live E2E

- [ ] existing mocked Admin Student tests pass
- [ ] live Spring Boot + PostgreSQL Admin Student scenario passes
- Evidence location:
