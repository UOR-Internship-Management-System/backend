# BMD-012 Audit Logging and System Events Backend Implementation Plan

## Document control

| Item | Value |
| --- | --- |
| Module | BMD-012 — Audit Logging and System Events |
| Core package | `lk.ac.ruhuna.dcs.cvmanagement.modules.auditlog` |
| Shared contract | `lk.ac.ruhuna.dcs.cvmanagement.shared.audit` |
| Backend | Spring Boot modular monolith |
| Database | PostgreSQL with Flyway |
| Frontend | React, TypeScript, TanStack Query, Zod, Playwright |
| Canonical transport contract | `CV_Management_API_OpenAPI_v1.6.0.yaml` |
| Audited backend baseline | `feature/candidate-filtering`, including `origin/develop` through Shortlist Management |
| Current migration ceiling | `V090` |
| First available migration | `V091` (must be rechecked immediately before coding) |
| Plan status | Implementation-ready after the policy gates in Section 5 are recorded |
| Audit date | 2026-08-22 |

## 1. Executive summary

BMD-012 is the cross-cutting backend capability that makes security-sensitive and Admin-critical activity traceable without exposing credentials, OTPs, JWTs, personal documents, SQL, or other secrets. Its primary responsibility is durable, append-only persistence of classified audit and security events. It also supplies correlation-aware diagnostics and operational evidence for the already implemented Authentication, Academic Ledger, Company/Internship, CV, Candidate Filtering, Shortlist, and Export workflows.

The codebase already has a useful partial foundation:

- Flyway `V004` created `public.audit_events`.
- `AuditEventPublisher` writes JSONB metadata and correlation IDs.
- critical workflows can call `recordRequired(...)` in their business transaction;
- non-critical diagnostics can call `recordBestEffort(...)`;
- several modules already publish meaningful events.

However, BMD-012 is not complete. The dedicated `modules/auditlog` controller, service, entities, repositories, mapper, request, and response types are still scaffolds. The shared publisher owns JDBC persistence directly, security event names are free-form strings, metadata has no central allow-list or secret sanitizer, coverage is inconsistent, retention is unresolved, append-only behavior is not enforced, and there is no module-level PostgreSQL acceptance suite.

The safe implementation is a **forward-only hardening of the existing `public.audit_events` model**, beginning at `V091`. It must not introduce parallel `audit.audit_log` and `audit.security_event` tables beside a deployed table that already contains production-relevant history. The conceptual Database Design entities will be mapped into the established physical table using `event_category`, `severity`, `outcome`, actor/target fields, correlation ID, timestamps, and sanitized metadata.

The approved Version 1 frontend has no Audit Log page. OpenAPI v1.6 explicitly says `/admin/audit-events` may be omitted when audit remains an internal backend concern. Therefore, the default plan keeps audit data internal, verifies it through backend/PostgreSQL tests, and performs real frontend E2E by executing audited workflows and correlating them with persisted events. A read-only Admin audit viewer is a separately gated optional slice, not an assumed requirement.

## 2. Audited sources and authority

### 2.1 Mandatory sources audited

- Backend Module Document.
- Database Design Document.
- Software Requirements Specification.
- Student and Admin Workflow Document.
- Final Reduced Scope Baseline Document.
- Backend Folder Structure Implementation Plan.
- `CV_Management_API_OpenAPI_v1.6.0.yaml`.

### 2.2 Supporting sources audited

- Production-Ready Use Case Document.
- Admin page/workflow specification.
- UI Frontend Document.
- Frontend Folder Structure Implementation Plan.
- Scope-reduction document.
- OpenAPI generated-client notes.
- Current backend source, migrations, security configuration, architecture tests, audit callers, and PostgreSQL tests.
- Current frontend routes, features, API clients, runtime schemas, unit tests, and Playwright suites.

### 2.3 Explicit exclusion

The outdated **API Specification Document** was not used. Where older material conflicts with OpenAPI v1.6, the reduced-scope baseline, the current SRS, or the deployed code/database model, it must not control implementation.

### 2.4 Source-of-truth order

1. Final Reduced Scope Baseline and explicit approved team decisions.
2. Current Software Requirements Specification.
3. Database Design and Backend Module documents.
4. OpenAPI v1.6 for approved HTTP behavior.
5. Workflow and Use Case documents.
6. Current production backend and frontend conventions.
7. Historical UI material only for presentation intent.

