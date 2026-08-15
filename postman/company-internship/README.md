# Company & Internship Postman/Newman Acceptance

Patch 9 release-acceptance assets for the Company Management and Internship Request Management backend.

## Files

- `company-internship.postman_collection.json` — Postman Collection v2.1.
- `company-internship.local.template.postman_environment.json` — committed environment template with no credentials.
- `run-newman.ps1` — Windows/PowerShell runner.
- `run-newman.sh` — POSIX shell runner.

## Coverage

The collection runs serially and covers:

1. Admin and Student authentication.
2. Company create/list/detail/update.
3. Company normalized duplicate protection.
4. Company missing/stale `If-Match` behavior.
5. Rejection of the removed Company `active` field.
6. Internship Request create/list/detail/update.
7. Required-skill list/add/remove and atomic replacement.
8. Missing Company, invalid taxonomy Skill, duplicate Skill, missing/stale `If-Match`.
9. Rejection of removed Request status/GPA fields.
10. Company hard-delete cascade to Requests/Request-Skills.
11. Proof that canonical Skills survive the cascade by reusing a Skill afterward.
12. `401` for unauthenticated Admin access and `403` for a Student token.
13. Cleanup of generated business data.

The suite requires **two distinct, existing ACTIVE taxonomy Skill UUIDs**. It deliberately does not discover Skills implicitly because the public Skill list does not expose taxonomy status, so explicit IDs make acceptance deterministic.

## Local environment

Copy the template to an ignored local file:

```powershell
Copy-Item \
  postman/company-internship/company-internship.local.template.postman_environment.json \
  postman/company-internship/company-internship.local.postman_environment.json
```

Fill only the local file:

- `baseUrl`, normally `http://127.0.0.1:8080`
- `adminEmail`
- `adminPassword`
- `studentEmail`
- `studentPassword`
- `activeSkillId`
- `secondActiveSkillId`

Do not commit the local environment.

## Newman

Install Newman locally on the machine running acceptance. The scripts require a `newman` executable and do not auto-download packages:

```powershell
npm install --global newman@6.2.2
```

Run:

```powershell
.\postman\company-internship\run-newman.ps1
```

or:

```bash
./postman/company-internship/run-newman.sh
```

Reports are written under `postman/company-internship/reports/` and are ignored by Git.

## Expected result

A release-acceptance run must finish with **0 failed assertions and 0 failed requests**. A failed run is not release evidence even if cleanup subsequently succeeds.

Keep the generated JUnit/JSON report files with the release evidence outside source control if required by your submission or CI process.
