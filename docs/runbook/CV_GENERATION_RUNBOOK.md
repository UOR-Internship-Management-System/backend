# CV Generation and Versioning Runbook

## Runtime dependencies

BMD-007 requires:

- Java 21;
- PostgreSQL with Flyway schema at V084 or later approved project migration;
- private writable CV storage root;
- XeLaTeX available through `CV_LATEX_COMMAND` (default `xelatex`).

Recommended local settings:

```text
CV_STORAGE_ROOT=./data/cv
CV_LATEX_COMMAND=xelatex
CV_LATEX_TIMEOUT=PT10S
CV_PDF_MAX_BYTES=5242880
CV_PDF_MAX_CONCURRENT=2
CV_PREVIEW_TTL=PT15M
CV_CONSUMED_PREVIEW_RETENTION=PT24H
```

## Health/preflight

```powershell
java -version
xelatex --version
.\mvnw.cmd test
```

Start the backend and confirm Flyway/Hibernate validation. Batch 3 itself must not create V085.

## Failure recovery

### `CV_GENERATION_FAILED`

Check XeLaTeX availability, configured timeout/capacity, storage write permission, and server logs using the request correlation ID. Never expose compiler diagnostics or temporary paths to clients. A failed preview must not create an active CV.

### `CV_PREVIEW_EXPIRED`

The Student must regenerate the preview. This is expected after source mutation, preview expiry, failed integrity verification, or invalidated ownership/configuration.

### `STALE_VERSION`

Reload current CV metadata, obtain the new ETag/revision, regenerate a preview if required, then retry with the current `If-Match` value. Never bypass the conditional request check.

### `CV_FILE_UNAVAILABLE`

Do not regenerate automatically. Preserve active metadata for investigation. Verify `system.file_asset` metadata against the private stored object (size and SHA-256). Student/Admin should receive the same stable 503 problem code.

### Superseded/orphan file cleanup

The scheduled CV cleanup only scans the `cv/objects/` namespace and never Academic Ledger storage. It must not delete a file asset still referenced by `cvs.pdf_file_asset_id`. Cleanup failures are retried after the grace period.

## Database verification

```sql
SELECT version, description, success
FROM flyway_schema_history
ORDER BY installed_rank DESC
LIMIT 10;

SELECT student_id, COUNT(*)
FROM cvs
GROUP BY student_id
HAVING COUNT(*) > 1;

SELECT c.id
FROM cvs c
LEFT JOIN system.file_asset f ON f.file_asset_id = c.pdf_file_asset_id
WHERE c.pdf_file_asset_id IS NOT NULL
  AND f.file_asset_id IS NULL;
```

The duplicate and dangling-file queries must return zero rows.

## Release acceptance

Run:

```powershell
.\mvnw.cmd test
.\mvnw.cmd test "-Dcv.latex.integration=true"
.\postman\cv-generation\run-newman.ps1
```

Then run the frontend live acceptance described in the frontend repository. Record results in `postman/cv-generation/RELEASE_EVIDENCE.md`.