## 3. Requirements traceability

### 3.1 Backend Module Document

BMD-012 requires:

- durable persistence of Admin-critical audit events and security events;
- sanitized metadata, correlation IDs, event classification, actor and target references;
- append-only behavior, except controlled retention or archival;
- required audit writes to share the business transaction or use a reliable local outbox;
- audit persistence failure to fail a protected critical operation;
- operational logs and metrics that contain no PII labels or secrets;
- restricted access to audit data;
- unit, repository, transaction, authorization, failure, and secret-leakage tests.

The document leaves three policy questions open: retention duration, whether an Admin viewer is available, and whether audit-log export is available.

### 3.2 Database Design Document

The conceptual model describes:

- `audit.audit_log` for business traceability;
- `audit.security_event` for classified security events;
- actor, role, target, time, optional sanitized client context, and JSONB metadata;
- severity values `INFO`, `WARN`, `HIGH`, and `CRITICAL`;
- append-only retention, time-based archival, restricted review, and no secrets.

The current physical model predates that conceptual split and uses one table. Section 7 defines the safe reconciliation.

### 3.3 Software Requirements Specification

The implementation must cover:

- Admin-critical actions;
- Academic Ledger commit activity;
- Internship request changes;
- Shortlist finalization;
- CV downloads and bulk exports;
- authentication and security events;
- Admin change traceability;
- strict exclusion of passwords, OTPs, JWTs, secrets, and unsafe error details.

### 3.4 Workflow requirements

The audited workflows require actor, action, target, timestamp, and outcome evidence for:

- login, authentication failure, password setup/reset, and OTP attempt limits;
- ledger upload/validation/commit/failure;
- company and internship request mutation;
- filtering execution;
- shortlist creation, membership mutation, and finalization;
- Student/Admin CV download;
- CSV and bulk-CV export lifecycle.

### 3.5 OpenAPI v1.6

OpenAPI defines an optional read endpoint:

```text
GET /api/v1/admin/audit-events
```

It is Admin-only and pageable/searchable, but the contract is intentionally weak:

- the description says it may be omitted if audit remains internal;
- `PagedAuditEventResponse.items` is currently an untyped object array;
- `AuditEventResponse` is only partially specified;
- the endpoint lacks the stronger security and extension metadata used by newer operations.

The endpoint must not be activated until the policy decision and OpenAPI contract correction in Section 5 are approved.

## 4. Current-state audit

### 4.1 Database

`V004__create_audit_tables.sql` created:

```text
public.audit_events
```

Current columns:

```text
id
actor_user_id
actor_role
event_type
event_category
resource_type
resource_id
metadata
correlation_id
occurred_at
```

`V006` added indexes for actor, event type/time, and correlation ID. The latest migration in the audited branch is `V090`, so BMD-012 should begin at `V091` unless another branch lands first.

Current deficiencies:

- no explicit `outcome` or security `severity`;
- metadata is nullable and has no JSON-object check;
- no controlled database constraints for category, actor role, severity, or outcome;
- no target/time index suited to traceability queries;
- no event-category/time index suited to operational review;
- no database or privilege rule establishing append-only application access;
- no documented retention/archival behavior.

### 4.2 Backend code

Implemented foundation:

- `shared.audit.AuditEventPublisher`;
- controlled `AuditEventCategory` values;
- a partial `AuditEventType` enum for newer business modules;
- required versus best-effort persistence methods;
- correlation ID capture through `CorrelationIdContext`;
- existing publishers in Auth, Verification, Academics, Companies, Internships, Admin Students, CV, Candidate Filtering, Shortlists, and Exports.

Incomplete or unsafe areas:

- `AuditEventPublisher` depends directly on `JdbcTemplate`, so persistence is owned by `shared` rather than BMD-012;
- security events are mostly raw strings rather than controlled event types;
- `SecurityEventService` is only a thin wrapper;
- Audit Log entities/repositories/service/controller/DTOs are empty scaffolds;
- no central metadata policy, key allow-list, value limits, recursion limits, or secret-key rejection;
- best-effort failures log only a warning and expose no counter/health signal;
- event outcome and severity are implicit in event names;
- audit failure handling is not tested consistently across every critical workflow;
- `auditlog` is not yet in the active endpoint-module list in `ModuleDependencyRulesTest`;
- only one shared publisher unit test exists; there is no dedicated BMD-012 PostgreSQL suite.

