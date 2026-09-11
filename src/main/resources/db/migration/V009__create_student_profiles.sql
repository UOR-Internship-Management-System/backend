CREATE TABLE student_profiles (
                                  id UUID PRIMARY KEY,
                                  student_id UUID NOT NULL UNIQUE,

                                  personal_email VARCHAR(254),
                                  headline VARCHAR(200),
                                  summary TEXT,
                                  phone VARCHAR(30),
                                  location VARCHAR(150),

                                  profile_photo_file_id UUID,

                                  version BIGINT NOT NULL DEFAULT 0,

                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                  CONSTRAINT fk_student_profile_student
                                      FOREIGN KEY (student_id)
                                          REFERENCES eligible_students(id)
                                          ON DELETE CASCADE
);

CREATE INDEX idx_student_profiles_student
    ON student_profiles(student_id);
