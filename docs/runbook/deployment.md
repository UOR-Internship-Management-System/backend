# Deployment

Sprint 1 deployment documentation covers local/dev/test readiness only. No production hosting, Kubernetes, cloud automation, or production secret procedure is defined in this sprint.

## Build Artifact

```sh
./mvnw -B package -DskipTests
```

The Spring Boot jar is produced under `target/`.

## Container Image

`docker/Dockerfile` provides a multi-stage Java 21 build and non-root runtime image. It does not bake credentials into the image.

## Required Runtime Configuration

Use environment variables for database URL/credentials, JWT settings, and CORS origins. Values in repository examples are local placeholders only.

## Readiness

Use `/actuator/health` or `/api/v1/health` for local readiness checks. Production deployment design remains outside Sprint 1.
