CREATE TABLE academic.official_student_grade (
    official_student_grade_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES public.eligible_students(id) ON DELETE RESTRICT,
    subject_id UUID NOT NULL REFERENCES academic.subject(subject_id) ON DELETE RESTRICT,
    academic_ledger_upload_id UUID NOT NULL
        REFERENCES academic.academic_ledger_upload(academic_ledger_upload_id) ON DELETE RESTRICT,
    semester VARCHAR(80) NOT NULL,
    academic_year VARCHAR(9) NOT NULL,
    attempt_number SMALLINT NOT NULL,
    credits NUMERIC(4,1) NOT NULL,
    grade_point NUMERIC(3,2) NOT NULL,
    letter_grade VARCHAR(5) NOT NULL REFERENCES ref.grade_scale(grade_code) ON DELETE RESTRICT,
    result_status VARCHAR(30) NOT NULL,
    committed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_official_grade_attempt UNIQUE (
        student_id, subject_id, semester, academic_year, attempt_number
    ),
    CONSTRAINT chk_official_grade_semester_nonblank CHECK (btrim(semester) <> ''),
    CONSTRAINT chk_official_grade_academic_year CHECK (academic_year ~ '^[0-9]{4}/[0-9]{4}$'),
    CONSTRAINT chk_official_grade_attempt_number CHECK (attempt_number BETWEEN 1 AND 20),
    CONSTRAINT chk_official_grade_credits CHECK (credits > 0.0 AND credits <= 30.0),
    CONSTRAINT chk_official_grade_point CHECK (grade_point >= 0.00 AND grade_point <= 4.00),
    CONSTRAINT chk_official_grade_letter_nonblank CHECK (btrim(letter_grade) <> ''),
    CONSTRAINT chk_official_grade_result_status_nonblank CHECK (btrim(result_status) <> '')
);

CREATE TABLE academic.student_academic_summary (
    student_id UUID PRIMARY KEY REFERENCES public.eligible_students(id) ON DELETE CASCADE,
    computer_science_gpa NUMERIC(3,2) NOT NULL,
    total_credits NUMERIC(5,1) NOT NULL,
    calculated_at TIMESTAMPTZ NOT NULL,
    source_upload_id UUID NOT NULL
        REFERENCES academic.academic_ledger_upload(academic_ledger_upload_id) ON DELETE RESTRICT,
    CONSTRAINT chk_student_academic_summary_gpa CHECK (
        computer_science_gpa >= 0.00 AND computer_science_gpa <= 4.00
    ),
    CONSTRAINT chk_student_academic_summary_credits CHECK (total_credits >= 0.0)
);
