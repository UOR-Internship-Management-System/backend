# Sprint 2 Complete Authentication Addendum

**Project:** CV Management and Deterministic Internship Candidate Filtering System  
**Document type:** Supplemental Sprint 2 implementation addendum  
**Status:** Draft for supervisor/team review  
**Generated:** 2026-07-09  
**Purpose:** Capture the supervisor-requested Sprint 2 authentication expansion without modifying existing project documents directly.

---

## 1. Executive Summary

The original Sprint 2 implementation plan focused on Student onboarding and authentication only. The supervisor has now requested **complete authentication during Sprint 2**, including:

1. Student onboarding and authentication.
2. Student forgot-password reset.
3. Admin Sign-In.
4. Admin forgot-password reset.
5. Admin password creation/reset using predefined Admin accounts provisioned by the development team.

This addendum confirms that the change is feasible if it is treated as a **Sprint 2 scope reallocation** rather than a broad Admin module expansion.

Only **Admin authentication** moves into Sprint 2. Admin dashboard data, academic ledger management, registered student inspection, companies, internship requests, filtering, shortlisting, and exports remain in their later planned sprints.

---

## 2. Scope Decision

### 2.1 Approved Sprint 2 Authentication Scope

Sprint 2 shall now be titled:

> **Complete Authentication: Student Onboarding, Student Login/Reset, Admin Sign-In, and Admin Password Reset**

Sprint 2 includes:

| Area | Included in Sprint 2 |
|---|---|
| Student sign-up | Full Name, Index Number, University Email |
| Student auto-verification | Index Number + University Email against eligible records |
| Student OTP | issue, verify, resend, expiry, retry handling |
| Student initial password creation | after OTP verification |
| Student login/logout | JWT/session handling and `/auth/me` |
| Student forgot password | OTP-based recovery and password reset |
| Admin login | predefined Admin email/password |
| Admin password reset | OTP-based recovery and password reset |
| Admin RBAC | Admin token, `/auth/me`, Admin route protection |
| Shared security | safe errors, rate limits, audit/security event hooks |

### 2.2 Explicitly Not Included in Sprint 2

The Sprint 2 change must **not** introduce any of the following:

| Excluded Item | Reason |
|---|---|
| Admin self-registration | Admin accounts remain predefined/provisioned by the development team |
| Admin approval of Student sign-up | Removed scope |
| Pending/rejected Student registration lifecycle | Removed scope |
| Temporary password workflow | Removed scope |
| Admin-issued temporary password | Removed scope |
| Admin dashboard metrics implementation | Still belongs to Sprint 6 unless separately approved |
| Academic ledger upload/validation/commit | Still Sprint 6 |
| Registered Student list/deep-dive | Still Sprint 6/7 |
| Company login or company portal | Removed scope |
| Admin Skill Master / skill CRUD/import | Removed scope |
| CV review/approval workflow | Removed scope |
| AI scoring/ranking/match percentage | Removed scope |

---

## 3. Feasibility Audit

### 3.1 Feasibility Conclusion

The requested change is feasible because:

1. Admin login is already an approved Version 1 capability.
2. Admin accounts are already expected to be predefined, not self-registered.
3. The backend authentication module already covers Student/Admin login, JWT/RBAC, and role enforcement.
4. The database model already supports role-based user accounts and predefined Admin users.
5. Existing OTP/password-reset patterns can be reused for Admin reset if the API wording and frontend routes are extended carefully.
6. No removed-scope feature is required to support Admin Sign-In or Admin password reset.

### 3.2 Main Documentation Gap

The only gap is that current password reset wording is Student-centered. This addendum resolves that by defining a safe extension:

> Password reset is supported for predefined `STUDENT` and `ADMIN` account types through OTP-based account recovery.

This does **not** authorize Admin self-registration, temporary passwords, Admin approval, or Admin operational modules.

---

## 4. Sprint Implementation Plan Addendum

### 4.1 Original Sprint 2 Position

Original Sprint 2:

> Student Onboarding and Authentication

Original Sprint 2 deliverable:

> Student sign-up, auto-verification, OTP, password creation, login, logout, and forgot-password reset.

### 4.2 Revised Sprint 2 Position

Revised Sprint 2:

