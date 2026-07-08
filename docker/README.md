# Docker Setup

This directory contains Docker configuration for the CV Management Backend.

## Files

| File | Purpose |
|------|---------|
| `Dockerfile` | Multi-stage build for the Spring Boot application (Java 21) |
| `docker-compose.dev.yml` | Local development PostgreSQL with persistent volume |
| `docker-compose.test.yml` | Ephemeral test PostgreSQL with tmpfs (no data persistence) |
| `postgres/init/` | PostgreSQL initialisation scripts run on first container start |

## Development Usage

Start the local PostgreSQL database:

```bash
docker compose -f docker/docker-compose.dev.yml up -d
```

Stop it:

```bash
docker compose -f docker/docker-compose.dev.yml down
```

## Test Usage

Start a disposable test database (port 5433):

```bash
docker compose -f docker/docker-compose.test.yml up -d
```

Data is stored in tmpfs and is lost when the container stops.

## Building the Application Image

```bash
docker build -f docker/Dockerfile -t cv-management-backend .
```

Run the image (requires a running PostgreSQL instance):

```bash
docker run -p 8080:8080 \
  -e CV_DB_HOST=host.docker.internal \
  -e CV_DB_PORT=5432 \
  -e CV_DB_NAME=cv_management \
  -e CV_DB_USERNAME=cv_user \
  -e CV_DB_PASSWORD=cv_local_password \
  -e JWT_SECRET=change-this-local-development-secret-at-least-32-characters \
  cv-management-backend
```

## Security Notes

- The Dockerfile does not bake secrets into the image.
- The runtime container runs as a non-root user (`appuser`).
- Environment variables must be injected at container start time.
