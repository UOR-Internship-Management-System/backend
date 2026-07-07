# Security Testing

Security tests must confirm backend enforcement, not frontend-only assumptions.

## Sprint 1 Checks

- Health endpoint is public.
- OpenAPI-declared public onboarding/authentication paths are not blocked by Spring Security.
- Student and admin protected patterns are not public.
- Removed-scope endpoint names and class names are absent from implementation packages.

Because Sprint 2 controllers are not implemented yet, `404` or `405` is acceptable for public onboarding/auth paths. `401` or `403` on those public paths indicates a security configuration error.

## Sensitive Data

Tests must not use real passwords, OTPs, JWTs, SMTP credentials, or production connection strings. OTP-related tests in future sprints must assert hashed storage only.
