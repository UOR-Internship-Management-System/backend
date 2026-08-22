-- Deterministic shortlist list and detail query support.
CREATE INDEX idx_shortlists_status_updated_at_id
    ON shortlists (status, updated_at DESC, id ASC);

CREATE INDEX idx_shortlists_updated_at_id
    ON shortlists (updated_at DESC, id ASC);

CREATE INDEX idx_shortlists_created_at_id
    ON shortlists (created_at DESC, id ASC);

CREATE INDEX idx_shortlists_filter_run_id
    ON shortlists (filter_run_id)
    WHERE filter_run_id IS NOT NULL;

CREATE INDEX idx_shortlist_candidates_shortlist_selected_at_id
    ON shortlist_candidates (shortlist_id, selected_at ASC, id ASC);

CREATE INDEX idx_shortlist_candidates_student_shortlist
    ON shortlist_candidates (student_id, shortlist_id);

CREATE INDEX idx_shortlist_candidates_selected_by
    ON shortlist_candidates (selected_by_account_id);
