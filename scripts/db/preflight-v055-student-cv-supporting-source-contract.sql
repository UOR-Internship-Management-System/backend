-- Run this read-only report before applying V055 to an existing V054 database.
-- Any returned row must be remediated explicitly; do not invent Student-owned values.
SELECT 'student_certificates.issuer_required' AS issue, id AS record_id
FROM student_certificates
WHERE issuer IS NULL OR btrim(issuer) = ''
UNION ALL
SELECT 'student_certificates.issue_date_required', id
FROM student_certificates
WHERE issue_date IS NULL
UNION ALL
SELECT 'student_awards.issuer_required', id
FROM student_awards
WHERE issuer IS NULL OR btrim(issuer) = ''
UNION ALL
SELECT 'student_awards.award_date_required', id
FROM student_awards
WHERE award_date IS NULL
UNION ALL
SELECT 'student_activities.role_title_required', id
FROM student_activities
WHERE role_title IS NULL OR btrim(role_title) = ''
UNION ALL
SELECT 'student_activities.invalid_date_range', id
FROM student_activities
WHERE start_date IS NOT NULL AND end_date IS NOT NULL AND end_date < start_date
UNION ALL
SELECT 'student_work_experience.position_title_required', id
FROM student_work_experience
WHERE position_title IS NULL OR btrim(position_title) = ''
UNION ALL
SELECT 'student_work_experience.start_date_required', id
FROM student_work_experience
WHERE start_date IS NULL
UNION ALL
SELECT 'student_work_experience.invalid_date_range', id
FROM student_work_experience
WHERE start_date IS NOT NULL AND end_date IS NOT NULL AND end_date < start_date
UNION ALL
SELECT 'student_work_experience.current_role_has_end_date', id
FROM student_work_experience
WHERE is_current_role = TRUE AND end_date IS NOT NULL
ORDER BY issue, record_id;
