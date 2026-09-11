# BMD-012 Audit Event Inventory

This inventory records the BMD-012 baseline before persistence ownership is
moved out of `shared.audit`. Event names are normalized in later BMD-012
patches; existing call sites must not be removed during that refactor.

| Capability | Current publisher coverage | Required criticality |
| --- | --- | --- |
| Admin/Student login | success and failure | best effort for attempts |
| Logout | present | best effort |
| Student verification and OTP | start, sent, failed, verified | best effort for attempts; required for completed setup |
| Password setup/reset | start, ineligible, complete | required for completed credential mutation |
| Academic Ledger | processing, validation, commit and failure | required with protected transitions |
| Company Management | create, update, delete | required |
| Internship Requests | create, update, delete, skill mutation | required |
| CV | preview, save, failure, Student/Admin download, unavailable file | required for save/Admin download; best effort for diagnostics |
| Candidate Filtering | run creation | required |
| Shortlists | create, add, remove, finalize | required |
| Exports | create, complete, fail, download | required for persisted lifecycle transitions |

## Known baseline gaps

- Security event names are free-form strings rather than controlled enum values.
- Outcome and security severity are not persisted explicitly.
- JSONB metadata has no central allow-list, size, or forbidden-key policy.
- `AuditEventPublisher` owns JDBC persistence directly.
- Best-effort failure has no metric or degraded operational signal.
- The dedicated Audit Log module has no PostgreSQL acceptance suite.
- Runtime database privileges do not yet enforce append-only access.
- Retention, Admin viewer availability, and audit export remain policy gates.

