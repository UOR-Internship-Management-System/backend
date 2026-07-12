# Sprint 2 Backend & Database Implementation Plan — Complete Authentication

**Project:** CV Management and Deterministic Internship Candidate Filtering System  
**Plan type:** Backend and database implementation plan  
**Sprint:** Sprint 2 — Complete Authentication  
**Target stack:** Spring Boot modular monolith, Java LTS, PostgreSQL, Flyway, REST/JSON, JWT/RBAC, OTP/email service, audit/security event hooks  
**Status:** Implementation-ready planning document  
**Prepared role perspective:** Senior Solutions Architect / Technical Lead  

---

## 1. Purpose

This document defines the backend and database implementation plan for Sprint 2 Complete Authentication. It converts the supervisor-requested authentication expansion into a controlled implementation sequence for:

1. Student sign-up with auto-verification.
2. Student OTP verification and resend.
3. Student initial password creation.
4. Student login, logout, `/auth/me`, and route/API RBAC support.
5. Student forgot-password reset.
6. Admin Sign-In using predefined Admin accounts.
7. Admin forgot-password reset using OTP.
8. Shared password reset infrastructure for supported `STUDENT` and `ADMIN` account types.

The plan is constrained to authentication only. It does not implement Admin dashboard metrics, academic ledger, registered Student inspection, companies, internship requests, filtering, shortlisting, export, skill management, or CV workflows.

---

## 2. Reviewed Source Set and How Each Source Controls This Plan

| Source | Backend/database relevance for this plan | Rule applied |
|---|---|---|
| `Sprint_2_Complete_Authentication_Addendum.md` | Defines controlled Sprint 2 expansion: Admin Sign-In and Admin reset move into Sprint 2. | Implement complete authentication only; keep Admin operations in later sprints. |
| `45_Day_Agile_Sprint_Implementation_Plan_CV_Management (1).md` | Original sprint sequencing and developer split. | Adjust Sprint 2/Sprint 6 ownership without changing Version 1 total scope. |
| `Final_Reduced_Scope_Baseline_Document_v1.1 (1).docx` | Root scope authority for retained and removed features. | No backend module, endpoint, table, enum, or status may conflict with baseline. |
| `Scope reductions.docx` | Confirms removal of Admin approval, temporary password, Admin Skill Master, skill CRUD/import. | Do not create tables, APIs, DTOs, services, or test fixtures for removed workflows. |
| `Software_Requirements_Specification_v3.0.1 (1) (1).docx` | Functional/security requirements, authentication lifecycle, RBAC, validation, negative requirements. | Backend is the authority for auth, verification, OTP, password policy, access control, and persistence. |
| `Backend_Module_Documentation_v1.0 (1).docx` | Modular monolith module boundaries, service responsibilities, RBAC, transaction, audit, testing. | Implement inside approved modules, not as ad hoc controllers/services. |
| `Backend_Folder_Structure_Implementation_Plan_v1.0 (1).docx` | Package-by-feature Spring Boot structure, migration locations, test structure. | Use `modules/<module>/api/application/domain/mapper/persistence` pattern. |
| `Database_Design_Document_v1.0 (1) (1).docx` | Physical PostgreSQL schema expectations, account/role/admin/OTP/audit table direction. | Use Flyway migrations, constraints, indexes, and no forbidden persistence artifacts. |
| `API_Specification_Document_v1.0 (1) (1).docx` | REST/JSON standards, status codes, error model, RBAC, endpoint groups, audit behavior. | Keep API contracts aligned; only add documented Sprint 2 wording/schema addendum for reset `accountType`. |
| `CV_Management_API_OpenAPI_v1.0 (1).yaml` | Machine-readable endpoint and schema baseline. | Implement auth endpoints against OpenAPI; update reset wording/schema in addendum or patch. |
| `Student_Admin_Workflow_Document_v1.0 (1).docx` | Student auth workflows and Admin Login workflow. | Backend actions must match workflow: verify, issue OTP, create password, login, RBAC, audit. |
| `Production_Ready_Use_Case_Documentation_v1.0 (1).docx` | Actor-goal use cases, supporting systems, negative use-case checklist. | Only Student and Department Admin authenticate; no Company user/skill manager/CV reviewer. |
| `UI_Frontend_Specification_v1.1 (1).docx` | Frontend route/data expectations, loading/error behavior, API dependencies. | Return predictable DTOs/errors that frontend can map safely. |
| `Frontend_Folder_Structure_Implementation_Plan_v1.0 (1).docx` | API integration structure and route map expectations. | Keep endpoint names stable for generated client/hooks. |
| `Student.docx` | Student auth page and workflow behavior evidence. | Support Student identity verification and reset flows as backend capabilities. |
| `Admin.docx` | Admin Sign-In page behavior evidence. | Support Admin credential validation for predefined accounts. |
| `Student Pages.zip` | Student HTML prototype evidence. | Do not preserve frontend-only demo state; backend persistence is required. |
| `Admin Pages.zip` | Admin Sign-In prototype and page inventory. | Only Admin Sign-In is Sprint 2 backend scope. |
| `New DESIGN (1).md` | UI design only. | No backend functionality may be inferred from visual design. |
| `Skill List breakdown.pdf` | Skill taxonomy structure. | Not part of Sprint 2 auth; do not implement Admin skill management. |
| `Project Management Diagrams.zip` | Project workflow/scheduling visuals. | Reference only; does not override backend scope. |