> Complete Authentication

Revised Sprint 2 deliverable:

> A Student can complete onboarding and authentication, and a predefined Admin can sign in and reset a forgotten password using OTP-based account recovery.

### 4.3 Sprint 6 Adjustment

Original Sprint 6 included:

> Admin login/dashboard, ledger upload/staging/validation/commit, registered Student list.

Revised Sprint 6 should become:

> Admin dashboard, academic ledger upload/staging/validation/commit, registered Student list, and Admin operational module integration.

Admin login moves to Sprint 2. Admin dashboard implementation remains Sprint 6, except for a route shell needed to verify successful Admin login redirection.

### 4.4 Revised Sprint 2 Goal

Build the complete authentication foundation for both valid actors:

- Student account lifecycle.
- Predefined Admin sign-in.
- OTP-based password recovery for Student and Admin.
- JWT/RBAC session handling.
- Public/protected route separation.
- Role-aware redirects.
- Removed-scope guardrails.

### 4.5 Revised Sprint 2 Day Plan

| Day | Focus | Output |
|---:|---|---|
| Day 1 | Contract alignment and branch setup | Confirm API changes, route names, schema change for reset account type, DB migration plan |
| Day 2 | Student auth backend/frontend | Student sign-up, verification modal, OTP, initial password, login |
| Day 3 | Admin Sign-In | `/admin/login`, backend admin credential validation, Admin JWT, Admin route guard |
| Day 4 | Unified password reset | Student/Admin forgot-password, OTP verify/resend, reset password, integration tests |
| Day 5 | QA, demo, retrospective | End-to-end Student auth demo, Admin auth demo, negative-scope scan, supervisor review |

---

## 5. API / OpenAPI Addendum

### 5.1 API Design Decision

Use a **shared account recovery flow** for Student and Admin accounts.

Reason:

- Student and Admin accounts both authenticate through the backend authentication system.
- Admin accounts are predefined, not self-registered.
- A shared reset flow avoids duplicated OTP logic.
- Role-specific frontend routes can still call the same backend endpoint with `accountType`.

### 5.2 Endpoint Summary

| Endpoint | Method | Access | Sprint 2 Use |
|---|---:|---|---|
| `/auth/student/login` | POST | Public | Student login |
| `/auth/admin/login` | POST | Public | Admin Sign-In |
| `/auth/me` | GET | Student/Admin | Current authenticated user context |
| `/auth/logout` | POST | Student/Admin | Logout/session invalidation |
| `/student-verifications` | POST | Public | Start Student sign-up verification |
| `/student-verifications/{verificationId}/otp/verify` | POST | Public | Verify Student onboarding OTP |
| `/student-verifications/{verificationId}/otp/resend` | POST | Public | Resend Student onboarding OTP |
| `/student-verifications/{verificationId}/password` | POST | Public | Create initial Student password |
| `/password-resets` | POST | Public | Start Student/Admin password reset |
| `/password-resets/{resetId}/otp/verify` | POST | Public | Verify reset OTP |
| `/password-resets/{resetId}/otp/resend` | POST | Public | Resend reset OTP |
| `/password-resets/{resetId}/password` | POST | Public | Set new password after reset OTP |

### 5.3 Required OpenAPI Wording Change

Current Student-only wording should be replaced with account-type wording.

#### Replace

```yaml
summary: Start forgot-password flow for a Student university email.
```

#### With

```yaml
summary: Start OTP-based password reset for a supported Student or Admin account.
```

#### Replace

```yaml
description: Access: Public. Audit: Security. Uses OTP, not temporary password.
```

#### With

```yaml
description: >
  Access: Public. Audit: Security.
  Supports OTP-based password reset for STUDENT and ADMIN accounts.
  Admin accounts must already be predefined/provisioned.
  This endpoint must not create accounts, approve registrations, or issue temporary passwords.
```

### 5.4 PasswordResetStartRequest Schema Addendum

Recommended request schema:

```yaml
PasswordResetStartRequest:
  type: object
  required:
    - accountType
    - email
  properties:
    accountType:
      type: string
      enum:
        - STUDENT
        - ADMIN
      description: Account type requesting password reset.
    email:
      type: string
      format: email
      description: University email for Student or predefined Admin email for Admin.
```

