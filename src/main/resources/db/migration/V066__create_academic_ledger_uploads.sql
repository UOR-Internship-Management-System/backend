-- Minimal placeholder table until the full Admin ledger ingestion pipeline (BMD upload/validate/commit)
-- is built later. Only exists so committed academic_records rows have a real source to reference.
CREATE TABLE academic_ledger_uploads (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         committed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