---

## 3. Backend & Database Scope for Sprint 2

### 3.1 Included

| Area | Included backend/database behavior |
|---|---|
| Student auto-verification | Match Index Number + University Email against existing eligible Level 3/Level 4 student record. |
| Student onboarding OTP | Generate, hash, store, send, verify, resend, expire, and limit attempts. |
| Student initial password creation | Allow only after verified onboarding OTP; store password hash. |
| Student login | Authenticate university email/password and issue Student JWT/session context. |
| Admin login | Authenticate predefined Admin email/password and issue Admin JWT/session context. |
| `/auth/me` | Return current authenticated user/account/role context. |
| Logout | Invalidate token/session where supported or acknowledge stateless logout contract. |
| Student password reset | OTP-based account recovery for existing Student account. |
| Admin password reset | OTP-based account recovery for predefined Admin account. |
| RBAC | Enforce Student/Admin roles on protected endpoints. |
| Audit/security events | Log safe events for auth, OTP attempts, password reset, and security failures. |
| Migrations/seed | Confirm roles, predefined dev/test Admin, eligible Student seed if needed, OTP/reset tables. |

### 3.2 Explicitly Excluded

Do not implement or scaffold:

- Admin self-registration.
- Admin approval of Student registration.
- Pending/rejected Student registration lifecycle.
- Temporary password generation or temporary-password table/column.
- Admin-issued password workflow.
- Admin account management UI/API beyond controlled provisioning seed/bootstrap.
- Admin dashboard metrics APIs.
- Academic ledger upload/staging/validation/commit.
- Registered Student list/deep-dive APIs.
- Company login, company account, company role.
- Admin Skill Master, skill taxonomy CRUD/import/upload.
- Skill verification or verified skill status.
- CV review/approval workflow.
- AI scoring/ranking/match-percentage fields.
- Project approval/verification.
- Hard shortlist blocking.
- GPA persisted in internship request data.

---

## 4. Target Backend Architecture

### 4.1 Package Pattern

Use modular monolith package-by-feature structure:

```text
src/main/java/lk/ac/ruhuna/dcs/cvmanagement/
  modules/
    auth/
      api/
      application/
      domain/
      mapper/
      persistence/
    verification/
      api/
      application/
      domain/
      mapper/
      persistence/
    student/
      application/
      domain/
      persistence/
    admin/
      application/
      domain/
      persistence/
    audit/
      application/
      domain/
      persistence/
  shared/
    errors/
    security/
    validation/
    pagination/
    time/
  infrastructure/
    email/
    jwt/
    persistence/
    observability/
```

Rules:

1. Controllers live in `modules/<module>/api`.
2. Use-case orchestration services live in `modules/<module>/application`.
3. Domain policies and value objects live in `modules/<module>/domain`.
4. JPA entities/repositories live in `modules/<module>/persistence`.
5. Cross-cutting primitives only go into `shared`.
6. Email/JWT adapters go into `infrastructure`.
7. Do not create global `service/` or `controller/` dumps.

### 4.2 Sprint 2 Module Responsibilities

| Module | Responsibility |
|---|---|
| `auth` | Login, logout, `/auth/me`, password reset start/verify/resend/complete, password hashing coordination, token/session issue. |
| `verification` | Student sign-up verification, onboarding OTP issue/verify/resend, onboarding password creation eligibility. |
| `student` | Eligible Student lookup, Student account activation/update, Student account status checks. |
| `admin` | Predefined Admin lookup, Admin account eligibility checks, disabled-account handling. |
| `audit` | Safe security event logging without secrets. |
| `shared/security` | Auth principal, role model, RBAC utilities, password encoder abstraction, security constants. |
| `infrastructure/email` | OTP delivery adapter for dev/test and future production integration. |
| `infrastructure/jwt` | JWT generation, validation, claims, token lifetime config. |

---

## 5. API Contract Implementation Plan

### 5.1 Endpoint Inventory

