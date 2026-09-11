-- BMD-007: harden the single-active CV resource and introduce durable preview lifecycle state.
-- V080-V082 are already applied in existing environments and must remain immutable.

INSERT INTO cv_source_freshness (student_id)
SELECT es.id
FROM eligible_students es
ON CONFLICT (student_id) DO NOTHING;

CREATE TABLE cv_previews (
    preview_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES eligible_students(id) ON DELETE CASCADE,
    source_fingerprint CHAR(64) NOT NULL,
    staged_storage_key TEXT UNIQUE,
    staged_file_name VARCHAR(255),
    staged_file_size_bytes BIGINT,
    staged_checksum_sha256 CHAR(64),
    generated_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    result_cv_id UUID REFERENCES cvs(id) ON DELETE SET NULL,
    result_revision INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_cv_preview_source_fingerprint CHECK (source_fingerprint ~ '^[0-9a-f]{64}$'),
    CONSTRAINT chk_cv_preview_expiry CHECK (expires_at > generated_at),
    CONSTRAINT chk_cv_preview_staged_file_name CHECK (
        staged_file_name IS NULL OR staged_file_name ~ '^[A-Za-z0-9._-]+[.]pdf$'
    ),
    CONSTRAINT chk_cv_preview_staged_file_size CHECK (
        staged_file_size_bytes IS NULL OR staged_file_size_bytes > 0
    ),
    CONSTRAINT chk_cv_preview_staged_checksum CHECK (
        staged_checksum_sha256 IS NULL OR staged_checksum_sha256 ~ '^[0-9a-f]{64}$'
    ),
    CONSTRAINT chk_cv_preview_staged_metadata_consistent CHECK (
        (staged_storage_key IS NULL AND staged_file_name IS NULL AND staged_file_size_bytes IS NULL AND staged_checksum_sha256 IS NULL)
        OR
        (staged_storage_key IS NOT NULL AND staged_file_name IS NOT NULL AND staged_file_size_bytes IS NOT NULL AND staged_checksum_sha256 IS NOT NULL)
    ),
    CONSTRAINT chk_cv_preview_result_consistent CHECK (
        (consumed_at IS NULL AND result_cv_id IS NULL AND result_revision IS NULL)
        OR
        (consumed_at IS NOT NULL AND result_cv_id IS NOT NULL AND result_revision IS NOT NULL AND result_revision >= 1)
    )
);

CREATE UNIQUE INDEX uq_cv_previews_id_student
    ON cv_previews (preview_id, student_id);
CREATE INDEX idx_cv_previews_student_generated
    ON cv_previews (student_id, generated_at DESC);
CREATE INDEX idx_cv_previews_expiry_unconsumed
    ON cv_previews (expires_at)
    WHERE consumed_at IS NULL;

ALTER TABLE cvs
    ADD COLUMN source_fingerprint CHAR(64),
    ADD COLUMN pdf_file_asset_id UUID REFERENCES system.file_asset(file_asset_id) ON DELETE RESTRICT,
    ADD COLUMN last_saved_preview_id UUID REFERENCES cv_previews(preview_id) ON DELETE SET NULL,
    ADD COLUMN updated_at TIMESTAMPTZ;

UPDATE cvs
SET updated_at = COALESCE(saved_at, created_at)
WHERE updated_at IS NULL;

ALTER TABLE cvs
    ADD CONSTRAINT chk_cvs_revision_positive CHECK (revision >= 1),
    ADD CONSTRAINT chk_cvs_source_fingerprint CHECK (
        source_fingerprint IS NULL OR source_fingerprint ~ '^[0-9a-f]{64}$'
    ),
    ADD CONSTRAINT chk_cvs_pdf_file_name_safe CHECK (
        pdf_file_name IS NULL OR pdf_file_name ~ '^[A-Za-z0-9._-]+[.]pdf$'
    ),
    ADD CONSTRAINT chk_cvs_pdf_file_size_nonnegative CHECK (
        pdf_file_size_bytes IS NULL OR pdf_file_size_bytes >= 0
    );

CREATE UNIQUE INDEX uq_cvs_id_student
    ON cvs (id, student_id);

CREATE UNIQUE INDEX uq_cvs_pdf_file_asset_id
    ON cvs (pdf_file_asset_id)
    WHERE pdf_file_asset_id IS NOT NULL;

COMMENT ON COLUMN cvs.included_experience_ids IS 'Legacy V082 CSV snapshot. New writes use cv_selected_experiences.';
COMMENT ON COLUMN cvs.included_project_ids IS 'Legacy V082 CSV snapshot. New writes use cv_selected_projects.';
COMMENT ON COLUMN cvs.included_certificate_ids IS 'Legacy V082 CSV snapshot. New writes use cv_selected_certificates.';
COMMENT ON COLUMN cvs.included_award_ids IS 'Legacy V082 CSV snapshot. New writes use cv_selected_awards.';
COMMENT ON COLUMN cvs.included_activity_ids IS 'Legacy V082 CSV snapshot. New writes use cv_selected_activities.';
