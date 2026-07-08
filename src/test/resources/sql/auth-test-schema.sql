DROP ALL OBJECTS;

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_accounts (
    id UUID PRIMARY KEY,
    university_email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    account_status VARCHAR(32) NOT NULL,
    password_changed_at TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE eligible_students (
    id UUID PRIMARY KEY,
    index_number VARCHAR(32) NOT NULL UNIQUE,
    university_email VARCHAR(254) NOT NULL UNIQUE,
    full_name VARCHAR(160) NOT NULL,
    academic_level SMALLINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    user_account_id UUID UNIQUE REFERENCES user_accounts(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE admin_users (
    id UUID PRIMARY KEY,
    user_account_id UUID NOT NULL UNIQUE REFERENCES user_accounts(id) ON DELETE CASCADE,
    display_name VARCHAR(160) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE verification_sessions (
    id UUID PRIMARY KEY,
    eligible_student_id UUID REFERENCES eligible_students(id) ON DELETE CASCADE,
    index_number VARCHAR(32) NOT NULL,
    university_email VARCHAR(254) NOT NULL,
    purpose VARCHAR(32) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    account_type VARCHAR(32),
    user_account_id UUID REFERENCES user_accounts(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    verified_at TIMESTAMP WITH TIME ZONE,
    consumed_at TIMESTAMP WITH TIME ZONE,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    resend_count INTEGER NOT NULL DEFAULT 0,
    last_resend_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_events (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    actor_user_id UUID,
    actor_role VARCHAR(50),
    event_type VARCHAR(100) NOT NULL,
    event_category VARCHAR(50) NOT NULL,
    resource_type VARCHAR(100),
    resource_id VARCHAR(100),
    metadata VARCHAR(2000),
    correlation_id VARCHAR(100),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO roles (id, name, description) VALUES
    ('10000000-0000-0000-0000-000000000001', 'ROLE_STUDENT', 'Student portal user'),
    ('10000000-0000-0000-0000-000000000002', 'ROLE_ADMIN', 'Department administrator');

INSERT INTO eligible_students (
    id, index_number, university_email, full_name, academic_level, is_active
) VALUES (
    '20000000-0000-0000-0000-000000000001',
    'SC-2020-001',
    'sc2020001@dcs.ruh.ac.lk',
    'Nimal Perera',
    3,
    TRUE
);
