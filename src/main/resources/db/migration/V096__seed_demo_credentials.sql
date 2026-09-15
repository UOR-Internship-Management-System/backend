-- ==============================================================================
-- Migration V096: Seed demo/test credentials for manual verification
--
-- Admin :   admin@dcs.ruh.ac.lk  /  TestAdmin@123
-- Student:  demo.student@dcs.ruh.ac.lk  /  TestStudent@123  (index SC/2024/99999)
--
-- These accounts are safe to include in non-production deployments.
-- Do NOT run this migration against a real production database that already
-- has real user data — use it only for staging / demo environments.
-- ==============================================================================

-- ── 1. Restore / create the admin account with a known password ─────────────
-- V005 seeded the admin; V008 wiped its password to force a setup flow.
-- This migration re-activates it with a known plaintext: TestAdmin@123
UPDATE user_accounts
SET password_hash      = '$2b$10$wMXDl8/00Dh./gk6tRytUe7OHCiDWsjIAbWeEqMhGVO5ACjTbtmg.',
    account_status     = 'ACTIVE',
    password_changed_at = NOW(),
    updated_at         = NOW()
WHERE university_email = 'admin@dcs.ruh.ac.lk'
  AND account_status   = 'PASSWORD_SETUP_REQUIRED';

-- ── 2. Seed a demo student user account ────────────────────────────────────
INSERT INTO user_accounts (id, university_email, password_hash, account_status, password_changed_at)
VALUES (
    'aaaaaaaa-0000-0000-0000-000000000001',
    'demo.student@dcs.ruh.ac.lk',
    '$2b$10$Uo7LCcTyloV5XA5fJtT/suEKPAxSi8lUXZevbgpxa4ZOoAMC.JSMC',
    'ACTIVE',
    NOW()
)
ON CONFLICT (university_email) DO NOTHING;

-- Grant ROLE_STUDENT
INSERT INTO user_roles (user_id, role_id)
SELECT 'aaaaaaaa-0000-0000-0000-000000000001', r.id
FROM roles r
WHERE r.name = 'ROLE_STUDENT'
ON CONFLICT DO NOTHING;

-- ── 3. Add demo student to eligible_students ────────────────────────────────
INSERT INTO eligible_students (id, index_number, university_email, full_name, academic_level, is_active, user_account_id)
VALUES (
    'bbbbbbbb-0000-0000-0000-000000000001',
    'SC/2024/99999',
    'demo.student@dcs.ruh.ac.lk',
    'Demo Student',
    3,
    TRUE,
    'aaaaaaaa-0000-0000-0000-000000000001'
)
ON CONFLICT (index_number) DO NOTHING;

-- ── 4. Create a basic student profile ──────────────────────────────────────
INSERT INTO student_profiles (id, student_id, display_name, headline, summary, phone, location, personal_email)
VALUES (
    'cccccccc-0000-0000-0000-000000000001',
    'bbbbbbbb-0000-0000-0000-000000000001',
    'Demo Student',
    'Computer Science Undergraduate',
    'Demo account for testing the CV Management System.',
    '+94 77 000 0000',
    'Matara, Sri Lanka',
    'demo@example.com'
)
ON CONFLICT (student_id) DO NOTHING;
