# 45-Day Agile Sprint Implementation Plan

## CV Management and Deterministic Internship Candidate Filtering System

**Project context:** Undergraduate capstone implementation plan  
**Team size:** 2 full-time student developers  
**Total duration:** 45 days  
**Sprint model:** 9 short Scrum sprints × 5 days each  
**Frontend technology:** React + TypeScript, Vite, React Router, feature-by-feature structure  
**Backend technology:** Spring Boot modular monolith, PostgreSQL, Flyway migrations, REST/JSON APIs, JWT/RBAC, LaTeX/PDF CV generation  

---

## 1. Source Audit Summary

This plan was prepared after auditing the current project documentation and treating the reduced-scope baseline and SRS as the main controlling sources. The implementation plan follows the approved Version 1 scope and does not reintroduce removed features.

| Source audited | How it controls this implementation plan |
|---|---|
| Final Reduced Scope Baseline Document v1.1 | Root scope authority. This plan is built around OTP-based onboarding, Student-owned profile/skills/projects/CV management, Admin ledger, deterministic filtering, shortlist/export, and removed-scope guardrails. |
| Scope Reductions.docx | Confirms removal of Admin approval workflow, temporary password flow, Admin Skill Master page, and Admin skill CRUD/import. Confirms developer-managed skill taxonomy seed. |
| Software Requirements Specification v3.0.1 | Controls required functional modules, security/RBAC, negative requirements, validation, backend query behavior, and final acceptance expectations. |
| Student and Admin Workflow Document v1.0 | Controls Student and Admin page-to-page flows, system actions, failure states, and workflow testing expectations. |
| Production-Ready Use-Case Documentation v1.0 | Controls actor-goal behavior for Student, Department Admin, and supporting system services. |
| UI Frontend Specification v1.1 | Controls React + TypeScript page responsibilities, route guards, API integration, UI states, validation, dark mode, accessibility, and frontend testing gates. |
| Backend Module Documentation v1.0 | Controls Spring Boot modular monolith module boundaries, service responsibilities, transaction rules, RBAC, audit, LaTeX/PDF generation, and backend testing. |
| Database Design Document v1.0 and ERD Documentation v1.0 | Controls PostgreSQL schema direction, official academic ledger source of truth, many-to-many skill taxonomy mapping, CV versioning, shortlist data, and audit logging. |
| API Specification Document v1.0 and OpenAPI YAML | Controls REST/JSON endpoint groups, DTO direction, status/error model, RBAC boundaries, pagination/filtering/export behavior, and contract testing. |
| Student.docx, Admin.docx, Student Pages.zip, Admin Pages.zip, New DESIGN.md | Used only as UI/page/design evidence. They do not override the reduced-scope baseline or SRS. |
| Skill List Breakdown.pdf | Used as the structural taxonomy reference only. The final complete skill list is still developer-managed through backend seed/migration support. |

---

## 2. Fixed Scope Guardrails for All Sprints

The team must keep these rules visible during sprint planning, implementation, reviews, and testing.

### 2.1 Included Version 1 Scope

- Student sign-up using Full Name, Index Number, and University Email.
- Automatic student verification using Index Number + University Email against existing Level 3 and Level 4 records.
- OTP issue, OTP verification, OTP resend, password creation, login, and forgot-password reset.
- Student dashboard with profile/project/skill/GPA summary cards.
- Student-owned editable profile and CV-supporting data.
- Student declared skills selected from developer-managed skill taxonomy.
- Student project portfolio CRUD.
- Student CV Builder with save current CV version and ATS-compliant PDF download.
- Student read-only academic records and official Computer Science GPA view.
- Admin login and dashboard.
- Admin academic ledger upload, staging, validation, and commit.
- Admin registered Student search/filter/sort/pagination and read-only deep-dive.
- Admin read-only latest saved Student CV view/download.
- Admin company metadata and internship request management.
- Deterministic candidate filtering using latest committed official GPA and declared skills at runtime.
- Admin manual shortlist selection, shortlist finalization with warning guidance, and bulk latest saved CV export.
- Audit logs, RBAC, negative-scope tests, and final handover documentation.

### 2.2 Strictly Excluded Scope

Do **not** build or scaffold any of the following:

- Admin student approval workflow.
- Pending/rejected student registration states.
- Temporary password feature.
- Admin Skill Master page.
- Admin skill CRUD/import/upload page.
- Admin skill verification or verified skill status.
- Estimated GPA planner.
- CV submission to Admin.
- CV review, approval, rejection, or correction workflow.
- Company portal, company login, or company API role.
- AI scoring, AI ranking, match percentage, or automated final selection.
- Project approval or project verification.
- Hard shortlist capacity blocking.
- GPA stored inside internship request records.
- Frontend-only demo-state behavior in final implementation.

---

## 3. Agile Scrum Working Model

This plan uses Scrum-style short iterations. The project still has dependencies, but each sprint must produce a working increment that can be shown to the supervisor.

### 3.1 Sprint Rhythm

Each sprint lasts 5 days.

| Day | Activity |
|---:|---|
| Day 1 | Sprint Planning, backlog selection, task split, API contract check, branch setup. |
| Days 2–4 | Build, test, integrate, and fix increment. Each developer commits daily. |
| Day 5 | Sprint Review demo, bug triage, negative-scope check, retrospective, next sprint preparation. |

### 3.2 Daily Team Rules

