# Complete End-to-End Test Report

## CV Management and Candidate Filtering System

| Field | Value |
|---|---|
| Report date | 24 August 2026 |
| Application | CV Management and Candidate Filtering System |
| Backend branch | `develop` |
| Frontend branch | `develop` |
| Test environment | Local Windows development environment |
| Backend URL | `http://localhost:8080` |
| Frontend URL | `http://localhost:5173` |
| Database | PostgreSQL 18, `cv_management` |
| Backend runtime | Java 21, Spring Boot |
| Frontend runtime | Node.js 24.19.0, npm 11.17.0, Vite |
| PDF runtime | MiKTeX XeLaTeX 4.18 (MiKTeX 26.5) |

## 1. Executive summary

The application's principal Student and Admin workflows were exercised against the real frontend, Spring Boot backend, and PostgreSQL database with API mocks disabled. The tested business flows successfully reached the backend and persisted or retrieved real PostgreSQL data.

The following major workflows passed end-to-end:

- Admin and Student authentication;
- Student verification, password establishment, and login;
- Student profile and portfolio record management;
- declared-skill and project management;
- CV preview, version save, PDF generation, and download;
- Admin Student roster, deep-dive inspection, and saved-CV download;
- Academic Ledger upload, staging, validation, atomic commit, and GPA calculation;
- Company and Internship Request management;
- Candidate Filtering execution and candidate retrieval;
- shortlist creation, candidate addition, and finalization;
- shortlist CSV export, individual CV download, and bulk-CV ZIP export.

The E2E process found several integration defects. The blocking Academic Ledger, Skill Taxonomy, shortlist-list, and post-finalization navigation defects were corrected in focused pull requests and passed their complete automated suites and CI checks. Two minor Student-side frontend/API issues remain recorded for separate ownership: Student Dashboard metrics and profile upload-policy loading.

**Overall assessment:** the tested core application workflows passed. Backend and database behavior is stable for the tested scope. Final browser confirmation of the two most recently merged shortlist corrections remains the last runtime verification step.

## 2. Test approach

Testing used the following layers:

1. Manual browser E2E testing through the real React frontend.
2. Chrome Network inspection and sanitized HAR capture.
3. Direct PostgreSQL verification through pgAdmin.
4. Backend unit, integration, architecture, security, and PostgreSQL/Testcontainers tests.
5. Frontend unit/component tests and CI browser checks.
6. Generated PDF inspection and download verification.

Transient requests recorded with status `0` or `(canceled)` were browser-request cancellations caused by navigation, refresh, React query replacement, or duplicate development-mode execution. They were not treated as failures when the replacement request completed successfully.

Browser-extension traffic such as `quillbot-content.js`, `guide.css`, and requests named `invalid` was excluded from application results because it was not initiated by the application.

## 3. Environment and readiness verification

| Component | Verification | Result |
|---|---|---|
| PostgreSQL | Application connected to the local `cv_management` database | Pass |
| Flyway | Schema validated and current migrations applied | Pass |
| Database migrations | 55 migration files present, ending at `V091` | Pass |
| Spring Boot | Application started under the `local` profile on port 8080 | Pass |
| Frontend | Vite application served on port 5173 | Pass |
| API proxy | Frontend `/api/v1` requests reached the backend | Pass |
| XeLaTeX | `xelatex --version` returned MiKTeX XeTeX successfully | Pass |
| PDF download interference | IDM interception was disabled/excluded for localhost | Pass |

Local database history required controlled cleanup of stale Candidate Filtering migration artifacts created during earlier development. The cleanup was performed transactionally, the obsolete tables and indexes were verified absent, and the correct migrations were then applied successfully.

## 4. Authentication and account lifecycle

### 4.1 Admin authentication

