CREATE UNIQUE INDEX uq_ledger_upload_active_file_hash
    ON academic.academic_ledger_upload (file_hash)
    WHERE upload_status IN (
        'RECEIVED', 'PROCESSING', 'STAGED', 'READY_TO_COMMIT', 'COMMITTING', 'COMMITTED'
    );

CREATE INDEX idx_ledger_upload_created_at
    ON academic.academic_ledger_upload (created_at DESC);

CREATE INDEX idx_ledger_upload_status_created_at
    ON academic.academic_ledger_upload (upload_status, created_at DESC);

CREATE INDEX idx_ledger_upload_validation_created_at
    ON academic.academic_ledger_upload (validation_status, created_at DESC);

CREATE INDEX idx_ledger_staging_upload_validation
    ON academic.academic_ledger_staging_row (academic_ledger_upload_id, validation_status, row_number);

CREATE INDEX idx_ledger_staging_student_index
    ON academic.academic_ledger_staging_row (student_index_number);

CREATE INDEX idx_ledger_staging_course_code
    ON academic.academic_ledger_staging_row (course_code);

CREATE INDEX idx_ledger_validation_error_staging
    ON academic.academic_ledger_validation_error (staging_row_id, severity);

CREATE INDEX idx_subject_course_code_active
    ON academic.subject (course_code, is_active);

CREATE INDEX idx_subject_cohort_lookup
    ON academic.subject (course_code, cohort_start_year, cohort_end_year, is_active);

CREATE INDEX idx_official_grade_student_period
    ON academic.official_student_grade (student_id, academic_year DESC, semester, subject_id);

CREATE INDEX idx_official_grade_subject_student
    ON academic.official_student_grade (subject_id, student_id, academic_year DESC);

CREATE INDEX idx_official_grade_upload
    ON academic.official_student_grade (academic_ledger_upload_id);