### 5.5 Password Reset Response Rules

The reset start response must be safe against account enumeration.

Recommended response model:

```yaml
PasswordResetResponse:
  type: object
  required:
    - message
  properties:
    resetId:
      type: string
      format: uuid
      nullable: true
      description: Present only when a reset context is created.
    message:
      type: string
      description: Safe user-facing message.
    expiresInSeconds:
      type: integer
      nullable: true
```

Implementation rule:

- For eligible accounts, create reset context and send OTP.
- For ineligible accounts, return a safe response or safe error according to existing API error standards.
- UI text must not reveal whether an Admin or Student account exists.
- Never return raw OTP.

### 5.6 Admin Login Endpoint

`POST /auth/admin/login` remains the Admin Sign-In endpoint.

Request:

```yaml
AdminLoginRequest:
  type: object
  required:
    - email
    - password
  properties:
    email:
      type: string
      format: email
    password:
      type: string
      format: password
```

Success response:

```yaml
AuthTokenResponse:
  type: object
  required:
    - accessToken
    - tokenType
    - expiresInSeconds
    - user
  properties:
    accessToken:
      type: string
    tokenType:
      type: string
      example: Bearer
    expiresInSeconds:
      type: integer
    user:
      $ref: '#/components/schemas/CurrentUserResponse'
```

Admin login rules:

- Only predefined Admin accounts can authenticate.
- Backend must verify `ADMIN` role.
- Disabled Admin accounts must not authenticate.
- Invalid credentials must return a safe generic error.
- No Admin self-registration endpoint may be added.

---

## 6. Workflow Document Addendum

### 6.1 New Workflow Inventory Item

Add the following Admin authentication workflow:

| Workflow ID | Workflow Name | Actor | Route Entry |
|---|---|---|---|
| `WF-ADM-001A` | Admin Forgot Password / Password Reset | Department Admin | `/admin/forgot-password` |

### 6.2 WF-ADM-001A - Admin Forgot Password / Password Reset

| Section | Specification |
|---|---|
| Workflow ID | `WF-ADM-001A` |
| Workflow Name | Admin Forgot Password / Password Reset |
| Primary Actor | Department Admin |
| Supporting Systems | Authentication/RBAC Service, Email Service, PostgreSQL Data Store, Audit/Security Event Logging |
| Trigger | Admin clicks Forgot Password on Admin Login page and submits predefined Admin email |
| Preconditions | Admin account exists and was provisioned by the development team or approved operations process |
| Related UI Pages | `/admin/forgot-password`, `/admin/verify-reset-otp`, `/admin/create-password`, `/admin/login` |
| Related API Groups | Authentication, Password Reset / OTP |
| RBAC Notes | Reset starts before login, but completion is tied to a valid reset context for an existing predefined Admin account |
| Exit State | Admin password hash is updated and Admin can return to Admin Login |

### 6.3 WF-ADM-001A Step Table

| Step | Actor Action | UI Response | System Action | Data State Change |
|---:|---|---|---|---|
| 1 | Admin clicks Forgot Password on `/admin/login` | Routes to Admin Forgot Password page | None | No data change |
| 2 | Admin enters predefined Admin email | Shows Send OTP action | Validates email format and checks account eligibility safely | Reads account without exposing existence |
| 3 | Admin submits reset request | Shows safe send/pending status | Creates reset context if eligible, generates OTP, sends OTP through email adapter | Stores reset context, hashed OTP, expiry, attempt count |
| 4 | Admin enters OTP | Shows OTP verify action and resend option | Validates OTP hash, expiry, status, and retry count | Marks reset context as verified |
| 5 | Admin enters New Password and Confirm Password | Shows Create Password action | Validates password policy and confirmation match | No password write until validation passes |
| 6 | Admin submits new password | Shows pending state | Hashes password and updates Admin account credential | Stores new password hash, closes reset context |
| 7 | Admin receives success | Routes back to `/admin/login` | Invalidates reset context and optional active sessions | Admin can authenticate with new password |

### 6.4 Alternate Paths

| Path | Description |
|---|---|
| A1 | Admin returns to login before completing reset |
| A2 | Admin requests OTP resend after cooldown |
| A3 | Admin restarts recovery after reset context expires |

