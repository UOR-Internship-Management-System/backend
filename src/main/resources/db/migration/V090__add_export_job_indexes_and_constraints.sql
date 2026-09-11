CREATE INDEX idx_export_jobs_status_created
    ON export_jobs (status, created_at, id);

CREATE INDEX idx_export_jobs_shortlist_created
    ON export_jobs (shortlist_id, created_at DESC, id);

CREATE INDEX idx_export_jobs_expires
    ON export_jobs (expires_at, id)
    WHERE status = 'COMPLETED';

CREATE INDEX idx_export_missing_cv_job
    ON export_missing_cv_students (export_job_id, index_number, student_id);

CREATE UNIQUE INDEX uq_export_jobs_active_type_per_shortlist
    ON export_jobs (shortlist_id, export_type)
    WHERE status IN ('QUEUED', 'PROCESSING');