| Endpoint | Method | Access | Controller | Sprint 2 behavior |
|---|---:|---|---|---|
| `/api/v1/student-verifications` | POST | Public | `StudentVerificationController` | Start Student auto-verification and onboarding OTP. |
| `/api/v1/student-verifications/{verificationId}/otp/verify` | POST | Public | `StudentVerificationController` | Verify onboarding OTP. |
| `/api/v1/student-verifications/{verificationId}/otp/resend` | POST | Public | `StudentVerificationController` | Resend onboarding OTP subject to cooldown/rate limits. |
| `/api/v1/student-verifications/{verificationId}/password` | POST | Public | `StudentVerificationController` or `StudentPasswordController` | Create initial Student password after verified onboarding OTP. |
| `/api/v1/auth/student/login` | POST | Public | `AuthController` | Authenticate Student account and issue token/current user. |
| `/api/v1/auth/admin/login` | POST | Public | `AuthController` | Authenticate predefined Admin and issue token/current user. |
| `/api/v1/auth/me` | GET | Student/Admin | `AuthController` | Return authenticated user role context. |
| `/api/v1/auth/logout` | POST | Student/Admin | `AuthController` | Invalidate session where supported or return no-content safe response. |
| `/api/v1/password-resets` | POST | Public | `PasswordResetController` | Start reset for `STUDENT` or `ADMIN`. |
| `/api/v1/password-resets/{resetId}/otp/verify` | POST | Public | `PasswordResetController` | Verify reset OTP. |
| `/api/v1/password-resets/{resetId}/otp/resend` | POST | Public | `PasswordResetController` | Resend reset OTP. |
| `/api/v1/password-resets/{resetId}/password` | POST | Public | `PasswordResetController` | Complete password reset after verified reset OTP. |

### 5.2 OpenAPI Addendum Required

Existing Student-only password reset wording must be generalized through an implementation addendum or OpenAPI patch:

```yaml
PasswordResetStartRequest:
  type: object
  required:
    - accountType
    - email
  properties:
    accountType:
      type: string
      enum: [STUDENT, ADMIN]
    email:
      type: string
      format: email
```

Rules:

1. `ADMIN` reset is allowed only for predefined Admin accounts.
2. Reset endpoint must not create an account.
3. Reset endpoint must not approve accounts.
4. Reset endpoint must not generate a temporary password.
5. Raw OTP must never be returned.

### 5.3 DTOs

```java
record StudentVerificationStartRequest(
    String fullName,
    String indexNumber,
    String universityEmail
) {}

record StudentVerificationResponse(
    UUID verificationId,
    String message,
    Integer expiresInSeconds
) {}

record OtpVerifyRequest(String otp) {}

record OtpResendResponse(
    String message,
    Integer expiresInSeconds,
    Integer resendAvailableInSeconds
) {}

record PasswordCreateRequest(
    String newPassword,
    String confirmPassword
) {}

record StudentLoginRequest(
    String universityEmail,
    String password
) {}

record AdminLoginRequest(
    String email,
    String password
) {}

record PasswordResetStartRequest(
    AccountType accountType,
    String email
) {}

record PasswordResetResponse(
    UUID resetId,
    String message,
    Integer expiresInSeconds
) {}

record AuthTokenResponse(
    String accessToken,
    String tokenType,
    Integer expiresInSeconds,
    CurrentUserResponse user
) {}

record CurrentUserResponse(
    UUID userId,
    UUID accountId,
    String email,
    String displayName,
    Set<String> roles,
    String primaryRole
) {}
```

DTO rules:

- Never include `passwordHash`.
- Never include raw OTP.
- Never include internal security flags unless needed by the frontend and safe.
- Use generic messages where account enumeration is a risk.

---

## 6. Domain Model and Database Plan

### 6.1 Required Entities / Tables

The actual physical names must remain consistent with existing Database Design naming. The table names below are implementation planning names aligned to the database document direction.

| Table / entity | Purpose | Sprint 2 action |
|---|---|---|
| `auth.user_account` | Shared account identity for Student/Admin login. | Confirm/implement. |
| `auth.role` | Role catalogue with `STUDENT`, `ADMIN`. | Seed/confirm. |
| `auth.user_role` | Account-to-role mapping. | Confirm constraints and indexes. |
| `student.student` or equivalent | Student master/profile record and eligible record link. | Read for verification; update account link/status after password creation. |
| `auth.admin_user` | Predefined Admin profile/account link. | Seed/confirm lookup by account. |
| `auth.otp_context` or `auth.auth_otp_context` | OTP metadata for signup and reset. | Implement if not present. |
| `auth.password_reset_context` or shared OTP context | Reset context status and account association. | Implement if separate context design is preferred. |
| `audit.audit_event` | Security event logging. | Confirm/implement minimal auth event logging. |

### 6.2 Recommended Single OTP Context Design

Use one context table for both onboarding OTP and password reset OTP to avoid duplicated logic.

```text
auth.otp_context
  id uuid primary key
  purpose varchar not null              -- STUDENT_SIGNUP, PASSWORD_RESET
  account_type varchar null             -- STUDENT, ADMIN; nullable for pre-account signup if needed
  user_account_id uuid null             -- present for reset, may be null before Student account creation
  student_id uuid null                  -- present for signup verification context
  email varchar not null
  otp_hash varchar not null
  status varchar not null               -- PENDING, VERIFIED, EXPIRED, BLOCKED, CONSUMED
  expires_at timestamptz not null
  verified_at timestamptz null
  consumed_at timestamptz null
  attempt_count int not null default 0
  resend_count int not null default 0
  last_resend_at timestamptz null
  created_at timestamptz not null
  updated_at timestamptz not null
```

If the Database Design already mandates a separate reset-context table, follow it. The key is that OTP generation, hashing, expiry, attempt counts, and consumption rules must be consistent.

### 6.3 User Account Fields Needed

