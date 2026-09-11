-- Required skills are normalized associations to the canonical developer-managed taxonomy.
-- Deleting a request removes only its associations; deleting a referenced canonical skill is restricted.
CREATE TABLE internship_request_skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    internship_request_id UUID NOT NULL,
    skill_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_internship_request_skills_request
        FOREIGN KEY (internship_request_id) REFERENCES internship_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_internship_request_skills_skill
        FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE RESTRICT,
    CONSTRAINT uq_internship_request_skills_request_skill
        UNIQUE (internship_request_id, skill_id)
);
