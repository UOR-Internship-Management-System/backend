# BMD-007 Batch 2 — Application Instructions

## Scope

Batch 2 contains:

1. **Patch 4 — ATS LaTeX/PDF Generation and Staged File Storage**
2. **Patch 5 — Atomic Active-CV Save, ETag, and Idempotency**
3. **Patch 6 — Student Download and Admin Latest-CV Integration**

Apply these patches to the existing **`feature/cvBuilder`** branch after Batch 1.

Expected Batch-1 commits already present:

```text
2bd9386 feat(cv): validate and snapshot cv source data
462ba74 feat(db): harden active cv and preview persistence
d8b6db1 fix(cv): align contract validation and source freshness
```

The Batch-1 database has already been validated at **Flyway v084**. Batch 2 intentionally adds **no new Flyway migration**; V083/V084 already provide the required schema.

---

## 1. Preconditions

From the backend repository:

```powershell
git checkout feature/cvBuilder
git branch --show-current
git status
git log --oneline -6
```

Required:

- branch is `feature/cvBuilder`;
- working tree is clean except intentionally untracked patch files;
- Batch-1 commits are present;
- PostgreSQL database is already at v084.

Create a rollback pointer:

```powershell
git branch backup/cvBuilder-before-bmd007-batch2
```

Do **not** modify or rerun V083/V084 manually. Do not run `flyway:repair` as a routine Batch-2 step.

---

# Patch 4 — ATS PDF Generation and Staged Storage

Patch file:

```text
0004-patch-4-ats-pdf-staged-storage.patch
```

## 2. Apply Patch 4

```powershell
git apply --check "C:\path\BMD-007_Batch2\0004-patch-4-ats-pdf-staged-storage.patch"
git apply "C:\path\BMD-007_Batch2\0004-patch-4-ats-pdf-staged-storage.patch"
git diff --check
git status
```

Run backend tests:

```powershell
.\mvnw.cmd test
```

Do not commit if Maven tests fail.

## 3. Install/verify XeLaTeX

Batch 2 requires a real XeLaTeX executable. Verify:

```powershell
xelatex --version
```

If `xelatex` is installed but is not on `PATH`, set the executable explicitly before starting Spring Boot:

```powershell
$env:CV_LATEX_COMMAND="C:\full\path\to\xelatex.exe"
```

Configure CV storage/runtime for local testing if defaults are not suitable:

```powershell
$env:CV_STORAGE_ROOT=".\data\cv"
$env:CV_LATEX_TIMEOUT="PT10S"
$env:CV_PDF_MAX_BYTES="5242880"
$env:CV_PDF_MAX_CONCURRENT="2"
$env:CV_PREVIEW_TTL="PT15M"
$env:CV_CONSUMED_PREVIEW_RETENTION="PT24H"
$env:CV_CLEANUP_POLL_DELAY_MS="60000"
$env:CV_ORPHAN_GRACE_PERIOD="PT1H"
```

The Docker runtime is also updated to install XeLaTeX. Rebuild the backend image before container acceptance:

```powershell
docker build -f docker/Dockerfile -t cv-management-backend:bmd007-batch2 .
```

## 4. Patch-4 smoke check

Start the backend:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Using Postman with an authenticated Student token, call:

```http
POST /api/v1/me/cv/preview
Authorization: Bearer <student-token>
Content-Type: application/json
```

Body must contain all five arrays, for example:

```json
{
  "includedExperienceIds": [],
  "includedProjectIds": [],
  "includedCertificateIds": [],
  "includedAwardIds": [],
  "includedActivityIds": []
}
```

Expected:

- `200 OK`;
- non-empty `previewId`;
- non-empty `htmlPreview`;
- no raw LaTeX field;
- `expiresAt > generatedAt`;
- one `cv_previews` row with non-null staged storage metadata;
- `staged_file_size_bytes > 0`;
- 64-character lowercase SHA-256 checksum;
- storage key under `cv/objects/...`;
- physical PDF exists under the configured private CV storage root.

Database check:

```sql
SELECT
    preview_id,
    student_id,
    source_fingerprint,
    staged_storage_key,
    staged_file_name,
    staged_file_size_bytes,
    staged_checksum_sha256,
    generated_at,
    expires_at,
    consumed_at
FROM cv_previews
ORDER BY generated_at DESC
LIMIT 10;
```

