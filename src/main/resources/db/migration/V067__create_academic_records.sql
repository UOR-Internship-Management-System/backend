CREATE TABLE academic_records (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  student_id UUID NOT NULL REFERENCES eligible_students(id) ON DELETE CASCADE,
                                  subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE RESTRICT,
                                  letter_grade VARCHAR(5) NOT NULL,
                                  grade_point NUMERIC(3,2) NOT NULL CHECK (grade_point >= 0 AND grade_point <= 4),
                                  semester VARCHAR(80) NOT NULL,
                                  academic_year VARCHAR(30) NOT NULL,
                                  attempt_number INTEGER NOT NULL DEFAULT 1,
                                  result_status VARCHAR(30) NOT NULL,
                                  source_upload_id UUID NOT NULL REFERENCES academic_ledger_uploads(id) ON DELETE RESTRICT,
                                  committed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_academic_records_student ON academic_records(student_id);
