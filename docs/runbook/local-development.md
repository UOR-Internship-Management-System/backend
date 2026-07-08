# Local Development

Sprint 1 local development runs the backend with Java 21, Maven Wrapper, Docker Compose, PostgreSQL, and Flyway.

## Start Database

```sh
docker compose -f docker/docker-compose.dev.yml up -d
```

## Run Backend

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

On Windows, use `.\mvnw.cmd` with the same Maven goals.

## Verify Health

- `GET http://localhost:8080/api/v1/health`
- `GET http://localhost:8080/actuator/health`

## Local Secrets

Use `.env.example` and local environment variables only. Do not commit real database passwords, SMTP credentials, JWT secrets, or production URLs.

## Sprint Boundary

Local startup verifies foundation readiness only. Student onboarding/authentication business logic begins in Sprint 2.
