# Contract Testing

The OpenAPI contract is stored in both documentation and runtime resource locations:

- `docs/api/CV_Management_API_OpenAPI_v1.1.yaml`
- `src/main/resources/openapi/CV_Management_API_OpenAPI_v1.1.yaml`

## Sprint 1 Check

`scripts/validate-openapi.sh` verifies both files exist and are byte-for-byte synchronized.

## Rules

- Do not change API semantics during Sprint 1 closure hardening.
- Keep public path security aligned with OpenAPI `security: []` operations.
- Future endpoint implementation must use DTOs, status codes, errors, pagination, filtering, and RBAC from the approved contract.
