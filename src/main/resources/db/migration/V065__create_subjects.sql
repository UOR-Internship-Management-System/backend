CREATE TABLE subjects (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          course_code VARCHAR(30) NOT NULL UNIQUE,
                          course_title VARCHAR(250) NOT NULL,
                          credits NUMERIC(3,1) NOT NULL
);