### 4.3 Existing event coverage

Coverage already exists for many important actions:

- authentication login success/failure, logout, verification, OTP verification/failure, and password reset/setup;
- ledger processing, validation, commit, and failure paths;
- company and internship request mutations and skill changes;
- CV preview, save, failure, Student/Admin download, and unavailable file;
- candidate-filter run creation;
- shortlist create/add/remove/finalize;
- export create/complete/fail/download.

The BMD-012 implementation must preserve these call sites, normalize their contracts, and close gaps. It must not replace them with an unrelated second event system.

### 4.4 Frontend

The current frontend contains implemented feature areas for Academic Ledger, Admin Authentication, Candidate Filtering, CV Builder, Exports, Internship Management, Shortlists, Student Management, and related Student workflows. It has no audit feature directory, route, navigation entry, API wrapper, runtime schema, or Audit Log page.

This matches the optional nature of the OpenAPI read endpoint. Frontend work for the default BMD-012 release is therefore **acceptance instrumentation**, not a new UI:

- preserve/read correlation IDs from API errors and responses;
- exercise real audited actions through existing Admin/Student pages;
- capture the action's correlation ID in Playwright evidence;
- verify corresponding persisted audit rows through the backend acceptance harness;
- ensure UI errors never expose sensitive audit metadata.

## 5. Policy gates and locked decisions

### 5.1 Decisions required before production release

| Decision | Current status | Safe default for implementation |
| --- | --- | --- |
| Audit retention duration | Unresolved in authoritative documents | No automatic deletion; retain until a written policy is approved |
| Admin audit viewer | Optional and not represented in current UI | Internal-only for Version 1 |
| Audit export | Unapproved | Out of scope |
| Sanitized client IP/user agent collection | Optional in Database Design | Disabled by default until privacy approval; support nullable columns/configuration |
| Runtime DB role separation | Not established in current local setup | Required deployment hardening before DB-level append-only privileges can be claimed |

These are not reasons to delay the persistence and safety work. They are release gates. No developer should invent a destructive retention period or expose a viewer/export without approval.

### 5.2 Locked implementation decisions

- Continue using `public.audit_events` as the single physical event store.
- Do not create parallel `audit.audit_log` and `audit.security_event` tables.
- Distinguish security events with `event_category=SECURITY` plus `severity`.
- Use `recordRequired` for business-critical audit invariants and propagate failure.
- Use `recordBestEffort` only for non-critical diagnostics and unsuccessful attempts where failing the user operation would be unsafe or misleading.
- Keep metadata sanitized, bounded, and structured.
- Store no raw password, OTP, JWT, authorization header, reset token, CV content, exported file content, SQL, stack trace, or unrestricted request body.
- Preserve correlation IDs end to end.
- Keep the Admin audit read API disabled unless separately approved and fully specified.
- Use forward-only Flyway migrations; never rewrite `V004` or `V006`.

## 6. Target architecture

### 6.1 Ownership model

`shared.audit` remains the stable cross-module contract so business modules do not import BMD-012 repositories or entities. BMD-012 owns storage.

```text
Business module
    ↓
shared.audit.AuditEventPublisher
    ↓
shared.audit.AuditEventSink (port)
    ↓
modules.auditlog.persistence.JdbcAuditEventSink (adapter)
    ↓
public.audit_events
```

`AuditEventPublisher` must no longer contain SQL. This keeps existing callers stable while moving persistence responsibility into the correct module.

### 6.2 Recommended package structure

```text
shared/audit/
├── AuditEvent.java
├── AuditEventCategory.java
├── AuditEventCriticality.java
├── AuditEventOutcome.java
├── AuditEventPublisher.java
├── AuditEventSeverity.java
├── AuditEventSink.java
└── AuditEventType.java

modules/auditlog/
├── api/                              # activated only if viewer is approved
│   ├── AuditLogController.java
│   └── dto/
├── application/
│   ├── AuditLogQueryService.java     # optional viewer slice
│   ├── AuditMetadataPolicy.java
│   ├── AuditRetentionPolicy.java
│   └── SecurityEventService.java
├── domain/
│   ├── AuditMetadata.java
│   └── exception/
├── mapper/
└── persistence/
    ├── JdbcAuditEventSink.java
    ├── AuditEventReadRepository.java # optional viewer slice
    └── projection/
```