```text
auth.user_account
  id uuid primary key
  email varchar unique not null
  password_hash varchar null
  account_status varchar not null       -- ACTIVE, SETUP_PENDING, DISABLED, LOCKED if already approved
  password_changed_at timestamptz null
  last_login_at timestamptz null
  created_at timestamptz not null
  updated_at timestamptz not null
```

Rules:

1. `password_hash` may be null only before initial password creation where approved.
2. Do not store temporary passwords.
3. Do not store raw password.
4. Student eligibility must be based on official Student record match, not name.
5. Admin eligibility must be based on predefined Admin account/profile.

### 6.4 Admin Provisioning

Admin accounts remain provisioned by the development team.

Development/test provisioning options:

1. Flyway seed migration for non-production dev/test only.
2. Environment-driven bootstrap runner for local/demo credentials.
3. SQL seed file excluded from production secrets.

Production-like controls:

1. No real password in version control.
2. Store only a strong password hash.
3. Initial password comes from deployment secret or controlled manual bootstrap.
4. Require reset flow to change forgotten Admin password.
5. Unknown Admin email in reset must not create an account.

### 6.5 Suggested Flyway Migration Sequence

| Migration | Purpose | Notes |
|---|---|---|
| `V002__auth_roles_and_accounts.sql` | Create/confirm auth schema, `user_account`, `role`, `user_role`. | Skip objects already created in Sprint 1 foundation; use correct version numbers. |
| `V003__student_verification_support.sql` | Add onboarding verification/OTP context if missing. | Supports Student sign-up + OTP. |
| `V004__predefined_admin_seed_dev.sql` | Seed dev/test Admin account and role mapping. | Dev/test only; do not commit production secrets. |
| `V005__password_reset_context.sql` | Add reset support or shared OTP purpose fields. | Supports `STUDENT` and `ADMIN`. |
| `V006__auth_audit_events.sql` | Add minimal security audit table if not already present. | Do not log secrets. |

Adjust migration numbers to the actual repository baseline. Do not create migrations for Admin dashboard, ledger, skills, companies, filtering, or CV in Sprint 2.

---

## 7. Security Design

### 7.1 Password Hashing

Use Spring Security `PasswordEncoder`:

- BCrypt is acceptable for Sprint 2.
- Argon2id is acceptable if dependency/runtime support is already planned.
- Do not implement a custom hashing algorithm.

Rules:

1. Hash before storing.
2. Never log password or hash.
3. Return generic authentication failures.
4. Change `password_changed_at` after successful create/reset.

### 7.2 OTP Generation and Storage

Recommended parameters:

| Parameter | Value |
|---|---:|
| OTP length | 6 digits |
| Expiry | 5 minutes |
| Verify attempts | 3 |
| Resend cooldown | 60 seconds |
| Max resend attempts | 3 configurable |

Rules:

1. Generate OTP using cryptographically secure random generator.
2. Store only OTP hash.
3. Compare submitted OTP by hashing and constant-time/safe comparison where practical.
4. Mark context `VERIFIED` only for correct unexpired OTP.
5. Mark context `CONSUMED` after password creation/reset.
6. Mark context `BLOCKED` after retry limit.
7. Do not reuse consumed contexts.

### 7.3 JWT / Session Claims

Token must include or allow resolving:

- User/account ID.
- Email.
- Roles (`STUDENT`, `ADMIN`).
- Token issue/expiry time.

Rules:

1. Token secret from environment/config, not source code.
2. Expiration configurable.
3. `/auth/me` must validate token and return current backend-authoritative user context.
4. Backend RBAC must guard protected endpoints even when frontend route guard exists.

### 7.4 Error Safety

Use generic error messages for:

- Invalid credentials.
- Unknown Admin email during reset.
- Unknown Student email during reset.
- Disabled account where revealing details is unsafe.

Use specific but safe messages for:

- Invalid input format.
- Password mismatch.
- Expired OTP.
- OTP retry limit reached.
- Resend cooldown active.

Never return:

- Raw OTP.
- Password hash.
- Internal account status that leaks security state unnecessarily.
- Stack trace.

---

## 8. Service-Level Implementation Plan

### 8.1 Student Verification Service

`StudentVerificationService`

Responsibilities:

1. Validate request DTO.
2. Normalize index number and email consistently.
3. Match eligible Student by Index Number + University Email.
4. Ignore Full Name for verification authority; store/update as display data only where approved.
5. Reject already active account where appropriate.
6. Create verification OTP context.
7. Send OTP through email adapter.
8. Return verification ID and safe message.

Pseudocode:

```text
startVerification(request):
  validate index/email/name
  student = eligibleStudentRepository.findByIndexAndEmail(index, email)
  if missing -> safe validation failure
  if account already active -> safe conflict or direct-to-login guidance
  context = otpContextService.create(STUDENT_SIGNUP, student, email)
  emailService.sendOtp(email, context.otp)
  audit AUTH_STUDENT_VERIFICATION_STARTED
  return verificationId
```

### 8.2 Onboarding OTP Service

Responsibilities:

1. Verify OTP for `STUDENT_SIGNUP` context.
2. Enforce expiry, attempts, resend limits.
3. Mark verified status.
4. Permit initial password creation.

