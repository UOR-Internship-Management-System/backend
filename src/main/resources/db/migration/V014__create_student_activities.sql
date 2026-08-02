CREATE TABLE student_activities (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    student_id UUID NOT NULL REFERENCES eligible_students(id) ON DELETE CASCADE,
                                    activity_name VARCHAR(200) NOT NULL,
                                    role_title VARCHAR(150),
                                    start_date DATE,
                                    end_date DATE,
                                    description TEXT,
                                    cv_include BOOLEAN NOT NULL DEFAULT TRUE,
                                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_student_activities_student ON student_activities(student_id);
