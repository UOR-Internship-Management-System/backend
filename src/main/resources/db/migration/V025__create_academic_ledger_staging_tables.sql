CREATE TABLE academic.academic_ledger_upload (
    academic_ledger_upload_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    uploaded_by_account_id UUID NOT NULL REFERENCES public.user_accounts(id) ON DELETE RESTRICT,
    source_file_asset_id UUID NOT NULL REFERENCES system.file_asset(file_asset_id) ON DELETE RESTRICT,
    file_name VARCHAR(255) NOT NULL,
    file_hash CHAR(64) NOT NULL,
    upload_status VARCHAR(30) NOT NULL,
    validation_status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
    total_rows INTEGER NOT NULL DEFAULT 0,
    valid_rows INTEGER NOT NULL DEFAULT 0,
    invalid_rows INTEGER NOT NULL DEFAULT 0,
    failure_summary VARCHAR(500),
    processing_started_at TIMESTAMPTZ,
    validation_completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    committed_at TIMESTAMPTZ,
    CONSTRAINT chk_ledger_upload_file_name_nonblank CHECK (btrim(file_name) <> ''),
    CONSTRAINT chk_ledger_upload_file_hash CHECK (file_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT chk_ledger_upload_status CHECK (
        upload_status IN (
            'RECEIVED', 'PROCESSING', 'STAGED', 'READY_TO_COMMIT', 'COMMITTING', 'COMMITTED',
            'VALIDATION_FAILED', 'PROCESSING_FAILED'
        )
    ),
    CONSTRAINT chk_ledger_validation_status CHECK (
        validation_status IN ('NOT_STARTED', 'IN_PROGRESS', 'PASSED', 'FAILED')
    ),
    CONSTRAINT chk_ledger_upload_total_rows CHECK (total_rows >= 0),
    CONSTRAINT chk_ledger_upload_valid_rows CHECK (valid_rows >= 0),
    CONSTRAINT chk_ledger_upload_invalid_rows CHECK (invalid_rows >= 0),
    CONSTRAINT chk_ledger_upload_row_counts CHECK (valid_rows + invalid_rows <= total_rows),
    CONSTRAINT chk_ledger_upload_failure_summary_nonblank CHECK (
        failure_summary IS NULL OR btrim(failure_summary) <> ''
    ),
    CONSTRAINT chk_ledger_upload_ready_requires_passed_validation CHECK (
        upload_status NOT IN ('READY_TO_COMMIT', 'COMMITTING', 'COMMITTED')
        OR validation_status = 'PASSED'
    ),
    CONSTRAINT chk_ledger_upload_validation_failure_state CHECK (
        upload_status <> 'VALIDATION_FAILED' OR validation_status = 'FAILED'
    ),
    CONSTRAINT chk_ledger_upload_committed_timestamp CHECK (
        (upload_status = 'COMMITTED' AND committed_at IS NOT NULL)
        OR (upload_status <> 'COMMITTED' AND committed_at IS NULL)
    )
);

CREATE TABLE academic.academic_ledger_staging_row (
    staging_row_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    academic_ledger_upload_id UUID NOT NULL
        REFERENCES academic.academic_ledger_upload(academic_ledger_upload_id) ON DELETE CASCADE,
    row_number INTEGER NOT NULL,
    raw_payload JSONB NOT NULL,
    student_index_number VARCHAR(40) NOT NULL,
    student_id UUID REFERENCES public.eligible_students(id) ON DELETE RESTRICT,
    course_code VARCHAR(30) NOT NULL,
    course_title VARCHAR(250),
    credits NUMERIC(4,1) NOT NULL,
    letter_grade VARCHAR(5) NOT NULL,
    grade_point NUMERIC(3,2),
    semester VARCHAR(80) NOT NULL,
    academic_year VARCHAR(9) NOT NULL,
    attempt_number SMALLINT NOT NULL,
    result_status VARCHAR(30) NOT NULL,
    validation_status VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_ledger_staging_upload_row UNIQUE (academic_ledger_upload_id, row_number),
    CONSTRAINT chk_ledger_staging_row_number CHECK (row_number >= 2),
    CONSTRAINT chk_ledger_staging_student_index_nonblank CHECK (btrim(student_index_number) <> ''),
    CONSTRAINT chk_ledger_staging_course_code_nonblank CHECK (btrim(course_code) <> ''),
    CONSTRAINT chk_ledger_staging_course_title_nonblank CHECK (course_title IS NULL OR btrim(course_title) <> ''),
    CONSTRAINT chk_ledger_staging_credits CHECK (credits > 0.0 AND credits <= 30.0),
    CONSTRAINT chk_ledger_staging_letter_grade_nonblank CHECK (btrim(letter_grade) <> ''),
    CONSTRAINT chk_ledger_staging_grade_point CHECK (grade_point IS NULL OR (grade_point >= 0.00 AND grade_point <= 4.00)),
    CONSTRAINT chk_ledger_staging_semester_nonblank CHECK (btrim(semester) <> ''),
    CONSTRAINT chk_ledger_staging_academic_year CHECK (academic_year ~ '^[0-9]{4}/[0-9]{4}$'),
    CONSTRAINT chk_ledger_staging_attempt_number CHECK (attempt_number BETWEEN 1 AND 20),
    CONSTRAINT chk_ledger_staging_result_status_nonblank CHECK (btrim(result_status) <> ''),
    CONSTRAINT chk_ledger_staging_validation_status CHECK (
        validation_status IS NULL OR validation_status IN ('VALID', 'WARNING', 'INVALID')
    )
);

CREATE TABLE academic.academic_ledger_validation_error (
    validation_error_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staging_row_id UUID NOT NULL
        REFERENCES academic.academic_ledger_staging_row(staging_row_id) ON DELETE CASCADE,
    field_name VARCHAR(80),
    error_code VARCHAR(64) NOT NULL,
    error_message VARCHAR(300) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    rejected_value VARCHAR(120),
    related_row_number INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_ledger_validation_field_name_nonblank CHECK (field_name IS NULL OR btrim(field_name) <> ''),
    CONSTRAINT chk_ledger_validation_error_code CHECK (error_code ~ '^[A-Z][A-Z0-9_]{2,63}$'),
    CONSTRAINT chk_ledger_validation_error_message_nonblank CHECK (btrim(error_message) <> ''),
    CONSTRAINT chk_ledger_validation_severity CHECK (severity IN ('ERROR', 'WARNING')),
    CONSTRAINT chk_ledger_validation_related_row CHECK (related_row_number IS NULL OR related_row_number >= 2)
);
