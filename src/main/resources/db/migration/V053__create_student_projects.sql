CREATE TABLE student_projects (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  student_id UUID NOT NULL REFERENCES eligible_students(id) ON DELETE CASCADE,
                                  title VARCHAR(200) NOT NULL,
                                  description TEXT,
                                  repository_url TEXT,
                                  demo_url TEXT,
                                  start_date DATE,
                                  end_date DATE,
                                  include_in_cv BOOLEAN NOT NULL DEFAULT TRUE,
                                  version BIGINT NOT NULL DEFAULT 0,
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_student_projects_student ON student_projects(student_id);
