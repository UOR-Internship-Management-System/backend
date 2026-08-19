# BMD-007 Batch 2 — QA Report

## Baseline

Batch 2 was produced against the supplied **Backend after Batch 1** archive, not the pre-Batch-1 backend.

Expected prerequisite state supplied by the user:

- Batch-1 code applied on `feature/cvBuilder`;
- V083 and V084 successfully applied to local PostgreSQL;
- Hibernate schema validation succeeded;
- current Flyway version v084.

## Patch composition

- Patch 4: ATS XeLaTeX/PDF generation, private staged storage, bounded process execution, preview artifact persistence/cleanup.
- Patch 5: conditional atomic active-CV promotion, ETag/revision/idempotency, source-fingerprint race protection, `system.file_asset` promotion, normalized active selections, orphan cleanup.
- Patch 6: Student PDF download, reusable active/latest-CV read ports, Admin latest-CV metadata/download, read-only Admin integration.

No new Flyway migration is required in Batch 2.

## Static/application QA performed

### Patch replay

Each generated patch was replayed sequentially from a fresh extraction of the supplied post-Batch-1 backend using:

```text
git apply --check
git apply
```

Result:

```text
Patch 4 PASS
Patch 5 PASS
Patch 6 PASS
```

### Whitespace/diff integrity

The project contains CRLF working-copy files from the supplied Windows-oriented ZIP. Diff validation was performed with CR-at-EOL treated correctly:

```text
git -c core.whitespace=cr-at-eol diff --check
```

Result: **PASS**.

### Project-local import resolution

All project-local imports in the changed Java source files were resolved against `src/main/java` / `src/test/java`.

Result:

```text
39 changed Java files
0 missing project-local imports
```

## XeLaTeX/ATS runtime smoke test

The sandbox contains a real `xelatex` and `pdftotext` runtime.

A single-column test document using the same minimal package/command assumptions was compiled with:

```text
-no-shell-escape
-interaction=nonstopmode
-halt-on-error
-file-line-error
```

Result:

```text
XeLaTeX exit code: 0
PDF: valid PDF 1.7
Extracted text: successful
Headings and ordinary text remained searchable/extractable
```

This validates the external-runtime assumptions, but does not replace the project Maven integration tests required on the user's machine.

## Frontend contract guardrails

Against the supplied frontend:

```text
npm run openapi:check  PASS
npm run verify:scope   PASS
```

The existing frontend remains synchronized to OpenAPI v1.6.0 and no Batch-2 frontend source changes were required.

## Tests not executable in this sandbox

### Maven

Attempted:

```text
bash ./mvnw -o -DskipTests compile
```

The supplied Maven wrapper requires Maven 3.9.16 to be downloaded from Maven Central. Network bootstrap is unavailable in this sandbox, so Maven compilation/tests could not be executed here.

This is an environment limitation, not a passing test result. The application instructions therefore require:

```powershell
.\mvnw.cmd test
```

after every patch on the user's machine.

### Full frontend typecheck/test/build

The supplied frontend ZIP does not contain `node_modules`. `npm run typecheck` therefore cannot resolve `vite/client`, `vitest/globals`, or Node type definitions until `npm ci` is run with registry access.

Required locally:

```text
npm ci
npm run typecheck
npm run lint
npm run test
npm run build
```

### PostgreSQL / authenticated E2E

The sandbox does not have the user's local PostgreSQL/authentication fixture. Batch 2 adds no migrations, so the database should remain at v084. Real Student/Admin Preview -> Save -> Download checks are documented in `APPLY_INSTRUCTIONS.md` and must be run locally.

## Security/reliability controls included

- no shell command string construction;
- XeLaTeX `-no-shell-escape`;
- isolated temp directories;
- compilation timeout;
- bounded compiler diagnostics;
- PDF maximum size;
- bounded concurrent compilation semaphore;
- opaque CV-specific storage namespace `cv/objects/...`;
- storage path canonicalization inherited from `LocalFileStorageAdapter`;
- staged size/SHA-256 verification before save;
- active PDF size/SHA-256 verification before Student/Admin download;
- first-save and replacement optimistic preconditions;
- Student-row + freshness-row + preview locking;
- freshness timestamps taken after freshness-row lock to close save/source-change races;
- durable consumed-preview idempotency state;
- consumed-preview retention cleanup;
- post-commit superseded-file cleanup plus scheduled retry;
- Admin latest-CV operations are read-only except required audit insertion;
- no regeneration during download/Admin inspection;
- no raw LaTeX API exposure;
- no historical CV/review/approval workflow introduced.

## Local acceptance gate

Do not mark Batch 2 passed until all of the following succeed locally:

1. `mvnw test` after Patch 4;
2. real XeLaTeX Preview returns 200 and creates a staged non-zero PDF;
3. `mvnw test` after Patch 5;
4. first Save -> 201 + ETag `"1"`;
5. replacement -> 200 + incremented ETag;
6. stale ETag -> 412;
7. stale source preview -> 409;
8. idempotent same-preview retry does not increment revision;
9. `mvnw test` after Patch 6;
10. Student PDF download succeeds with safe headers and verified PDF bytes;
11. Admin metadata resolves the same active CV;
12. Admin PDF download resolves the same active PDF;
13. Student/Admin RBAC checks pass;
14. frontend OpenAPI/scope/typecheck/lint/tests/build pass;
15. Git working tree is clean.
