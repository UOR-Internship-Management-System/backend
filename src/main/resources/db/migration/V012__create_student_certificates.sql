CREATE TABLE student_certificates (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      student_id UUID NOT NULL REFERENCES eligible_students(id) ON DELETE CASCADE,
                                      title VARCHAR(200) NOT NULL,
                                      issuer VARCHAR(200),
                                      issue_date DATE,
                                      credential_url TEXT,
                                      cv_include BOOLEAN NOT NULL DEFAULT TRUE,
                                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_student_certificates_student ON student_certificates(student_id);
