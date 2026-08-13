CREATE TABLE system.file_asset (
    file_asset_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_account_id UUID REFERENCES public.user_accounts(id) ON DELETE SET NULL,
    file_name VARCHAR(255) NOT NULL,
    storage_key TEXT NOT NULL UNIQUE,
    mime_type VARCHAR(120) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    checksum_sha256 CHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_file_asset_file_name_nonblank CHECK (btrim(file_name) <> ''),
    CONSTRAINT chk_file_asset_storage_key_nonblank CHECK (btrim(storage_key) <> ''),
    CONSTRAINT chk_file_asset_mime_type_nonblank CHECK (btrim(mime_type) <> ''),
    CONSTRAINT chk_file_asset_size_positive CHECK (file_size_bytes > 0),
    CONSTRAINT chk_file_asset_sha256 CHECK (checksum_sha256 ~ '^[0-9a-f]{64}$')
);

CREATE TABLE ref.grade_scale (
    grade_code VARCHAR(5) PRIMARY KEY,
    grade_point NUMERIC(3,2) NOT NULL,
    is_passing BOOLEAN NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    minimum_mark SMALLINT,
    maximum_mark SMALLINT,
    CONSTRAINT chk_grade_scale_code_nonblank CHECK (btrim(grade_code) <> ''),
    CONSTRAINT chk_grade_scale_point CHECK (grade_point >= 0.00 AND grade_point <= 4.00),
    CONSTRAINT chk_grade_scale_minimum_mark CHECK (minimum_mark IS NULL OR minimum_mark BETWEEN 0 AND 100),
    CONSTRAINT chk_grade_scale_maximum_mark CHECK (maximum_mark IS NULL OR maximum_mark BETWEEN 0 AND 100),
    CONSTRAINT chk_grade_scale_mark_range CHECK (
        (minimum_mark IS NULL AND maximum_mark IS NULL)
        OR (minimum_mark IS NOT NULL AND maximum_mark IS NOT NULL AND minimum_mark <= maximum_mark)
    )
);

CREATE TABLE academic.subject (
    subject_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    catalog_version VARCHAR(50) NOT NULL,
    cohort_start_year SMALLINT NOT NULL,
    cohort_end_year SMALLINT,
    course_code VARCHAR(30) NOT NULL,
    course_title VARCHAR(250) NOT NULL,
    credits NUMERIC(4,1) NOT NULL,
    academic_level SMALLINT NOT NULL,
    semester VARCHAR(80) NOT NULL,
    course_type VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_subject_catalog_course UNIQUE (catalog_version, course_code),
    CONSTRAINT chk_subject_catalog_version_nonblank CHECK (btrim(catalog_version) <> ''),
    CONSTRAINT chk_subject_cohort_start_year CHECK (cohort_start_year BETWEEN 2000 AND 2200),
    CONSTRAINT chk_subject_cohort_end_year CHECK (cohort_end_year IS NULL OR cohort_end_year BETWEEN 2000 AND 2200),
    CONSTRAINT chk_subject_cohort_range CHECK (cohort_end_year IS NULL OR cohort_end_year >= cohort_start_year),
    CONSTRAINT chk_subject_course_code CHECK (course_code ~ '^CSC[0-9]{3}[0-9A-Za-zαβδ]$'),
    CONSTRAINT chk_subject_course_title_nonblank CHECK (btrim(course_title) <> ''),
    CONSTRAINT chk_subject_credits CHECK (credits > 0.0 AND credits <= 30.0),
    CONSTRAINT chk_subject_academic_level CHECK (academic_level BETWEEN 1 AND 4),
    CONSTRAINT chk_subject_semester_nonblank CHECK (btrim(semester) <> ''),
    CONSTRAINT chk_subject_course_type CHECK (course_type IN ('CORE', 'OPTIONAL'))
);