- Hold a 15-minute daily stand-up.
- Update the sprint board before starting coding each day.
- Keep frontend and backend branches small and mergeable.
- Use OpenAPI contract and DTO names as the common integration language.
- Every sprint must include at least one integration test or end-to-end path check.
- Every sprint review must include a removed-scope check.

### 3.3 Developer Responsibility Split

| Developer | Main responsibility |
|---|---|
| Developer 1: Frontend Lead | React + TypeScript, Vite setup, routing, pages, components, UI states, form validation, API integration, frontend tests, accessibility checks. |
| Developer 2: Backend & Database Lead | PostgreSQL schema, Flyway migrations, Spring Boot modular monolith, REST APIs, JWT/RBAC, OTP/email service, LaTeX/PDF CV generation, backend tests, integration tests, documentation orchestration. |

---

## 4. 45-Day Sprint Roadmap

| Sprint | Days | Sprint name | Main working increment |
|---:|---:|---|---|
| 1 | 1–5 | Project Foundation and Scope Lock | Repositories, architecture skeletons, CI basics, database baseline, route/API skeletons. |
| 2 | 6–10 | Student Onboarding and Authentication | Student sign-up, auto-verification, OTP, password creation, login, forgot password. |
| 3 | 11–15 | Student Dashboard and Profile | Student protected shell, dashboard, editable profile, CV-supporting data. |
| 4 | 16–20 | Declared Skills and Projects | Skill taxonomy browsing, declared skills CRUD, project portfolio CRUD. |
| 5 | 21–25 | CV Builder and Student Academic View | Save/download ATS CV, CV freshness, academic records and GPA view. |
| 6 | 26–30 | Admin Foundation and Academic Ledger | Admin login/dashboard, ledger upload/staging/validation/commit, registered Student list. |
| 7 | 31–35 | Admin Inspection, Companies, and Requests | Student deep-dive, latest CV access, company metadata, internship request management. |
| 8 | 36–40 | Filtering, Shortlisting, and Bulk Export | Runtime GPA/skills filtering, manual shortlist, finalization, bulk CV export. |
| 9 | 41–45 | Hardening, Acceptance, and Handover | End-to-end QA, security checks, deployment readiness, documentation, final demo. |

---

# Sprint 1: Project Foundation and Scope Lock

**Duration:** 5 days, Days 1–5

## Sprint Goal

Create a clean project foundation so both developers can start building real features safely. By the end of this sprint, the frontend and backend repositories should run locally, connect to a PostgreSQL database, and show basic public/protected route skeletons without implementing removed scope.

## Developer 1 (Frontend) Work Packages

- Create the Vite + React + TypeScript frontend project.
- Configure strict TypeScript, ESLint, Prettier, and basic folder structure.
- Create the feature-based structure under `src/features`, `src/app`, and `src/shared`.
- Add React Router route groups:
  - Public auth routes.
  - Student protected routes.
  - Admin protected routes.
  - Unauthorized and not-found pages.
- Create layout shells:
  - `AuthLayout`.
  - `StudentLayout`.
  - `AdminLayout`.
- Add placeholder pages only for approved scope:
  - Student sign-up.
  - OTP verification.
  - Create password.
  - Student login.
  - Forgot password.
  - Student dashboard.
  - Student profile.
  - Student skills.
  - Student projects.
  - Student CV Builder.
  - Student academic records.
  - Admin login.
  - Admin dashboard.
  - Academic ledger.
  - Registered Students.
  - Student deep-dive.
  - Companies.
  - Internship requests.
  - Candidate filtering.
  - Shortlists and exports.
- Add shared UI primitives:
  - Button.
  - Text input.
  - Select input.
  - Modal shell.
  - Toast container.
  - Loading spinner.
  - Empty-state block.
  - Error-state block.
- Add global design tokens from the approved Material-inspired academic dashboard style.
- Add dark mode structure but keep business behavior out of it.
- Add frontend environment variable template for API base URL.
- Add a first removed-scope scan list inside frontend QA notes.

## Developer 2 (Backend & Database) Work Packages

- Create Spring Boot backend project with Java package baseline.
- Configure modular monolith package structure:
  - `auth`.
  - `verification`.
  - `student`.
  - `skills`.
  - `projects`.
  - `academic`.
  - `cv`.
  - `admin`.
  - `companies`.
  - `internships`.
  - `filtering`.
  - `shortlists`.
  - `audit`.
  - `shared`.
  - `infrastructure`.
- Configure PostgreSQL connection for local development.
- Configure Flyway migrations.
- Add initial migration skeleton for:
  - schemas/namespaces if used.
  - roles.
  - predefined Admin account placeholder.
  - audit table placeholder.
- Add base security dependencies for JWT/RBAC.
- Add global API response and error model skeleton.
- Add OpenAPI YAML to backend resources and documentation directory.
- Add basic health endpoint.
- Add Docker Compose file for local PostgreSQL if allowed by the team environment.
- Add backend test setup:
  - Unit test framework.
  - Integration test profile.
  - Testcontainers placeholder if the team can run Docker.
- Add backend removed-scope guardrail notes.

## Integration & Testing Focus

- Run frontend and backend locally at the same time.
- Confirm the frontend can call the backend health endpoint.
- Confirm PostgreSQL starts and Flyway migration runs successfully.
- Confirm route guards exist as UX protection, while backend RBAC remains the real security authority.
- Add a simple CI or local quality command:
  - Frontend type check.
  - Frontend lint.
  - Backend test.
  - Backend build.
