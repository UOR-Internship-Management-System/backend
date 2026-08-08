INSERT INTO skill_core_clusters (id, cluster_name, description, display_order) VALUES
                                                                                   ('a0000000-0000-0000-0000-000000000001', 'Software Engineering', 'Core software development skills', 0),
                                                                                   ('a0000000-0000-0000-0000-000000000002', 'Data and AI', 'Data science and machine learning skills', 1);

INSERT INTO skill_categories (id, core_cluster_id, category_name, description, display_order) VALUES
                                                                                                  ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001', 'Frontend Development', NULL, 0),
                                                                                                  ('b0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000001', 'Backend Development', NULL, 1),
                                                                                                  ('b0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000002', 'Data Science', NULL, 0);

INSERT INTO skills (id, skill_category_id, skill_name, skill_description, display_order) VALUES
                                                                                             ('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'React', NULL, 0),
                                                                                             ('c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', 'TypeScript', NULL, 1),
                                                                                             ('c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002', 'Spring Boot', NULL, 0),
                                                                                             ('c0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000002', 'Python', NULL, 1);

-- TypeScript also selectable under Backend Development; Python also under Data Science
INSERT INTO skill_category_mappings (skill_id, skill_category_id) VALUES
                                                                      ('c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002'),
                                                                      ('c0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000003');
