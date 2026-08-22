# BMD-012 Postman/Newman Acceptance

1. Start the backend with the `local` profile and PostgreSQL.
2. Copy `audit-system-events.local.template.postman_environment.json` to `audit-system-events.local.postman_environment.json`.
3. Add local Admin credentials. The local file is ignored and must never be committed.
4. Run `./run-newman.ps1` on Windows or `./run-newman.sh` on Unix.
5. Run `AUDIT_VERIFICATION.sql` against the same database.
6. Record sanitized results in `RELEASE_EVIDENCE.md`.

The collection validates public workflows only. The SQL companion validates internal persistence because the optional Admin audit viewer is outside the approved Version 1 scope.
