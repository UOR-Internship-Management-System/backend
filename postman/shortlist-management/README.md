# BMD-011 Shortlist Management Postman/Newman Acceptance

This collection exercises the real Admin shortlist lifecycle and asynchronous export endpoints against Spring Boot and PostgreSQL. It does not use frontend mocks.

## Preconditions

1. Start the backend with the `local` profile and a PostgreSQL database migrated through `V090`.
2. Use dedicated active Admin and Student test accounts.
3. Supply one ACTIVE Skill UUID.
4. Supply one active eligible Student UUID whose latest saved CV PDF is available and one active eligible Student UUID without a saved CV. The collection proves partial and zero-available-CV behavior.
5. Install Newman 6.2.2: `npm install --global newman@6.2.2`.

Copy the template and add local-only values:

```powershell
Copy-Item .\postman\shortlist-management\shortlist-management.local.template.postman_environment.json `
  .\postman\shortlist-management\shortlist-management.local.postman_environment.json
```

Never commit the copied environment, credentials, tokens, downloaded files, generated reports, or storage paths.

## Run

```powershell
.\postman\shortlist-management\run-newman.ps1
```

The collection creates uniquely named Company, Internship Request, filtering run and shortlist records. It verifies ETag mutation, duplicate/stale/finalized protections, persistence, Admin-only authorization, CSV export and bulk latest-CV ZIP export.

Completed export jobs intentionally retain references to their finalized shortlists for auditability. The collection therefore does not delete its acceptance records through public APIs. Use the unique `BMD-011 ... <runId>` names to identify them, and purge them only through an approved retention/maintenance procedure after release evidence is recorded.

Release gate: **0 failed requests and 0 failed assertions**. Copy only sanitized report totals into `RELEASE_EVIDENCE.md`.