JPA entities are not required for an append-only write path. A small JDBC adapter is preferred because it matches the existing implementation, avoids accidental dirty checking/update support, and participates naturally in the caller's Spring transaction.

### 6.3 Event contract

Required fields:

| Field | Rule |
| --- | --- |
| `eventId` | UUID generated by PostgreSQL |
| `eventType` | Controlled enum/string, maximum 100 characters |
| `category` | Controlled category |
| `outcome` | `SUCCEEDED`, `FAILED`, `DENIED`, or `ATTEMPTED` |
| `severity` | Required for `SECURITY`; optional/defaulted for business events |
| `actorUserId` | Nullable only for anonymous/system events |
| `actorRole` | `ADMIN`, `STUDENT`, `SYSTEM`, or `ANONYMOUS` |
| `resourceType` | Controlled, non-sensitive target type |
| `resourceId` | Sanitized logical identifier; never a path/token |
| `metadata` | Non-null JSON object, centrally sanitized and bounded |
| `correlationId` | Required for HTTP-originated actions; nullable for workers/startup |
| `occurredAt` | PostgreSQL `TIMESTAMPTZ` |

### 6.4 Metadata policy

Use event-specific allow-lists. Global rejection rules must be case-insensitive and reject keys or nested keys containing concepts such as:

```text
password, passphrase, otp, secret, token, authorization, cookie,
credential, privateKey, resetCode, fileContent, sql, stackTrace
```

Additional rules:

- JSON object only;
- maximum serialized size, recommended 8 KiB;
- bounded string length, recommended 512 characters per value;
- bounded collection size and nesting depth;
- UUIDs and numeric counts preferred over names/emails;
- no raw request/response bodies;
- no filesystem storage paths;
- log sanitization failures using only the event type and correlation ID.

### 6.5 Required versus best-effort semantics

Required events execute inside the protected business transaction. If the sink fails, the business mutation rolls back and the API returns a sanitized server/dependency error.

Best-effort events:

- must never log the rejected metadata;
- increment a failure counter;
- emit a sanitized structured warning;
- expose degraded audit health to operations without exposing event content;
- must not silently masquerade as successfully persisted evidence.

No external broker is required for Version 1. If asynchronous audit delivery is introduced later, it must use a transactional local outbox rather than an in-memory queue.

## 7. Database implementation

### 7.1 Conceptual-to-physical reconciliation

| Conceptual design | Physical implementation |
| --- | --- |
| `audit.audit_log` | `public.audit_events` where category is non-`SECURITY` |
| `audit.security_event` | `public.audit_events` where category is `SECURITY` and severity is populated |
| `event_at` / `occurred_at` | `occurred_at` |
| entity schema/table/ID | controlled `resource_type`, `resource_id`, and allowed metadata |
| security severity | new `severity` column |
| event result | new `outcome` column |

This preserves all existing rows and avoids the academic-style duplicate persistence problem previously encountered elsewhere in the project.

### 7.2 Migration numbering

Expected first migration:

```text
V091__harden_audit_event_contract.sql
```

Optional later migration, only after runtime-role design is approved:

```text
V092__enforce_audit_append_only_runtime_permissions.sql
```

Before creating either file:

```powershell
git fetch origin
git switch develop
git pull --ff-only origin develop
Get-ChildItem src/main/resources/db/migration/V*.sql |
  Sort-Object Name |
  Select-Object -Last 10 -ExpandProperty Name
```

Use the next actual version. Never reserve or reuse a number based only on this plan.

### 7.3 `V091` changes

Forward-only changes to `public.audit_events`:

1. Add `outcome VARCHAR(20)`.
2. Add nullable `severity VARCHAR(20)`.
3. Optionally add nullable sanitized client-context columns only if privacy approval exists.
4. Normalize null metadata to `{}` and make it non-null.
5. Require metadata to be a JSON object.
6. Backfill outcome deterministically:
   - event names ending in `_FAILED`/`_FAILURE` become `FAILED`;
   - denial/rejection events become `DENIED` where unambiguous;
   - completed/succeeded events become `SUCCEEDED`;
   - ambiguous historical attempts become `ATTEMPTED` rather than inventing success.