### 8.3 Student Initial Password Service

Responsibilities:

1. Validate verified onboarding context.
2. Validate password policy and confirmation.
3. Create or update `user_account` for Student.
4. Assign `STUDENT` role.
5. Store password hash.
6. Link Student record to account.
7. Mark OTP context consumed.
8. Audit password creation without secrets.

Transaction rule: Account creation/linking, role mapping, password hash update, and OTP consumption must be committed atomically.

### 8.4 Auth Service — Student Login

Responsibilities:

1. Lookup account by university email.
2. Confirm `STUDENT` role.
3. Confirm account active.
4. Verify password hash.
5. Issue token.
6. Update last login timestamp.
7. Audit success/failure safely.

### 8.5 Auth Service — Admin Login

Responsibilities:

1. Lookup account by Admin email.
2. Confirm `ADMIN` role and predefined `admin_user` profile.
3. Confirm account active/not disabled.
4. Verify password hash.
5. Issue token with `ADMIN` role.
6. Update last login timestamp.
7. Audit success/failure safely.

Rules:

- No Admin self-registration.
- No fallback to Student account.
- No account creation during login.

### 8.6 Password Reset Service

`PasswordResetService`

Responsibilities:

1. Start reset for `STUDENT` or `ADMIN`.
2. Use account type to restrict lookup.
3. Create reset OTP context only for eligible accounts.
4. Return safe response.
5. Verify reset OTP.
6. Resend reset OTP.
7. Complete password reset and update password hash.
8. Consume reset context.
9. Audit security events.

Start reset pseudocode:

```text
startReset(accountType, email):
  validate accountType/email
  account = accountRepository.findByEmailAndRole(email, accountType)
  if not eligible:
    audit PASSWORD_RESET_REQUEST_NON_ELIGIBLE
    return safe response according to API standard
  context = otpContextService.create(PASSWORD_RESET, account, email, accountType)
  emailService.sendOtp(email, context.otp)
  audit PASSWORD_RESET_STARTED
  return resetId and safe message
```

Complete reset pseudocode:

```text
completeReset(resetId, newPassword, confirmPassword):
  context = otpContextRepository.find(resetId)
  require context.purpose == PASSWORD_RESET
  require context.status == VERIFIED
  validate password policy and confirmation
  account = accountRepository.findById(context.accountId)
  require account active or reset-eligible
  account.passwordHash = passwordEncoder.encode(newPassword)
  account.passwordChangedAt = now
  context.status = CONSUMED
  context.consumedAt = now
  audit PASSWORD_RESET_COMPLETED
```

### 8.7 `/auth/me` Service

Responsibilities:

1. Resolve authenticated principal from security context.
2. Load current account and roles.
3. Return safe user context.
4. Reject disabled/locked accounts.

### 8.8 Logout Service

Two valid Sprint 2 approaches:

| Approach | Behavior | Notes |
|---|---|---|
| Stateless JWT logout | Return `204`, frontend clears token. | Simplest. No server blacklist. |
| Server-side session/token registry | Mark session revoked. | Use only if already planned in DB/security design. |

Do not add complex refresh-token rotation unless already approved.

---

## 9. Transaction and Concurrency Rules

| Flow | Transaction rule |
|---|---|
| Start Student verification | Create OTP context and related state in one transaction; email send may occur after commit or through safe adapter. |
| Verify OTP | Increment attempts or mark verified atomically. |
| Resend OTP | Enforce cooldown and update hash/expiry/resend count atomically. |
| Create initial password | Account creation/linking/role assignment/password hash/context consumption in one transaction. |
| Admin login | Read-only plus last-login update; no account creation. |
| Start password reset | Create reset context atomically; do not create account. |
| Complete password reset | Password hash update and reset context consumption in one transaction. |

Concurrency controls:

- Prevent two active valid reset contexts for the same account if database design supports it, or invalidate older contexts when creating a new one.
- Prevent consumed/verified contexts from being reused.
- Use optimistic locking or status checks for OTP contexts.
- For resend, update only if status is still pending and cooldown has passed.

---

## 10. Audit and Logging Plan

### 10.1 Events

| Event code | Trigger | Include | Exclude |
|---|---|---|---|
| `AUTH_STUDENT_VERIFICATION_STARTED` | Eligible Student verification request | Account/student ID if known, email hash or masked email, IP/correlation ID | Raw OTP, password |
| `AUTH_OTP_VERIFIED` | OTP verified | Context ID, purpose, actor/account type | Raw OTP |
| `AUTH_OTP_FAILED` | Incorrect/expired OTP | Context ID, reason code, attempt count | Raw OTP |
| `AUTH_STUDENT_PASSWORD_CREATED` | Initial password created | Student/account ID | Password/hash |
| `AUTH_STUDENT_LOGIN_SUCCESS` | Student login success | Account ID, role | Password/token |
| `AUTH_ADMIN_LOGIN_SUCCESS` | Admin login success | Account ID, role | Password/token |
| `AUTH_LOGIN_FAILURE` | Login failure | Masked email, account type if known, reason category | Password |
| `AUTH_PASSWORD_RESET_STARTED` | Reset context created | Account ID, account type | Raw OTP |
| `AUTH_PASSWORD_RESET_COMPLETED` | Password hash updated | Account ID, account type | Password/hash |
| `AUTH_LOGOUT` | Logout request | Account ID | Token value |

