CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_roles_name CHECK (name IN ('ROLE_STUDENT', 'ROLE_ADMIN'))
);

CREATE TABLE user_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    university_email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    account_status VARCHAR(32) NOT NULL DEFAULT 'PASSWORD_SETUP_REQUIRED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_user_accounts_status CHECK (
        account_status IN ('PASSWORD_SETUP_REQUIRED', 'ACTIVE', 'LOCKED', 'DISABLED')
    ),
    CONSTRAINT chk_user_accounts_email_lower CHECK (university_email = LOWER(university_email)),
    CONSTRAINT chk_user_accounts_password_state CHECK (
        (account_status = 'PASSWORD_SETUP_REQUIRED' AND password_hash IS NULL)
        OR (account_status <> 'PASSWORD_SETUP_REQUIRED')
    )
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id)
);