7. Backfill security severity conservatively and allow explicit service values for new rows.
8. Add controlled checks for outcome and severity.
9. Add indexes:
   - `(event_category, occurred_at DESC, id)`;
   - `(resource_type, resource_id, occurred_at DESC, id)`;
   - `(actor_user_id, occurred_at DESC, id)`;
   - retain existing event-type/time and correlation indexes.
10. Analyze query plans before adding a JSONB GIN index; do not add one without a real supported metadata query.

The migration must never delete or rewrite historical rows beyond safe classification backfills.

### 7.4 Append-only enforcement

Application code exposes insert and read operations only. It must not expose save/update/delete repositories.

Database-level enforcement requires separate roles:

- Flyway/owner role: DDL and controlled retention maintenance;
- runtime application role: `INSERT` and approved `SELECT`, no `UPDATE` or `DELETE`;
- optional restricted reviewer role: bounded `SELECT` only.

Do not add a trigger with a client-settable bypass flag. Until deployment roles are separated, append-only is an application guarantee backed by architecture/integration tests and an explicit production hardening gate.

### 7.5 Retention

No automatic purge is implemented without an approved duration and archive rule. The plan must still prepare for future retention:

- time-ordered indexes;
- documented maintenance ownership;
- batch deletion only by the maintenance role;
- archival verification before purge;
- a separate audit event or operational record for retention jobs;
- tests proving runtime application credentials cannot perform retention.

## 8. Event catalogue and coverage matrix

### 8.1 Security events

Normalize existing strings into controlled event types, including:

```text
AUTH_ADMIN_LOGIN_SUCCEEDED
AUTH_STUDENT_LOGIN_SUCCEEDED
AUTH_LOGIN_FAILED
AUTH_LOGOUT_SUCCEEDED
AUTH_STUDENT_VERIFICATION_STARTED
AUTH_OTP_SENT
AUTH_OTP_VERIFICATION_FAILED
AUTH_OTP_VERIFIED
AUTH_OTP_MAX_ATTEMPTS_REACHED
AUTH_PASSWORD_SETUP_COMPLETED
AUTH_PASSWORD_RESET_STARTED
AUTH_PASSWORD_RESET_REQUEST_INELIGIBLE
AUTH_PASSWORD_RESET_COMPLETED
AUTHORIZATION_DENIED
```

Do not include the attempted password, OTP, JWT, email address, or index number in metadata. Anonymous login failures may use a one-way, rotating keyed fingerprint only if the security/privacy policy approves it; otherwise record no user identifier.

### 8.2 Admin-critical business events

At minimum retain or add controlled types for:

- ledger upload/validation/commit/failure;
- company create/update/delete;
- internship request create/update/delete and required-skill mutation;
- candidate-filter execution;
- shortlist create/add/remove/finalize and failed finalization;
- Admin CV download and unavailable file;
- export job create/complete/fail/download.

Student CV lifecycle and Student download events remain auditable where the BMD/SRS marks them as required or recommended.

### 8.3 Outcome rules

- Do not infer success merely because a method was entered.
- Persist success only after the protected state is ready to commit.
- Persist a required success event in the same transaction as the mutation.
- Failure/denial events must not contain exception messages from JDBC, storage, authentication providers, or PDF tooling.
- Retries use separate event IDs and the same correlation ID where they belong to one request; background retries use job/resource identifiers and a new worker correlation ID.

## 9. Optional Admin read API

### 9.1 Default Version 1 decision

Do not activate `/api/v1/admin/audit-events` in the core BMD-012 release. Internal persistence, restricted database review, and acceptance evidence satisfy the approved mandatory scope without adding an unapproved page or expanding exposure of Restricted data.

### 9.2 If the viewer is approved

Treat it as a separately reviewable patch and first correct OpenAPI v1.6:

- type `PagedAuditEventResponse.items` as `AuditEventResponse`;
- declare required fields and `additionalProperties: false`;
- add bearer security, `x-access-role: ADMIN`, and `x-audit-required: false`;
- define supported filters and sort allow-list;
- decide whether security events are visible, redacted, or excluded;
- prohibit metadata searching that could leak or create unbounded queries.

