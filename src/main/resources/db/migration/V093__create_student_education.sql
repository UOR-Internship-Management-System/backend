CREATE TABLE student_education (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES eligible_students(id) ON DELETE CASCADE,
    degree VARCHAR(200) NOT NULL,
    institution VARCHAR(200) NOT NULL,
    institution_url TEXT,
    location VARCHAR(150),
    start_date DATE,
    end_date DATE,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    result_note VARCHAR(500),
    cv_include BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_student_education_student ON student_education(student_id);