### 6.5 Exception Paths

| Exception | Required Behavior |
|---|---|
| Invalid email format | Show validation message |
| Unknown email | Show safe generic response; do not reveal account existence |
| Disabled Admin account | Block reset completion and show safe support message |
| Incorrect OTP | Show safe incorrect OTP message and increment attempt count |
| Expired OTP | Require resend or restart |
| Too many attempts | Lock reset context and require restart/cooldown |
| Password mismatch | Block submission until values match |
| Password policy failure | Show safe password policy guidance |
| Backend/email failure | Show retry-safe error and preserve user-entered email where safe |

### 6.6 Workflow Exclusions

`WF-ADM-001A` must not include:

- Admin self-registration.
- Temporary passwords.
- Admin approval steps.
- Student registration approval.
- Account creation for unknown Admin email.
- Company login.
- Admin Skill Master features.

---

## 7. Use-Case Documentation Addendum

### 7.1 New Use Case

| Field | Value |
|---|---|
| Use Case ID | `UC-ADM-001A` |
| Use Case Name | Reset Forgotten Admin Password |
| Primary Actor | Department Admin |
| Supporting Actors/Systems | Authentication/RBAC Service, Email Service, PostgreSQL Data Store, Audit/Security Event Logging |
| Goal | Allow a predefined Admin to recover access after forgetting credentials |
| Scope | OTP-based Admin password reset only |
| Preconditions | Admin account is already predefined/provisioned and has a reachable email address |
| Trigger | Admin selects Forgot Password from the Admin Login page |
| UI Entry Points | `/admin/forgot-password`, `/admin/verify-reset-otp`, `/admin/create-password`, `/admin/login` |

### 7.2 Main Success Scenario

1. Admin opens Admin Login.
2. Admin selects Forgot Password.
3. Admin enters predefined Admin email.
4. System validates the request safely.
5. System creates a password reset context.
6. System sends OTP to the Admin email.
7. Admin enters OTP.
8. System verifies OTP.
9. Admin enters New Password and Confirm Password.
10. System validates password policy and confirmation.
11. System stores the new password hash.
12. System invalidates the reset context.
13. Admin returns to login and authenticates with the new password.

### 7.3 Alternate Flows

| ID | Flow |
|---|---|
| A1 | Admin cancels and returns to login |
| A2 | Admin resends OTP after cooldown |
| A3 | Admin restarts password reset after OTP expiry |

### 7.4 Exception Flows

| ID | Flow |
|---|---|
| E1 | Email not associated with predefined Admin account; system returns safe generic response |
| E2 | OTP invalid; system shows safe error |
| E3 | OTP expired; system requires resend/restart |
| E4 | Retry limit exceeded; system blocks context |
| E5 | Password mismatch; system blocks completion |
| E6 | Password policy failure; system shows policy guidance |
| E7 | Disabled Admin account; system blocks reset completion and directs to IT/operations support |

### 7.5 Postconditions

Successful postconditions:

- Admin password hash is updated.
- Reset context is closed/invalidated.
- Raw OTP is not stored.
- Admin can authenticate using the new password.
- Security/audit event is recorded where applicable.

Failure postconditions:

- No password is changed.
- Reset context remains pending, expired, or blocked according to failure type.
- No account is created.
- No temporary password is issued.

### 7.6 Explicit Exclusions

This use case excludes:

- Admin self-registration.
- Admin account creation from reset flow.
- Temporary password generation.
- Admin approval workflow.
- Student approval workflow.
- Company login.
- Admin Skill Master.
- Any operational Admin modules outside authentication.

---

## 8. Frontend Specification Addendum

### 8.1 Route Additions

Recommended route additions:

| Route | Access | Purpose |
|---|---|---|
| `/admin/login` | Public | Admin Sign-In |
| `/admin/forgot-password` | Public | Start Admin password reset |
| `/admin/verify-reset-otp` | Public with reset context | Verify Admin reset OTP |
| `/admin/create-password` | Public with verified reset context | Set new Admin password |
| `/admin/dashboard` | Admin-protected shell | Redirect target after successful Admin login |

### 8.2 Route Guard Rules

