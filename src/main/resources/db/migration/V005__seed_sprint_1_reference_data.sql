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
    ('SC-2020-001', 'sc2020001@dcs.ruh.ac.lk', 'Nimal Perera', 3),
    ('SC-2020-002', 'sc2020002@dcs.ruh.ac.lk', 'Ayesha Fernando', 3),
    ('SC-2019-001', 'sc2019001@dcs.ruh.ac.lk', 'Kasun Silva', 4),
    ('SC-2019-002', 'sc2019002@dcs.ruh.ac.lk', 'Hiruni Jayasinghe', 4)
ON CONFLICT (index_number) DO NOTHING;
