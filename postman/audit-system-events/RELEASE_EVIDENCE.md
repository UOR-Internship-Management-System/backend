# BMD-012 Release Evidence

## Automated backend verification

- Focused audit/authentication/metrics/architecture tests: `18` run, `0` failures, `0` errors
- Complete Maven suite: `366` run, `0` failures, `0` errors, `60` skipped
- PostgreSQL/Testcontainers audit checks: pending Docker-capable CI

## Newman acceptance

- Newman version: 6.2.2
- Execution date: pending
- Failed requests: pending
- Failed assertions: pending

## Database verification

- Required security event rows present: pending
- Forbidden metadata rows: expected `0`; pending
- Correlation coverage reviewed: pending

## Frontend safety

BMD-012 Version 1 is internal-only and introduces no frontend route or audit viewer. Existing live frontend workflows must pass with API mocks disabled; evidence is pending a credentialed local run.

Do not place credentials, access tokens, OTPs, personal email addresses, or full audit metadata in this file.
