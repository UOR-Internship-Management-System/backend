CREATE TABLE student_awards (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                student_id UUID NOT NULL REFERENCES eligible_students(id) ON DELETE CASCADE,
                                title VARCHAR(200) NOT NULL,
                                issuer VARCHAR(200),
                                award_date DATE,
                                description TEXT,
                                cv_include BOOLEAN NOT NULL DEFAULT TRUE,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_student_awards_student ON student_awards(student_id);
