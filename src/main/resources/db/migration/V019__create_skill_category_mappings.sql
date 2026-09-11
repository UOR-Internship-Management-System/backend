-- Lets one skill (e.g. "Python") appear under more than one category
-- (e.g. Backend Development AND Data Science)
CREATE TABLE skill_category_mappings (
                                         skill_id UUID NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
                                         skill_category_id UUID NOT NULL REFERENCES skill_categories(id) ON DELETE CASCADE,
                                         mapping_reason VARCHAR(200),
                                         PRIMARY KEY (skill_id, skill_category_id)
);
