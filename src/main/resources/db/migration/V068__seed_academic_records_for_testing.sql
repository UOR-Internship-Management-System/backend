-- Temporary manual test data, standing in for the real Admin ledger upload/commit pipeline (not built yet).
INSERT INTO subjects (id, course_code, course_title, credits) VALUES
                                                                  ('d0000000-0000-0000-0000-000000000001', 'CS4010', 'Distributed Systems', 3),
                                                                  ('d0000000-0000-0000-0000-000000000002', 'CS4020', 'Cloud Computing', 3),
                                                                  ('d0000000-0000-0000-0000-000000000003', 'CS4030', 'Machine Learning', 3);

INSERT INTO academic_ledger_uploads (id, committed_at) VALUES
    ('e0000000-0000-0000-0000-000000000001', NOW());

INSERT INTO academic_records
(student_id, subject_id, letter_grade, grade_point, semester, academic_year, attempt_number, result_status, source_upload_id)
SELECT
    s.id, 'd0000000-0000-0000-0000-000000000001', 'A', 4.00, 'Semester 1', '2023/2024', 1, 'PASS', 'e0000000-0000-0000-0000-000000000001'
FROM eligible_students s WHERE s.index_number = 'SC/2020/00001'
UNION ALL
SELECT
    s.id, 'd0000000-0000-0000-0000-000000000002', 'A-', 3.70, 'Semester 1', '2023/2024', 1, 'PASS', 'e0000000-0000-0000-0000-000000000001'
FROM eligible_students s WHERE s.index_number = 'SC/2020/00001'
UNION ALL
SELECT
    s.id, 'd0000000-0000-0000-0000-000000000003', 'B+', 3.30, 'Semester 1', '2023/2024', 1, 'PASS', 'e0000000-0000-0000-0000-000000000001'
FROM eligible_students s WHERE s.index_number = 'SC/2020/00001';