- Negative-scope check:
  - No Admin approval page.
  - No temporary password page.
  - No Admin skill management route.
  - No company login route.
  - No AI/ranking folder.

## Sprint Deliverable / Definition of Done

The team can demonstrate a running frontend and backend, a connected database, basic navigation shells, and a clean repository structure that follows the approved scope. The supervisor can see that the project foundation is ready for feature work and does not contain removed-scope placeholders.

---

# Sprint 2: Student Onboarding and Authentication

**Duration:** 5 days, Days 6–10

## Sprint Goal

Build the complete Student account lifecycle: sign-up, automatic verification, OTP, resend OTP, password creation, login, logout, and forgot-password reset. By the end of the sprint, an eligible Student can create access and log in without Admin approval.

## Developer 1 (Frontend) Work Packages

- Build the Student sign-up page with:
  - Full Name field.
  - Index Number field.
  - University Email field.
  - Sign Up button.
- Add client-side validation:
  - Required fields.
  - Index number format check.
  - University email format check.
  - Full name used as display data only.
- Build the verification pop-up states:
  - “Your details are verifying ...” with spinner.
  - “Your details are verified ...” with success mark.
  - “There is an issue with your entered details. Please consider them again and try again.” with error mark and close button.
- Build the OTP verification page:
  - Six-digit OTP input.
  - Verify button.
  - Resend OTP button.
  - Expired OTP message.
  - Incorrect OTP message.
- Build the create-new-password page:
  - New Password field.
  - Confirm New Password field.
  - Create Password button.
  - Password match validation.
- Build the Student login page:
  - University Email field.
  - Password field.
  - Login button.
  - Forgot Password link.
- Build the forgot-password page:
  - University Email field.
  - Send OTP button.
  - Back to login link.
- Add auth state handling:
  - Store token securely according to team decision.
  - Fetch `/auth/me` after login.
  - Redirect Student to dashboard after login.
  - Show unauthorized page when role is wrong.
- Add loading, success, and error states for every auth form.
- Write frontend tests for form validation and route redirects.

## Developer 2 (Backend & Database) Work Packages

- Implement migrations for:
  - eligible student master records.
  - student account table.
  - verification context table.
  - OTP metadata table using hashed OTP only.
  - password hash storage.
  - role mapping for Student and Admin.
- Seed sample eligible Level 3 and Level 4 Student records for development/testing.
- Implement Student verification API:
  - Start verification using Full Name, Index Number, and University Email.
  - Validate Index Number + University Email against eligible Student records.
  - Ignore Full Name for identity verification; store/update it only as display name where allowed.
  - Return safe success/error responses.
- Implement OTP service:
  - Generate six-digit OTP.
  - Store OTP hash, expiry, attempt count, and status.
  - Send OTP through email adapter placeholder or local log adapter for development.
  - Resend OTP with rate-limit logic.
  - Never return or log raw OTP in production mode.
- Implement password creation API after OTP verification.
- Implement Student login API with JWT issue.
- Implement Admin login API skeleton for predefined Admin users.
- Implement forgot-password OTP request and reset flow.
- Add backend tests for:
  - No matching eligible record.
  - Invalid email/index format.
  - OTP success.
  - OTP invalid.
  - OTP expired.
  - OTP resend.
  - Password confirmation mismatch.
  - Login success/failure.
- Add security tests to confirm there is no Admin approval or temporary-password path.

## Integration & Testing Focus

- Connect frontend auth pages to backend APIs.
- Run a full Student onboarding test:
  1. Student enters sign-up data.
  2. Backend verifies against eligible records.
  3. OTP is issued.
  4. Student enters OTP.
  5. Student creates password.
  6. Student logs in.
  7. Student reaches dashboard shell.
- Test failure paths:
  - Wrong index/email.
  - Expired OTP.
  - Incorrect OTP.
  - Resend OTP.
  - Password mismatch.
  - Wrong login password.
- Negative-scope check:
  - No Admin approval needed at any step.
  - No pending/rejected status is shown.
  - No temporary password is generated.

## Sprint Deliverable / Definition of Done

A supervisor can create a Student account using an eligible test record, verify OTP, create a password, log in, log out, and reset a forgotten password. The flow is fully student-driven and contains no Admin approval workflow.

---

# Sprint 3: Student Dashboard and Profile

**Duration:** 5 days, Days 11–15

## Sprint Goal

Build the first usable Student portal increment. By the end of this sprint, a logged-in Student can view the dashboard and manage editable profile/CV-supporting data while protected identity and official academic fields remain read-only.

## Developer 1 (Frontend) Work Packages

- Build the Student protected layout with:
  - Sidebar or top navigation.
  - Student name display.
  - Logout action.
  - Active route highlighting.
- Build Student dashboard page with summary cards:
  - Portfolio Projects count.
  - Shortlisted Internships count.
  - Skills Count.
  - Official Cumulative GPA value.
- Add dashboard loading skeletons and empty states.
- Build Student profile page:
  - Editable preferred/full display name.
  - Editable profile picture/avatar area if supported by final UI.
  - Editable personal summary.
  - Editable contact details except University Email.
  - Editable links such as GitHub, LinkedIn, portfolio website where approved.
  - CV-supporting sections such as activities, awards, certificates, and experience where supported by the final UI.