### 10.2 Logging Rules

1. Use correlation ID if available.
2. Mask email in logs where possible.
3. Never log raw request bodies for auth endpoints.
4. Never log secrets, passwords, OTPs, JWTs, password hashes, or email credentials.
5. Ensure audit failures do not expose auth secrets.

---

## 11. Email Adapter Plan

### 11.1 Interface

```java
public interface OtpEmailSender {
    void sendOtp(String recipientEmail, String purpose, String otp, Instant expiresAt);
}
```

### 11.2 Sprint 2 Implementations

| Adapter | Environment | Rule |
|---|---|---|
| `LoggingOtpEmailSender` | Local/dev only | May print OTP in dev logs only if explicitly enabled; never use in production profile. |
| `NoopOtpEmailSender` | Unit tests | Captures send call without external dependency. |
| SMTP/MailHog adapter | Integration/demo if available | Use config values, no hardcoded credentials. |

If logging OTP in development, label it dev-only and ensure production profile fails fast if real sender config is missing.

---

## 12. Validation and Error Model

### 12.1 Validation

| Request | Required backend validation |
|---|---|
| Student verification start | Full Name required; Index Number required/format; University Email required/format/domain if specified; eligible record match. |
| OTP verify | Context exists; OTP is six digits; context pending; not expired; attempts under limit. |
| Password create/reset | New password required; confirm required; match; password policy; context verified. |
| Student login | Email required; password required; account active; role Student; password matches. |
| Admin login | Email required; password required; account active; role Admin; predefined Admin profile exists; password matches. |
| Password reset start | Account type required; email required; role-specific account lookup; safe response. |

### 12.2 Suggested Error Codes

Use existing project standard error model. If adding codes, keep them auth-specific and not broad product scope.

| Code | Use |
|---|---|
| `VALIDATION_ERROR` | DTO/field validation failure. |
| `AUTH_INVALID_CREDENTIALS` | Generic login failure. |
| `AUTH_FORBIDDEN_ROLE` | Authenticated user lacks role. |
| `AUTH_ACCOUNT_DISABLED` | Account cannot authenticate/reset. |
| `OTP_INVALID` | OTP does not match. |
| `OTP_EXPIRED` | OTP expired. |
| `OTP_ATTEMPTS_EXCEEDED` | Retry limit reached. |
| `OTP_RESEND_COOLDOWN` | Resend attempted too early. |
| `RESET_CONTEXT_INVALID` | Reset context missing/consumed/invalid. |
| `PASSWORD_POLICY_FAILED` | Password does not satisfy policy. |

---

## 13. Backend Implementation Sequence

### Day 1 — Contract, Migration, and Security Foundation

| Task ID | Task | Output |
|---|---|---|
| BE-S2-001 | Confirm OpenAPI delta for password reset `accountType` | Signed-off DTO/API patch |
| BE-S2-002 | Confirm auth package structure | Module folders and class placeholders |
| BE-S2-003 | Confirm roles and predefined Admin persistence | `STUDENT`/`ADMIN` roles, dev/test Admin seed plan |
| BE-S2-004 | Implement password encoder and JWT config skeleton | Secure config from environment |
| BE-S2-005 | Implement OTP context entity/repository or confirm existing | Migration + repository ready |
| BE-S2-006 | Implement global auth error mapping | Standard safe responses |

### Day 2 — Student Authentication Lifecycle

| Task ID | Task | Output |
|---|---|---|
| BE-S2-101 | Implement Student verification start | Eligible Student can start OTP flow |
| BE-S2-102 | Implement onboarding OTP verify/resend | OTP lifecycle works |
| BE-S2-103 | Implement initial Student password creation | Account/role/password hash created or updated |
| BE-S2-104 | Implement Student login | JWT/current user returned |
| BE-S2-105 | Implement Student forgot-password reset | Reset context + OTP + password update |
| BE-S2-106 | Add Student auth tests | Happy/error path coverage |

### Day 3 — Admin Sign-In

| Task ID | Task | Output |
|---|---|---|
| BE-S2-201 | Implement predefined Admin lookup | Admin account/profile validated |
| BE-S2-202 | Implement `/auth/admin/login` | Admin JWT/current user returned |
| BE-S2-203 | Enforce Admin role in token/current user | `ADMIN` role claim/context works |
| BE-S2-204 | Add Admin disabled/invalid credential handling | Safe errors |
| BE-S2-205 | Add Admin login tests | Success, invalid, disabled, wrong role |

### Day 4 — Unified Password Reset and RBAC

