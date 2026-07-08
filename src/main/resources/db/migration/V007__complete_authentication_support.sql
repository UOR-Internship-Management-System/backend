ALTER TABLE verification_sessions
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS account_type VARCHAR(32),
    ADD COLUMN IF NOT EXISTS user_account_id UUID REFERENCES user_accounts(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS verified_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_resend_at TIMESTAMPTZ;

ALTER TABLE user_accounts
    ADD COLUMN IF NOT EXISTS password_changed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMPTZ;

ALTER TABLE verification_sessions
    DROP CONSTRAINT IF EXISTS chk_verification_sessions_status;

ALTER TABLE verification_sessions
    ADD CONSTRAINT chk_verification_sessions_status CHECK (
        status IN ('PENDING', 'VERIFIED', 'EXPIRED', 'BLOCKED', 'CONSUMED')
    );

ALTER TABLE verification_sessions
    DROP CONSTRAINT IF EXISTS chk_verification_sessions_account_type;

ALTER TABLE verification_sessions
    ADD CONSTRAINT chk_verification_sessions_account_type CHECK (
        account_type IS NULL OR account_type IN ('STUDENT', 'ADMIN')
    );

CREATE TABLE admin_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_account_id UUID NOT NULL UNIQUE REFERENCES user_accounts(id) ON DELETE CASCADE,
    display_name VARCHAR(160) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO admin_users (id, user_account_id, display_name, is_active)
VALUES (
    '00000000-0000-0000-0000-000000000101',
    '00000000-0000-0000-0000-000000000001',
    'Department Administrator',
    TRUE
)
ON CONFLICT (user_account_id) DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_verification_sessions_status
    ON verification_sessions(status);

CREATE INDEX IF NOT EXISTS idx_verification_sessions_user_account
    ON verification_sessions(user_account_id);

CREATE INDEX IF NOT EXISTS idx_verification_sessions_reset_lookup
    ON verification_sessions(purpose, account_type, university_email);
