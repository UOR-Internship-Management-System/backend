-- BMD-007: normalize active and preview record-level inclusion snapshots.
-- Source UUIDs intentionally have no FK to live source tables: saved/preview selections are immutable snapshot references.

CREATE TABLE cv_selected_experiences (
    cv_id UUID NOT NULL,
    student_id UUID NOT NULL,
    source_record_id UUID NOT NULL,
    PRIMARY KEY (cv_id, source_record_id),
    FOREIGN KEY (cv_id, student_id) REFERENCES cvs(id, student_id) ON DELETE CASCADE
);
CREATE INDEX idx_cv_selected_experiences_student ON cv_selected_experiences (student_id);

CREATE TABLE cv_selected_projects (
    cv_id UUID NOT NULL,
    student_id UUID NOT NULL,
    source_record_id UUID NOT NULL,
    PRIMARY KEY (cv_id, source_record_id),
    FOREIGN KEY (cv_id, student_id) REFERENCES cvs(id, student_id) ON DELETE CASCADE
);
CREATE INDEX idx_cv_selected_projects_student ON cv_selected_projects (student_id);

CREATE TABLE cv_selected_certificates (
    cv_id UUID NOT NULL,
    student_id UUID NOT NULL,
    source_record_id UUID NOT NULL,
    PRIMARY KEY (cv_id, source_record_id),
    FOREIGN KEY (cv_id, student_id) REFERENCES cvs(id, student_id) ON DELETE CASCADE
);
CREATE INDEX idx_cv_selected_certificates_student ON cv_selected_certificates (student_id);

CREATE TABLE cv_selected_awards (
    cv_id UUID NOT NULL,
    student_id UUID NOT NULL,
    source_record_id UUID NOT NULL,
    PRIMARY KEY (cv_id, source_record_id),
    FOREIGN KEY (cv_id, student_id) REFERENCES cvs(id, student_id) ON DELETE CASCADE
);
CREATE INDEX idx_cv_selected_awards_student ON cv_selected_awards (student_id);

CREATE TABLE cv_selected_activities (
    cv_id UUID NOT NULL,
    student_id UUID NOT NULL,
    source_record_id UUID NOT NULL,
    PRIMARY KEY (cv_id, source_record_id),
    FOREIGN KEY (cv_id, student_id) REFERENCES cvs(id, student_id) ON DELETE CASCADE
);
CREATE INDEX idx_cv_selected_activities_student ON cv_selected_activities (student_id);

CREATE TABLE cv_preview_experiences (
    preview_id UUID NOT NULL,
    student_id UUID NOT NULL,
    source_record_id UUID NOT NULL,
    PRIMARY KEY (preview_id, source_record_id),
    FOREIGN KEY (preview_id, student_id) REFERENCES cv_previews(preview_id, student_id) ON DELETE CASCADE
);
CREATE INDEX idx_cv_preview_experiences_student ON cv_preview_experiences (student_id);

CREATE TABLE cv_preview_projects (
    preview_id UUID NOT NULL,
    student_id UUID NOT NULL,
    source_record_id UUID NOT NULL,
    PRIMARY KEY (preview_id, source_record_id),
    FOREIGN KEY (preview_id, student_id) REFERENCES cv_previews(preview_id, student_id) ON DELETE CASCADE
);
CREATE INDEX idx_cv_preview_projects_student ON cv_preview_projects (student_id);

CREATE TABLE cv_preview_certificates (
    preview_id UUID NOT NULL,
    student_id UUID NOT NULL,
    source_record_id UUID NOT NULL,
    PRIMARY KEY (preview_id, source_record_id),
    FOREIGN KEY (preview_id, student_id) REFERENCES cv_previews(preview_id, student_id) ON DELETE CASCADE
);
CREATE INDEX idx_cv_preview_certificates_student ON cv_preview_certificates (student_id);

CREATE TABLE cv_preview_awards (
    preview_id UUID NOT NULL,
    student_id UUID NOT NULL,
    source_record_id UUID NOT NULL,
    PRIMARY KEY (preview_id, source_record_id),
    FOREIGN KEY (preview_id, student_id) REFERENCES cv_previews(preview_id, student_id) ON DELETE CASCADE
);
CREATE INDEX idx_cv_preview_awards_student ON cv_preview_awards (student_id);

CREATE TABLE cv_preview_activities (
    preview_id UUID NOT NULL,
    student_id UUID NOT NULL,
    source_record_id UUID NOT NULL,
    PRIMARY KEY (preview_id, source_record_id),
    FOREIGN KEY (preview_id, student_id) REFERENCES cv_previews(preview_id, student_id) ON DELETE CASCADE
);
CREATE INDEX idx_cv_preview_activities_student ON cv_preview_activities (student_id);

-- Backfill legacy V082 comma-separated snapshots. UUID casts intentionally fail the migration on malformed legacy data.
INSERT INTO cv_selected_experiences (cv_id, student_id, source_record_id)
SELECT c.id, c.student_id, btrim(token)::uuid
FROM cvs c
CROSS JOIN LATERAL regexp_split_to_table(COALESCE(c.included_experience_ids, ''), ',') AS token
WHERE btrim(token) <> ''
ON CONFLICT DO NOTHING;

INSERT INTO cv_selected_projects (cv_id, student_id, source_record_id)
SELECT c.id, c.student_id, btrim(token)::uuid
FROM cvs c
CROSS JOIN LATERAL regexp_split_to_table(COALESCE(c.included_project_ids, ''), ',') AS token
WHERE btrim(token) <> ''
ON CONFLICT DO NOTHING;

INSERT INTO cv_selected_certificates (cv_id, student_id, source_record_id)
SELECT c.id, c.student_id, btrim(token)::uuid
FROM cvs c
CROSS JOIN LATERAL regexp_split_to_table(COALESCE(c.included_certificate_ids, ''), ',') AS token
WHERE btrim(token) <> ''
ON CONFLICT DO NOTHING;

INSERT INTO cv_selected_awards (cv_id, student_id, source_record_id)
SELECT c.id, c.student_id, btrim(token)::uuid
FROM cvs c
CROSS JOIN LATERAL regexp_split_to_table(COALESCE(c.included_award_ids, ''), ',') AS token
WHERE btrim(token) <> ''
ON CONFLICT DO NOTHING;

INSERT INTO cv_selected_activities (cv_id, student_id, source_record_id)
SELECT c.id, c.student_id, btrim(token)::uuid
FROM cvs c
CROSS JOIN LATERAL regexp_split_to_table(COALESCE(c.included_activity_ids, ''), ',') AS token
WHERE btrim(token) <> ''
ON CONFLICT DO NOTHING;
