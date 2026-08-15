-- Company metadata is Admin-managed only. No company account or lifecycle-status column exists.
-- normalized_name is database-derived so duplicate protection cannot be bypassed by application code.
CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    normalized_name VARCHAR(200) GENERATED ALWAYS AS (
        LOWER(REGEXP_REPLACE(BTRIM(name), '[[:space:]]+', ' ', 'g'))
    ) STORED,
    website_url VARCHAR(500),
    contact_person VARCHAR(150),
    contact_email VARCHAR(254),
    contact_phone VARCHAR(30),
    notes TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_companies_name_not_blank CHECK (BTRIM(name) <> ''),
    CONSTRAINT chk_companies_notes_length CHECK (notes IS NULL OR CHAR_LENGTH(notes) <= 4000),
    CONSTRAINT chk_companies_version_non_negative CHECK (version >= 0),
    CONSTRAINT uq_companies_normalized_name UNIQUE (normalized_name)
);
