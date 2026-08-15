-- Stable secondary UUID keys support deterministic paging for every currently approved sort.
CREATE INDEX idx_companies_name_id
    ON companies (name ASC, id ASC);

CREATE INDEX idx_companies_updated_at_id
    ON companies (updated_at DESC, id ASC);

CREATE INDEX idx_internship_requests_company_created_at_id
    ON internship_requests (company_id, created_at DESC, id ASC);

CREATE INDEX idx_internship_requests_created_at_id
    ON internship_requests (created_at DESC, id ASC);

-- Supports the user-account FK action without requiring a full request-table scan.
CREATE INDEX idx_internship_requests_created_by_account_id
    ON internship_requests (created_by_account_id)
    WHERE created_by_account_id IS NOT NULL;

CREATE INDEX idx_internship_requests_title_id
    ON internship_requests (title ASC, id ASC);

-- The unique (internship_request_id, skill_id) constraint already indexes request-prefix lookups.
CREATE INDEX idx_internship_request_skills_skill_id
    ON internship_request_skills (skill_id);