Recommended endpoint:

```http
GET /api/v1/admin/audit-events?page=0&size=20&search=&category=&eventType=&outcome=&from=&to=&sort=occurredAt,desc
```

Required behavior:

- ADMIN only at controller and service layers;
- maximum page size 100;
- deterministic ordering with ID tie-breaker;
- no raw metadata by default; return a policy-approved projection;
- no event creation/update/delete API;
- audit viewer reads are not recursively audited unless policy explicitly requires it;
- rate limiting and bounded date windows for broad queries.

Only after backend approval should a frontend `audit-events` feature, route, navigation entry, Zod schema, API wrapper, and Playwright suite be added.

## 10. Error handling and observability

### 10.1 Error model

Add or reuse standardized error codes for:

```text
AUDIT_EVENT_INVALID
AUDIT_METADATA_REJECTED
AUDIT_PERSISTENCE_FAILED
AUDIT_QUERY_INVALID              # optional viewer
AUDIT_EVENT_NOT_FOUND            # optional viewer only if detail endpoint is approved
```

Client responses expose a safe message and correlation ID, never SQL or rejected metadata.

### 10.2 Structured logs

Required fields:

```text
correlationId, module=BMD-012, operation, eventType, category,
criticality, outcome, result
```

Identifiers must be omitted or sanitized. Never log the complete metadata JSON.

### 10.3 Metrics

Recommended low-cardinality metrics:

```text
audit.events.persisted{category,outcome,criticality}
audit.events.persistence_failures{criticality}
audit.metadata.rejections{reason_class}
audit.query.duration             # optional viewer
```

Event IDs, actor IDs, resource IDs, emails, index numbers, and correlation IDs must not be metric labels.

### 10.4 Operational health

A required audit write failure naturally fails the protected request. Repeated best-effort failures must create an operations-visible degraded signal. Storage-capacity monitoring and database alerting belong in the deployment runbook; the application must not attempt unsafe local fallback files containing Restricted data.

## 11. Testing strategy

### 11.1 Unit tests

- event-type/category/outcome/severity validation;
- required security severity;
- actor rules for anonymous/system/authenticated events;
- metadata allow-list and secret-key rejection, including nested and mixed-case keys;
- maximum JSON size, string length, collection size, and nesting depth;
- null metadata normalization;
- outcome classification for historical backfill helper if implemented in Java;
- required versus best-effort failure behavior;
- no sensitive content in warning logs.

### 11.2 PostgreSQL/Flyway tests

- empty PostgreSQL database migrates through the latest version;
- upgrade from the pre-BMD-012 `V090` schema preserves every audit row;
- backfill produces only allowed outcome/severity values;
- metadata is non-null JSON object;
- constraints reject invalid outcome, severity, and JSON shape;
- target/category/actor/time queries use bounded deterministic ordering;
- correlation lookup works;
- no update/delete repository is available;
- runtime-role append-only permissions are tested when role separation is introduced;
- migration validation and rerun do not duplicate or delete events.

### 11.3 Transaction tests

For each required critical event family:

- successful mutation and its audit row commit together;
- forced audit insert failure rolls back the business mutation;
- forced business failure does not leave a false success event;
- concurrent mutation produces the correct winning event only;
- retry behavior does not overwrite prior events.

Prioritize ledger commit, internship mutation, shortlist finalization, CV Admin download authorization, and export job lifecycle.

### 11.4 Security tests

- anonymous/Student access cannot query audit data if viewer enabled;
- raw passwords, OTPs, JWTs, authorization headers, cookies, reset tokens, SQL, stack traces, CV content, and filesystem paths never appear in rows or logs;
- malformed metadata cannot bypass nested-key checks;
- oversized metadata is rejected before SQL;
- safe error responses contain correlation IDs;
- audit data is not returned by unrelated endpoints;
- removed company login and removed-scope endpoints remain absent.

### 11.5 Architecture tests

- `shared.audit` contains contracts only and no JDBC/JPA persistence;
- only `modules.auditlog.persistence` writes `audit_events`;
- business modules do not import Audit Log entities/repositories;
- event type strings are controlled, not ad hoc literals;
- `auditlog` remains inactive as an HTTP module for the internal-only release;
- if viewer approved, add `auditlog` to active modules and approve only the documented GET endpoint.

