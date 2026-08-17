-- Filter-run history lookups and deterministic history ordering.
CREATE INDEX idx_candidate_filter_runs_request_created_at_id
    ON candidate_filter_runs (internship_request_id, created_at DESC, id ASC);

CREATE INDEX idx_candidate_filter_runs_actor_created_at_id
    ON candidate_filter_runs (run_by_account_id, created_at DESC, id ASC);

-- The primary key supports run-first skill lookup; this reverse index supports taxonomy-skill
-- references and future integrity/diagnostic queries without scanning the whole association table.
CREATE INDEX idx_candidate_filter_run_skills_skill_run
    ON candidate_filter_run_skills (skill_id, filter_run_id);

-- Existing uniqueness is student-first. Candidate Filtering also needs efficient skill-first
-- membership checks for deterministic AND/OR predicates.
CREATE INDEX idx_student_declared_skills_skill_student
    ON student_declared_skills (skill_id, student_id);

-- GPA is an optional runtime range predicate. student_id completes a stable, covering lookup key
-- for the authoritative current GPA summary used by Candidate Filtering.
CREATE INDEX idx_student_academic_summary_gpa_student
    ON academic.student_academic_summary (computer_science_gpa, student_id);
