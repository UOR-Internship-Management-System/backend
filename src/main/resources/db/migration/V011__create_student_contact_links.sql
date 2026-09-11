CREATE TABLE student_contact_links (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       student_id UUID NOT NULL REFERENCES eligible_students(id) ON DELETE CASCADE,
                                       label VARCHAR(60) NOT NULL,
                                       url TEXT NOT NULL,
                                       display_order INTEGER NOT NULL DEFAULT 0,
                                       cv_include BOOLEAN NOT NULL DEFAULT TRUE,
                                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_student_contact_links_student ON student_contact_links(student_id);
