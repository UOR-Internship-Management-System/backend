-- Internship requests intentionally contain no lifecycle status and no GPA requirement fields.
-- GPA constraints belong only to a later candidate-filter execution, never to request persistence.
CREATE TABLE internship_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    shortlist_guidance_value INTEGER,
    created_by_account_id UUID,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_internship_requests_company
        FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_internship_requests_created_by_account
        FOREIGN KEY (created_by_account_id) REFERENCES user_accounts(id) ON DELETE SET NULL,
    CONSTRAINT chk_internship_requests_title_not_blank CHECK (BTRIM(title) <> ''),
    CONSTRAINT chk_internship_requests_description_length
        CHECK (description IS NULL OR CHAR_LENGTH(description) <= 10000),
    CONSTRAINT chk_internship_requests_shortlist_guidance
        CHECK (shortlist_guidance_value IS NULL OR shortlist_guidance_value BETWEEN 0 AND 10000),
    CONSTRAINT chk_internship_requests_version_non_negative CHECK (version >= 0)
);
