# Backend Folder Structure Audit and Complete Directory Structure

**Project:** CV Management and Deterministic Internship Candidate Filtering System  
**Audited source:** `Backend_Folder_Structure_Implementation_Plan_v1.0.docx`  
**Output purpose:** Audit the backend folder-structure plan and extract a complete implementation-ready backend directory structure as Markdown.

> Note: The request said “frontend directory structure,” but the document provided for audit is the backend folder-structure plan. This file therefore extracts and organizes the **backend** directory structure.

---

## 1. Audit Verdict

**Overall status:** Accepted as a strong backend implementation baseline, with minor cleanup actions recommended before coding.

The document is structurally complete for a Spring Boot modular-monolith backend. It defines the repository root, Java package baseline, shared and infrastructure packages, module package standard, backend module catalogue, required module files, Flyway migrations, seed files, tests, configuration files, security/RBAC structure, OpenAPI contract locations, observability/audit structure, CI/CD files, removed-scope guardrails, open implementation decisions, and a final acceptance checklist.

The architecture decision is also appropriate for the project scope: a single Spring Boot backend repository using package-by-feature/domain modules instead of global controller/service/repository folders. Controllers, services, repositories, entities, mappers, DTOs, and tests are placed in clear module-specific boundaries.

---

## 2. Completeness Audit

| Area Checked | Audit Result | Notes |
|---|---|---|
| Repository root | Complete | Includes `.github`, `.mvn`, `docker`, `docs`, `scripts`, `src`, and root governance files. |
| Java root package | Complete | Uses `lk.ac.ruhuna.dcs.cvmanagement` with `CvManagementApplication.java` at the scan root. |
| Modular-monolith package layout | Complete | Uses `modules/<module>/api`, `application`, `domain`, `mapper`, and `persistence`. |
| Controller/service/repository placement | Correct | Controllers are inside module `api`; services are inside module `application`; repositories are inside module `persistence/repository`. |
| Shared kernel | Mostly complete | Shared utilities are listed clearly. Keep this limited to reusable primitives only. |
| Infrastructure adapters | Complete | Email, JWT, LaTeX/PDF, storage, persistence helpers, and observability adapters are included. |
| Backend modules | Complete | Covers auth, verification, student profile, skills, projects, academics, CV, admin student inspection, companies, internships, filtering, shortlists, exports, and audit logging. |
| Database migrations | Complete as placeholders | Flyway sequence V001 to V020 is defined. Final SQL must still be aligned with ERD and Database Design before coding. |
| Skill taxonomy seed | Correct | Uses developer-controlled migration/seed strategy; no Admin skill CRUD/import is introduced. |
| Tests | Complete baseline | Includes architecture, support, module, integration, and removed-scope guardrail tests. |
| Configuration files | Complete baseline | Covers local/dev/test/prod profiles, logging, messages, OpenAPI, templates, and banner. |
| Security/RBAC | Complete baseline | Public endpoints are limited to auth/verification/reset flows; protected endpoints require JWT/RBAC. |
| API contract | Complete but needs formatting cleanup | OpenAPI YAML is stored under both `docs/api` and `src/main/resources/openapi`. The table in the source document has duplicated headers. |
| Observability/audit | Complete baseline | Correlation IDs, metrics, structured logging, audit publishing, and secret redaction are covered. |
| Removed-scope guardrails | Strong | Explicitly blocks Admin approval, temporary passwords, Admin Skill Master, AI ranking, company login, CV review workflow, and GPA stored in internship request data. |
| Acceptance checklist | Complete | Strong pre-code validation checklist is included. |

---

## 3. Accuracy Audit

### 3.1 Confirmed Correct

- The document correctly uses a **single Spring Boot modular monolith**, not microservices.
- The selected package model is suitable for this project because it keeps Student, Admin, CV, filtering, shortlist, export, and audit responsibilities separated.
- The service layer is correctly placed inside each module’s `application` package, not in a global `service` folder.
- The controller layer is correctly placed inside each module’s `api` package.
- The repository layer is correctly placed inside each module’s `persistence/repository` package.
- The backend structure supports the locked Version 1 scope without introducing removed legacy features.
- The Flyway migration sequence is consistent with the project domains: auth, student identity, profile, skill taxonomy, projects, academic ledger, CV versioning, companies, internship requests, filtering, shortlists, exports, audit, indexes, constraints, and seeds.
- The CV module correctly keeps ATS-compliant CV generation backend-controlled.
- The internship request module correctly excludes GPA fields from persisted internship request data.
- The filtering module correctly treats GPA as a runtime filtering parameter, not stored request data.
- The shortlist module correctly uses manual selection and non-blocking guidance, not automated selection or hard blocking.

### 3.2 Corrections / Cleanup Recommended

| Priority | Finding | Recommended Action |
|---:|---|---|
| High | The user-facing wording in the request says “frontend,” but the audited document is backend. | Treat this extracted structure as backend. Rename consistently as `backend_directory_structure_audit.md`. |
| Medium | API Contract Structure table has duplicated columns: `Contract Location | Contract Location | Purpose | Purpose`. | Clean the source document table to `Contract Location | Purpose`. |
| Medium | `CvFreshnessUpdatePort.java` is shown under `domain/policy`; semantically it behaves like a port. | Move it to `application/port` or create `domain/port` if the team wants domain-owned ports. |
| Medium | Some package names differ from shorthand sprint wording, for example `studentprofile`, `adminstudents`, `auditlog`. | Lock these as canonical backend package names and avoid alternate names like `student`, `admin`, or `audit` in implementation prompts. |
| Medium | Several placeholder folders are intentionally empty. | Keep them only if the team will use them soon; otherwise Java cannot track empty folders without marker files. Consider adding `.gitkeep` or `package-info.java` where needed. |
| Low | Open implementation decisions remain unresolved. | Resolve Spring Boot version, SMTP provider, generated-file storage, export execution mode, final skill seed, retention rules, deployment topology, and monitoring stack before production build. |

