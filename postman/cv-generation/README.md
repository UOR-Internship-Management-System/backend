# BMD-007 CV Generation Postman/Newman Acceptance

This collection is a **live acceptance suite** for BMD-007. It must run against a real Spring Boot backend, PostgreSQL database, private CV storage root, and a working XeLaTeX executable. It intentionally does not mock any CV endpoint.

## Preconditions

1. Use a dedicated Student acceptance account and an ADMIN account.
2. The Student Profile must already exist and the Student must be active/registered.
3. Reset the dedicated Student's BMD-007 active-CV state before the run so the suite can prove first-save `If-None-Match: *` semantics. Do not use a real user's account.
4. Backend Flyway must be at **V084**; Batch 3 adds no migration.
5. `xelatex --version` must succeed and `CV_STORAGE_ROOT` must be writable.
6. Install the pinned Newman version: `npm install --global newman@6.2.2`.

## Local environment

```powershell
Copy-Item .\postman\cv-generation\cv-generation.local.template.postman_environment.json `
  .\postman\cv-generation\cv-generation.local.postman_environment.json
```

Fill only local credentials in the copied file. The local file and generated reports are ignored by Git.

## Run

```powershell
.\postman\cv-generation\run-newman.ps1
```

or:

```bash
./postman/cv-generation/run-newman.sh
```

Release gate: **0 failed requests and 0 failed assertions**.

## What is proved

- Student/Admin authentication and role isolation;
- clean `NOT_SAVED` state;
- strict five-array preview validation and generic 422 ownership failure;
- real Preview -> XeLaTeX -> staged PDF;
- 428 missing save precondition;
- 201 first save with strong ETag;
- consumed-preview idempotency;
- persisted Student PDF download and security headers;
- source freshness after a real Profile mutation;
- stale preview 409;
- stale revision 412;
- replacement 200 with monotonic revision;
- restoration of the original Profile summary and final `CURRENT` freshness;
- Admin latest-CV metadata/download resolving the same active CV.

The collection deliberately uses empty optional-selection arrays so it does not depend on pre-created experience/project/certificate/award/activity fixtures. Record-specific selection behavior remains covered by backend tests.
