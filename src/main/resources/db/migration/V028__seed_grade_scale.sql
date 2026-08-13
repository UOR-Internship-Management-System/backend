INSERT INTO ref.grade_scale (
    grade_code,
    grade_point,
    is_passing,
    is_active,
    minimum_mark,
    maximum_mark
) VALUES
    ('A+', 4.00, TRUE,  TRUE, 85, 100),
    ('A',  4.00, TRUE,  TRUE, 70, 84),
    ('A-', 3.70, TRUE,  TRUE, 65, 69),
    ('B+', 3.30, TRUE,  TRUE, 60, 64),
    ('B',  3.00, TRUE,  TRUE, 55, 59),
    ('B-', 2.70, TRUE,  TRUE, 50, 54),
    ('C+', 2.30, TRUE,  TRUE, 45, 49),
    ('C',  2.00, TRUE,  TRUE, 40, 44),
    ('C-', 1.70, FALSE, TRUE, 35, 39),
    ('D+', 1.30, FALSE, TRUE, 30, 34),
    ('D',  1.00, FALSE, TRUE, 25, 29),
    ('E',  0.00, FALSE, TRUE, 0, 24);