| Route Type | Rule |
|---|---|
| Public auth routes | Available without JWT |
| Student protected routes | Require authenticated user with `STUDENT` role |
| Admin protected routes | Require authenticated user with `ADMIN` role |
| Wrong role | Redirect to `/unauthorized` |
| Missing reset context | Redirect to corresponding forgot-password page |
| Verified reset context missing | Block password creation and redirect safely |

### 8.3 Admin Auth Feature Structure

Recommended feature folder:

```text
src/features/admin-auth/
  api/
    adminAuthApi.ts
  components/
    AdminLoginForm.tsx
    AdminForgotPasswordForm.tsx
    AdminResetOtpForm.tsx
    AdminCreatePasswordForm.tsx
  hooks/
    useAdminLogin.ts
    useStartAdminPasswordReset.ts
    useVerifyAdminResetOtp.ts
    useResendAdminResetOtp.ts
    useCompleteAdminPasswordReset.ts
  pages/
    AdminLoginPage.tsx
    AdminForgotPasswordPage.tsx
    AdminVerifyResetOtpPage.tsx
    AdminCreatePasswordPage.tsx
  schemas/
    adminAuthSchemas.ts
  types/
    adminAuthTypes.ts
  tests/
    admin-auth.test.tsx
```

Shared reusable components may live under `src/shared/ui` only if they do not contain business-specific behavior.

### 8.4 Admin Login Page UI

Admin Login page should follow the existing Admin login visual direction:

- Split layout.
- Left-side welcome panel with administrative greeting.
- Right-side login form card.
- Email field.
- Password field.
- Primary Log In button.
- Forgot Password link.
- Safe error display.
- Loading state during submission.

Required fields:

| Field | Validation |
|---|---|
| Admin Email Address | Required, email format |
| Security Password | Required |

Successful login behavior:

1. Call `POST /auth/admin/login`.
2. Store token according to team security decision.
3. Call or hydrate `/auth/me`.
4. Confirm role is `ADMIN`.
5. Redirect to `/admin/dashboard`.

Failure behavior:

- Invalid credentials show generic message.
- Non-Admin role is blocked.
- Disabled account shows safe support message.
- Network/API failure shows retry-safe message.

### 8.5 Admin Forgot Password Page UI

Route:

```text
/admin/forgot-password
```

Content:

- Page title: `Forgot Password?`
- Helper text: `Provide your administrator email address to request a One-Time Password.`
- Admin email input.
- Send OTP button.
- Back to Admin Login link.

Submission behavior:

```ts
startPasswordReset({
  accountType: 'ADMIN',
  email
})
```

### 8.6 Admin Reset OTP Page UI

Route:

```text
/admin/verify-reset-otp
```

Content:

- Six-digit OTP input.
- Verify button.
- Resend OTP button.
- Expired OTP message.
- Incorrect OTP message.
- Cooldown state after resend.

### 8.7 Admin Create Password Page UI

Route:

```text
/admin/create-password
```

Content:

- Page title: `Create your new password`
- New Password field.
- Confirm New Password field.
- Create Password button.
- Password match validation.
- Password policy helper text where required.

Success behavior:

- Show success state.
- Redirect to `/admin/login`.

Do not auto-login Admin after reset unless explicitly approved. Returning to login is safer and consistent with controlled authentication.

---

## 9. Backend and Database Implementation Addendum

### 9.1 Backend Modules Affected

| Module | Sprint 2 Responsibility |
|---|---|
| `auth` | Student/Admin login, logout, `/auth/me`, password reset coordination |
| `verification` | Student onboarding verification and OTP |
| `admin` | Predefined Admin account profile lookup only |
| `student` | Student account lookup and onboarding activation |
| `audit` | Security event logging hooks |
| `infrastructure/email` | OTP delivery adapter |
| `shared/security` | Password hashing, JWT, RBAC helpers |
| `shared/errors` | Safe error model |

### 9.2 Database Requirements

Minimum relevant tables/entities:

| Table/Entity | Purpose |
|---|---|
| `user_account` | Shared authentication account record |
| `role` | Includes `STUDENT` and `ADMIN` |
| `user_role` | Maps account to role |
| `admin_user` | Predefined Admin profile record |
| `student` / eligible student record | Student identity and verification source |
| `otp_context` or `password_reset_context` | OTP/reset metadata |
| `audit_event` | Security/audit event logging |

