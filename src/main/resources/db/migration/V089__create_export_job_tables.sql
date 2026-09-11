CREATE TABLE export_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shortlist_id UUID NOT NULL,
    export_type VARCHAR(40) NOT NULL,
    format VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    requested_by_account_id UUID NOT NULL,
    file_asset_id UUID,
    total_candidate_count INTEGER NOT NULL DEFAULT 0,
    included_file_count INTEGER NOT NULL DEFAULT 0,
    missing_cv_count INTEGER NOT NULL DEFAULT 0,
    failure_code VARCHAR(100),
    failure_message VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    CONSTRAINT fk_export_jobs_shortlist
        FOREIGN KEY (shortlist_id) REFERENCES shortlists(id) ON DELETE RESTRICT,
    CONSTRAINT fk_export_jobs_requester
        FOREIGN KEY (requested_by_account_id) REFERENCES user_accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_export_jobs_file_asset
        FOREIGN KEY (file_asset_id) REFERENCES system.file_asset(file_asset_id) ON DELETE RESTRICT,
    CONSTRAINT ck_export_jobs_type_format CHECK (
        (export_type = 'SHORTLIST_SUMMARY_CSV' AND format = 'CSV')
        OR (export_type = 'BULK_LATEST_CV_ZIP' AND format = 'ZIP')
    ),
    CONSTRAINT ck_export_jobs_status CHECK (
        status IN ('QUEUED', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')
    ),
    CONSTRAINT ck_export_jobs_counts CHECK (
        total_candidate_count >= 0
        AND included_file_count >= 0
        AND missing_cv_count >= 0
        AND included_file_count <= total_candidate_count
        AND missing_cv_count <= total_candidate_count
    ),
    CONSTRAINT ck_export_jobs_version CHECK (version >= 0),
    CONSTRAINT ck_export_jobs_lifecycle CHECK (
        (status = 'QUEUED' AND started_at IS NULL AND completed_at IS NULL AND file_asset_id IS NULL)
        OR (status = 'PROCESSING' AND started_at IS NOT NULL AND completed_at IS NULL AND file_asset_id IS NULL)
        OR (status = 'COMPLETED' AND started_at IS NOT NULL AND completed_at IS NOT NULL
            AND expires_at IS NOT NULL AND file_asset_id IS NOT NULL AND failure_code IS NULL)
        OR (status IN ('FAILED', 'CANCELLED') AND completed_at IS NOT NULL AND file_asset_id IS NULL)
    )
);

CREATE TABLE export_missing_cv_students (
    export_job_id UUID NOT NULL,
    student_id UUID NOT NULL,
    index_number VARCHAR(30) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    PRIMARY KEY (export_job_id, student_id),
    CONSTRAINT fk_export_missing_cv_job
        FOREIGN KEY (export_job_id) REFERENCES export_jobs(id) ON DELETE CASCADE,
    CONSTRAINT fk_export_missing_cv_student
        FOREIGN KEY (student_id) REFERENCES eligible_students(id) ON DELETE RESTRICT
);

CREATE TABLE export_warnings (
    export_job_id UUID NOT NULL,
    warning_code VARCHAR(40) NOT NULL,
    message VARCHAR(500) NOT NULL,
    PRIMARY KEY (export_job_id, warning_code),
    CONSTRAINT fk_export_warnings_job
        FOREIGN KEY (export_job_id) REFERENCES export_jobs(id) ON DELETE CASCADE,
    CONSTRAINT ck_export_warning_code CHECK (warning_code IN ('MISSING_CVS', 'PARTIAL_EXPORT'))
);
