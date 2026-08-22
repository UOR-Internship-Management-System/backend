# BMD-011 Shortlist Management Backend Implementation Plan

## Document control

| Item | Value |
| --- | --- |
| Module | BMD-011 — Shortlist and Export |
| Core package | `lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists` |
| Export package | `lk.ac.ruhuna.dcs.cvmanagement.modules.exports` |
| Backend | Spring Boot modular monolith |
| Database | PostgreSQL with Flyway |
| Frontend | React, TypeScript, TanStack Query, Zod |
| Canonical transport contract | `CV_Management_API_OpenAPI_v1.6.0.yaml` |
| Plan status | Implementation-ready after prerequisite branch integration |
| Audit date | 2026-08-22 |

## 1. Executive summary

BMD-011 persists and manages Admin-selected internship shortlists. It must allow an Admin to create the single Version 1 shortlist for an internship request, add and remove candidates while the shortlist is a draft, inspect the shortlist using bounded server-side queries, and finalize it atomically. The internship request's configured shortlist value is advisory. Exceeding it requires an explicit acknowledgement but must never prevent finalization.

The canonical OpenAPI contract also places shortlist summary export and bulk latest-saved CV export in BMD-011. This plan therefore separates delivery into two controlled slices:

1. **Shortlist lifecycle:** the six endpoints requested for create, list, detail, candidate mutation and finalization.
2. **Export closure:** asynchronous CSV summary and ZIP latest-saved-CV exports required to call the complete BMD-011 module finished.

The current backend has only empty Shortlists scaffolding and no shortlist tables. The current frontend already contains strict shortlist/export transport schemas, API wrappers, TanStack Query hooks, mocks, a Candidate Filtering handoff and finalized-shortlist screens. It still needs real-backend verification and a deliberate decision about whether draft review/removal/finalization remains in Candidate Filtering or is also exposed on the Shortlists page.

At the time of this audit, the latest migration on `origin/develop` is `V084`. The expected first BMD-011 migration is therefore `V085`, but the implementation branch must re-check after updating from `origin/develop`. Never reuse a migration version that exists in the final branch baseline.

## 2. Audited sources and authority

### 2.1 Mandatory sources audited

- Backend Module Document v1.0.
- Database Design Document v1.0.
- Software Requirements Specification v3.0.1.
- Student and Admin Workflow Document v1.0.
- Final Reduced Scope Baseline Document v1.1.
- Backend Folder Structure Implementation Plan v1.0.
- `CV_Management_API_OpenAPI_v1.6.0.yaml`.

### 2.2 Supporting sources audited

- Production-Ready Use Case Document v1.0.
- Admin workflow/page specification.
- UI Frontend Specification v1.1.
- Frontend Folder Structure Implementation Plan.
- Scope-reduction document.
- OpenAPI generated-client notes.
- Sprint 7–8 contract traceability matrix and decision register in the frontend.
- Original Admin page `08_shortlisted_page.html`.
- Current backend source, migrations, architecture tests and BMD-010 branch state.
- Current frontend routes, shortlist/filter/export feature code, runtime schemas, mocks, unit tests and Playwright tests.

### 2.3 Explicit exclusion

The outdated API Specification Document was not used. When an older document conflicts with OpenAPI v1.6, the reduced-scope baseline, SRS v3.0.1 or the current production codebase, it must not control the implementation.

### 2.4 Source-of-truth order used by this plan

1. Final Reduced Scope Baseline and explicit user decisions.
2. SRS v3.0.1.
3. Database Design and Backend Module documents.
4. OpenAPI v1.6 for exact HTTP transport behavior.
5. Workflow and Use Case documents.
6. Current production backend and frontend conventions.
7. Historical wireframes only for presentation intent.

## 3. Locked scope

### 3.1 Required shortlist responsibilities

