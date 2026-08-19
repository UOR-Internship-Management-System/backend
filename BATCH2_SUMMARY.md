# BMD-007 Batch 2 Summary

## Patch 4

Implements the exact PDF candidate during Preview:

- canonical `CvDocumentModel` -> ATS LaTeX;
- XeLaTeX through `ProcessBuilder` (no shell);
- `-no-shell-escape`;
- timeout, output-size and concurrency bounds;
- private qualified CV storage root;
- opaque `cv/objects/...` staged keys;
- staged size/SHA-256 metadata;
- durable preview persistence and cleanup;
- Docker XeLaTeX runtime.

## Patch 5

Promotes the already-previewed PDF atomically:

- `If-None-Match: *` first-create contract;
- `If-Match: "revision"` replacement contract;
- 201/200 + strong ETag;
- 428/412/409 behavior;
- stable one-active-CV row;
- monotonic revision;
- Student/freshness/preview locking;
- current source fingerprint revalidation;
- `system.file_asset` promotion;
- normalized selection snapshot promotion;
- durable consumed-preview idempotency;
- source freshness timestamp race fix;
- post-commit and scheduled orphan cleanup.

## Patch 6

Completes current saved-CV consumption:

- `GET /api/v1/me/cv/download`;
- byte size/checksum/PDF signature verification;
- safe PDF response headers;
- `LatestSavedCvQuery` internal read boundary;
- batch-capable latest-CV lookup for future BMD-011;
- `ActiveCvFileResolver` internal file boundary;
- Admin latest-CV metadata;
- Admin latest-CV download;
- Admin and Student download audit events;
- removes obsolete CV-history/download placeholder DTOs.

## Database

No migration is added. Current database remains **v084**.
