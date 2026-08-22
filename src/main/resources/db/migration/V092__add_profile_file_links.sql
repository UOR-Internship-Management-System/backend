-- Links a certificate to its supporting evidence file.
-- ON DELETE SET NULL: removing the underlying asset must not delete the certificate itself.
ALTER TABLE student_certificates
    ADD COLUMN evidence_file_id UUID
        REFERENCES system.file_asset(file_asset_id) ON DELETE SET NULL;

CREATE INDEX idx_student_certificates_evidence_file
    ON student_certificates(evidence_file_id)
    WHERE evidence_file_id IS NOT NULL;

-- The profile photo column already exists (V009) but was never constrained.
-- Adding the reference now keeps orphaned asset rows from accumulating.
ALTER TABLE student_profiles
    ADD CONSTRAINT fk_student_profiles_photo_file
        FOREIGN KEY (profile_photo_file_id)
        REFERENCES system.file_asset(file_asset_id) ON DELETE SET NULL;

CREATE INDEX idx_student_profiles_photo_file
    ON student_profiles(profile_photo_file_id)
    WHERE profile_photo_file_id IS NOT NULL;
