INSERT INTO roles (name, description)
VALUES
    ('ROLE_STUDENT', 'Student portal user'),
    ('ROLE_ADMIN', 'Department administrator')
ON CONFLICT (name) DO NOTHING;

INSERT INTO user_accounts (id, university_email, password_hash, account_status)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin@dcs.ruh.ac.lk',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy',
    'ACTIVE'
)
ON CONFLICT (university_email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT '00000000-0000-0000-0000-000000000001', r.id
FROM roles r
WHERE r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO eligible_students (index_number, university_email, full_name, academic_level)
VALUES
    ('SC/2021/12430', 'nimalp@usci.ruh.ac.lk', 'Nimal Perera', 4),
    ('SC/2022/13120', 'ayeshaf@usci.ruh.ac.lk', 'Ayesha Fernando', 3),
    ('SC/2022/12933', 'kasuns@usci.ruh.ac.lk', 'Kasun Silva', 3),
    ('SC/2022/12888', 'basnayake12888@usci.ruh.ac.lk', 'Sahan Basnayake', 3),
    ('SC/2021/12500', 'hirunij@usci.ruh.ac.lk', 'Hiruni Jayasinghe', 4)
ON CONFLICT (index_number) DO NOTHING;
