ALTER TABLE student_work_experience
    ADD COLUMN location VARCHAR(150),
    ADD COLUMN is_current_role BOOLEAN NOT NULL DEFAULT FALSE;
