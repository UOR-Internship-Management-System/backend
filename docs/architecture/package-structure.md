# Package Structure

The backend uses package-by-feature organization under:

`src/main/java/lk/ac/ruhuna/dcs/cvmanagement`

## Top-Level Packages

- `config`: Spring Boot configuration and security wiring.
- `infrastructure`: adapters for external or technical concerns such as JWT, storage, email, observability, and rendering.
- `modules`: feature/domain module boundaries.
- `shared`: reusable API, security, validation, pagination, time, file, audit, and utility types.

## Module Layer Pattern

Expected module layers are:

- `api`: REST controllers and request/response DTOs.
- `application`: use-case orchestration services.
- `domain`: policies, domain rules, and ports.
- `mapper`: conversion between API/application/persistence models.
- `persistence`: entities and Spring Data repositories.

The `health` module is a Sprint 1 system module and may only contain `api`.

## Current Module Boundaries

Sprint 1 may contain reserved packages for approved future modules, including authentication, verification, student profile, skills, projects, academics, CV, admin student inspection, companies, internships, filtering, shortlists, exports, audit log, and health.

Reserved packages must compile cleanly without pretending that Sprint 2+ business workflows are implemented.
