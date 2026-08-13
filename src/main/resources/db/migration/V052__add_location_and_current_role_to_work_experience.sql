ALTER TABLE student_work_experience
    ADD COLUMN location VARCHAR(150),
    ADD COLUMN current_role BOOLEAN NOT NULL DEFAULT FALSE;
