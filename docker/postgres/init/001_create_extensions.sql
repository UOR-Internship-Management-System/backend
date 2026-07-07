-- Bootstrap pgcrypto extension for gen_random_uuid() support.
-- This runs on first container start via the Docker entrypoint.
-- Flyway V001 also creates this extension to ensure it exists
-- regardless of how PostgreSQL is provisioned.
CREATE EXTENSION IF NOT EXISTS pgcrypto;