- Keep Index Number and University Email read-only.
- Add profile edit modals or forms based on approved page evidence.
- Add form validation:
  - Required profile fields where applicable.
  - URL validation for links.
  - Safe length limits.
- Add success/error toasts after save.
- Add read-after-write refresh after profile update.
- Write frontend tests for:
  - Student route guard.
  - Read-only identity fields.
  - Profile save validation.
  - Dashboard loading and empty states.

## Developer 2 (Backend & Database) Work Packages

- Implement migrations for:
  - student profile details.
  - CV-supporting profile sections.
  - profile image/avatar metadata if supported.
  - audit events for profile update.
- Implement Student dashboard summary API:
  - Project count.
  - Declared skill count.
  - Shortlisted internship count.
  - Official GPA summary if available.
- Implement Student profile APIs:
  - Get own profile.
  - Update own editable profile data.
  - Add/update/delete CV-supporting profile entries where needed.
- Enforce object ownership:
  - Student can access only own profile.
  - Student cannot update Index Number.
  - Student cannot update University Email.
  - Student cannot update official academic records.
- Add CV freshness invalidation hook:
  - When profile/CV-supporting data changes, mark latest CV source data as changed or stale where applicable.
- Add backend tests for:
  - Own profile read/update.
  - Attempt to update protected identity fields.
  - Unauthorized profile access.
  - Dashboard summary values.
  - Audit event creation.

## Integration & Testing Focus

- Connect Student dashboard and profile UI to backend APIs.
- Test update flow:
  - Student logs in.
  - Student opens profile.
  - Student edits allowed data.
  - Backend saves data.
  - UI refreshes data.
  - Protected identity fields remain unchanged.
- Test dashboard metrics after changes.
- Test token expiry or unauthorized API response handling.
- Negative-scope check:
  - Admin cannot edit Student-owned profile data.
  - No CV approval workflow appears after profile updates.
  - No frontend-only profile state remains after refresh.

## Sprint Deliverable / Definition of Done

A logged-in Student can use a real dashboard and edit profile/CV-supporting data through the backend. Identity fields and official academic records remain protected and read-only.

---

# Sprint 4: Declared Skills and Projects

**Duration:** 5 days, Days 16–20

## Sprint Goal

Build Student skill and project management. By the end of the sprint, Students can select declared skills from the developer-managed taxonomy and manage portfolio projects, with no Admin skill management or project approval features.

## Developer 1 (Frontend) Work Packages

- Build Student Skills page with:
  - Skill cluster selector.
  - Skill category selector.
  - Skill search/filter field.
  - Available skills list.
  - Declared skills list.
  - Competency level selector: Beginner, Intermediate, Advanced.
- Add declared skill actions:
  - Add skill.
  - Update competency level.
  - Remove skill.
- Add duplicate prevention in the UI.
- Add loading skeletons for taxonomy and declared skills.
- Add clear empty states:
  - No declared skills yet.
  - No matching skill search results.
- Build Student Projects page with:
  - Project list.
  - Add project form/modal.
  - Edit project form/modal.
  - Delete confirmation modal.
  - Project title, description, technologies, date range, repository link, live link where approved.
- Add project validation:
  - Required title.
  - URL validation.
  - Reasonable description limits.
- Add frontend tests for:
  - Skill selection.
  - Competency update.
  - Skill delete.
  - Project CRUD form validation.
  - No verified-skill wording.

## Developer 2 (Backend & Database) Work Packages

- Implement migrations for skill taxonomy:
  - skill clusters.
  - skill categories.
  - skills.
  - skill-category many-to-many mapping.
  - student declared skills.
- Create developer-managed seed migration for the sample taxonomy structure.
- Add clear migration notes so the final complete Master Skill List can be inserted later by developers, not Admin users.
- Implement taxonomy read APIs:
  - Get clusters.
  - Get categories by cluster.
  - Get skills by category.
  - Search skills.
- Implement declared skills APIs:
  - Get my declared skills.
  - Add declared skill with competency level.
  - Update competency level.
  - Remove declared skill.
- Implement migrations for portfolio projects.
- Implement project portfolio APIs:
  - Get my projects.
  - Create project.
  - Update project.
  - Delete project.
- Enforce ownership for declared skills and projects.
- Add CV freshness invalidation when skills/projects change.
- Add backend tests for:
  - Taxonomy read-only behavior.
  - Declared skill duplicate prevention.
  - Competency enum validation.
  - Project CRUD ownership.
  - Attempted Admin skill CRUD route absence.
  - Attempted project approval state absence.

## Integration & Testing Focus

- Connect Skills page to taxonomy and declared skill APIs.
- Connect Projects page to project APIs.
- Test many-to-many skill mapping with cross-mapped skills.
- Test state refresh after add/update/delete.
- Test dashboard summary updates after skills/projects change.
- Negative-scope check:
  - No Admin Skill Master page.
  - No Admin skill CRUD/import route.
  - No verified-skill status.
  - No project approval/verification status.
  - Skill list is seeded by developer migration only.

## Sprint Deliverable / Definition of Done

A Student can manage declared skills and portfolio projects through real APIs. The taxonomy is read-only from the UI and maintained by developer-controlled database seed/migration scripts.

---

# Sprint 5: CV Builder and Student Academic View

**Duration:** 5 days, Days 21–25

## Sprint Goal

Build the Student CV and academic records increment. By the end of this sprint, a Student can view official academic records, see official GPA, generate a current ATS-compliant CV, save a CV version, and download it as a PDF.

