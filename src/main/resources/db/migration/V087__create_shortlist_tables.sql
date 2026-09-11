-- BMD-011 extends the established public Internship persistence model.
-- One shortlist is allowed for each internship request in Version 1.
CREATE TABLE shortlists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    internship_request_id UUID NOT NULL,
    filter_run_id UUID,
    name VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    guidance_value_snapshot INTEGER,
    guidance_warning_acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    finalization_note VARCHAR(1000),
    created_by_account_id UUID NOT NULL,
    finalized_by_account_id UUID,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    finalized_at TIMESTAMPTZ,
    CONSTRAINT uq_shortlists_internship_request UNIQUE (internship_request_id),
    CONSTRAINT fk_shortlists_internship_request
        FOREIGN KEY (internship_request_id) REFERENCES internship_requests(id) ON DELETE RESTRICT,
    CONSTRAINT fk_shortlists_filter_run
        FOREIGN KEY (filter_run_id) REFERENCES candidate_filter_runs(id) ON DELETE SET NULL,
    CONSTRAINT fk_shortlists_created_by
        FOREIGN KEY (created_by_account_id) REFERENCES user_accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_shortlists_finalized_by
        FOREIGN KEY (finalized_by_account_id) REFERENCES user_accounts(id) ON DELETE RESTRICT,
    CONSTRAINT chk_shortlists_name
        CHECK (name IS NULL OR (BTRIM(name) <> '' AND CHAR_LENGTH(name) <= 200)),
    CONSTRAINT chk_shortlists_status
        CHECK (status IN ('DRAFT', 'FINALIZED')),
    CONSTRAINT chk_shortlists_guidance
        CHECK (guidance_value_snapshot IS NULL OR guidance_value_snapshot BETWEEN 0 AND 10000),
    CONSTRAINT chk_shortlists_version
        CHECK (version >= 0),
    CONSTRAINT chk_shortlists_finalization_state
        CHECK (
            (status = 'DRAFT'
                AND finalized_by_account_id IS NULL
                AND finalized_at IS NULL
                AND finalization_note IS NULL
                AND guidance_warning_acknowledged = FALSE)
            OR
            (status = 'FINALIZED'
                AND finalized_by_account_id IS NOT NULL
                AND finalized_at IS NOT NULL)
        )
);

CREATE TABLE shortlist_candidates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shortlist_id UUID NOT NULL,
    student_id UUID NOT NULL,
    selected_by_account_id UUID NOT NULL,
    selected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    selection_note VARCHAR(1000),
    CONSTRAINT uq_shortlist_candidates_membership UNIQUE (shortlist_id, student_id),
    CONSTRAINT fk_shortlist_candidates_shortlist
        FOREIGN KEY (shortlist_id) REFERENCES shortlists(id) ON DELETE CASCADE,
    CONSTRAINT fk_shortlist_candidates_student
        FOREIGN KEY (student_id) REFERENCES eligible_students(id) ON DELETE RESTRICT,
    CONSTRAINT fk_shortlist_candidates_selected_by
        FOREIGN KEY (selected_by_account_id) REFERENCES user_accounts(id) ON DELETE RESTRICT,
    CONSTRAINT chk_shortlist_candidates_note
        CHECK (selection_note IS NULL OR CHAR_LENGTH(selection_note) <= 1000)
);