- Create one draft shortlist for an internship request.
- List shortlists with server-side search, filtering, sorting and pagination.
- View a shortlist and a separately paged candidate collection.
- Add up to 100 manually selected Students in one request.
- Report already-present candidates without creating duplicates.
- Remove a candidate while the shortlist is a draft.
- Calculate and expose selected count and advisory guidance state.
- Require explicit acknowledgement only when guidance is exceeded.
- Finalize a non-empty shortlist atomically.
- Make candidates immutable after finalization.
- Prevent stale, repeated and concurrent mutations/finalization.
- Emit sanitized audit events for create, candidate mutation and finalization.
- Provide factual cross-shortlist counts to BMD-010.
- Make finalized shortlist counts available to Student/Admin dashboard consumers through module-owned read ports.

### 3.2 Required primary endpoints

All paths are under `/api/v1`.

```text
GET    /admin/shortlists
POST   /admin/shortlists
GET    /admin/shortlists/{shortlistId}
POST   /admin/shortlists/{shortlistId}/candidates
DELETE /admin/shortlists/{shortlistId}/candidates/{studentId}
POST   /admin/shortlists/{shortlistId}/finalize
```

### 3.3 Full BMD-011 export closure

OpenAPI v1.6 additionally freezes these Admin-only endpoints:

```text
POST /admin/exports/shortlists/{shortlistId}
GET  /admin/exports/{exportJobId}
GET  /admin/exports/{exportJobId}/download
POST /admin/exports/shortlists/{shortlistId}/bulk-cvs
GET  /admin/exports/{exportJobId}/bulk-cvs/download
```

Summary export is asynchronous CSV. Bulk CV export is an asynchronous ZIP containing available active/latest-saved ATS-compliant PDF CVs. Missing CVs must be reported explicitly; a shortlist is not rolled back when export fails.

### 3.4 Explicit non-goals

- No automated candidate selection.
- No AI score, match percentage, probability, recommendation, weight or rank.
- No project-based eligibility.
- No verified-skill state.
- No GPA persisted in internship requests.
- No hard shortlist capacity rule.
- No CV review, approval, rejection or correction workflow.
- No company login or company-facing shortlist endpoint.
- No candidate mutation after finalization.
- No second parallel shortlist persistence model.
- No frontend-only shortlist state in the production flow.

## 4. Current-state audit

### 4.1 Backend

- `modules/shortlists` exists, but its controller, DTOs, services, entities, repositories, mapper and status type are empty reserved scaffolds.
- `ModuleDependencyRulesTest` already identifies `shortlists` as an active module name, so implementation must comply with existing modular boundaries.
- BMD-009 is implemented with physical tables in the PostgreSQL `public` schema:
  - `companies`
  - `internship_requests`
  - `internship_request_skills`
- `internship_requests.shortlist_guidance_value` already exists with a `0..10000` constraint.
- BMD-010 patches 1–5 exist on `feature/candidate-filtering`, including `V070` and `V071`, but its public candidate response deliberately fails closed until authoritative CV and shortlist enrichment are available.
- BMD-007 is present on current `origin/develop` through migrations `V080`–`V084`.
- Shared foundations already exist for:
  - `CurrentActorProvider` and ADMIN role enforcement;
  - `AuditEventPublisher`;
  - standardized error responses;
  - `IfMatchSupport` with quoted integer ETags;
  - JPA `@Version` optimistic locking;
  - bounded pagination helpers.
- No shortlist or export tables currently exist.
- Current maximum `origin/develop` migration: `V084`; expected next version: `V085`.

### 4.2 Frontend

The frontend already contains:

- `/admin/shortlists` route and Admin navigation entry.
- `shortlistsApi.ts` implementing all six requested shortlist calls.
- Quoted `If-Match` headers for candidate add/remove and finalization.
- Strict Zod schemas matching OpenAPI v1.6.
- TanStack Query list/detail/mutation hooks with stale-state recovery for `412` and `428`.
- Candidate Filtering selection state and a review modal that performs:
  - create draft;
  - batch add candidates;
  - guidance acknowledgement;
  - finalization;
  - retry/recovery handoff to Shortlists.
- Mock Service Worker handlers for the draft lifecycle.
- Unit tests for API contracts and shortlist presentation.
- Playwright coverage for authorization and finalized shortlist/export presentation.

Identified frontend gaps and constraints:

- The current Shortlists page requests `status=FINALIZED` and intentionally hides draft removal/finalization controls.
- Candidate Filtering is therefore the current mutation/finalization UI, while Shortlists is the finalized review/export UI.
- The final implementation must verify that this division satisfies the workflow. If draft recovery must be possible after a browser restart or failed finalization, the Shortlists page needs a draft review workspace using the already-existing mutation hooks.
- The audited frontend working tree is on `feature/intro-page-enhancements` with unrelated local changes. BMD-011 work must use a clean feature branch from the latest frontend `develop` and must not include those changes.

## 5. Prerequisites and branch strategy

### 5.1 Required baseline

Do not start BMD-011 implementation from the current paused Candidate Filtering branch. First ensure:

1. BMD-007 is merged and green on `develop`.
2. BMD-009 Company/Internship is merged and green.
3. BMD-010 patches 1–5 are reconciled with the latest `develop`, tested and merged, because Shortlist creation may reference `candidate_filter_runs`.
4. The full backend CI baseline is green.
5. The latest migration number is re-audited.

### 5.2 Backend branch

```powershell
git switch develop
git pull --ff-only origin develop
git status --short
git switch -c feature/shortlist-management
```

The working tree must be clean before branching. If the newest migration remains `V084`, begin at `V085`. If another migration has landed, use the next actual version.

### 5.3 Frontend branch

Create a separate clean frontend branch from its latest `develop`, for example:

```powershell
git switch develop
git pull --ff-only origin develop
git switch -c feature/shortlist-management
```

Do not copy the entire current frontend directory into the backend and do not mix unrelated intro-page work into the shortlist PR.

## 6. Architecture and module boundaries

### 6.1 Package structure

```text
modules/shortlists/
├── api/
│   ├── ShortlistController.java
│   └── dto/
│       ├── request/
│       │   ├── ShortlistCreateRequest.java
│       │   ├── ShortlistCandidateRequest.java
│       │   └── ShortlistFinalizeRequest.java
│       └── response/
│           ├── ShortlistResponse.java
│           ├── ShortlistDetailResponse.java
│           ├── ShortlistCandidateResponse.java
│           ├── ShortlistCandidateMutationResponse.java
│           └── ShortlistFinalizeResponse.java
├── application/
│   ├── ShortlistService.java
│   ├── ShortlistQueryService.java
│   ├── ShortlistFinalizationService.java
│   ├── model/
│   └── port/
│       ├── ShortlistInternshipLookup.java
│       ├── ShortlistFilterRunLookup.java
│       ├── ShortlistStudentLookup.java
│       ├── ShortlistAcademicLookup.java
│       └── ShortlistCvLookup.java
├── domain/
│   ├── exception/
│   └── policy/
│       └── ShortlistStatus.java
├── mapper/
│   └── ShortlistMapper.java
└── persistence/
    ├── entity/
    │   ├── ShortlistEntity.java
    │   └── ShortlistCandidateEntity.java
    ├── projection/
    ├── query/
    │   └── ShortlistReadRepository.java
    └── repository/
        ├── ShortlistRepository.java
        └── ShortlistCandidateRepository.java
```

### 6.2 Boundary rules

- Shortlists owns shortlist and candidate writes.
- It must not directly import another module's repository, entity or public API DTO.
- Cross-module reads use Shortlists-owned ports and small internal records.
- The infrastructure adapter may query authoritative tables with bounded SQL where that matches existing architecture conventions.
- BMD-010 consumes shortlist facts through a Shortlists-owned read port; it must not query `ShortlistRepository` directly.
- Export orchestration belongs in `modules/exports`; it reads finalized shortlist data and CV file metadata through ports.
- Audit remains cross-cutting through the existing shared publisher.

## 7. Domain model and lifecycle

### 7.1 Version 1 cardinality

There is exactly one shortlist resource per internship request. A database unique constraint enforces this. Duplicate creation returns `409` and must never create another row.

### 7.2 Statuses

The shortlist API exposes only:

```text
DRAFT → FINALIZED
```