| Task ID | Task | Output |
|---|---|---|
| BE-S2-301 | Generalize `/password-resets` to `STUDENT`/`ADMIN` | Shared reset start endpoint |
| BE-S2-302 | Implement reset OTP verify/resend | Reset OTP lifecycle works |
| BE-S2-303 | Implement reset password completion | Password hash updates for Student/Admin |
| BE-S2-304 | Implement `/auth/me` | Current user context works |
| BE-S2-305 | Implement/confirm logout | Logout contract works |
| BE-S2-306 | Add RBAC tests | Student/Admin access boundaries enforced |

### Day 5 — Integration, QA, Contract, and Negative-Scope Scan

| Task ID | Task | Output |
|---|---|---|
| BE-S2-401 | Run migration from empty database | Flyway clean/migrate passes in local/test |
| BE-S2-402 | Run API tests against auth endpoints | Contract behavior verified |
| BE-S2-403 | Integrate with frontend | Student/Admin flows pass end-to-end |
| BE-S2-404 | Run audit/log review | No secrets logged |
| BE-S2-405 | Run removed-scope scan | No forbidden APIs/tables/enums/services |
| BE-S2-406 | Prepare Sprint 2 demo data | Eligible Student + predefined Admin ready |

---

## 14. Testing Plan

### 14.1 Unit Tests

| Service | Required coverage |
|---|---|
| `StudentVerificationService` | Eligible match, no match, already active account, OTP context creation. |
| `OtpContextService` | Generate hash, verify, invalid OTP, expired OTP, retry limit, resend cooldown. |
| `StudentPasswordService` | Verified context required, password mismatch, successful hash and account activation. |
| `AuthService` | Student login success/failure, Admin login success/failure, disabled account, role mismatch. |
| `PasswordResetService` | Start Student/Admin reset, unknown email safe behavior, OTP verify, resend, complete reset. |
| `JwtService` | Role claims, expiry, invalid token. |
| `CurrentUserService` | `/auth/me` mapping for Student/Admin. |

### 14.2 Repository/Migration Tests

| Test | Required behavior |
|---|---|
| Migration from empty DB | All Sprint 2 migrations apply successfully. |
| Role seed | `STUDENT` and `ADMIN` exist and are unique. |
| Admin seed/dev provisioning | Dev/test Admin account is role-mapped and password-hashed. |
| OTP context constraints | Status/purpose/account type constraints work. |
| Unique active contexts | No duplicate active contexts if enforced by design. |
| No forbidden schema | No temporary password table/column; no approval status; no company account. |

### 14.3 API Integration Tests

| Test ID | Scenario | Expected result |
|---|---|---|
| API-AUTH-001 | Eligible Student starts verification | `201`, verification ID, OTP sent through adapter. |
| API-AUTH-002 | Ineligible Student starts verification | Safe failure, no active account created. |
| API-AUTH-003 | Correct onboarding OTP | Context verified. |
| API-AUTH-004 | Incorrect onboarding OTP | Attempt count increments, safe error. |
| API-AUTH-005 | Expired onboarding OTP | Safe expiry error. |
| API-AUTH-006 | Initial password creation | Password hash stored, Student role assigned, context consumed. |
| API-AUTH-007 | Student login success | Token and `STUDENT` current user. |
| API-AUTH-008 | Admin login success | Token and `ADMIN` current user. |
| API-AUTH-009 | Admin login invalid password | Generic 401. |
| API-AUTH-010 | Admin login with non-Admin account | 403/401 according to standard, no token. |
| API-AUTH-011 | Student reset success | New hash works for login. |
| API-AUTH-012 | Admin reset success | New hash works for Admin login. |
| API-AUTH-013 | Unknown Admin reset email | No account created; safe response/error. |
| API-AUTH-014 | `/auth/me` with valid token | Current user returned. |
| API-AUTH-015 | `/auth/me` with invalid token | 401. |

### 14.4 Security Tests

| Test | Pass condition |
|---|---|
| Password not logged | Logs contain no submitted password. |
| OTP not stored raw | Database contains only OTP hash. |
| OTP not logged in non-dev profile | Production-like profile never logs raw OTP. |
| JWT secret externalized | No hardcoded token secret in source. |
| Admin self-registration absent | No endpoint/controller/schema for Admin registration. |
| Temporary password absent | No endpoint/table/column/test fixture for temporary password. |
| RBAC backend enforced | Student token cannot access Admin-protected test endpoint. |
| Safe account enumeration | Unknown reset emails do not reveal sensitive details. |

---

## 15. Removed-Scope Backend/Database Scan

Search code, migrations, OpenAPI, DTOs, tests, and seed data for forbidden artifacts.

Forbidden terms/concepts:

```text
student_approval
registration_approval
pending_registration
rejected_registration
temporary_password
temp_password
admin_registration
admin_signup
admin_self_registration
skill_master
verified_skill
skill_verification
cv_review
cv_approval
company_user
company_login
ai_score
ranking_score
match_percentage
automated_selection
project_approval
estimated_gpa
gpa_in_request
```

Pass condition:

- No active endpoint, DTO, enum, entity, table, column, migration, service, repository, or test fixture implements these concepts.
- Historical text in documentation references may exist only as removed-scope guardrails, not as active implementation.

---

## 16. Environment Configuration

### 16.1 Required Config Keys

