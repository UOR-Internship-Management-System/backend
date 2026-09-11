CREATE TABLE skill_core_clusters (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     cluster_name VARCHAR(120) NOT NULL UNIQUE,
                                     description TEXT,
                                     display_order INTEGER NOT NULL DEFAULT 0,
                                     is_active BOOLEAN NOT NULL DEFAULT TRUE
);
