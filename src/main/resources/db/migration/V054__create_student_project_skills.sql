CREATE TABLE student_project_skills (
                                        project_id UUID NOT NULL REFERENCES student_projects(id) ON DELETE CASCADE,
                                        skill_id UUID NOT NULL REFERENCES skills(id) ON DELETE RESTRICT,
                                        PRIMARY KEY (project_id, skill_id)
);
