# BMD-010 Candidate Filtering Postman/Newman Acceptance

This collection exercises deterministic Candidate Filtering against a real Spring Boot backend and PostgreSQL database. It does not use frontend mocks or production Flyway test data.

## Preconditions

1. Start the backend with the `local` profile against PostgreSQL migrated through `V090`.
2. Prepare dedicated active Admin and Student accounts.
3. Prepare an Internship Request with at least one active required Skill.
4. Prepare deterministic eligible Student fixtures with committed official GPA and declared Skills. Include candidates inside and outside the configured GPA bounds.
5. Provide one additional active Skill that does not belong to the Internship Request.
6. Install Newman 6.2.2: `npm install --global newman@6.2.2`.

Copy and fill the ignored local environment:

```powershell
Copy-Item .\postman\candidate-filtering\candidate-filtering.local.template.postman_environment.json `
  .\postman\candidate-filtering\candidate-filtering.local.postman_environment.json
```

Never commit the copied environment, credentials, bearer tokens, Student data or generated reports.

## Run

```powershell
.\postman\candidate-filtering\run-newman.ps1
```

The collection creates immutable filtering runs only. It does not create production test candidates or mutate academic/skill data. Freshness mutation, query plans and audit-row assertions remain PostgreSQL integration-test responsibilities.

Release gate: **0 failed requests and 0 failed assertions**. Record sanitized totals in `RELEASE_EVIDENCE.md`.
