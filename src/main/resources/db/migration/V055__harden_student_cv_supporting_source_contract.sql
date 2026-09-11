-- Align persisted Student CV-supporting data with the canonical OpenAPI v1.6.0/frontend contract.
-- This migration intentionally fails when legacy rows violate the contract. Run the V055 preflight
-- report first and remediate affected Student-owned data through an explicitly authorized process.
DO $$
DECLARE
    violation_count BIGINT;
BEGIN
    SELECT COUNT(*) INTO violation_count
    FROM student_certificates
    WHERE issuer IS NULL OR btrim(issuer) = '' OR issue_date IS NULL;
    IF violation_count > 0 THEN
        RAISE EXCEPTION 'V055 preflight failed: % student_certificates row(s) violate required issuer/issue_date values.', violation_count
            USING HINT = 'Run scripts/db/preflight-v055-student-cv-supporting-source-contract.sql and remediate the reported rows.';
    END IF;

    SELECT COUNT(*) INTO violation_count
    FROM student_awards
    WHERE issuer IS NULL OR btrim(issuer) = '' OR award_date IS NULL;
    IF violation_count > 0 THEN
        RAISE EXCEPTION 'V055 preflight failed: % student_awards row(s) violate required issuer/award_date values.', violation_count
            USING HINT = 'Run scripts/db/preflight-v055-student-cv-supporting-source-contract.sql and remediate the reported rows.';
    END IF;

    SELECT COUNT(*) INTO violation_count
    FROM student_activities
    WHERE role_title IS NULL
       OR btrim(role_title) = ''
       OR (start_date IS NOT NULL AND end_date IS NOT NULL AND end_date < start_date);
    IF violation_count > 0 THEN
        RAISE EXCEPTION 'V055 preflight failed: % student_activities row(s) violate role/date constraints.', violation_count
            USING HINT = 'Run scripts/db/preflight-v055-student-cv-supporting-source-contract.sql and remediate the reported rows.';
    END IF;

    SELECT COUNT(*) INTO violation_count
    FROM student_work_experience
    WHERE position_title IS NULL
       OR btrim(position_title) = ''
       OR start_date IS NULL
       OR (end_date IS NOT NULL AND end_date < start_date)
       OR (is_current_role = TRUE AND end_date IS NOT NULL);
    IF violation_count > 0 THEN
        RAISE EXCEPTION 'V055 preflight failed: % student_work_experience row(s) violate position/date/current-role constraints.', violation_count
            USING HINT = 'Run scripts/db/preflight-v055-student-cv-supporting-source-contract.sql and remediate the reported rows.';
    END IF;
END $$;

ALTER TABLE student_certificates
    ALTER COLUMN issuer SET NOT NULL,
    ALTER COLUMN issue_date SET NOT NULL,
    ADD CONSTRAINT chk_student_certificates_issuer_not_blank
        CHECK (btrim(issuer) <> '');

ALTER TABLE student_awards
    ALTER COLUMN issuer SET NOT NULL,
    ALTER COLUMN award_date SET NOT NULL,
    ADD CONSTRAINT chk_student_awards_issuer_not_blank
        CHECK (btrim(issuer) <> '');

ALTER TABLE student_activities
    ALTER COLUMN role_title SET NOT NULL,
    ADD CONSTRAINT chk_student_activities_role_title_not_blank
        CHECK (btrim(role_title) <> ''),
    ADD CONSTRAINT chk_student_activities_date_range
        CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date);

ALTER TABLE student_work_experience
    ALTER COLUMN position_title SET NOT NULL,
    ALTER COLUMN start_date SET NOT NULL,
    ADD CONSTRAINT chk_student_work_experience_position_title_not_blank
        CHECK (btrim(position_title) <> ''),
    ADD CONSTRAINT chk_student_work_experience_date_range
        CHECK (end_date IS NULL OR end_date >= start_date),
    ADD CONSTRAINT chk_student_work_experience_current_role_end_date
        CHECK (NOT is_current_role OR end_date IS NULL);
