# Removed Scope Guardrails

The reduced-scope baseline and SRS remove several legacy workflows. Sprint 1 code, tests, docs, seed data, and comments must not present these as supported features.

## Forbidden Features

- Admin student approval workflow.
- Pending or rejected student registration lifecycle.
- Temporary passwords.
- Admin Skill Master page or admin skill CRUD/import/upload.
- Skill verification or verified skill status.
- Estimated GPA planner.
- CV submission to admin, review, approval, rejection, or correction.
- Company portal, company login, or company API role.
- AI scoring, AI ranking, match percentage, or automated final selection.
- Project approval or verification.
- Hard shortlist capacity blocking.
- GPA stored in internship request records.

## Sprint 1 Enforcement

Architecture tests and repository scans should fail when implementation packages expose removed-scope class names, endpoint names, request mappings, or future business behavior. Documentation may mention removed scope only as a guardrail.