## Developer 1 (Frontend) Work Packages

- Build Student CV Builder page:
  - CV data preview based on structured profile, declared skills, projects, and academic summary.
  - Save Current CV Version button.
  - Download PDF button.
  - Source-data freshness notice.
  - Clear warning when profile/skills/projects changed after the last saved CV.
- Add CV preview loading and generation states:
  - Preparing preview.
  - Saving CV version.
  - Generating PDF.
  - Download ready.
  - Generation failed.
- Build Student Academic Records page:
  - Academic summary card.
  - Official Computer Science GPA.
  - Semester/module records table.
  - Read-only notices.
- Add table states:
  - Loading.
  - Empty records.
  - API error.
- Add frontend tests for:
  - CV freshness warning.
  - Save CV action.
  - Download action trigger.
  - Academic records are read-only.
  - No CV submission to Admin wording.

## Developer 2 (Backend & Database) Work Packages

- Implement migrations for:
  - official student grades if not already present.
  - student academic summary.
  - CV version metadata.
  - generated file metadata.
  - CV freshness/source snapshot fields.
- Implement Student academic APIs:
  - Get own academic records.
  - Get own official GPA summary.
- Ensure academic records are read-only for Students.
- Implement CV generation module:
  - Build CV data aggregation service.
  - Create LaTeX template for ATS-compliant CV.
  - Generate PDF from structured Student data.
  - Save current CV version metadata.
  - Store generated file metadata safely.
  - Download latest saved CV.
- Implement CV freshness logic:
  - Detect whether profile, skills, projects, or relevant data changed after last saved CV.
  - Return freshness status to frontend.
- Add backend tests for:
  - Student academic record ownership.
  - Student cannot edit grades/GPA.
  - CV generation success.
  - CV generation failure handling.
  - Latest saved CV retrieval.
  - Freshness flag after profile/skills/projects changes.
- Add audit events for CV save/download.

## Integration & Testing Focus

- Connect CV Builder page to CV APIs.
- Connect academic records page to academic APIs.
- Run full Student path:
  1. Login.
  2. Edit profile.
  3. Add skills.
  4. Add project.
  5. Open CV Builder.
  6. See freshness notice.
  7. Save CV version.
  8. Download PDF.
  9. View academic records.
- Validate generated PDF is parser-friendly and avoids complex visual layout.
- Negative-scope check:
  - No “Submit CV for Admin Review”.
  - No CV approval or rejection state.
  - No Estimated GPA planner.
  - Student cannot edit academic records.

## Sprint Deliverable / Definition of Done

A Student can generate and download an ATS-compliant CV and view official academic records/GPA. The CV workflow is self-service and has no Admin review or approval step.

---

# Sprint 6: Admin Foundation and Academic Ledger

**Duration:** 5 days, Days 26–30

## Sprint Goal

Build the first usable Admin portal increment. By the end of this sprint, a predefined Admin can log in, view dashboard metrics, upload an academic ledger, validate staged rows, commit official grades, and inspect registered Student records through search and pagination.

## Developer 1 (Frontend) Work Packages

- Build Admin login page:
  - Admin Email Address field.
  - Security Password field.
  - Login button.
  - Error states.
- Build Admin protected layout:
  - Admin navigation.
  - Logout.
  - Active page indicator.
- Build Admin dashboard page with summary cards:
  - Total Students.
  - Registered Students.
  - Internship Requests Created.
- Build Academic Ledger page:
  - File upload control.
  - Upload instructions.
  - Staging table.
  - Validation result indicators.
  - Commit button.
  - Rollback/cancel staged upload action where approved.
  - Error summary for invalid rows.
- Build Registered Students page:
  - Search by name/index/email.
  - Filter by level where supported.
  - Sort controls.
  - Pagination.
  - Table row click to future deep-dive route.
- Add loading, upload progress, validation, commit, and error states.
- Add frontend tests for:
  - Admin route guard.
  - Ledger upload form states.
  - Registered Student search/pagination.
  - Student cannot access Admin pages.

## Developer 2 (Backend & Database) Work Packages

- Finalize Admin authentication with predefined Admin users.
- Implement Admin dashboard APIs:
  - Total initialized Students.
  - Registered Students.
  - Internship request count.
- Implement academic ledger migrations:
  - ledger upload table.
  - staging rows table.
  - official grades table.
  - academic summary table.
  - audit logs for upload/validate/commit.
- Implement ledger upload API:
  - Accept approved file type.
  - Validate file size.
  - Parse rows.
  - Stage rows.
  - Return validation summary.
- Implement ledger validation logic:
  - Existing Student index check.
  - Subject/module code check where applicable.
  - Grade and credits validation.
  - Duplicate handling.
  - Error row reporting.
- Implement ledger commit API:
  - Commit valid staged rows transactionally.
  - Recalculate official Computer Science GPA summary.
  - Prevent concurrent commits for same upload.
  - Audit commit success/failure.
- Implement Registered Students list API:
  - Search.
  - Filter.
  - Sort.
  - Pagination.
  - Server-side query behavior.
- Add backend tests for:
  - Admin-only ledger mutation.
  - Student forbidden from ledger upload/commit.
  - Invalid ledger rows.
  - Commit transaction rollback on failure.
  - GPA recalculation after commit.
  - Registered Students pagination.

## Integration & Testing Focus

