ALTER TABLE cvs
    ADD COLUMN included_experience_ids TEXT,
    ADD COLUMN included_project_ids TEXT,
    ADD COLUMN included_certificate_ids TEXT,
    ADD COLUMN included_award_ids TEXT,
    ADD COLUMN included_activity_ids TEXT,
    ADD COLUMN pdf_file_name VARCHAR(255),
    ADD COLUMN pdf_file_size_bytes BIGINT;