`FINALIZED` is terminal in the six-endpoint shortlist lifecycle. Database Design mentions additional conceptual states, but OpenAPI v1.6 freezes `DRAFT` and `FINALIZED` for this API. Do not expose undocumented status values.

### 7.3 Draft behavior

- Creation never auto-adds filtering results.
- An optional `filterRunId` records provenance, but each candidate remains an explicit Admin choice.
- Candidate add is batch-based, unique and idempotent with respect to already-present Students.
- Candidate removal is permitted only in `DRAFT`.
- Every membership mutation advances the shortlist version exactly once.
- A stale or missing version precondition is rejected before mutation.

### 7.4 Guidance behavior

```text
guidanceExceeded = guidanceValue != null
                   AND selectedCandidateCount > guidanceValue
```

- Guidance is advisory only.
- Within or below guidance, acknowledgement is not required.
- Above guidance, `acknowledgeGuidanceWarning=true` is required.
- Acknowledgement permits finalization; no maximum-count validation may reject it.
- The guidance value used for finalization must be stable and traceable. Snapshot the request's guidance value when the shortlist is created so a later request edit cannot silently rewrite shortlist history.

### 7.5 Finalization rules

- ADMIN actor required.
- `If-Match` required.
- Shortlist must be `DRAFT`.
- At least one persisted candidate is required.
- All selected Students must still reference valid Student identity rows.
- Guidance acknowledgement must be consistent with the snapshot and selected count.
- Status, acknowledgement, finalizer, note, timestamp, version and audit event are committed in one transaction.
- A repeated or concurrent finalization returns `409` or `412` according to whether the state or version is the conflict. It must not silently succeed as a second finalization.
- Candidate rows become immutable after commit.

## 8. Database implementation

### 8.1 Physical naming decision

The design documents use conceptual names such as `internship.shortlist`, but the merged BMD-009 implementation physically owns `public.companies` and `public.internship_requests`. To avoid creating a second parallel Internship persistence model, BMD-011 should extend the established physical model:

```text
public.shortlists
public.shortlist_candidates
```

Do not create duplicate `internship.internship_request` or company tables. If the team decides to normalize all Internship tables into a dedicated schema, that must be a separate forward-only reconciliation project.

### 8.2 Expected migrations

Assuming `V084` remains the latest migration:

```text
V085__create_shortlist_tables.sql
V086__add_shortlist_query_and_concurrency_indexes.sql
```

For full export closure:

```text
V087__create_export_job_tables.sql
V088__add_export_job_indexes_and_constraints.sql
```

The final numbers are assigned only after refreshing the implementation branch.

### 8.3 `shortlists`

Recommended columns and constraints:

| Column | Type | Rule |
| --- | --- | --- |
| `id` | UUID | PK, `gen_random_uuid()` |
| `internship_request_id` | UUID | NOT NULL, FK to `internship_requests`, UNIQUE |
| `filter_run_id` | UUID | nullable FK to `candidate_filter_runs`, `ON DELETE SET NULL` |
| `name` | VARCHAR(200) | nullable, non-blank when present |
| `status` | VARCHAR(20) | NOT NULL, `DRAFT` or `FINALIZED` |
| `guidance_value_snapshot` | INTEGER | nullable, `0..10000` |
| `guidance_warning_acknowledged` | BOOLEAN | NOT NULL default false |
| `finalization_note` | VARCHAR(1000) | nullable |
| `created_by_account_id` | UUID | NOT NULL FK to `user_accounts`, RESTRICT |
| `finalized_by_account_id` | UUID | nullable FK to `user_accounts`, RESTRICT |
| `version` | BIGINT | NOT NULL default 0, non-negative |
| `created_at` | TIMESTAMPTZ | NOT NULL |
| `updated_at` | TIMESTAMPTZ | NOT NULL |
| `finalized_at` | TIMESTAMPTZ | nullable |

Database checks must enforce consistency between `status`, finalization actor/time and acknowledgement metadata where possible.

### 8.4 `shortlist_candidates`

