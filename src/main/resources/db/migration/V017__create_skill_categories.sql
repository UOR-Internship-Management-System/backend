CREATE TABLE skill_categories (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  core_cluster_id UUID NOT NULL REFERENCES skill_core_clusters(id) ON DELETE CASCADE,
                                  category_name VARCHAR(120) NOT NULL,
                                  description TEXT,
                                  display_order INTEGER NOT NULL DEFAULT 0,
                                  UNIQUE (core_cluster_id, category_name)
);

CREATE INDEX idx_skill_categories_cluster ON skill_categories(core_cluster_id);