- Connect Admin pages to backend APIs.
- Run complete ledger flow:
  1. Admin logs in.
  2. Admin uploads ledger file.
  3. Backend stages and validates rows.
  4. UI displays valid/invalid rows.
  5. Admin commits valid upload.
  6. Student academic GPA summary updates.
- Test concurrent commit prevention manually or through backend tests.
- Negative-scope check:
  - Admin cannot approve Student registrations.
  - Admin cannot edit Student profile/skills/projects.
  - Ledger upload is not a Student action.
  - No GPA is added to internship request records.

## Sprint Deliverable / Definition of Done

A predefined Admin can manage official academic ledger data through upload, staging, validation, and commit. Students can only view the resulting academic records/GPA through their own portal.

---

# Sprint 7: Admin Inspection, Companies, and Internship Requests

**Duration:** 5 days, Days 31–35

## Sprint Goal

Complete Admin operational setup before filtering. By the end of the sprint, Admin can inspect Students read-only, access latest saved CVs, manage company metadata, and create internship requests without GPA criteria.

## Developer 1 (Frontend) Work Packages

- Build Student Deep-Dive page for Admin:
  - Student identity summary.
  - Profile summary.
  - Declared skills list.
  - Projects list.
  - Academic summary.
  - Latest saved CV download/view action.
  - Clear read-only labels.
- Add deep-dive loading, not-found, and error states.
- Build Companies page:
  - Company list.
  - Add company form.
  - Edit company form.
  - Delete/deactivate confirmation if approved by API.
  - Search and pagination where needed.
- Build Internship Requests page:
  - Request list.
  - Create request form.
  - Edit request form.
  - Close/archive request action where approved.
  - Required skill selector using taxonomy.
  - Role/title, company, description, deadline, notes, and capacity guidance fields where approved.
- Ensure internship request form does **not** include GPA fields.
- Add frontend tests for:
  - Admin deep-dive read-only behavior.
  - Latest CV download button behavior.
  - Company form validation.
  - Internship request form without GPA field.
  - Student cannot access Admin deep-dive.

## Developer 2 (Backend & Database) Work Packages

- Implement Admin Student Inspection APIs:
  - Get registered Student list details.
  - Get Student deep-dive read-only data.
  - Get latest saved Student CV metadata.
  - Download latest saved Student CV as Admin.
- Enforce read-only Admin access to Student data.
- Implement company metadata migrations and APIs:
  - Create company.
  - Read company list/detail.
  - Update company.
  - Delete/deactivate company if approved by scope.
- Implement internship request migrations and APIs:
  - Create request.
  - Read request list/detail.
  - Update request.
  - Close/archive request if approved by scope.
  - Map required skills to request.
- Ensure internship request persistence excludes GPA fields.
- Implement server-side pagination/search for companies and requests.
- Add audit logs for Admin CV download and internship request changes.
- Add backend tests for:
  - Admin read-only deep-dive.
  - Admin CV download without review state.
  - Company metadata CRUD.
  - Internship request CRUD without GPA field.
  - Required skills mapping.
  - Student forbidden from Admin APIs.

## Integration & Testing Focus

- Connect deep-dive page to backend data.
- Connect latest CV download from Admin deep-dive.
- Connect company and internship request forms to APIs.
- Test that Admin can create an internship request using required skills but no GPA criteria.
- Test request listing with server-side search/pagination.
- Negative-scope check:
  - No company login or company account table/route.
  - No Admin editing of Student-owned data.
  - No CV review/approval wording.
  - No GPA field in internship request UI, DTO, database table, or API response.

## Sprint Deliverable / Definition of Done

Admin can inspect Students read-only, download latest saved CVs, maintain company metadata, and manage internship requests without GPA request data. The system is ready for candidate filtering.

---

# Sprint 8: Filtering, Shortlisting, and Bulk Export

**Duration:** 5 days, Days 36–40

## Sprint Goal

Implement the core project differentiator: deterministic internship candidate filtering and shortlist export. By the end of this sprint, Admin can filter candidates using official GPA and declared skills at runtime, manually select candidates, finalize a shortlist with guidance warning, and export latest saved CVs in bulk.

## Developer 1 (Frontend) Work Packages

- Build Candidate Filtering page:
  - Select internship request.
  - Runtime minimum GPA filter.
  - Runtime maximum GPA filter if needed.
  - Required/optional declared skills selector based on taxonomy/request skills.
  - Search/filter results table.
  - Candidate result cards or table rows.
  - Clear explanation that filtering is deterministic, not AI scoring.
- Display candidate fields:
  - Student name.
  - Index number.
  - Official GPA.
  - Matching declared skills.
  - Project count or profile summary where approved.
  - Latest CV availability.
- Add manual selection controls:
  - Select/unselect candidate.
  - View selected list.
  - Confirm shortlist draft.
- Build Shortlist page:
  - Shortlist details.
  - Selected candidates.
  - Non-blocking guidance warning.
  - Finalize shortlist button.
  - Export latest saved CVs button.
  - Export status and download link.
- Ensure no AI rank, score, match percentage, or automatic final selection is displayed.
- Add frontend tests for:
  - Runtime GPA filter inputs only on filtering page.
  - Filtering results render.
  - Manual candidate selection.
  - Finalization warning.
  - Bulk export trigger.
  - No hard capacity block.

## Developer 2 (Backend & Database) Work Packages

