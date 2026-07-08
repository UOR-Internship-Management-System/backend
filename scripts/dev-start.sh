#!/usr/bin/env sh
set -eu

docker compose -f docker/docker-compose.dev.yml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