### 11.6 Full regression

```powershell
.\mvnw.cmd test
```

With Docker available, PostgreSQL/Testcontainers tests must run rather than skip. Required GitHub checks:

```text
Backend CI
Code Quality
Dependency Check
```

## 12. Frontend integration and real E2E

### 12.1 Default internal-only release

No new Audit page is created. Use existing real-backend flows:

1. Admin login.
2. Academic Ledger commit.
3. Company or internship request mutation.
4. Candidate filter run.
5. Shortlist finalization.
6. Admin CV download.
7. CSV or bulk-CV export request/download.
8. Student login/password or Student CV action for security/Student evidence.

The E2E harness records the returned correlation ID or request ID, then a backend/PostgreSQL verification step asserts the expected event type, actor class, target, outcome, and timestamp. Do not add a test-only production endpoint to expose audit rows.

### 12.2 Frontend safety checks

- existing error UI displays only safe messages and correlation IDs;
- browser console/network logs do not print bearer tokens or sensitive event metadata;
- download failures remain sanitized;
- retrying a UI action does not create a false success event;
- mocks remain for unit/UI isolation, but release evidence uses `VITE_ENABLE_API_MOCKS=false`;
- no audit viewer navigation is shown unless the optional viewer is approved and implemented.

### 12.3 If viewer is approved

Create a separate frontend branch and feature boundary:

```text
src/features/audit-events/
├── api/
├── components/
├── hooks/
├── pages/
├── schemas/
├── tests/
└── types/
```

Implement server-side pagination/filtering, strict Zod parsing, restricted metadata projection, loading/empty/error states, URL state, accessibility, and real-backend authorization tests. Do not expose a raw JSON metadata inspector.

## 13. Postman/Newman acceptance

Create a version-controlled collection and ignored local environment. Postman validates public workflows; a companion SQL verification script validates internal audit persistence.

### 13.1 Positive workflow collection

- Admin and Student authentication success.
- Academic Ledger commit.
- Company create/update and internship request mutation.
- Candidate filter run.
- Shortlist creation/member change/finalization.
- Admin latest-CV download.
- export job create/poll/download.
- password reset/setup flow using safe local test delivery only.

Assertions include expected status, response contract, safe error shape, and correlation header/body where available.

### 13.2 Negative workflow collection

- failed login;
- invalid/expired OTP without exposing the OTP;
- anonymous and wrong-role protected requests;
- stale `If-Match` mutation;
- failed shortlist finalization;
- unavailable CV/export file;
- invalid request body and UUID;
- dependency failure where safely reproducible.

### 13.3 Companion SQL verification

The release bundle includes read-only SQL that checks:

- each accepted action produced the expected controlled event;
- actor role, resource type/ID, category, outcome, correlation, and time are correct;
- failure paths did not record false success;
- metadata keys contain no forbidden names;
- event rows are append-only and no duplicate persistence model exists.

No password, OTP, token, or production personal data is stored in the collection, environment, SQL, screenshots, or release evidence.

Newman release gate:

```text
0 failed requests
0 failed assertions
```

## 14. Patch sequence

Each patch must be independently reviewable and committed separately. Recheck `develop`, migration numbers, and CI before Patch 1.

| Patch | Scope | Exit gate |
| --- | --- | --- |
| 0 | Contract reconciliation, event inventory, ADR for single-table physical model, policy register, architecture guards | Source matrix approved; no migration/code conflict |
| 1 | `V091` audit schema hardening, backfill, constraints, indexes, Flyway/PostgreSQL tests | Upgrade preserves rows; empty-db migration green |
| 2 | Shared event contract, sink port, Audit Log-owned JDBC adapter, metadata policy | Existing callers compile; unit/repository tests green |
| 3 | Security event normalization, severity/outcome, auth/OTP/authorization coverage | Security and secret-leakage tests green |
| 4 | Admin-critical workflow coverage and transactional failure semantics | Ledger/request/shortlist/CV/export rollback tests green |
| 5 | Observability, counters, degraded-state/runbook, append-only deployment design | Failure signals verified; no PII metric labels |
| 6A | Internal-only contract closure and architecture cleanup | Dedicated BMD-012 PostgreSQL suite and full backend suite green |
| 6B | Optional Admin read API and OpenAPI correction, only if approved | Authorization/query/contract tests green |
| 7 | Frontend real-backend cross-workflow E2E and optional viewer integration if approved | Playwright green with mocks disabled |
| 8 | Postman/Newman, companion SQL, security evidence, and release report | Newman zero failures; all CI checks green |