| Scenario | Expected | Observed | Result |
|---|---:|---:|---|
| Admin login | `POST /api/v1/auth/admin/login` → 200 | 200 | Pass |
| Authenticated Admin dashboard request | `GET /api/v1/admin/dashboard/metrics` → 200 | 200 | Pass |
| Admin logout | `POST /api/v1/auth/logout` → 204 | 204 | Pass |

### 4.2 Student registration and authentication

The initial registration attempt used credentials not present in `eligible_students` and correctly returned 404. Testing then used the authoritative seeded Student identity from PostgreSQL.

| Scenario | Expected | Observed | Result |
|---|---:|---:|---|
| Unknown Student verification | 404 | 404 | Pass — negative test |
| Eligible Student verification request | 201 | 201 | Pass |
| OTP verification | 200 | 200 | Pass |
| Password establishment | 204 | 204 | Pass |
| Student login | 200 | 200 | Pass |
| Current authenticated user | `GET /api/v1/auth/me` → 200 | 200 | Pass |
| Unauthenticated protected request | 401 | 401 | Pass — negative test |

Development OTP logging was enabled only for local account provisioning and was disabled afterward with `APP_EMAIL_LOG_OTP=false` before continuing the final tests.

## 5. Student profile and portfolio E2E results

All tested create, read, update, persistence-after-refresh, and delete operations reached the real backend.

| Feature | Create | Read/refresh | Update | Delete | Result |
|---|---:|---:|---:|---:|---|
| Professional/contact links | 201 | 200 | 200 | 204 | Pass |
| Certificates | 201 | 200 | 200 | 204 | Pass |
| Awards and honours | 201 | 200 | 200 | 204 | Pass |
| Extracurricular/professional activities | 201 | 200 | 200 | 204 | Pass |
| Professional experience | 201 | 200 | 200 | 204 | Pass |
| Declared skills | 201 | 200 | 200 | 204 | Pass |
| Projects | 201 | 200 | 200 | 204 | Pass |

Additional checks included:

- values remained visible after refresh;
- record versions advanced after successful updates;
- deleted project retrieval returned 404;
- project skill lookup and linking succeeded;
- declared-skill taxonomy lookup succeeded;
- authorization failures returned 401 when the session was unavailable.

## 6. CV Generation and Versioning E2E results

| Scenario | Endpoint/result | Result |
|---|---|---|
| Load CV source records | Profile, skills, projects, activities, awards, certificates, and experience returned 200 | Pass |
| Generate ATS preview | `POST /api/v1/me/cv/preview` → 200 | Pass |
| Render preview in frontend | Sanitized ATS preview displayed | Pass |
| Save current CV version | `PUT /api/v1/me/cv` → 200 | Pass |
| Retrieve current CV metadata | `GET /api/v1/me/cv` → 200 | Pass |
| Check source freshness | `GET /api/v1/me/cv/source-freshness` → 200 | Pass |
| Download Student PDF | `GET /api/v1/me/cv/download` → 200 | Pass |
| Produce actual PDF | XeLaTeX-generated PDF downloaded and opened | Pass |
| Admin latest-CV metadata | Admin Student endpoint returned 200 | Pass |
| Admin latest-CV download | Download endpoint returned 200 | Pass |

After Academic Ledger data changed, the previously saved CV correctly appeared as **Outdated**, proving that source-freshness tracking was active.

## 7. Admin Student Inspection E2E results

| Scenario | Observed | Result |
|---|---:|---|
| Load registered Student roster | 200 | Pass |
| Open Student deep-dive | 200 | Pass |
| Load declared skills | 200 | Pass |
| Load projects | 200 | Pass |
| Load official academic records | 200 | Pass |
| Load latest saved CV metadata | 200 | Pass |
| Download latest saved CV | 200 | Pass |

The final deep-dive page displayed the Student profile, degree and batch details, calculated GPA, three committed academic records, latest saved CV metadata, freshness status, and working CV download.

## 8. Academic Ledger E2E results

The UTF-8 CSV contained three valid CSC records for the seeded Student.

