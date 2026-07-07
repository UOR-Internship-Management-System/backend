CREATE TABLE eligible_students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    index_number VARCHAR(32) NOT NULL UNIQUE,
    university_email VARCHAR(254) NOT NULL UNIQUE,
    full_name VARCHAR(160) NOT NULL,
    academic_level SMALLINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    user_account_id UUID UNIQUE REFERENCES user_accounts(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_eligible_students_level CHECK (academic_level IN (3, 4)),
    CONSTRAINT chk_eligible_students_email_lower CHECK (university_email = LOWER(university_email))
);

CREATE TABLE verification_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    eligible_student_id UUID REFERENCES eligible_students(id) ON DELETE CASCADE,
    index_number VARCHAR(32) NOT NULL,
    university_email VARCHAR(254) NOT NULL,
    purpose VARCHAR(32) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    resend_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_verification_sessions_purpose CHECK (purpose IN ('SIGN_UP', 'PASSWORD_RESET')),
    CONSTRAINT chk_verification_sessions_attempt_count CHECK (attempt_count >= 0),
    CONSTRAINT chk_verification_sessions_resend_count CHECK (resend_count >= 0),
    CONSTRAINT chk_verification_sessions_email_lower CHECK (university_email = LOWER(university_email))
);
