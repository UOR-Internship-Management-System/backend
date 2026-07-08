# Api Contract Notes

The approved REST/JSON contract is the OpenAPI 3.1.1 file under `docs/api` and the synchronized runtime copy under `src/main/resources/openapi`.

## Sprint 1 Position

Sprint 1 places and protects the contract but does not implement the full API. Security allows public access to OpenAPI-declared authentication, student verification, and password reset paths so Sprint 2 can add controllers without security-path churn.

## Contract Rules

- Base server path is `/api/v1`.
- Use the standard error model from the contract.
- Do not invent endpoints outside the approved Version 1 scope.
- Do not add company login, admin approval, temporary password, skill verification, CV review, AI ranking, match percentage, or GPA-in-request semantics.
