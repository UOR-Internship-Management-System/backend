# PostgreSQL Initialisation

Scripts in `init/` are executed by the official PostgreSQL Docker image on first container start (when the data directory is empty).

## 001_create_extensions.sql

Creates the `pgcrypto` extension required for `gen_random_uuid()` in Flyway migrations.

This is a Docker-entrypoint convenience only. The same extension is also created by `V001__create_schemas.sql` in Flyway, ensuring it exists regardless of how PostgreSQL is provisioned.