- Implement deterministic filtering service:
  - Use latest committed official GPA from academic summary.
  - Use Student declared skills at runtime.
  - Use internship request skills where applicable.
  - Accept GPA only as filter/query parameters, not request persistence.
  - Return deterministic result set without AI score/ranking.
- Implement filtering API:
  - Request ID.
  - Runtime GPA parameters.
  - Skill parameters.
  - Pagination/sort where approved.
- Implement shortlist migrations:
  - shortlist header.
  - shortlist candidates.
  - finalization metadata.
  - export metadata.
- Implement shortlist APIs:
  - Create/update shortlist draft.
  - Add/remove selected candidates.
  - Finalize shortlist.
  - Return non-blocking warning information.
- Implement bulk export API:
  - Collect latest saved CVs for selected candidates.
  - Generate ZIP or approved archive format.
  - Handle missing latest CV safely.
  - Store export metadata.
  - Return download link or file stream.
- Add audit events for filtering, shortlist finalization, and export.
- Add backend tests for:
  - Filtering by committed GPA.
  - Filtering by declared skills.
  - No GPA read from internship request data.
  - No AI score/ranking field in result DTO.
  - Manual shortlist selection.
  - Finalize shortlist without hard capacity block.
  - Bulk export with latest saved CVs.
  - Missing CV handling.

## Integration & Testing Focus

- Run complete Admin filtering flow:
  1. Admin logs in.
  2. Admin selects internship request.
  3. Admin enters runtime GPA and skill filters.
  4. Backend returns deterministic candidates.
  5. Admin manually selects candidates.
  6. Admin sees guidance warning.
  7. Admin finalizes shortlist.
  8. Admin exports latest saved CVs in bulk.
- Check that Student GPA changes after ledger commit are reflected in filtering results.
- Check that Student skill changes are reflected after refresh.
- Negative-scope check:
  - No AI ranking/scoring/match percentage.
  - No automated selection.
  - No hard shortlist blocking.
  - No GPA in internship request persistence.
  - No CV review workflow.

## Sprint Deliverable / Definition of Done

Admin can deterministically filter candidates, manually create and finalize a shortlist, and export latest saved ATS-compliant CVs in bulk. This is the main supervisor-demo feature for the project.

---

# Sprint 9: Hardening, Acceptance, and Handover

**Duration:** 5 days, Days 41–45

## Sprint Goal

Stabilize the full system for final handover. By the end of this sprint, the team should have a tested, documented, deployable Version 1 system with supervisor-ready demo data, acceptance evidence, and no removed-scope leakage.

## Developer 1 (Frontend) Work Packages

- Complete UI polish across all approved pages:
  - Consistent spacing.
  - Consistent typography.
  - Consistent button states.
  - Accessible labels.
  - Keyboard-friendly modals.
  - Responsive mobile/tablet behavior.
- Complete dark mode checks if included in final UI.
- Add final route-level loading and error fallbacks.
- Add frontend negative-scope route tests:
  - No Admin approval route.
  - No temporary password route.
  - No Admin Skill Master route.
  - No company login route.
  - No CV review route.
  - No Estimated GPA route.
- Run Playwright end-to-end tests for:
  - Student onboarding.
  - Student profile/skills/projects/CV.
  - Student academic records.
  - Admin ledger.
  - Admin request management.
  - Filtering/shortlist/export.
- Fix UI bugs found during supervisor-style demo rehearsal.
- Prepare frontend README:
  - Setup commands.
  - Environment variables.
  - Test commands.
  - Build command.
  - Route list.
  - Scope guardrails.

## Developer 2 (Backend & Database) Work Packages

- Complete backend hardening:
  - Final RBAC checks.
  - Object ownership checks.
  - Validation messages.
  - Error response consistency.
  - Audit event coverage.
  - Logging without secrets.
- Run backend test suite:
  - Unit tests.
  - Integration tests.
  - API tests.
  - Security tests.
  - Migration tests.
- Run database checks:
  - All migrations apply from empty database.
  - Seed data works.
  - No forbidden columns/tables exist.
  - Indexes support search/filter/pagination.
- Validate OpenAPI contract against implemented APIs.
- Prepare deployment-ready configuration:
  - Development profile.
  - Test profile.
  - Production-like environment variable template.
  - No hardcoded secrets.
- Prepare backend README:
  - Setup commands.
  - Database setup.
  - Migration command.
  - Test command.
  - API documentation location.
  - Scope guardrails.
- Prepare final handover notes:
  - Known assumptions.
  - Open decisions.
  - Final skill list migration procedure.
  - Ledger file format assumptions.
  - CV generation dependency notes.

## Integration & Testing Focus

- Run full end-to-end acceptance scenario:
  1. Seed eligible Students and predefined Admin.
  2. Student signs up with auto-verification and OTP.
  3. Student creates password and logs in.
  4. Student edits profile, declared skills, and projects.
  5. Student saves/downloads CV.
  6. Admin logs in.
  7. Admin uploads and commits academic ledger.
  8. Student views GPA.
  9. Admin creates company and internship request.
  10. Admin filters candidates using runtime GPA and declared skills.
  11. Admin finalizes shortlist.
  12. Admin exports latest saved CVs in bulk.
- Run final negative-scope audit:
  - UI route scan.
  - API endpoint scan.
  - DTO/schema scan.
  - Database table/column scan.
  - Test name scan.
  - Documentation wording scan.
