-- One row per student. Tracks whether their saved CV (if any) is stale relative to
-- their latest Profile/Skills/Projects/Academic-Records edits.
CREATE TABLE cv_source_freshness (
                                     student_id UUID PRIMARY KEY REFERENCES eligible_students(id) ON DELETE CASCADE,
                                     profile_changed_at TIMESTAMPTZ,
                                     declared_skills_changed_at TIMESTAMPTZ,
                                     projects_changed_at TIMESTAMPTZ,
                                     academic_records_changed_at TIMESTAMPTZ
);
