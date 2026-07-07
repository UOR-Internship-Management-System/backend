# Rollback

Sprint 1 rollback guidance is limited to local/dev/test environments.

## Application Rollback

Revert to the previous reviewed commit or container image and restart the backend with the same environment variables.

## Database Rollback

Flyway migrations are forward-only for this project. For local development, reset the Docker volume only when data loss is acceptable:

```sh
docker compose -f docker/docker-compose.dev.yml down -v
docker compose -f docker/docker-compose.dev.yml up -d
```

Do not use destructive database reset commands against shared or production-like data.

## Incident Notes

Record the failed commit, migration version, local profile, and command output before changing state.