```properties
# Database
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=

# JWT
APP_SECURITY_JWT_SECRET=
APP_SECURITY_JWT_ACCESS_TOKEN_TTL_SECONDS=

# OTP
APP_AUTH_OTP_LENGTH=6
APP_AUTH_OTP_TTL_SECONDS=300
APP_AUTH_OTP_MAX_ATTEMPTS=3
APP_AUTH_OTP_RESEND_COOLDOWN_SECONDS=60
APP_AUTH_OTP_MAX_RESENDS=3

# Admin bootstrap - dev/test only or controlled production bootstrap
APP_BOOTSTRAP_ADMIN_EMAIL=
APP_BOOTSTRAP_ADMIN_INITIAL_PASSWORD=
APP_BOOTSTRAP_ADMIN_ENABLED=

# Email
APP_EMAIL_MODE=LOGGING_DEV_ONLY|SMTP|NOOP_TEST
APP_EMAIL_FROM=
APP_EMAIL_SMTP_HOST=
APP_EMAIL_SMTP_PORT=
APP_EMAIL_SMTP_USERNAME=
APP_EMAIL_SMTP_PASSWORD=
```

Rules:

1. No default production secrets.
2. Dev logging OTP mode must be impossible or disabled in production profile.
3. Bootstrap Admin password must not appear in source code or committed SQL for production.

---

## 17. Integration Contract with Frontend

Backend must provide predictable behavior for frontend Sprint 2:

| Frontend need | Backend responsibility |
|---|---|
| Verification modal success/failure | Return clear status/error for `/student-verifications`. |
| OTP page routing | Return `verificationId`/`resetId` and expiry metadata where safe. |
| Resend cooldown | Return cooldown or safe error status. |
| Password mismatch | Validate and return field-safe error. |
| Admin Login | Return `AuthTokenResponse` with `ADMIN` role. |
| `/auth/me` | Return current user with roles and display name. |
| Safe reset | Unknown email must not create account or leak sensitive info. |
| Route protection | Return 401/403 reliably for missing/wrong-role tokens. |

---

## 18. Sprint 2 Backend & Database Definition of Done

### Student Auth

- [ ] Eligible Student verification works using Index Number + University Email.
- [ ] Full Name is not used as verification authority.
- [ ] OTP issue, verify, resend, expiry, retry limit work.
- [ ] Initial Student password creation stores only password hash.
- [ ] Student login returns valid token/current user with `STUDENT` role.
- [ ] Student forgot-password reset works through OTP.

### Admin Auth

- [ ] Predefined Admin account can authenticate through `/auth/admin/login`.
- [ ] Invalid Admin credentials return safe generic failure.
- [ ] Disabled/non-Admin accounts cannot authenticate as Admin.
- [ ] Admin token/current user contains `ADMIN` role.
- [ ] Admin forgot-password reset works through OTP.
- [ ] Unknown Admin email does not create an account.
- [ ] Admin reset completion updates password hash and consumes reset context.

### Shared Auth/RBAC

- [ ] `/auth/me` works for Student/Admin.
- [ ] Logout endpoint contract works.
- [ ] Protected endpoints enforce backend RBAC.
- [ ] Audit/security events are recorded without secrets.
- [ ] Standard error model is used consistently.
- [ ] OpenAPI/auth DTOs are aligned or documented through Sprint 2 patch.

### Database

- [ ] Flyway migrations apply from empty database.
- [ ] Roles are seeded/confirmed.
- [ ] Dev/test predefined Admin provisioning works safely.
- [ ] OTP/reset context stores only hashes and metadata.
- [ ] Passwords are hashed only.
- [ ] No forbidden tables/columns/enums exist.

### Quality

- [ ] Unit tests pass.
- [ ] Integration/API tests pass.
- [ ] Security tests pass.
- [ ] Migration tests pass.
- [ ] Removed-scope backend/database scan passes.
- [ ] Frontend integration demo passes.

---

## 19. Supervisor Demo Data Requirements

Prepare only the data needed for authentication demo:

| Data | Purpose |
|---|---|
| One eligible Level 3 or Level 4 Student record | Student sign-up verification. |
| One predefined Admin account | Admin login/reset demo. |
| `STUDENT` role | Student account role. |
| `ADMIN` role | Admin account role. |
| Dev/test email adapter | OTP delivery without production email dependency. |

Do not seed ledger data, skills, projects, companies, requests, filtering criteria, shortlist data, or CV data for Sprint 2 unless needed by existing foundation tests. Those belong to later sprint demos.

---

## 20. Final Implementation Notes

- Treat Admin password reset as account recovery for an existing predefined Admin, not Admin account creation.
- Use shared password reset infrastructure, but enforce `accountType` and role-specific eligibility.
- Keep Student sign-up separate from Admin login/reset. Admins do not sign up.
- Keep all auth decisions backend-authoritative.
- Use frontend route guards only as UX support.
- Keep Admin dashboard as shell-only for Sprint 2 redirect verification.
- Move real Admin dashboard, ledger, registered Student list, companies, internship requests, filtering, shortlisting, and exports to their original later sprint implementation scope.
