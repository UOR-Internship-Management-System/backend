# Contract Testing

The active OpenAPI contract is stored in both documentation and runtime resource locations:

- `docs/api/CV_Management_API_OpenAPI_v1.6.0.yaml`
- `src/main/resources/openapi/CV_Management_API_OpenAPI_v1.6.0.yaml`

## Contract Sync Check

`scripts/validate-openapi.sh` verifies both v1.6.0 files exist, are byte-for-byte synchronized,
and advertise the expected OpenAPI/version headers.

Older versioned OpenAPI files may remain as historical artifacts, but they are not implementation
authority for new backend work.

## Rules

- Implement new endpoints against the canonical v1.6.0 contract.
- Keep public-path security aligned with OpenAPI `security: []` operations.
- Use approved DTOs, status codes, errors, pagination, filtering, and RBAC.
- Do not use the outdated API Specification Document to override v1.6.0 behavior.
