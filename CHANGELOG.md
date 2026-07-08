# Changelog

All notable changes to the CV Management Backend are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

## [0.0.1-SNAPSHOT] — Sprint 1 Foundation — 2026-07-07

### Added

- Spring Boot 4.1.0 project skeleton with Java 21 and Maven Wrapper.
- Modular-monolith package structure: `config`, `infrastructure`, `modules`, `shared`.
- 15 reserved module packages: academics, adminstudents, auditlog, auth, companies, cv, exports, filtering, health, internships, projects, shortlists, skills, studentprofile, verification.
- PostgreSQL 16 integration with Flyway schema versioning (6 baseline migrations).
- Database tables: `roles`, `user_accounts`, `user_roles`, `eligible_students`, `verification_sessions`, `audit_events`.
- Sprint 1 seed data: STUDENT/ADMIN roles, predefined admin account, sample eligible students.
- Spring Security scaffolding with stateless JWT configuration, CORS, RBAC role guards.
- Health endpoint at `/api/v1/health` with database and Flyway status reporting.
- Global exception handler with RFC 9457 Problem Details–style error responses.
- Shared foundation: base entities, pagination DTOs, error hierarchy, validation annotations, API path constants.
- Docker Compose dev environment for local PostgreSQL.
- Docker Compose test environment for ephemeral test database.
- Multi-stage Dockerfile for Java 21 Spring Boot containerisation.
- CI workflows: build-test, code-quality, dependency-check.
- Test suite: application context, health endpoint, security smoke, Flyway migration, architecture package structure, module dependency rules, removed-scope guardrails.
- Sprint 1 documentation: architecture, runbooks, testing strategy, ADRs, API contract notes.
- OpenAPI 3.1.1 contract placed at `docs/api/` and `src/main/resources/openapi/`.
- Utility scripts: dev-start, dev-stop, run-tests, migrate-local, validate-openapi, generate-local-secrets example.

### Security

- No production secrets are committed. All sensitive values are injected via environment variables.
- Removed-scope guardrail test prevents accidental introduction of forbidden features.