| Column | Type | Rule |
| --- | --- | --- |
| `id` | UUID | PK, `gen_random_uuid()` |
| `shortlist_id` | UUID | NOT NULL FK to `shortlists`, CASCADE |
| `student_id` | UUID | NOT NULL FK to `eligible_students`, RESTRICT |
| `selected_by_account_id` | UUID | NOT NULL FK to `user_accounts`, RESTRICT |
| `selected_at` | TIMESTAMPTZ | NOT NULL |
| `selection_note` | VARCHAR(1000) | nullable |

Required uniqueness:

```sql
UNIQUE (shortlist_id, student_id)
```

No score, rank, weight, probability or match field may exist.

### 8.5 Indexes

- Unique index on `shortlists(internship_request_id)`.
- List index on `(status, updated_at DESC, id)`.
- Company/list joins rely on existing request/company indexes and may add a verified request FK index if missing.
- `shortlist_candidates(shortlist_id, selected_at, id)`.
- `shortlist_candidates(student_id, shortlist_id)` for cross-shortlist counts and dashboard metrics.
- Optional case-insensitive name search index only if the final query plan proves it useful.
- All list queries must include deterministic UUID tie-breakers.

### 8.6 Delete and retention rules

- A finalized shortlist is retained.
- Internship request deletion must be rejected when a shortlist exists unless the already-approved request lifecycle explicitly permits safe draft cascade. Current implementation must not cascade-delete finalized history.
- Candidate identity deletion remains restricted while shortlist history exists.
- Draft shortlist removal is not part of OpenAPI v1.6 and must not be invented.

## 9. API contract

### 9.1 List

```http
GET /api/v1/admin/shortlists?page=0&size=20&search=&sort=updatedAt,desc&status=FINALIZED&companyId={uuid}
```

- ADMIN only.
- Candidate arrays are not embedded.
- Supported sort values: `updatedAt,desc`, `createdAt,desc`, `companyName,asc`, `roleTitle,asc`.
- Bounded page sizes and escaped search.

### 9.2 Create

```json
{
  "requestId": "uuid",
  "filterRunId": "uuid-or-null",
  "name": "Optional shortlist name"
}
```

- Returns `201`, `Location`, `ETag` and `ShortlistResponse`.
- Validates request, optional run ownership/context and one-shortlist-per-request uniqueness.
- Does not add candidates.

### 9.3 Detail

```http
GET /api/v1/admin/shortlists/{shortlistId}?candidatePage=0&candidateSize=100&candidateSearch=&sort=officialGpa,desc
```

- Returns shortlist summary plus a separately paged candidate collection.
- Candidate sort values: `officialGpa,desc`, `officialGpa,asc`, `fullName,asc`, `indexNumber,asc`.
- Returns `ETag` for subsequent mutations.
- Candidate facts come from authoritative current Student/GPA/CV data plus persisted selection metadata.

### 9.4 Add candidates

```json
{
  "studentIds": ["uuid"],
  "note": "Optional note"
}
```

- Requires quoted `If-Match` version.
- One to 100 unique IDs.
- Only `DRAFT` may mutate.
- Response reports `addedCount`, `alreadyPresentCount`, total selected count, guidance state and new version.

### 9.5 Remove candidate

- Requires quoted `If-Match` version.
- Only `DRAFT` may mutate.
- A missing candidate should be idempotent with `removedCount=0` unless OpenAPI error semantics are explicitly revised before coding.
- Response returns new selected count, guidance state and version.

### 9.6 Finalize

```json
{
  "acknowledgeGuidanceWarning": true,
  "finalizationNote": "Optional note"
}
```

- Requires quoted `If-Match` version.
- Returns `200`, updated `ETag`, status `FINALIZED`, counts, guidance state, acknowledgement, version and timestamp.
- Empty shortlists return validation failure.
- Guidance exceedance without acknowledgement returns the documented `409` guidance-acknowledgement error.

### 9.7 Error mapping

| Condition | HTTP |
| --- | --- |
| Invalid query, UUID or malformed request | 400 |
| Unauthenticated | 401 |
| Non-Admin | 403 |
| Missing shortlist/request/filter run | 404 |
| Duplicate shortlist or invalid lifecycle state | 409 |
| Guidance exceeded without acknowledgement | 409 |
| Stale `If-Match` | 412 |
| Unsupported media type | 415 |
| Semantically invalid body | 422 |
| Missing `If-Match` | 428 |
| Rate limit | 429 |
| Dependency unavailable | 503 |

