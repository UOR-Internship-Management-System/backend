-- Unified authoritative CSC catalogue approved for cohorts from 2019 onward.
-- Overlapping definitions use the SC/2025 handbook wording. Older-only and
-- newer-only CSC subjects are retained in the same non-overlapping catalogue.

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM academic.subject) THEN
        RAISE EXCEPTION
            'V060 preflight failed: academic.subject rows already exist; reconcile them before applying the authoritative catalogue';
    END IF;

    IF EXISTS (SELECT 1 FROM ref.grade_scale WHERE upper(grade_code) = 'E*') THEN
        RAISE EXCEPTION
            'V060 preflight failed: ref.grade_scale already contains E*; reconcile it before applying the authoritative grade rule';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM academic.official_student_grade
        WHERE NOT (
            (result_status = 'PASSED' AND letter_grade IN ('A+', 'A', 'A-', 'B+', 'B', 'B-', 'C+', 'C'))
            OR (result_status = 'FAILED' AND letter_grade IN ('C-', 'D+', 'D', 'E'))
            OR (result_status = 'ABSENT' AND letter_grade = 'E*')
        )
    ) THEN
        RAISE EXCEPTION
            'V060 preflight failed: official grades contain an unapproved result status or grade/status mapping';
    END IF;
END
$$;

INSERT INTO ref.grade_scale (
    grade_code,
    grade_point,
    is_passing,
    is_active,
    minimum_mark,
    maximum_mark
) VALUES ('E*', 0.00, FALSE, TRUE, NULL, NULL);

INSERT INTO academic.subject (
    catalog_version,
    cohort_start_year,
    cohort_end_year,
    course_code,
    course_title,
    credits,
    academic_level,
    semester,
    course_type,
    is_active
) VALUES
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC1122', 'Computer Systems I', 2.0, 1, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC1113', 'Programming Techniques', 3.0, 1, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC113α', 'Internet Services and Web Development', 1.5, 1, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC1142', 'System Analysis and Design', 2.0, 1, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC1153', 'Laboratory Assignments', 3.0, 1, 'Semester 1', 'CORE', TRUE),
    -- The handbook credit table prints 2.0, but the approved project curriculum decision confirms 3.0.
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC1213', 'Database Management Systems', 3.0, 1, 'Semester 2', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC1223', 'Data Structures and Algorithms', 3.0, 1, 'Semester 2', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC1233', 'Software Engineering', 3.0, 1, 'Semester 2', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC1242', 'Object Oriented System Development', 2.0, 1, 'Semester 2', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC1251', 'Computer Laboratory', 1.0, 1, 'Semester 2', 'CORE', TRUE),

    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC2113', 'Data Communication and Computer Networks', 3.0, 2, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC2123', 'Object Oriented Programming', 3.0, 2, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC2133', 'Operating Systems', 3.0, 2, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC2143', 'Computer Graphics and Image Processing', 3.0, 2, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC2213', 'Rapid Application Development', 3.0, 2, 'Semester 2', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC2222', 'Computer System II', 2.0, 2, 'Semester 2', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC2233', 'Internet Programming', 3.0, 2, 'Semester 2', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC2242', 'Advanced Database Management', 2.0, 2, 'Semester 2', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC2252', 'Project Management', 2.0, 2, 'Semester 2', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC2263', 'Multimedia and Video Production', 3.0, 2, 'Semester 2', 'OPTIONAL', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC2272', 'Data and Network Security', 2.0, 2, 'Semester 2', 'OPTIONAL', TRUE),

    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC3113', 'Group Project', 3.0, 3, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC3122', 'e-Commerce and Professional Practice', 2.0, 3, 'Semester 1', 'OPTIONAL', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC3132', 'Data Warehousing and Data Mining', 2.0, 3, 'Semester 1', 'OPTIONAL', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC3133', 'Network and System Administration', 3.0, 3, 'Semester 1', 'OPTIONAL', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC3142', 'Internet Services and Protocols', 2.0, 3, 'Semester 1', 'OPTIONAL', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC3152', 'Geographic Information Systems', 2.0, 3, 'Semester 1', 'OPTIONAL', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC3162', 'Research Methods', 2.0, 3, 'Semester 1', 'OPTIONAL', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC3172', 'Distributed Systems', 2.0, 3, 'Semester 1', 'OPTIONAL', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC3216', 'Industrial Training', 6.0, 3, 'Semester 2', 'CORE', TRUE),

    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC4112', 'Research Seminar', 2.0, 4, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC4122', 'Research Methodology', 2.0, 4, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC4133', 'Neural Networks', 3.0, 4, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC4152', 'Enterprise Modelling', 2.0, 4, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC4162', 'Data Mining for Business Intelligence', 2.0, 4, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC4172', 'High-Performance Computing', 2.0, 4, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC4182', 'Bioinformatics', 2.0, 4, 'Semester 1', 'OPTIONAL', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC4046', 'Individual Research Project', 6.0, 4, 'Semester 1', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC4212', 'Compilers and Theory of Computation', 2.0, 4, 'Semester 2', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC4222', 'Service-Oriented Computing', 2.0, 4, 'Semester 2', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC4232', 'Formal Methods and Software Verification', 2.0, 4, 'Semester 2', 'OPTIONAL', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC4242', 'Artificial Intelligence', 2.0, 4, 'Semester 2', 'CORE', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC4262', 'Selected Topics', 2.0, 4, 'Semester 2', 'OPTIONAL', TRUE),
    ('CSC-UNIFIED-V1', 2019, NULL, 'CSC4282', 'Reconfigurable Computing', 2.0, 4, 'Semester 2', 'OPTIONAL', TRUE);

ALTER TABLE academic.official_student_grade
    ADD CONSTRAINT chk_official_grade_result_status
    CHECK (result_status IN ('PASSED', 'FAILED', 'ABSENT'));

ALTER TABLE academic.official_student_grade
    ADD CONSTRAINT chk_official_grade_result_status_matches_grade
    CHECK (
        (result_status = 'PASSED' AND letter_grade IN ('A+', 'A', 'A-', 'B+', 'B', 'B-', 'C+', 'C'))
        OR (result_status = 'FAILED' AND letter_grade IN ('C-', 'D+', 'D', 'E'))
        OR (result_status = 'ABSENT' AND letter_grade = 'E*')
    );