### 9.3 Admin Provisioning Rule

Admin accounts are provisioned by the development team.

Recommended controls:

1. Development/test may seed a known Admin account through Flyway or a controlled seed script.
2. Production-like environments should avoid committing real Admin passwords to version control.
3. Initial production Admin credentials should be injected through environment variables, deployment secrets, or a controlled bootstrap process.
4. Admin passwords must be stored only as strong password hashes.
5. Reset flow must update the password hash; it must not expose current passwords.

### 9.4 OTP Storage Rule

Store only:

- OTP hash.
- Reset/verification context ID.
- Account ID.
- Account type.
- Expiry timestamp.
- Attempt count.
- Resend count/cooldown.
- Status.

Never store:

- Raw OTP.
- Plain-text password.
- Temporary password.
- JWT secrets.
- Email service credentials.

### 9.5 Recommended Security Parameters

| Parameter | Recommended Value |
|---|---:|
| OTP length | 6 digits |
| OTP expiry | 5 minutes |
| OTP verify attempts | 3 attempts |
| Resend cooldown | 60 seconds |
| Max resend attempts | Configurable, recommended 3 |
| Password hash | BCrypt or Argon2id |
| Access token lifetime | Configurable, short-lived preferred |
| Error wording | Generic, non-enumerating |

---

## 10. Testing Addendum

### 10.1 Sprint 2 Required End-to-End Tests

| Test ID | Scenario | Expected Result |
|---|---|---|
| E2E-AUTH-001 | Eligible Student signs up | Verification succeeds and OTP flow starts |
| E2E-AUTH-002 | Student verifies OTP | Student can create initial password |
| E2E-AUTH-003 | Student logs in | Student reaches Student dashboard shell |
| E2E-AUTH-004 | Student resets forgotten password | Student can log in with new password |
| E2E-AUTH-005 | Predefined Admin logs in | Admin reaches Admin dashboard shell |
| E2E-AUTH-006 | Admin resets forgotten password | Admin can log in with new password |
| E2E-AUTH-007 | Student token attempts Admin route | Redirect/deny unauthorized |
| E2E-AUTH-008 | Admin token attempts Student-only owner route | Redirect/deny unauthorized where applicable |

### 10.2 Backend Tests

| Test | Required Coverage |
|---|---|
| Admin login success | Valid predefined Admin credentials issue Admin JWT |
| Admin login invalid password | Generic 401 |
| Admin login disabled account | Access denied |
| Admin reset start valid account | Reset context created and OTP sent |
| Admin reset start unknown email | Safe response/error, no account creation |
| Admin OTP invalid | Attempt count increments |
| Admin OTP expired | Reset blocked until resend/restart |
| Admin reset password mismatch | No password update |
| Admin reset success | Password hash changes and reset context closes |
| Student/Admin RBAC | Role claims enforced by backend |
| Negative-scope scan | No temporary password or Admin approval endpoints |

### 10.3 Frontend Tests

| Test | Required Coverage |
|---|---|
| Admin Login form validation | Required email/password, invalid email |
| Admin Login success redirect | `/admin/dashboard` |
| Admin Login wrong role | `/unauthorized` |
| Admin Forgot Password validation | Required email and safe response |
| Admin OTP input | six-digit validation, resend state |
| Admin Create Password | mismatch blocked |
| Reset context guard | direct access without context redirects |
| Removed-scope UI scan | no Admin self-register, approval, temporary password wording |

---

## 11. Sprint 2 Definition of Done

Sprint 2 is complete when all items below pass:

### 11.1 Student Authentication

- [ ] Student sign-up page works.
- [ ] Student verification modal supports loading, success, and failure states.
- [ ] OTP verify works.
- [ ] OTP resend works.
- [ ] OTP expiry handling works.
- [ ] Initial password creation works.
- [ ] Student login works.
- [ ] Student logout works.
- [ ] Student forgot-password reset works.
- [ ] Student role routes are protected.

### 11.2 Admin Authentication

