CREATE TABLE skills (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        skill_category_id UUID NOT NULL REFERENCES skill_categories(id) ON DELETE RESTRICT,
                        skill_name VARCHAR(150) NOT NULL UNIQUE,
                        skill_description TEXT,
                        skill_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (skill_status IN ('ACTIVE', 'INACTIVE')),
                        display_order INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_skills_category ON skills(skill_category_id);
