CREATE TABLE student_declared_skills (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         student_id UUID NOT NULL REFERENCES eligible_students(id) ON DELETE CASCADE,
                                         skill_id UUID NOT NULL REFERENCES skills(id) ON DELETE RESTRICT,
                                         competency_level VARCHAR(20) NOT NULL CHECK (competency_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
                                         version BIGINT NOT NULL DEFAULT 0,
                                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                         updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                         UNIQUE (student_id, skill_id)
);

CREATE INDEX idx_student_declared_skills_student ON student_declared_skills(student_id);