All errors use the project's standardized problem response and correlation ID. No SQL, stack trace or sensitive content is exposed.

## 10. Concurrency and transaction design

### 10.1 Candidate mutation

Within one transaction:

1. Load shortlist with its version and state.
2. Validate `If-Match` and `DRAFT` status.
3. Validate all Student IDs in one bounded lookup.
4. Insert only missing `(shortlist_id, student_id)` pairs.
5. Touch the parent shortlist exactly once so `@Version` advances once.
6. Persist audit event.
7. Commit and return committed counts/version.

The unique key remains the final duplicate-race protection.

### 10.2 Finalization

Use a transaction plus pessimistic row lock on the shortlist or an atomic conditional update guarded by ID, status and version. A safe sequence is:

1. `SELECT ... FOR UPDATE` the shortlist.
2. Verify version and `DRAFT` state.
3. Count candidates within the same transaction.
4. Reject empty candidate set.
5. Compare count to guidance snapshot.
6. Require acknowledgement only when exceeded.
7. Write final state and audit event.
8. Commit.

Two concurrent finalizers must yield exactly one success. The other must receive `409` or `412` and no second audit-success record.

### 10.3 Read consistency

- Detail responses should use a read-only transaction and stable pagination.
- Candidate count, guidance state and version must be calculated from a consistent database view.
- Frontend mutation success invalidates both shortlist lists and detail queries.
- No optimistic UI finalization; UI waits for confirmed backend success.

## 11. Security and audit

### 11.1 Authorization

- Every endpoint requires JWT authentication and `ADMIN` role.
- Service-level authorization is mandatory even if Spring Security guards the controller.
- Student and company actors cannot access shortlist APIs.
- File downloads re-check Admin authorization at retrieval time.

### 11.2 Audit events

At minimum:

```text
SHORTLIST_CREATED
SHORTLIST_CANDIDATES_ADDED
SHORTLIST_CANDIDATE_REMOVED
SHORTLIST_FINALIZED
SHORTLIST_FINALIZATION_FAILED
SHORTLIST_SUMMARY_EXPORT_REQUESTED/COMPLETED/FAILED
BULK_CV_EXPORT_REQUESTED/COMPLETED/FAILED
```

Audit metadata may include actor account ID, shortlist ID, request ID, affected counts, version, guidance exceeded/acknowledged and outcome. It must not contain JWTs, raw CV contents, passwords, OTPs, SQL or filesystem paths.

## 12. BMD-010 integration

BMD-011 must provide an internal read contract that returns, for a bounded collection of Student IDs:

```text
studentId
existingActiveShortlistCount
hasExistingActiveShortlist = count > 0
```

For Version 1, define “existing active shortlist” explicitly in code and tests as membership in a retained shortlist for a different internship request. The recommended decision is to count both `DRAFT` and `FINALIZED` because both represent an active Admin selection and the warning is decision support, not an eligibility rule. Exclude the current request's shortlist when rendering that shortlist's own candidate details to avoid self-warning.

Once this read port exists, Candidate Filtering Patch 6 can combine:

- deterministic core results from BMD-010;
- active/latest-saved CV availability from BMD-007;
- factual cross-shortlist counts from BMD-011.

This replaces the current deliberate `503` dependency gate without changing the public OpenAPI response.

## 13. Test strategy

### 13.1 Unit tests

- Guidance null, below, equal and above selected count.
- Acknowledgement required only above guidance.
- Empty finalization rejected.
- DRAFT/FINALIZED transition policy.
- Candidate request validation, duplicate ID validation and note limits.
- Sort/query allow-lists.
- DTO consistency invariants.
- No hard guidance cap and no automated selection fields.

### 13.2 Repository/PostgreSQL tests