| Scenario | Expected/observed | Result |
|---|---|---|
| Upload ledger CSV | File accepted and batch created | Pass |
| Stage rows | 3 rows staged | Pass |
| Validate rows | 3 valid, 0 invalid | Pass |
| List uploads | 200 | Pass after query correction |
| Retrieve staged rows | 200 | Pass after query correction |
| Retrieve validation results | 200 | Pass |
| Commit official records | `POST .../commit` → 200 | Pass |
| Atomic persistence | Three official records appeared together | Pass |
| GPA calculation | Computer Science GPA calculated as 3.67 | Pass |
| Admin Student read model | Three academic records and GPA displayed | Pass |
| CV freshness propagation | Existing CV marked Outdated | Pass |

The original E2E run exposed nullable/unfiltered repository-query failures in upload and staged-row listing. A focused backend correction and PostgreSQL regression tests were added and merged through backend PR #36. The repeated E2E run successfully loaded the staged rows and processing history and completed the commit.

## 9. Company and Internship Request Management E2E results

| Scenario | Observed | Result |
|---|---:|---|
| List companies | 200 | Pass |
| Create company | 201 | Pass |
| Refresh company list | 200; new company persisted | Pass |
| Load company details | 200 | Pass |
| Load internship requests | 200 | Pass |
| Create internship request | 201 | Pass |
| Load request details | 200 | Pass |
| Load skill clusters | 200 | Pass after fix |
| Load skill categories | 200 | Pass after fix |
| Search/list skills | 200 | Pass |

Two Skill Taxonomy integration defects were found and corrected:

1. frontend sort aliases were not accepted by cluster/category endpoints;
2. nullable nested arrays did not match the frontend response contract.

The corrections were merged through focused backend PRs #33 and #34. The repeated E2E requests for clusters, categories, and skills all returned 200, and an Internship Request was created successfully.

## 10. Candidate Filtering E2E results

| Scenario | Observed | Result |
|---|---:|---|
| Load active companies and Internship Requests | 200 | Pass |
| Load skills and taxonomy | 200 | Pass |
| Create filtering run | 201 | Pass |
| Retrieve run definition | 200 | Pass |
| Retrieve matching candidates | 200 | Pass |
| Select matching Student | Student available for shortlist workflow | Pass |

The tested filtering flow used real Company, Internship Request, Student, skill, GPA, saved-CV, and Academic Ledger data.

## 11. Shortlist and export E2E results

| Scenario | Observed | Result |
|---|---:|---|
| Create shortlist | 201 | Pass |
| Add candidate | 200 | Pass |
| Load shortlist details | 200 | Pass |
| Finalize shortlist | 200 | Pass |
| Create shortlist CSV export job | 202 | Pass |
| Poll CSV export job | 200 | Pass |
| Download shortlist CSV | 200 | Pass |
| Download individual Student CV | 200 | Pass |
| Create bulk-CV ZIP export job | 202 | Pass |
| Poll bulk-CV export job | 200 | Pass |
| Download bulk-CV ZIP | 200 | Pass |

The original finalization run found two integration issues:

- listing shortlists with nullable filters returned 500;
- successful finalization redirected the user away from Candidate Filtering.

Both issues were corrected and merged:

- backend PR #37: nullable shortlist-list query plus PostgreSQL regression tests;
- frontend PR #39: close the review modal, retain success feedback, and remain in Candidate Filtering.

Both fixes passed their full automated suites and GitHub CI. A final browser rerun after restarting both applications is pending to replace the pre-fix HAR evidence.

## 12. Audit Logging and System Events

Audit events are exercised indirectly by the authenticated Admin and Student mutation workflows, including authentication, profile mutations, Academic Ledger commit, CV save/download, filtering, shortlist finalization, and exports.

The BMD-012 audit implementation, persistence rules, security normalization, operational safeguards, and acceptance assets are covered by the complete backend automated suite. A separate Admin audit-viewer UI was not part of the approved frontend scope and was therefore not treated as a missing manual E2E screen.

