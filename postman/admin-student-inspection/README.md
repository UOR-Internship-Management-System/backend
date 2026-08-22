# Admin Student Inspection Postman/Newman Acceptance

This suite exercises the live Admin Student Inspection API against Spring Boot and PostgreSQL. It does not use mocked endpoints.

## Preconditions

1. Start the backend with the `local` profile and a PostgreSQL database migrated through BMD-007.
2. Prepare dedicated active Admin and Student accounts.
3. Prepare registered Student fixtures covering populated data, empty collections, a current saved CV, an outdated saved CV, and no saved CV.
4. Install Newman 6.2.2: `npm install --global newman@6.2.2`.

Copy the template and enter only local values:

```powershell
Copy-Item .\postman\admin-student-inspection\admin-student-inspection.local.template.postman_environment.json `
  .\postman\admin-student-inspection\admin-student-inspection.local.postman_environment.json
```

Never commit the copied environment, credentials, JWTs, PDF data, or generated reports.

## Run

```powershell
.\postman\admin-student-inspection\run-newman.ps1
```

Release gate: **0 failed requests and 0 failed assertions**. Afterward, run `scripts/db/verify-admin-student-inspection-release.sql` in pgAdmin and record sanitized evidence in `RELEASE_EVIDENCE.md`.
