# Incident Response

Sprint 1 incident response covers local/dev/test issues only.

## First Checks

1. Confirm the active profile and environment variables.
2. Check `docker compose -f docker/docker-compose.dev.yml ps`.
3. Check `/api/v1/health` and `/actuator/health`.
4. Review logs for safe operational errors.

## Security Handling

If a secret is accidentally committed, rotate it immediately and remove it from repository history through the approved project process. Do not paste secrets into issues, logs, or chat.

## Scope Handling

If removed-scope behavior appears, stop extending it, document the file/path, and remove or quarantine it in the next hardening change.
