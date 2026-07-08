# Monitoring

Sprint 1 monitoring is intentionally small and local-focused.

## Health

- `/actuator/health` exposes Spring Boot health.
- `/api/v1/health` provides the project API health check.

## Logging

Application logs should be safe for local troubleshooting. Do not log raw passwords, OTPs, JWTs, secrets, or sensitive payloads.

## Correlation

The infrastructure package includes correlation ID support so later business modules can tie request logs and audit events together.

## Metrics

Actuator is present for foundation readiness. Broader metrics, alerts, and production dashboards are outside Sprint 1.
