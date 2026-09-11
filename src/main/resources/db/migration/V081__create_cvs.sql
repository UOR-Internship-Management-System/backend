-- Minimal shape for now: enough to support freshness comparison.
-- Configuration (included record IDs) and PDF metadata are added in a later migration
-- once the preview/save/PDF pieces are built.
CREATE TABLE cvs (
                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                     student_id UUID NOT NULL UNIQUE REFERENCES eligible_students(id) ON DELETE CASCADE,
                     revision INTEGER NOT NULL DEFAULT 1,
                     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                     generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                     saved_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