Patch 6B is not required for the internal-only Version 1 release. It must not be silently bundled into 6A.

## 15. Branch strategy

Create BMD-012 from the latest clean `develop` only after all current feature PRs intended for the baseline are merged:

```powershell
git switch develop
git pull --ff-only origin develop
git status --short
git switch -c feature/audit-system-events
```

Before creating migrations:

```powershell
Get-ChildItem src/main/resources/db/migration/V*.sql |
  Sort-Object Name |
  Select-Object -Last 10 -ExpandProperty Name
```

Use a separate frontend feature branch from its latest clean `develop` for Patch 7. Do not commit frontend changes into the backend repository, and do not mix unrelated working-tree changes into either PR.

Recommended PR sequence:

1. Backend Patches 0–6A as a draft PR with CI running continuously.
2. Optional 6B as a separate PR only after policy approval.
3. Frontend acceptance/integration PR.
4. Release-evidence update after live E2E and Newman pass.

## 16. Definition of Done

BMD-012 is production-ready only when:

- one authoritative audit persistence model exists;
- forward-only migrations preserve existing history;
- event type, category, outcome, severity, actor, target, time, and correlation rules are enforced;
- BMD-012 owns persistence while other modules depend only on shared contracts;
- required audit failure rolls back protected critical mutations;
- best-effort failure is operationally visible;
- metadata is allow-listed, bounded, and demonstrably secret-free;
- security and Admin-critical event coverage matches the BMD/SRS/workflows;
- no update/delete application repository or mutation endpoint exists;
- retention is either formally approved or safely disabled with no automatic purge;
- viewer/export remain absent unless approved;
- backend unit, architecture, security, transaction, migration, and PostgreSQL tests pass;
- real frontend workflows produce verifiable correlated events;
- Postman/Newman has zero failed requests/assertions;
- companion SQL verification passes;
- CI, runbook, and release evidence are green and complete;
- no raw password, OTP, JWT, authorization header, secret, SQL, stack trace, CV content, or filesystem path appears in logs, database rows, tests, or evidence.

## 17. Risks and controls

| Risk | Control |
| --- | --- |
| Creating a second audit persistence model | Extend `public.audit_events`; record the reconciliation ADR |
| Losing existing audit history | Forward-only `ALTER`/backfill; pre/post row-count and checksum tests |
| Secret leakage through flexible JSONB | Event-specific allow-lists, global forbidden-key checks, strict size/depth limits |
| Critical mutation commits without audit | Same-transaction `recordRequired` plus forced-failure tests |
| Best-effort failures remain invisible | Low-cardinality metrics, sanitized warning, degraded operational signal |
| Unbounded viewer queries | Viewer omitted by default; bounded page/date/sort allow-lists if approved |
| Unauthorized audit access | Internal-only default, DB role restriction, defense-in-depth role checks |
| Runtime application modifies history | Insert-only API now; separate DB runtime role before production claim |
| Invented retention destroys evidence | No automatic deletion before written policy approval |
| Test evidence leaks credentials | Ignored environments, sanitized fixtures/logs/screenshots, automated secret scanning |

## 18. Immediate next actions

1. Obtain written decisions for retention, Admin viewer, audit export, and client-context collection.
2. Refresh `origin/develop` and confirm the actual migration ceiling.
3. Create `feature/audit-system-events` from clean, latest `develop`.
4. Add the single-store reconciliation ADR and full event call-site inventory.
5. Implement Patch 1 using the next available Flyway number, expected `V091`.
6. Run focused migration tests, then the complete backend suite.
7. Continue Patches 2–6A without activating an HTTP viewer.
8. Run live frontend E2E, Newman, companion SQL, and security evidence before release closure.

No unresolved curriculum, CV, Candidate Filtering, Shortlist, or Export dependency blocks the core BMD-012 persistence hardening. The only remaining pre-code choices are policy boundaries; the safe defaults above allow implementation to begin without expanding access or deleting evidence.
