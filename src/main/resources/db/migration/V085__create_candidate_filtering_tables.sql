-- Candidate Filtering persists only immutable run criteria/history.
-- Candidate rows are recomputed from current committed GPA and declared-skill data.
CREATE TABLE candidate_filter_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    internship_request_id UUID NOT NULL,
    run_by_account_id UUID NOT NULL,
    runtime_gpa_lower_bound NUMERIC(3,2),
    runtime_gpa_upper_bound NUMERIC(3,2),
    skill_match_mode VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_candidate_filter_runs_request
        FOREIGN KEY (internship_request_id) REFERENCES internship_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_candidate_filter_runs_actor
        FOREIGN KEY (run_by_account_id) REFERENCES user_accounts(id) ON DELETE RESTRICT,
    CONSTRAINT chk_candidate_filter_runs_gpa_lower_bound
        CHECK (runtime_gpa_lower_bound IS NULL
            OR runtime_gpa_lower_bound BETWEEN 0.00 AND 4.00),
    CONSTRAINT chk_candidate_filter_runs_gpa_upper_bound
        CHECK (runtime_gpa_upper_bound IS NULL
            OR runtime_gpa_upper_bound BETWEEN 0.00 AND 4.00),
    CONSTRAINT chk_candidate_filter_runs_gpa_range
        CHECK (runtime_gpa_lower_bound IS NULL
            OR runtime_gpa_upper_bound IS NULL
            OR runtime_gpa_lower_bound <= runtime_gpa_upper_bound),
    CONSTRAINT chk_candidate_filter_runs_skill_match_mode
        CHECK (skill_match_mode IN ('AND', 'OR'))
);

-- The source discriminator is required because the API must reconstruct requestSkillIds and
-- additionalSkillIds exactly as submitted when the immutable run was created.
CREATE TABLE candidate_filter_run_skills (
    filter_run_id UUID NOT NULL,
    skill_id UUID NOT NULL,
    criteria_source VARCHAR(10) NOT NULL,
    CONSTRAINT pk_candidate_filter_run_skills
        PRIMARY KEY (filter_run_id, skill_id),
    CONSTRAINT fk_candidate_filter_run_skills_run
        FOREIGN KEY (filter_run_id) REFERENCES candidate_filter_runs(id) ON DELETE CASCADE,
    CONSTRAINT fk_candidate_filter_run_skills_skill
        FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE RESTRICT,
    CONSTRAINT chk_candidate_filter_run_skills_source
        CHECK (criteria_source IN ('REQUEST', 'ADDITIONAL'))
);