- Run final supervisor demo rehearsal.
- Fix only critical bugs; do not add new scope.

## Sprint Deliverable / Definition of Done

A production-ready Version 1 handover package is ready. The system can be demonstrated from Student onboarding to Admin filtering and CV export. All required tests pass, documentation is complete, and removed-scope features are absent.

---

## 5. Final Acceptance Checklist

Use this checklist during Sprint 9 before final handover.

| Area | Acceptance check | Pass condition |
|---|---|---|
| Student onboarding | Student can sign up through auto-verification and OTP. | Works without Admin approval. |
| OTP | OTP verify, resend, expiry, and invalid OTP are handled. | All success and failure paths tested. |
| Password lifecycle | Student creates password and can reset forgotten password through OTP. | No temporary password flow exists. |
| Student profile | Student can edit own allowed profile/CV-supporting data. | Index Number, University Email, and academic records remain protected. |
| Declared skills | Student can manage declared skills and competency levels. | No verified skill status exists. |
| Skill taxonomy | Taxonomy is read from developer-managed seed/migration data. | No Admin Skill Master or Admin skill CRUD exists. |
| Projects | Student can manage portfolio projects. | No project approval/verification exists. |
| CV Builder | Student can save and download an ATS-compliant CV. | No CV submission/review/approval workflow exists. |
| Academic records | Student can view official grades/GPA. | Student cannot edit academic records. |
| Admin ledger | Admin can upload, stage, validate, and commit official ledger data. | Commit updates official records and GPA. |
| Admin inspection | Admin can inspect Student data read-only. | Admin cannot edit Student-owned data. |
| Admin CV access | Admin can view/download latest saved CV. | Access does not create review/approval states. |
| Companies | Admin can manage company metadata. | No company portal/login/API role exists. |
| Internship requests | Admin can manage internship requests. | GPA is not stored in request data. |
| Filtering | Admin can filter deterministically using runtime GPA and declared skills. | Uses latest committed official GPA. |
| Shortlisting | Admin manually selects and finalizes shortlist. | No automated selection and no hard shortlist blocking. |
| Bulk export | Admin can export latest saved CVs for shortlisted candidates. | Missing CVs are handled safely. |
| Security | JWT/RBAC and object ownership are enforced. | Student/Admin cannot access wrong data. |
| Persistence | Final implementation uses backend/database persistence. | No frontend-only demo state remains. |
| Documentation | README, setup, test, scope guardrails, and handover notes are complete. | Supervisor can run and review the system. |

---

## 6. Suggested Final Demo Script

Use this final flow for supervisor demonstration.

1. Show the scope guardrail slide/page or README section.
2. Register a Student with Full Name, Index Number, and University Email.
3. Show automatic verification and OTP flow.
4. Create the Student password and log in.
5. Update Student profile.
6. Add declared skills.
7. Add portfolio projects.
8. Save and download the Student CV.
9. Show Student academic records page.
10. Log in as Admin.
11. Upload, validate, and commit academic ledger.
12. Show updated Student GPA.
13. Create company metadata.
14. Create internship request without GPA data.
15. Run deterministic filtering using runtime GPA and declared skills.
16. Manually select candidates.
17. Finalize shortlist with guidance warning.
18. Export latest saved CVs in bulk.
19. Show the negative-scope checklist proving excluded features are absent.

---

## 7. Main Risks and Simple Mitigations

| Risk | Why it matters | Simple mitigation |
|---|---|---|
| OTP/email delivery delay | Student onboarding depends on OTP. | Use a local development email/log adapter first, then connect real email later. |
| LaTeX/PDF setup difficulty | CV generation may fail if PDF dependencies are not configured. | Build a small CV template early in Sprint 5 and keep a fallback error path. |
| Ledger file format uncertainty | Admin ledger import can become complex. | Freeze one supported CSV/XLSX format before Sprint 6. |
| Skill list not final | Final taxonomy content is pending. | Implement seed/migration structure now and load final list later through developer migration. |
| Filtering misunderstood as ranking | AI/ranking is excluded. | Label the UI clearly as deterministic filtering and do not show scores or match percentages. |
| Two developers overloaded | Scope is broad for 45 days. | Keep each sprint demo-focused and avoid adding future-phase features. |
| Integration delayed | Frontend/backend may drift. | Use OpenAPI and agree DTOs before coding each feature. |
| Removed scope accidentally returns | Old documents and wireframes may contain legacy wording. | Run negative-scope checks every sprint review. |

---

## 8. Final Handover Package

At the end of Day 45, the team should submit:

- Frontend repository.
- Backend repository.
- PostgreSQL migration scripts.
- OpenAPI YAML contract.
- Sample seed data for demo.
- README for frontend setup.
- README for backend setup.
- Environment variable templates.
- Test evidence summary.
- Final negative-scope checklist.
- Supervisor demo script.
- Known assumptions and open decisions.
- Final skill taxonomy migration instructions.
- Ledger file format instructions.

---

## 9. Final Sprint Plan Control Statement

This 45-day sprint plan is intentionally limited to the approved Version 1 scope. Any new feature request must first be checked against the reduced-scope baseline and SRS. If a requested task introduces Admin approval, temporary passwords, Admin skill management, verified skill status, Estimated GPA planning, CV review/approval, company login, AI ranking/scoring, hard shortlist blocking, GPA inside internship requests, or frontend-only final demo state, it must be rejected or moved to a future phase outside this Version 1 implementation plan.