---

## 4. Complete Backend Directory Structure

```text
cv-management-backend/
├── .github/
│   └── workflows/
│       ├── ci.yml
│       ├── code-quality.yml
│       └── dependency-check.yml
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── docker/
│   ├── Dockerfile
│   ├── docker-compose.dev.yml
│   ├── docker-compose.test.yml
│   ├── postgres/
│   │   ├── init/
│   │   │   └── 001_create_extensions.sql
│   │   └── README.md
│   └── README.md
├── docs/
│   ├── api/
│   │   ├── CV_Management_API_OpenAPI_v1.0.yaml
│   │   └── api-contract-notes.md
│   ├── architecture/
│   │   ├── backend-architecture.md
│   │   ├── module-boundaries.md
│   │   ├── dependency-rules.md
│   │   ├── package-structure.md
│   │   └── removed-scope-guardrails.md
│   ├── adr/
│   │   ├── ADR-0001-modular-monolith.md
│   │   ├── ADR-0002-postgresql-flyway.md
│   │   ├── ADR-0003-jwt-rbac.md
│   │   ├── ADR-0004-backend-controlled-cv-generation.md
│   │   └── ADR-0005-no-removed-scope-features.md
│   ├── runbook/
│   │   ├── local-development.md
│   │   ├── database-migrations.md
│   │   ├── deployment.md
│   │   ├── rollback.md
│   │   ├── monitoring.md
│   │   └── incident-response.md
│   └── testing/
│       ├── testing-strategy.md
│       ├── contract-testing.md
│       └── security-testing.md
├── scripts/
│   ├── dev-start.sh
│   ├── dev-stop.sh
│   ├── run-tests.sh
│   ├── validate-openapi.sh
│   ├── migrate-local.sh
│   └── generate-local-secrets.example.sh
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── lk/
│   │   │       └── ac/
│   │   │           └── ruhuna/
│   │   │               └── dcs/
│   │   │                   └── cvmanagement/
│   │   │                       ├── CvManagementApplication.java
│   │   │                       ├── config/
│   │   │                       │   ├── AsyncConfig.java
│   │   │                       │   ├── AuditConfig.java
│   │   │                       │   ├── ClockConfig.java
│   │   │                       │   ├── CorsConfig.java
│   │   │                       │   ├── JacksonConfig.java
│   │   │                       │   ├── JpaConfig.java
│   │   │                       │   ├── MailConfig.java
│   │   │                       │   ├── OpenApiConfig.java
│   │   │                       │   ├── PaginationConfig.java
│   │   │                       │   ├── ProblemDetailsConfig.java
│   │   │                       │   ├── SecurityConfig.java
│   │   │                       │   ├── StorageConfig.java
│   │   │                       │   ├── TransactionConfig.java
│   │   │                       │   └── WebMvcConfig.java
│   │   │                       ├── shared/
│   │   │                       │   ├── api/
│   │   │                       │   │   ├── ApiPaths.java
│   │   │                       │   │   ├── ApiVersion.java
│   │   │                       │   │   ├── PageRequestDto.java
│   │   │                       │   │   ├── PageResponseDto.java
│   │   │                       │   │   ├── SortDirection.java
│   │   │                       │   │   └── package-info.java
│   │   │                       │   ├── audit/
│   │   │                       │   │   ├── AuditEventPublisher.java
│   │   │                       │   │   ├── AuditEventType.java
│   │   │                       │   │   ├── AuditableAction.java
│   │   │                       │   │   └── package-info.java
│   │   │                       │   ├── domain/
│   │   │                       │   │   ├── BaseEntity.java
│   │   │                       │   │   ├── OptimisticLockingEntity.java
│   │   │                       │   │   ├── SoftDeleteState.java
│   │   │                       │   │   └── package-info.java
│   │   │                       │   ├── error/
│   │   │                       │   │   ├── ApiErrorCode.java
│   │   │                       │   │   ├── ApplicationException.java
│   │   │                       │   │   ├── BadRequestException.java
│   │   │                       │   │   ├── ConflictException.java
│   │   │                       │   │   ├── DependencyUnavailableException.java
│   │   │                       │   │   ├── ForbiddenException.java
│   │   │                       │   │   ├── GlobalExceptionHandler.java
│   │   │                       │   │   ├── NotFoundException.java
│   │   │                       │   │   ├── ProblemDetailsFactory.java
│   │   │                       │   │   ├── UnauthorizedException.java
│   │   │                       │   │   ├── ValidationException.java
│   │   │                       │   │   └── package-info.java
│   │   │                       │   ├── file/
│   │   │                       │   │   ├── FileAssetReference.java
│   │   │                       │   │   ├── FileContentType.java
│   │   │                       │   │   ├── FileDownloadToken.java
│   │   │                       │   │   └── package-info.java
│   │   │                       │   ├── pagination/
│   │   │                       │   │   ├── PageCriteria.java
│   │   │                       │   │   ├── PageMapper.java
│   │   │                       │   │   ├── SearchCriteria.java
│   │   │                       │   │   ├── SortCriteria.java
│   │   │                       │   │   └── package-info.java
│   │   │                       │   ├── security/
│   │   │                       │   │   ├── CurrentActor.java
│   │   │                       │   │   ├── CurrentActorProvider.java
│   │   │                       │   │   ├── ObjectOwnershipGuard.java
│   │   │                       │   │   ├── Permission.java
│   │   │                       │   │   ├── RoleName.java
│   │   │                       │   │   └── package-info.java
│   │   │                       │   ├── time/
│   │   │                       │   │   ├── TimeProvider.java
│   │   │                       │   │   └── package-info.java
│   │   │                       │   ├── validation/
│   │   │                       │   │   ├── ValidationMessages.java
│   │   │                       │   │   ├── UniversityEmail.java
│   │   │                       │   │   ├── IndexNumber.java
│   │   │                       │   │   └── package-info.java
│   │   │                       │   └── util/
│   │   │                       │       ├── SanitizationUtils.java
│   │   │                       │       ├── StringNormalizer.java
│   │   │                       │       └── package-info.java
│   │   │                       ├── infrastructure/
│   │   │                       │   ├── email/
│   │   │                       │   │   ├── EmailSender.java
│   │   │                       │   │   ├── SmtpEmailSender.java
│   │   │                       │   │   ├── EmailTemplateRenderer.java
│   │   │                       │   │   └── package-info.java
│   │   │                       │   ├── jwt/
│   │   │                       │   │   ├── JwtAuthenticationFilter.java
│   │   │                       │   │   ├── JwtProperties.java
│   │   │                       │   │   ├── JwtService.java
│   │   │                       │   │   └── package-info.java
│   │   │                       │   ├── latex/
│   │   │                       │   │   ├── LatexCvRenderer.java
│   │   │                       │   │   ├── LatexCompilationException.java
│   │   │                       │   │   ├── PdfGenerationService.java
│   │   │                       │   │   └── package-info.java
│   │   │                       │   ├── persistence/
│   │   │                       │   │   ├── RepositorySupport.java
│   │   │                       │   │   ├── SpecificationSupport.java
│   │   │                       │   │   └── package-info.java
│   │   │                       │   ├── storage/
│   │   │                       │   │   ├── FileStoragePort.java
│   │   │                       │   │   ├── LocalFileStorageAdapter.java
│   │   │                       │   │   ├── StorageProperties.java
│   │   │                       │   │   └── package-info.java
│   │   │                       │   └── observability/
│   │   │                       │       ├── CorrelationIdFilter.java
│   │   │                       │       ├── LoggingMdcKeys.java
│   │   │                       │       ├── MetricsNames.java
│   │   │                       │       └── package-info.java
│   │   │                       └── modules/
│   │   │                           ├── auth/
│   │   │                           │   ├── api/
│   │   │                           │   │   ├── AuthController.java
│   │   │                           │   │   ├── dto/
│   │   │                           │   │   │   ├── request/
│   │   │                           │   │   │   │   ├── StudentLoginRequest.java
│   │   │                           │   │   │   │   ├── AdminLoginRequest.java
│   │   │                           │   │   │   │   └── LogoutRequest.java
│   │   │                           │   │   │   └── response/
│   │   │                           │   │   │       ├── AuthTokenResponse.java
│   │   │                           │   │   │       └── CurrentUserResponse.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── application/
│   │   │                           │   │   ├── AuthService.java
│   │   │                           │   │   ├── PasswordHashService.java
│   │   │                           │   │   ├── LoginAttemptService.java
│   │   │                           │   │   ├── command/
│   │   │                           │   │   ├── query/
│   │   │                           │   │   ├── port/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── domain/
│   │   │                           │   │   ├── model/
│   │   │                           │   │   ├── policy/
│   │   │                           │   │   ├── event/
│   │   │                           │   │   ├── exception/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── mapper/
│   │   │                           │   │   ├── AuthMapper.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── persistence/
│   │   │                           │   │   ├── entity/
│   │   │                           │   │   │   ├── UserAccountEntity.java
│   │   │                           │   │   │   ├── RoleEntity.java
│   │   │                           │   │   │   ├── UserRoleEntity.java
│   │   │                           │   │   │   └── AdminUserEntity.java
│   │   │                           │   │   ├── repository/
│   │   │                           │   │   │   ├── UserAccountRepository.java
│   │   │                           │   │   │   ├── RoleRepository.java
│   │   │                           │   │   │   └── AdminUserRepository.java
│   │   │                           │   │   ├── projection/
│   │   │                           │   │   ├── specification/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   └── package-info.java
│   │   │                           ├── verification/
│   │   │                           │   ├── api/
│   │   │                           │   │   ├── StudentVerificationController.java
│   │   │                           │   │   ├── dto/
│   │   │                           │   │   │   ├── request/
│   │   │                           │   │   │   │   ├── StudentVerificationStartRequest.java
│   │   │                           │   │   │   │   ├── OtpVerifyRequest.java
│   │   │                           │   │   │   │   ├── PasswordCreateRequest.java
│   │   │                           │   │   │   │   └── PasswordResetStartRequest.java
│   │   │                           │   │   │   └── response/
│   │   │                           │   │   │       ├── StudentVerificationResponse.java
│   │   │                           │   │   │       ├── OtpVerifyResponse.java
│   │   │                           │   │   │       ├── OtpResendResponse.java
│   │   │                           │   │   │       └── PasswordResetResponse.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── application/
│   │   │                           │   │   ├── StudentVerificationService.java
│   │   │                           │   │   ├── OtpService.java
│   │   │                           │   │   ├── PasswordResetService.java
│   │   │                           │   │   ├── command/
│   │   │                           │   │   ├── query/
│   │   │                           │   │   ├── port/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── domain/
│   │   │                           │   │   ├── model/
│   │   │                           │   │   ├── policy/
│   │   │                           │   │   │   └── OtpRateLimitPolicy.java
│   │   │                           │   │   ├── event/
│   │   │                           │   │   ├── exception/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── mapper/
│   │   │                           │   │   ├── StudentVerificationMapper.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── persistence/
│   │   │                           │   │   ├── entity/
│   │   │                           │   │   │   ├── StudentVerificationContextEntity.java
│   │   │                           │   │   │   └── OtpTokenEntity.java
│   │   │                           │   │   ├── repository/
│   │   │                           │   │   │   ├── StudentVerificationContextRepository.java
│   │   │                           │   │   │   └── OtpTokenRepository.java
│   │   │                           │   │   ├── projection/
│   │   │                           │   │   ├── specification/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   └── package-info.java
│   │   │                           ├── studentprofile/
│   │   │                           │   ├── api/
│   │   │                           │   │   ├── StudentProfileController.java
│   │   │                           │   │   ├── dto/
│   │   │                           │   │   │   ├── request/
│   │   │                           │   │   │   │   ├── StudentProfileUpdateRequest.java
│   │   │                           │   │   │   │   ├── ContactLinkRequest.java
│   │   │                           │   │   │   │   ├── CertificateRequest.java
│   │   │                           │   │   │   │   ├── AwardRequest.java
│   │   │                           │   │   │   │   ├── ActivityRequest.java
│   │   │                           │   │   │   │   └── WorkExperienceRequest.java
│   │   │                           │   │   │   └── response/
│   │   │                           │   │   │       └── StudentProfileResponse.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── application/
│   │   │                           │   │   ├── StudentProfileService.java
│   │   │                           │   │   ├── command/
│   │   │                           │   │   ├── query/
│   │   │                           │   │   ├── port/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── domain/
│   │   │                           │   │   ├── model/
│   │   │                           │   │   ├── policy/
│   │   │                           │   │   │   └── CvFreshnessUpdatePort.java
│   │   │                           │   │   ├── event/
│   │   │                           │   │   ├── exception/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── mapper/
│   │   │                           │   │   ├── StudentProfileMapper.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── persistence/
│   │   │                           │   │   ├── entity/
│   │   │                           │   │   │   ├── StudentEntity.java
│   │   │                           │   │   │   ├── StudentProfileEntity.java
│   │   │                           │   │   │   ├── ContactLinkEntity.java
│   │   │                           │   │   │   ├── CertificateEntity.java
│   │   │                           │   │   │   ├── AwardEntity.java
│   │   │                           │   │   │   ├── ActivityEntity.java
│   │   │                           │   │   │   └── WorkExperienceEntity.java
│   │   │                           │   │   ├── repository/
│   │   │                           │   │   │   ├── StudentRepository.java
│   │   │                           │   │   │   ├── StudentProfileRepository.java
│   │   │                           │   │   │   ├── ContactLinkRepository.java
│   │   │                           │   │   │   ├── CertificateRepository.java
│   │   │                           │   │   │   ├── AwardRepository.java
│   │   │                           │   │   │   ├── ActivityRepository.java
│   │   │                           │   │   │   └── WorkExperienceRepository.java
│   │   │                           │   │   ├── projection/
│   │   │                           │   │   ├── specification/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   └── package-info.java
│   │   │                           ├── skills/
│   │   │                           │   ├── api/
│   │   │                           │   │   ├── SkillTaxonomyController.java
│   │   │                           │   │   ├── StudentDeclaredSkillController.java
│   │   │                           │   │   ├── dto/
│   │   │                           │   │   │   ├── request/
│   │   │                           │   │   │   │   └── DeclaredSkillRequest.java
│   │   │                           │   │   │   └── response/
│   │   │                           │   │   │       ├── SkillTaxonomyResponse.java
│   │   │                           │   │   │       └── DeclaredSkillResponse.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── application/
│   │   │                           │   │   ├── SkillTaxonomyService.java
│   │   │                           │   │   ├── StudentDeclaredSkillService.java
│   │   │                           │   │   ├── command/
│   │   │                           │   │   ├── query/
│   │   │                           │   │   ├── port/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── domain/
│   │   │                           │   │   ├── model/
│   │   │                           │   │   ├── policy/
│   │   │                           │   │   │   └── CompetencyLevel.java
│   │   │                           │   │   ├── event/
│   │   │                           │   │   ├── exception/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── mapper/
│   │   │                           │   │   ├── SkillMapper.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── persistence/
│   │   │                           │   │   ├── entity/
│   │   │                           │   │   │   ├── SkillClusterEntity.java
│   │   │                           │   │   │   ├── SkillCategoryEntity.java
│   │   │                           │   │   │   ├── SkillEntity.java
│   │   │                           │   │   │   ├── SkillCategoryMappingEntity.java
│   │   │                           │   │   │   └── StudentDeclaredSkillEntity.java
│   │   │                           │   │   ├── repository/
│   │   │                           │   │   │   ├── SkillClusterRepository.java
│   │   │                           │   │   │   ├── SkillCategoryRepository.java
│   │   │                           │   │   │   ├── SkillRepository.java
│   │   │                           │   │   │   └── StudentDeclaredSkillRepository.java
│   │   │                           │   │   ├── projection/
│   │   │                           │   │   ├── specification/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   └── package-info.java
│   │   │                           ├── projects/
│   │   │                           │   ├── api/
│   │   │                           │   │   ├── ProjectController.java
│   │   │                           │   │   ├── dto/
│   │   │                           │   │   │   ├── request/
│   │   │                           │   │   │   │   └── ProjectRequest.java
│   │   │                           │   │   │   └── response/
│   │   │                           │   │   │       └── ProjectResponse.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── application/
│   │   │                           │   │   ├── ProjectService.java
│   │   │                           │   │   ├── command/
│   │   │                           │   │   ├── query/
│   │   │                           │   │   ├── port/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── domain/
│   │   │                           │   │   ├── model/
│   │   │                           │   │   ├── policy/
│   │   │                           │   │   ├── event/
│   │   │                           │   │   ├── exception/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── mapper/
│   │   │                           │   │   ├── ProjectMapper.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── persistence/
│   │   │                           │   │   ├── entity/
│   │   │                           │   │   │   ├── ProjectEntity.java
│   │   │                           │   │   │   └── ProjectSkillEntity.java
│   │   │                           │   │   ├── repository/
│   │   │                           │   │   │   ├── ProjectRepository.java
│   │   │                           │   │   │   └── ProjectSkillRepository.java
│   │   │                           │   │   ├── projection/
│   │   │                           │   │   ├── specification/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   └── package-info.java
│   │   │                           ├── academics/
│   │   │                           │   ├── api/
│   │   │                           │   │   ├── StudentAcademicRecordsController.java
│   │   │                           │   │   ├── AcademicLedgerController.java
│   │   │                           │   │   ├── dto/
│   │   │                           │   │   │   ├── request/
│   │   │                           │   │   │   └── response/
│   │   │                           │   │   │       ├── AcademicRecordResponse.java
│   │   │                           │   │   │       ├── GpaSummaryResponse.java
│   │   │                           │   │   │       ├── AcademicLedgerUploadResponse.java
│   │   │                           │   │   │       ├── LedgerValidationResultResponse.java
│   │   │                           │   │   │       └── LedgerCommitResponse.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── application/
│   │   │                           │   │   ├── AcademicLedgerService.java
│   │   │                           │   │   ├── AcademicLedgerValidationService.java
│   │   │                           │   │   ├── GpaCalculationService.java
│   │   │                           │   │   ├── command/
│   │   │                           │   │   ├── query/
│   │   │                           │   │   ├── port/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── domain/
│   │   │                           │   │   ├── model/
│   │   │                           │   │   ├── policy/
│   │   │                           │   │   ├── event/
│   │   │                           │   │   ├── exception/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── mapper/
│   │   │                           │   │   ├── AcademicMapper.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── persistence/
│   │   │                           │   │   ├── entity/
│   │   │                           │   │   │   ├── AcademicLedgerUploadEntity.java
│   │   │                           │   │   │   ├── AcademicLedgerStagingRowEntity.java
│   │   │                           │   │   │   ├── SubjectEntity.java
│   │   │                           │   │   │   ├── OfficialStudentGradeEntity.java
│   │   │                           │   │   │   └── StudentAcademicSummaryEntity.java
│   │   │                           │   │   ├── repository/
│   │   │                           │   │   │   ├── AcademicLedgerUploadRepository.java
│   │   │                           │   │   │   ├── AcademicLedgerStagingRowRepository.java
│   │   │                           │   │   │   ├── SubjectRepository.java
│   │   │                           │   │   │   ├── OfficialStudentGradeRepository.java
│   │   │                           │   │   │   └── StudentAcademicSummaryRepository.java
│   │   │                           │   │   ├── projection/
│   │   │                           │   │   ├── specification/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   └── package-info.java
│   │   │                           ├── cv/
│   │   │                           │   ├── api/
│   │   │                           │   │   ├── CvController.java
│   │   │                           │   │   ├── dto/
│   │   │                           │   │   │   ├── request/
│   │   │                           │   │   │   └── response/
│   │   │                           │   │   │       ├── CvPreviewResponse.java
│   │   │                           │   │   │       ├── CvVersionResponse.java
│   │   │                           │   │   │       ├── CvFreshnessResponse.java
│   │   │                           │   │   │       └── CvDownloadResponse.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── application/
│   │   │                           │   │   ├── CvService.java
│   │   │                           │   │   ├── CvGenerationService.java
│   │   │                           │   │   ├── CvFreshnessService.java
│   │   │                           │   │   ├── CvDownloadService.java
│   │   │                           │   │   ├── command/
│   │   │                           │   │   ├── query/
│   │   │                           │   │   ├── port/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── domain/
│   │   │                           │   │   ├── model/
│   │   │                           │   │   ├── policy/
│   │   │                           │   │   ├── event/
│   │   │                           │   │   ├── exception/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── mapper/
│   │   │                           │   │   ├── CvMapper.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── persistence/
│   │   │                           │   │   ├── entity/
│   │   │                           │   │   │   ├── CvVersionEntity.java
│   │   │                           │   │   │   ├── CvGeneratedFileEntity.java
│   │   │                           │   │   │   ├── CvSourceFreshnessEntity.java
│   │   │                           │   │   │   └── FileAssetEntity.java
│   │   │                           │   │   ├── repository/
│   │   │                           │   │   │   ├── CvVersionRepository.java
│   │   │                           │   │   │   ├── CvGeneratedFileRepository.java
│   │   │                           │   │   │   ├── CvSourceFreshnessRepository.java
│   │   │                           │   │   │   └── FileAssetRepository.java
│   │   │                           │   │   ├── projection/
│   │   │                           │   │   ├── specification/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   └── package-info.java
│   │   │                           ├── adminstudents/
│   │   │                           │   ├── api/
│   │   │                           │   │   ├── AdminStudentController.java
│   │   │                           │   │   ├── dto/
│   │   │                           │   │   │   ├── request/
│   │   │                           │   │   │   │   └── AdminStudentSearchCriteria.java
│   │   │                           │   │   │   └── response/
│   │   │                           │   │   │       ├── AdminStudentListItemResponse.java
│   │   │                           │   │   │       ├── AdminStudentDetailResponse.java
│   │   │                           │   │   │       └── AdminLatestCvResponse.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── application/
│   │   │                           │   │   ├── AdminStudentInspectionService.java
│   │   │                           │   │   ├── RegisteredStudentQueryService.java
│   │   │                           │   │   ├── command/
│   │   │                           │   │   ├── query/
│   │   │                           │   │   ├── port/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── domain/
│   │   │                           │   │   ├── model/
│   │   │                           │   │   ├── policy/
│   │   │                           │   │   ├── event/
│   │   │                           │   │   ├── exception/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── mapper/
│   │   │                           │   │   ├── AdminStudentMapper.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── persistence/
│   │   │                           │   │   ├── entity/
│   │   │                           │   │   ├── repository/
│   │   │                           │   │   ├── projection/
│   │   │                           │   │   ├── specification/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   └── package-info.java
│   │   │                           ├── companies/
│   │   │                           │   ├── api/
│   │   │                           │   │   ├── CompanyController.java
│   │   │                           │   │   ├── dto/
│   │   │                           │   │   │   ├── request/
│   │   │                           │   │   │   │   ├── CompanyRequest.java
│   │   │                           │   │   │   │   └── CompanySearchCriteria.java
│   │   │                           │   │   │   └── response/
│   │   │                           │   │   │       └── CompanyResponse.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── application/
│   │   │                           │   │   ├── CompanyService.java
│   │   │                           │   │   ├── command/
│   │   │                           │   │   ├── query/
│   │   │                           │   │   ├── port/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── domain/
│   │   │                           │   │   ├── model/
│   │   │                           │   │   ├── policy/
│   │   │                           │   │   ├── event/
│   │   │                           │   │   ├── exception/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── mapper/
│   │   │                           │   │   ├── CompanyMapper.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── persistence/
│   │   │                           │   │   ├── entity/
│   │   │                           │   │   │   └── CompanyEntity.java
│   │   │                           │   │   ├── repository/
│   │   │                           │   │   │   └── CompanyRepository.java
│   │   │                           │   │   ├── projection/
│   │   │                           │   │   ├── specification/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   └── package-info.java
│   │   │                           ├── internships/
│   │   │                           │   ├── api/
│   │   │                           │   │   ├── InternshipRequestController.java
│   │   │                           │   │   ├── dto/
│   │   │                           │   │   │   ├── request/
│   │   │                           │   │   │   │   ├── InternshipRequestCreateRequest.java
│   │   │                           │   │   │   │   ├── InternshipRequestUpdateRequest.java
│   │   │                           │   │   │   │   └── InternshipRequestSearchCriteria.java
│   │   │                           │   │   │   └── response/
│   │   │                           │   │   │       └── InternshipRequestResponse.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── application/
│   │   │                           │   │   ├── InternshipRequestService.java
│   │   │                           │   │   ├── command/
│   │   │                           │   │   ├── query/
│   │   │                           │   │   ├── port/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── domain/
│   │   │                           │   │   ├── model/
│   │   │                           │   │   ├── policy/
│   │   │                           │   │   │   └── InternshipRequestStatus.java
│   │   │                           │   │   ├── event/
│   │   │                           │   │   ├── exception/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── mapper/
│   │   │                           │   │   ├── InternshipRequestMapper.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── persistence/
│   │   │                           │   │   ├── entity/
│   │   │                           │   │   │   ├── InternshipRequestEntity.java
│   │   │                           │   │   │   └── InternshipRequestSkillEntity.java
│   │   │                           │   │   ├── repository/
│   │   │                           │   │   │   ├── InternshipRequestRepository.java
│   │   │                           │   │   │   └── InternshipRequestSkillRepository.java
│   │   │                           │   │   ├── projection/
│   │   │                           │   │   ├── specification/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   └── package-info.java
│   │   │                           ├── filtering/
│   │   │                           │   ├── api/
│   │   │                           │   │   ├── CandidateFilteringController.java
│   │   │                           │   │   ├── dto/
│   │   │                           │   │   │   ├── request/
│   │   │                           │   │   │   │   └── CandidateFilteringRunRequest.java
│   │   │                           │   │   │   └── response/
│   │   │                           │   │   │       ├── CandidateFilteringRunResponse.java
│   │   │                           │   │   │       └── CandidateFilterResultItemResponse.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── application/
│   │   │                           │   │   ├── CandidateFilteringService.java
│   │   │                           │   │   ├── CandidateFilteringQueryService.java
│   │   │                           │   │   ├── command/
│   │   │                           │   │   ├── query/
│   │   │                           │   │   ├── port/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── domain/
│   │   │                           │   │   ├── model/
│   │   │                           │   │   ├── policy/
│   │   │                           │   │   │   └── CandidateFilteringCriteria.java
│   │   │                           │   │   ├── event/
│   │   │                           │   │   ├── exception/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── mapper/
│   │   │                           │   │   ├── CandidateFilteringMapper.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── persistence/
│   │   │                           │   │   ├── entity/
│   │   │                           │   │   │   └── FilterRunEntity.java
│   │   │                           │   │   ├── repository/
│   │   │                           │   │   │   └── FilterRunRepository.java
│   │   │                           │   │   ├── projection/
│   │   │                           │   │   ├── specification/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   └── package-info.java
│   │   │                           ├── shortlists/
│   │   │                           │   ├── api/
│   │   │                           │   │   ├── ShortlistController.java
│   │   │                           │   │   ├── dto/
│   │   │                           │   │   │   ├── request/
│   │   │                           │   │   │   │   ├── ShortlistCreateRequest.java
│   │   │                           │   │   │   │   └── ShortlistCandidateRequest.java
│   │   │                           │   │   │   └── response/
│   │   │                           │   │   │       ├── ShortlistResponse.java
│   │   │                           │   │   │       └── ShortlistFinalizeResponse.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── application/
│   │   │                           │   │   ├── ShortlistService.java
│   │   │                           │   │   ├── ShortlistFinalizationService.java
│   │   │                           │   │   ├── command/
│   │   │                           │   │   ├── query/
│   │   │                           │   │   ├── port/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── domain/
│   │   │                           │   │   ├── model/
│   │   │                           │   │   ├── policy/
│   │   │                           │   │   │   └── ShortlistStatus.java
│   │   │                           │   │   ├── event/
│   │   │                           │   │   ├── exception/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── mapper/
│   │   │                           │   │   ├── ShortlistMapper.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── persistence/
│   │   │                           │   │   ├── entity/
│   │   │                           │   │   │   ├── ShortlistEntity.java
│   │   │                           │   │   │   └── ShortlistCandidateEntity.java
│   │   │                           │   │   ├── repository/
│   │   │                           │   │   │   ├── ShortlistRepository.java
│   │   │                           │   │   │   └── ShortlistCandidateRepository.java
│   │   │                           │   │   ├── projection/
│   │   │                           │   │   ├── specification/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   └── package-info.java
│   │   │                           ├── exports/
│   │   │                           │   ├── api/
│   │   │                           │   │   ├── ExportController.java
│   │   │                           │   │   ├── dto/
│   │   │                           │   │   │   ├── request/
│   │   │                           │   │   │   └── response/
│   │   │                           │   │   │       ├── ExportJobResponse.java
│   │   │                           │   │   │       └── ExportFileResponse.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── application/
│   │   │                           │   │   ├── ExportService.java
│   │   │                           │   │   ├── ExportJobService.java
│   │   │                           │   │   ├── BulkCvExportService.java
│   │   │                           │   │   ├── command/
│   │   │                           │   │   ├── query/
│   │   │                           │   │   ├── port/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── domain/
│   │   │                           │   │   ├── model/
│   │   │                           │   │   ├── policy/
│   │   │                           │   │   │   └── ExportStatus.java
│   │   │                           │   │   ├── event/
│   │   │                           │   │   ├── exception/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── mapper/
│   │   │                           │   │   ├── ExportMapper.java
│   │   │                           │   │   └── package-info.java
│   │   │                           │   ├── persistence/
│   │   │                           │   │   ├── entity/
│   │   │                           │   │   │   ├── ExportJobEntity.java
│   │   │                           │   │   │   └── ExportFileEntity.java
│   │   │                           │   │   ├── repository/
│   │   │                           │   │   │   ├── ExportJobRepository.java
│   │   │                           │   │   │   └── ExportFileRepository.java
│   │   │                           │   │   ├── projection/
│   │   │                           │   │   ├── specification/
│   │   │                           │   │   └── package-info.java
│   │   │                           │   └── package-info.java
│   │   │                           └── auditlog/
│   │   │                               ├── api/
│   │   │                               │   ├── AuditLogController.java
│   │   │                               │   ├── dto/
│   │   │                               │   │   ├── request/
│   │   │                               │   │   │   └── AuditLogSearchCriteria.java
│   │   │                               │   │   └── response/
│   │   │                               │   │       └── AuditLogResponse.java
│   │   │                               │   └── package-info.java
│   │   │                               ├── application/
│   │   │                               │   ├── AuditLogService.java
│   │   │                               │   ├── SecurityEventService.java
│   │   │                               │   ├── command/
│   │   │                               │   ├── query/
│   │   │                               │   ├── port/
│   │   │                               │   └── package-info.java
│   │   │                               ├── domain/
│   │   │                               │   ├── model/
│   │   │                               │   ├── policy/
│   │   │                               │   ├── event/
│   │   │                               │   ├── exception/
│   │   │                               │   └── package-info.java
│   │   │                               ├── mapper/
│   │   │                               │   ├── AuditLogMapper.java
│   │   │                               │   └── package-info.java
│   │   │                               ├── persistence/
│   │   │                               │   ├── entity/
│   │   │                               │   │   ├── AuditLogEntity.java
│   │   │                               │   │   └── SecurityEventEntity.java
│   │   │                               │   ├── repository/
│   │   │                               │   │   ├── AuditLogRepository.java
│   │   │                               │   │   └── SecurityEventRepository.java
│   │   │                               │   ├── projection/
│   │   │                               │   ├── specification/
│   │   │                               │   └── package-info.java
│   │   │                               └── package-info.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       ├── application-prod.yml
│   │       ├── logback-spring.xml
│   │       ├── messages.properties
│   │       ├── openapi/
│   │       │   └── CV_Management_API_OpenAPI_v1.0.yaml
│   │       ├── db/
│   │       │   ├── migration/
│   │       │   │   ├── V001__create_schemas.sql
│   │       │   │   ├── V002__create_auth_tables.sql
│   │       │   │   ├── V003__create_student_identity_tables.sql
│   │       │   │   ├── V004__create_student_profile_tables.sql
│   │       │   │   ├── V005__create_skill_taxonomy_tables.sql
│   │       │   │   ├── V006__create_declared_skill_tables.sql
│   │       │   │   ├── V007__create_project_tables.sql
│   │       │   │   ├── V008__create_academic_ledger_tables.sql
│   │       │   │   ├── V009__create_cv_versioning_tables.sql
│   │       │   │   ├── V010__create_company_tables.sql
│   │       │   │   ├── V011__create_internship_request_tables.sql
│   │       │   │   ├── V012__create_filtering_tables.sql
│   │       │   │   ├── V013__create_shortlist_tables.sql
│   │       │   │   ├── V014__create_export_tables.sql
│   │       │   │   ├── V015__create_audit_tables.sql
│   │       │   │   ├── V016__create_indexes.sql
│   │       │   │   ├── V017__create_constraints.sql
│   │       │   │   ├── V018__seed_roles.sql
│   │       │   │   ├── V019__seed_grade_scale.sql
│   │       │   │   └── V020__seed_initial_skill_taxonomy_placeholder.sql
│   │       │   ├── seed/
│   │       │   │   ├── README.md
│   │       │   │   ├── skill-taxonomy-seed-notes.md
│   │       │   │   └── sample-skill-taxonomy-placeholder.json
│   │       │   └── README.md
│   │       ├── templates/
│   │       │   ├── cv/
│   │       │   │   ├── ats-cv-template.tex
│   │       │   │   └── README.md
│   │       │   └── email/
│   │       │       ├── otp-verification.html
│   │       │       ├── password-reset-otp.html
│   │       │       └── README.md
│   │       └── banner.txt
│   └── test/
│       ├── java/
│       │   └── lk/
│       │       └── ac/
│       │           └── ruhuna/
│       │               └── dcs/
│       │                   └── cvmanagement/
│       │                       ├── architecture/
│       │                       │   ├── ModuleDependencyRulesTest.java
│       │                       │   ├── PackageStructureTest.java
│       │                       │   └── RemovedScopeGuardrailTest.java
│       │                       ├── support/
│       │                       │   ├── AbstractIntegrationTest.java
│       │                       │   ├── PostgresTestcontainerConfig.java
│       │                       │   ├── TestDataFactory.java
│       │                       │   ├── WithMockStudent.java
│       │                       │   ├── WithMockAdmin.java
│       │                       │   └── TestClockConfig.java
│       │                       └── modules/
│       │                           ├── auth/
│       │                           │   ├── AuthControllerTest.java
│       │                           │   ├── AuthServiceTest.java
│       │                           │   └── AuthRepositoryIntegrationTest.java
│       │                           ├── verification/
│       │                           │   ├── StudentVerificationServiceTest.java
│       │                           │   ├── OtpServiceTest.java
│       │                           │   └── VerificationApiIntegrationTest.java
│       │                           ├── studentprofile/
│       │                           │   ├── StudentProfileServiceTest.java
│       │                           │   └── StudentProfileApiIntegrationTest.java
│       │                           ├── skills/
│       │                           │   ├── SkillTaxonomyServiceTest.java
│       │                           │   └── DeclaredSkillApiIntegrationTest.java
│       │                           ├── projects/
│       │                           │   └── ProjectApiIntegrationTest.java
│       │                           ├── academics/
│       │                           │   ├── AcademicLedgerServiceTest.java
│       │                           │   ├── GpaCalculationServiceTest.java
│       │                           │   └── AcademicLedgerIntegrationTest.java
│       │                           ├── cv/
│       │                           │   ├── CvGenerationServiceTest.java
│       │                           │   ├── CvFreshnessServiceTest.java
│       │                           │   └── CvApiIntegrationTest.java
│       │                           ├── adminstudents/
│       │                           │   └── AdminStudentInspectionIntegrationTest.java
│       │                           ├── companies/
│       │                           │   └── CompanyApiIntegrationTest.java
│       │                           ├── internships/
│       │                           │   └── InternshipRequestApiIntegrationTest.java
│       │                           ├── filtering/
│       │                           │   ├── CandidateFilteringServiceTest.java
│       │                           │   └── CandidateFilteringIntegrationTest.java
│       │                           ├── shortlists/
│       │                           │   └── ShortlistFinalizationIntegrationTest.java
│       │                           ├── exports/
│       │                           │   └── ExportJobIntegrationTest.java
│       │                           └── auditlog/
│       │                               └── AuditLogSecurityTest.java
│       └── resources/
│           ├── application-test.yml
│           ├── logback-test.xml
│           └── test-data/
│               ├── students.json
│               ├── skills.json
│               ├── academic-ledger-valid.csv
│               ├── academic-ledger-invalid.csv
│               └── internship-requests.json
├── .dockerignore
├── .editorconfig
├── .env.example
├── .gitattributes
├── .gitignore
├── CHANGELOG.md
├── CONTRIBUTING.md
├── LICENSE
├── README.md
├── SECURITY.md
├── mvnw
├── mvnw.cmd
└── pom.xml
```

---

## 5. Implementation Notes

1. Use this structure as the backend baseline when bootstrapping the backend repository.
2. Do not add a global `controller`, `service`, or `repository` folder.
3. Do not add removed-scope folders, DTOs, entities, migrations, tests, routes, endpoint wrappers, or documentation placeholders.
4. Resolve the open implementation decisions before production deployment, especially exact Spring Boot version, SMTP provider, file storage, async export strategy, final master skill seed, retention rules, deployment topology, and monitoring stack.
5. If the team wants empty Java package folders committed to Git, add either `package-info.java` or `.gitkeep` consistently.