## 13. Automated regression results

### Backend

Recorded full-suite result after the final shortlist query change:

```text
Tests run: 373
Failures: 0
Errors: 0
Skipped: 63
BUILD SUCCESS
```

The locally skipped tests require Docker/Testcontainers. The focused PostgreSQL tests and GitHub CI checks passed for the merged correction.

### Frontend

Recorded full-suite result after the finalization-navigation change:

```text
Test files: 83 passed
Tests: 371 passed
Failures: 0
```

GitHub CI also passed after the initially flaky Academic Ledger timing check was rerun successfully.

## 14. Defects found and corrective status

| Defect | Severity | Resolution/status |
|---|---|---|
| Skill cluster/category sort alias returned 500 | Blocking integration | Fixed and merged in backend PR #33 |
| Nullable taxonomy arrays broke frontend parsing | Blocking integration | Fixed and merged in backend PR #34 |
| Academic Ledger upload/staged-row reads failed | Blocking integration | Fixed and merged in backend PR #36; E2E rerun passed |
| Shortlist list failed with nullable filters | Blocking integration | Fixed and merged in backend PR #37; final runtime rerun pending |
| Finalization redirected away from Candidate Filtering | Minor UX | Fixed and merged in frontend PR #39; final runtime rerun pending |
| IDM intercepted localhost PDF download | Environment | Resolved by disabling/excluding localhost interception |
| Student Dashboard metrics returned 500 | Known Student-side issue | Assigned outside this acceptance scope |
| Profile upload-policy endpoint returned 500 | Minor supporting-feature issue | Evidence-file controls remain disabled; metadata CRUD passed |
| Initial Student registration data returned 404 | Test-data mismatch | Correct seeded `eligible_students` identity used; flow then passed |

## 15. Evidence inventory

The following sanitized HAR files were reviewed for this report:

- `certificates_test.har`
- `Awards_test.har`
- `Extracurricular_test.har`
- `Professional_Actities_test.har`
- `Declared_Skills_test.har`
- `Projects_test.har`
- `Professional_experiance_test.har`
- `CV_Builder_test.har`
- `Registered_Students_CV_test.har`
- `Academic_Ledger_test.har`
- `Candidate_Filtering_and_Shortlisting_test.har`

Additional page captures and downloaded PDFs were used to verify visible persistence, GPA presentation, CV freshness, generated PDF output, and Admin Student inspection.

No passwords, OTPs, bearer tokens, or unsanitized authorization headers are reproduced in this report.

## 16. Final acceptance status

| Area | Status |
|---|---|
| Backend automated suite | Pass |
| Frontend automated suite | Pass |
| PostgreSQL persistence | Pass |
| Authentication | Pass |
| Student profile/portfolio | Pass, with upload-policy limitation recorded |
| CV generation/versioning | Pass |
| Admin Student inspection | Pass |
| Academic Ledger and GPA | Pass |
| Company/Internship Requests | Pass |
| Candidate Filtering | Pass |
| Shortlist finalization and exports | Business operations pass; final post-fix navigation/list browser rerun pending |
| Audit persistence/operations | Pass through automated coverage |

## 17. Required closure action

Restart the backend and frontend from the latest `develop`, then repeat one final shortlist flow:

1. Open Candidate Filtering.
2. Run a filter and select a candidate.
3. Create and finalize a shortlist.
4. Confirm the modal closes, the success message remains visible, and the page remains on Candidate Filtering.
5. Open Shortlists manually.
6. Confirm the shortlist-list request returns 200 and the finalized shortlist is displayed.
7. Reconfirm CSV, individual CV, and bulk-CV ZIP downloads.

Once this short rerun passes, the final pending status in Sections 11 and 16 can be changed to **Pass**, and the tested release can be considered E2E-verified for the approved scope.