- Flyway applies from an empty PostgreSQL database.
- One shortlist per request constraint.
- Duplicate candidate constraint.
- FK behavior for request, filter run, Student and Admin account.
- Finalization metadata/status checks.
- Query pagination, deterministic ordering, escaped search and company/status filters.
- Cross-shortlist count query with exclusion of current request.
- Candidate GPA/CV enrichment from authoritative sources.
- Migration rerun/validation and schema-history baseline.

### 13.3 Application integration tests

- Create → add → detail → remove → add → finalize happy path.
- Create never auto-selects filter results.
- Duplicate add returns already-present count.
- Finalized membership is immutable.
- Guidance exceeded + false acknowledgement fails without state change.
- Guidance exceeded + true acknowledgement succeeds.
- Guidance value zero remains guidance, not a hard block.
- Two concurrent creation attempts produce one shortlist.
- Two concurrent add operations do not duplicate candidates.
- Two concurrent finalizations produce exactly one success.
- Stale `If-Match` produces `412`; missing header produces `428`.
- Forced failure rolls back finalization and successful audit state.
- Non-Admin and anonymous access rejected.
- BMD-010 enrichment returns factual CV/shortlist fields.

### 13.4 HTTP/OpenAPI contract tests

- Paths, methods, operation IDs, request/response shapes and status codes match v1.6.
- `ETag`, `Location` and required `If-Match` behavior.
- Invalid UUID mapping is consistent.
- Candidate list is not embedded in list rows.
- Unknown JSON properties are rejected where strict DTO behavior is required.

### 13.5 Full regression

```powershell
.\mvnw.cmd test
```

With Docker available, PostgreSQL/Testcontainers tests must execute rather than skip. CI must pass Backend CI, Code Quality and Dependency Check.

## 14. Frontend integration and real E2E

### 14.1 Reuse before rewrite

Reuse the existing:

- API wrapper and transport types;
- Zod schemas;
- TanStack Query hooks and keys;
- Candidate Filtering review modal;
- stale-version recovery;
- Shortlists finalized review workspace;
- export hooks and download safety utilities.

### 14.2 Required frontend changes

1. Sync the canonical OpenAPI checksum/types with the backend copy.
2. Replace MSW-only assumptions with real backend behavior.
3. Ensure create/add/finalize uses the version returned by the immediately preceding operation.
4. Preserve the draft checkpoint after partial success long enough to prevent duplicate creation/add on retry.
5. Add a recoverable draft review UI if a persisted draft can remain after page refresh or failed finalization.
6. Show a non-blocking guidance warning and explicit acknowledgement.
7. Show clear `409`, `412`, `428`, `404` and `503` recovery states.
8. Refresh list/detail/filter candidate data after finalization.
9. Do not show rank, score, match percentage or automatic-selection language.

### 14.3 Live Playwright acceptance

Run against real Spring Boot and PostgreSQL with mocks disabled. Required scenarios:

- Anonymous Admin shortlist route redirects to login.
- Admin creates a filtering run, manually selects candidates, creates a draft, adds candidates and finalizes.
- Guidance-exceeded shortlist finalizes only after acknowledgement.
- Duplicate shortlist creation shows recovery guidance.
- Stale-version mutation reloads latest state.
- Finalized shortlist is visible after browser refresh.
- Finalized candidates cannot be removed.
- Cross-shortlist warning is factual and non-blocking.
- Export slice: CSV and bulk CV job status/download behavior, including missing CV reporting.

Record screenshots/traces only after removing tokens and personal data.

## 15. Postman/Newman acceptance

Create a version-controlled collection and environment with secrets ignored. The collection must cover:

### Positive flow

1. Admin login.
2. Find/create company and internship request with guidance.
3. Create candidate-filtering run.
4. Create draft shortlist.
5. Batch-add candidates using returned `ETag`.
6. Retrieve detail and validate counts.
7. Remove and re-add a candidate using the newest `ETag` each time.
8. Finalize with correct acknowledgement.
9. Reload and prove `FINALIZED` persistence.
10. Export slice: request CSV/ZIP jobs, poll, download and validate headers/content.

### Negative flow

