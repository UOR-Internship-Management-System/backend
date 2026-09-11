CREATE TABLE student_work_experience (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         student_id UUID NOT NULL REFERENCES eligible_students(id) ON DELETE CASCADE,
                                         organization VARCHAR(200) NOT NULL,
                                         position_title VARCHAR(150),
                                         start_date DATE,
                                         end_date DATE,
                                         description TEXT,
                                         cv_include BOOLEAN NOT NULL DEFAULT TRUE,
                                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                         updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_student_work_experience_student ON student_work_experience(student_id);