- [ ] `/admin/login` works for predefined Admin accounts.
- [ ] Invalid Admin credentials show safe error.
- [ ] Admin token includes/returns `ADMIN` role context.
- [ ] `/auth/me` returns Admin user context after Admin login.
- [ ] Admin protected route guard works.
- [ ] `/admin/forgot-password` starts OTP reset for predefined Admin email.
- [ ] `/admin/verify-reset-otp` verifies Admin reset OTP.
- [ ] `/admin/create-password` updates Admin password hash.
- [ ] Admin can log in with the new password after reset.
- [ ] Unknown Admin email does not create an account.

### 11.3 Security and Guardrails

- [ ] No temporary password is generated.
- [ ] No Admin self-registration route exists.
- [ ] No Student approval route or UI exists.
- [ ] No pending/rejected registration status appears.
- [ ] Raw OTP is not stored.
- [ ] Raw password is not logged.
- [ ] Backend RBAC protects routes beyond frontend guards.
- [ ] Negative-scope scan passes.

---

## 12. Backlog Items

### 12.1 Backend Backlog

| ID | Task |
|---|---|
| BE-AUTH-001 | Implement/confirm `POST /auth/admin/login` |
| BE-AUTH-002 | Add predefined Admin seed/provisioning support |
| BE-AUTH-003 | Generalize password reset start to `STUDENT` and `ADMIN` account types |
| BE-AUTH-004 | Implement reset OTP verify/resend for Admin |
| BE-AUTH-005 | Implement Admin password reset completion |
| BE-AUTH-006 | Add Admin auth security/audit events |
| BE-AUTH-007 | Add Admin RBAC tests |
| BE-AUTH-008 | Add removed-scope endpoint scan |

### 12.2 Frontend Backlog

| ID | Task |
|---|---|
| FE-AUTH-001 | Build Admin Login page |
| FE-AUTH-002 | Build Admin Forgot Password page |
| FE-AUTH-003 | Build Admin Reset OTP page |
| FE-AUTH-004 | Build Admin Create Password page |
| FE-AUTH-005 | Add Admin auth API wrappers/hooks |
| FE-AUTH-006 | Add Admin route guards and redirects |
| FE-AUTH-007 | Add Admin auth validation schemas |
| FE-AUTH-008 | Add Admin auth unit/component tests |
| FE-AUTH-009 | Add Playwright Admin auth flow test |
| FE-AUTH-010 | Add removed-scope UI scan |

### 12.3 Documentation/Contract Backlog

| ID | Task |
|---|---|
| DOC-AUTH-001 | Store this addendum with sprint planning artifacts |
| DOC-AUTH-002 | Add OpenAPI password reset wording update to implementation notes |
| DOC-AUTH-003 | Add `WF-ADM-001A` to workflow traceability notes |
| DOC-AUTH-004 | Add `UC-ADM-001A` to use-case traceability notes |
| DOC-AUTH-005 | Update sprint board labels from Student auth to Complete auth |

---

## 13. Open Decisions

| Decision | Recommendation |
|---|---|
| Separate Admin reset endpoints vs shared reset endpoint | Use shared `/password-resets` with `accountType` |
| Admin reset success behavior | Redirect to `/admin/login`, do not auto-login |
| Admin password provisioning method | Dev/test seed; production bootstrap via secret-controlled process |
| OTP delivery in dev | Use local email adapter/MailHog or controlled dev-only display; never persist raw OTP |
| Token storage | Prefer memory or secure cookie depending backend decision; avoid unnecessary long-lived localStorage |
| Admin dashboard in Sprint 2 | Shell only for redirect verification; real metrics stay Sprint 6 |

---

## 14. Final Recommendation

Accept the supervisor-requested change as a **controlled Sprint 2 authentication expansion**.

Implementation is feasible if the team follows these boundaries:

1. Move Admin Sign-In into Sprint 2.
2. Add Admin password reset through OTP.
3. Keep Admin accounts predefined/provisioned by the development team.
4. Use shared password reset infrastructure with explicit `accountType`.
5. Do not add Admin self-registration, temporary passwords, or Student approval.
6. Keep Admin operational modules in Sprint 6 and later.

This addendum should be used as a companion implementation reference without rewriting or directly editing the existing baseline documents.