- Student token and anonymous access.
- Invalid UUID and invalid pagination/sort.
- Missing request/filter run/Student.
- Duplicate shortlist creation.
- Duplicate candidate input.
- Missing and stale `If-Match`.
- Mutation/finalization after finalization.
- Empty shortlist finalization.
- Guidance exceeded without acknowledgement.
- Zero available CVs and partial missing-CV export.

Newman release gate:

```text
0 failed requests
0 failed assertions
```

Credentials, tokens and local IDs remain in an ignored environment file and are never committed.

## 16. Patch sequence

Each patch is independently reviewable and receives its own commit(s). Do not squash unrelated backend/frontend work together.

| Patch | Scope | Exit gate |
| --- | --- | --- |
| 0 | Baseline reconciliation: latest `develop`, BMD-010 integration, OpenAPI sync and architecture guards | Clean branch, migrations re-numbered, full baseline green |
| 1 | PostgreSQL shortlist tables, constraints, indexes and Flyway tests | Empty-db migration and schema tests pass |
| 2 | Domain policy, entities, repositories, ports, validation, DTOs and errors | Unit and repository tests pass |
| 3 | Create/list/detail and bounded read model | HTTP, query and security tests pass |
| 4 | Candidate batch add/remove, ETag/If-Match and audit | Duplicate/race/stale-version tests pass |
| 5 | Atomic finalization, guidance acknowledgement and concurrency hardening | Forced rollback and concurrent finalization tests pass |
| 6 | BMD-010/BMD-007 enrichment and dashboard read ports | Candidate endpoint no longer fails closed; factual enrichment tests pass |
| 7 | Export-job persistence and asynchronous CSV/ZIP implementation | Export security, retry, missing-CV and recovery tests pass |
| 8 | Frontend integration and live Playwright | Real-backend E2E green with mocks disabled |
| 9 | Postman/Newman, release evidence, runbook and final regression | Newman zero failures and all CI checks green |

If the user elects to deliver only the six primary shortlist endpoints first, Patches 0–6 plus the shortlist portion of Patch 8/9 form the core release. Full BMD-011 remains open until Patch 7 and export acceptance are complete.

## 17. Definition of Done

BMD-011 is production-ready only when:

- authoritative shortlist tables and constraints are in PostgreSQL;
- Flyway numbering is collision-free and baseline tests are current;
- all six primary endpoints match OpenAPI v1.6;
- one shortlist per request is enforced;
- candidate selection is manual and duplicate-safe;
- draft candidate membership is mutable and finalized membership is immutable;
- guidance exceedance warns and requires acknowledgement but never hard-blocks;
- concurrent finalization yields one success only;
- audit events are persisted without sensitive data;
- BMD-010 receives factual CV and cross-shortlist enrichment;
- server-side pagination/search/sort is bounded and indexed;
- full backend and PostgreSQL integration tests pass;
- frontend live E2E passes against real PostgreSQL-backed APIs;
- Postman/Newman reports zero failed requests/assertions;
- release evidence and runbook are updated;
- export endpoints pass if the complete BMD-011 module is being closed;
- no removed-scope behavior is present.

## 18. Known risks and pre-code decisions

### Locked by this audit

- One shortlist per internship request.
- Only `DRAFT` and `FINALIZED` in the public API.
- Manual selection only.
- Guidance is non-blocking after explicit acknowledgement.
- Quoted integer ETag/If-Match concurrency.
- Candidate membership immutable after finalization.
- Existing shortlist status is factual decision support, not eligibility.
- Current physical Internship persistence must be extended, not duplicated.

### Confirm before Patch 1

1. Whether the team accepts `public.shortlists`/`public.shortlist_candidates` as the physical continuation of merged BMD-009. This plan recommends yes.
2. Whether an “active shortlist” count includes both DRAFT and FINALIZED. This plan recommends yes, with the current request excluded for self-context.
3. Whether draft recovery controls are added to `/admin/shortlists` or remain exclusively in the Candidate Filtering handoff. Production recovery after a partial workflow strongly favors adding draft review controls.
4. Whether export closure is delivered in the same PR series or immediately after the six-endpoint shortlist core.

No other unresolved business data is required to begin shortlist persistence after the prerequisite branches are merged.