Commit Patch 4 only after the gate passes:

```powershell
git add -A
git commit -m "feat(cv): generate staged ats pdf previews"
```

---

# Patch 5 — Atomic Active CV Save / ETag / Idempotency

Patch file:

```text
0005-patch-5-atomic-active-cv-save.patch
```

## 5. Apply Patch 5

```powershell
git apply --check "C:\path\BMD-007_Batch2\0005-patch-5-atomic-active-cv-save.patch"
git apply "C:\path\BMD-007_Batch2\0005-patch-5-atomic-active-cv-save.patch"
git diff --check
.\mvnw.cmd test
```

Expected behavior introduced by this patch:

- first real CV save requires `If-None-Match: *`;
- first save returns `201 Created` and `ETag: "1"`;
- replacement requires current `If-Match: "<revision>"`;
- replacement returns `200 OK` and increments revision exactly once;
- stale revision returns `412 STALE_VERSION`;
- missing conditional header returns `428 PRECONDITION_REQUIRED`;
- changed/expired preview returns `409 CV_PREVIEW_EXPIRED`;
- same consumed preview retry is idempotent while its resulting revision remains active;
- save promotes the exact staged PDF rather than recompiling it;
- active selections are copied from normalized preview selections;
- source fingerprint is recomputed before promotion;
- source/save freshness timestamps are serialized correctly;
- superseded file assets are reclaimed after commit and retried by scheduled cleanup if required.

## 6. First-save Postman check

Create a fresh preview and capture `previewId`.

First, prove the precondition is enforced:

```http
PUT /api/v1/me/cv
Authorization: Bearer <student-token>
Content-Type: application/json

{
  "previewId": "<preview-id>"
}
```

Expected:

```text
428 PRECONDITION_REQUIRED
```

Then save correctly:

```http
PUT /api/v1/me/cv
Authorization: Bearer <student-token>
If-None-Match: *
Content-Type: application/json

{
  "previewId": "<preview-id>"
}
```

Expected:

```text
201 Created
ETag: "1"
```

Response requirements:

- `revision = 1`;
- `downloadUrl = "/me/cv/download"`;
- `pdfFile.mediaType = "application/pdf"`;
- `pdfFile.fileSizeBytes > 0`;
- returned configuration exactly matches the saved preview.

Retry the exact same successful preview request. Expected:

- same active CV/revision;
- no second revision increment;
- no duplicate generated PDF/file asset.

## 7. Verify active CV persistence

```sql
SELECT
    c.id,
    c.student_id,
    c.revision,
    c.source_fingerprint,
    c.pdf_file_asset_id,
    c.last_saved_preview_id,
    c.generated_at,
    c.saved_at,
    c.updated_at
FROM cvs c;
```

Then:

```sql
SELECT
    file_asset_id,
    owner_account_id,
    file_name,
    storage_key,
    mime_type,
    file_size_bytes,
    checksum_sha256,
    created_at
FROM system.file_asset
WHERE file_asset_id = '<pdf-file-asset-id>';
```

Required:

- MIME type `application/pdf`;
- positive file size;
- 64-character lowercase SHA-256;
- storage key uses the CV namespace;
- `cvs.pdf_file_asset_id` resolves to the row;
- one active `cvs` row maximum for the Student.

Verify normalized configuration as appropriate:

```sql
SELECT * FROM cv_selected_experiences WHERE cv_id = '<cv-id>';
SELECT * FROM cv_selected_projects WHERE cv_id = '<cv-id>';
SELECT * FROM cv_selected_certificates WHERE cv_id = '<cv-id>';
SELECT * FROM cv_selected_awards WHERE cv_id = '<cv-id>';
SELECT * FROM cv_selected_activities WHERE cv_id = '<cv-id>';
```

## 8. Replacement/stale-preview checks

Generate a new preview and capture the current ETag from:

```http
GET /api/v1/me/cv
```

Replace with:

```http
PUT /api/v1/me/cv
If-Match: "1"
```

Expected:

```text
200 OK
ETag: "2"
```

Then attempt another update with the old `If-Match: "1"`.

Expected:

```text
412 STALE_VERSION
```

Generate another preview, mutate a rendered Profile/Skill/Project source, then save the old preview.

Expected:

```text
409 CV_PREVIEW_EXPIRED
```

Commit Patch 5:

```powershell
git add -A
git commit -m "feat(cv): atomically save active cv with etag concurrency"
```

---

# Patch 6 — Student Download + Admin Latest CV

Patch file:

```text
0006-patch-6-student-admin-cv-downloads.patch
```

## 9. Apply Patch 6

```powershell
git apply --check "C:\path\BMD-007_Batch2\0006-patch-6-student-admin-cv-downloads.patch"
git apply "C:\path\BMD-007_Batch2\0006-patch-6-student-admin-cv-downloads.patch"
git diff --check
.\mvnw.cmd test
```

## 10. Student download check

```http
GET /api/v1/me/cv/download
Authorization: Bearer <student-token>
```

Expected:

```text
200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename="cv-<student-uuid>.pdf"
Cache-Control: no-store
X-Content-Type-Options: nosniff
```

Verify downloaded bytes begin with `%PDF-` and match the persisted size/checksum.

No saved CV must return:

```text
404 CV_NOT_SAVED
```

Missing/corrupted backing file must return:

```text
503 CV_FILE_UNAVAILABLE
```

## 11. Admin metadata/download checks

Authenticate as Admin.

```http
GET /api/v1/admin/students/<student-id>/latest-cv
Authorization: Bearer <admin-token>
```

For a Student with a saved CV, expected:

- `availability = "AVAILABLE"`;
- same `cvId` and `revision` as Student `GET /me/cv`;
- same safe filename/file size;
- `downloadUrl = "/admin/students/<student-id>/latest-cv/download"`.

For a registered Student with no active saved CV:

- `availability = "NOT_SAVED"`;
- all CV metadata fields are `null` according to v1.6.0.

Download:

```http
GET /api/v1/admin/students/<student-id>/latest-cv/download
Authorization: Bearer <admin-token>
```

Expected PDF must be byte-identical to the Student active PDF.

Security checks:

- Student token on Admin latest-CV routes -> `403`;
- Admin token on Student preview/save routes -> `403`;
- Admin download does not alter revision, freshness, or CV configuration.

Commit Patch 6:

```powershell
git add -A
git commit -m "feat(cv): add student and admin cv downloads"
```

---

# 12. Final Batch-2 Git verification

```powershell
git status
git log --oneline -8
git diff --check HEAD~3..HEAD
```

Expected new commit sequence:

```text
feat(cv): add student and admin cv downloads
feat(cv): atomically save active cv with etag concurrency
feat(cv): generate staged ats pdf previews
feat(cv): validate and snapshot cv source data
feat(db): harden active cv and preview persistence
fix(cv): align contract validation and source freshness
```

Working tree should be clean except deliberately untracked patch/archive files.

---

# 13. Flyway verification

Batch 2 adds **no new migration**. Confirm current maximum remains V084:

```sql
SELECT version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank DESC
LIMIT 5;
```

Expected latest CV migration:

```text
084 normalize cv record selection state
```

Do not create an artificial V085 just to mark this batch.

---

# 14. Frontend contract checks

No frontend source modification is required by Batch 2; the supplied CV Builder/Admin Student frontend already targets OpenAPI v1.6.0.

From the frontend repository run:

```powershell
npm ci
npm run openapi:check
npm run verify:scope
npm run typecheck
npm run lint
npm run test
npm run build
```

Then run the frontend against the real backend and manually verify:

```text
Student: Preview -> Save -> Download
Student: source edit -> OUTDATED -> Preview -> Replace
Admin: latest CV metadata -> download same active PDF
```

Full live Playwright/Postman/Newman release evidence remains Patch 8, but the above real integration smoke checks are mandatory before accepting Batch 2.

---

# 15. Rollback

If a patch fails before commit:

```powershell
git restore --staged .
git restore .
```

If a Batch-2 commit must be abandoned before pushing:

```powershell
git reset --hard backup/cvBuilder-before-bmd007-batch2
```

Do not delete the PostgreSQL database or V083/V084 schema as part of Batch-2 rollback; Batch 2 contains no schema migration.
