-- BMD-007 read-only PostgreSQL release verification. Safe to run from pgAdmin.

-- 1. Batch 3 adds no migration. The current BMD-007 maximum remains V084.
SELECT version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank DESC
LIMIT 10;

-- 2. One active/current CV row maximum per Student.
SELECT student_id, COUNT(*) AS cv_rows
FROM cvs
GROUP BY student_id
HAVING COUNT(*) > 1;

-- 3. No active CV may reference a missing system.file_asset row.
SELECT c.id AS cv_id, c.student_id, c.pdf_file_asset_id
FROM cvs c
LEFT JOIN system.file_asset f ON f.file_asset_id = c.pdf_file_asset_id
WHERE c.pdf_file_asset_id IS NOT NULL
  AND f.file_asset_id IS NULL;

-- 4. Every real active CV should resolve to non-zero PDF metadata.
SELECT
    c.id AS cv_id,
    c.student_id,
    c.revision,
    c.source_fingerprint,
    c.saved_at,
    f.file_name,
    f.mime_type,
    f.file_size_bytes,
    f.checksum_sha256
FROM cvs c
JOIN system.file_asset f ON f.file_asset_id = c.pdf_file_asset_id
ORDER BY c.saved_at DESC NULLS LAST;

-- 5. Durable preview rows and consumed-result state.
SELECT
    preview_id,
    student_id,
    generated_at,
    expires_at,
    consumed_at,
    result_cv_id,
    result_revision,
    staged_file_size_bytes
FROM cv_previews
ORDER BY created_at DESC
LIMIT 50;

-- 6. No duplicate selection snapshot should exist by primary-key design.
SELECT cv_id, source_record_id, COUNT(*)
FROM cv_selected_projects
GROUP BY cv_id, source_record_id
HAVING COUNT(*) > 1;
