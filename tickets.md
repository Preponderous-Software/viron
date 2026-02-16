# Viron MVP Implementation Tickets

**Optimized for Claude AI Coding Agent** — Tickets are grouped to minimize context switches and maximize completion within single agent sessions.

---

## 🎯 Ticket Structure

Each ticket is designed to:
- Complete a cohesive unit of functionality in one session
- Include all layers (model → repository → service → controller)
- Contain its own tests
- Minimize dependencies on other tickets where possible
- Reference the OpenAPI specification at `docs/openapi/viron-api.json`

---

## 🚀 Critical Path Tickets (Execute in Order)

### **Ticket 1: Core Domain Models & DTOs**
**Session Goal:** Establish all domain models and their DTOs in one session.

**Scope:**
- Create/update domain models: `Environment`, `Grid`, `Location`, `Entity` with proper JPA relationships
- Create DTOs: `EnvironmentDTO`, `GridDTO`, `LocationDTO`, `EntityDTO`
- Create request DTOs: `CreateEnvironmentRequest`, `UpdateEnvironmentNameRequest`, `CreateEntityRequest`
- Implement model-to-DTO mapping utilities (manual or MapStruct)
- Add Lombok annotations for boilerplate reduction
- Ensure proper bidirectional relationships and cascade configurations

**Acceptance Criteria:**
- All models have proper JPA annotations (@Entity, @Id, @GeneratedValue, relationships)
- DTOs expose only necessary fields (no internal IDs exposed unless needed)
- Mapping functions convert between models and DTOs accurately
- Code compiles without errors

**References:**
- `docs/MVP.md` — DTO Requirements section
- `docs/openapi/viron-api.json` — Response schemas

---

### **Ticket 2: Repository Layer & Database Configuration**
**Session Goal:** Implement all repository interfaces and test database connectivity.

**Scope:**
- Create JPA repositories: `EnvironmentRepository`, `GridRepository`, `LocationRepository`, `EntityRepository`
- Implement custom query methods for:
  - Find environment by name
  - Find environment/grid/location containing an entity
  - Find all grids/locations in an environment
  - Find location by grid
- Configure PostgreSQL datasource in `application.properties`
- Add Flyway or JPA schema generation for initial setup
- Write basic repository tests using @DataJpaTest

**Acceptance Criteria:**
- All repository interfaces extend JpaRepository
- Custom queries work correctly
- Database schema auto-generates or is managed by Flyway
- Repository tests pass with in-memory or test database

**References:**
- `docs/MVP.md` — Feature checklists for query requirements
- `compose.yml` — Database configuration

---

### **Ticket 3: Environment Service & API Complete**
**Session Goal:** Fully implement environment management end-to-end in one session.

**Scope:**
- Implement `EnvironmentService` with methods:
  - `createEnvironment(name, numGrids, gridSize)` — creates environment with grids and locations
  - `getAllEnvironments()`
  - `getEnvironmentById(id)`
  - `getEnvironmentByName(name)`
  - `getEnvironmentByEntityId(entityId)`
  - `updateEnvironmentName(id, name)`
  - `deleteEnvironment(id)` — with cascade delete of grids, locations
- Implement `EnvironmentController` with all endpoints:
  - `GET /api/v1/environments`
  - `GET /api/v1/environments/{id}`
  - `GET /api/v1/environments/name/{name}`
  - `GET /api/v1/environments/entity/{entityId}`
  - `POST /api/v1/environments/{name}/{numGrids}/{gridSize}`
  - `PATCH /api/v1/environments/{id}/name/{name}`
  - `DELETE /api/v1/environments/{id}`
- Add exception handling and proper HTTP status codes
- Write controller tests (@WebMvcTest or @SpringBootTest)
- Write service unit tests

**Acceptance Criteria:**
- All environment endpoints return correct DTOs
- Creating environment generates grids and locations automatically
- Deleting environment cascades to grids and locations
- All tests pass
- Endpoints match OpenAPI specification

**References:**
- `docs/MVP.md` — Environment Management section
- `docs/openapi/viron-api.json` — Environment endpoints

---

### **Ticket 4: Grid Service & API Complete**
**Session Goal:** Fully implement grid management end-to-end in one session.

**Scope:**
- Implement `GridService` with methods:
  - `getAllGrids()`
  - `getGridById(id)`
  - `getGridsByEnvironmentId(environmentId)`
  - `getGridByEntityId(entityId)`
- Implement `GridController` with all endpoints:
  - `GET /api/v1/grids`
  - `GET /api/v1/grids/{id}`
  - `GET /api/v1/grids/environment/{environmentId}`
  - `GET /api/v1/grids/entity/{entityId}`
- Add exception handling
- Write controller and service tests

**Acceptance Criteria:**
- All grid endpoints return correct DTOs
- Grid-environment relationships work correctly
- Finding grid by entity works
- All tests pass
- Endpoints match OpenAPI specification

**References:**
- `docs/MVP.md` — Grid Management section
- `docs/openapi/viron-api.json` — Grid endpoints

---

### **Ticket 5: Location Service & API Complete**
**Session Goal:** Fully implement location management end-to-end in one session.

**Scope:**
- Implement `LocationService` with methods:
  - `getAllLocations()`
  - `getLocationById(id)`
  - `getLocationsByEnvironmentId(environmentId)`
  - `getLocationsByGridId(gridId)`
  - `getLocationByEntityId(entityId)`
  - `addEntityToLocation(locationId, entityId)` — enforce single location per entity
  - `removeEntityFromLocation(locationId, entityId)`
  - `removeEntityFromCurrentLocation(entityId)`
- Implement `LocationController` with all endpoints:
  - `GET /api/v1/locations`
  - `GET /api/v1/locations/{id}`
  - `GET /api/v1/locations/environment/{environmentId}`
  - `GET /api/v1/locations/grid/{gridId}`
  - `GET /api/v1/locations/entity/{entityId}`
  - `PUT /api/v1/locations/{locationId}/entity/{entityId}`
  - `DELETE /api/v1/locations/{locationId}/entity/{entityId}`
  - `DELETE /api/v1/locations/entity/{entityId}`
- Add business logic to ensure entity is in at most one location
- Add exception handling
- Write controller and service tests including constraint tests

**Acceptance Criteria:**
- All location endpoints work correctly
- Entity placement constraint enforced (one location max)
- Adding entity to new location removes from old location
- All tests pass including edge cases
- Endpoints match OpenAPI specification

**References:**
- `docs/MVP.md` — Location Management section
- `docs/openapi/viron-api.json` — Location endpoints
- `docs/PLANNING.md` — M7 item 52 (entity placement constraint tests)

---

### **Ticket 6: Entity Service & API Complete**
**Session Goal:** Fully implement entity management end-to-end in one session.

**Scope:**
- Implement `EntityService` with methods:
  - `createEntity(name, type)` or similar
  - `getAllEntities()`
  - `getEntityById(id)`
  - `deleteEntity(id)` — handle location detachment
- Implement `EntityController` with all endpoints:
  - `GET /api/v1/entities`
  - `GET /api/v1/entities/{id}`
  - `POST /api/v1/entities`
  - `DELETE /api/v1/entities/{id}`
- Ensure deleting entity removes it from its location
- Add exception handling
- Write controller and service tests

**Acceptance Criteria:**
- All entity endpoints work correctly
- Creating entities returns proper DTOs
- Deleting entity cleans up location references
- All tests pass
- Endpoints match OpenAPI specification

**References:**
- `docs/MVP.md` — Entity Management section
- `docs/openapi/viron-api.json` — Entity endpoints

---

### **Ticket 7: Debug Utilities & Developer Experience**
**Session Goal:** Implement debug endpoints and developer tooling in one session.

**Scope:**
- Implement `DebugController` with endpoints:
  - `POST /api/v1/debug/create-sample-data` — creates environment with sample entities
  - `POST /api/v1/debug/create-world-and-place-entity/{environmentName}` — quick world setup
- Use factory patterns (`EnvironmentFactory`, `EntityFactory`) for data generation
- Add configuration to enable/disable debug endpoints (via profile or property)
- Test debug endpoints manually
- Update `sample.env` if needed
- Update README.md with debug endpoint usage

**Acceptance Criteria:**
- Debug endpoints create valid sample data
- Endpoints can be disabled in production
- Manual testing confirms data is properly created
- Documentation updated

**References:**
- `docs/MVP.md` — Debug Utilities section
- `docs/openapi/viron-api.json` — Debug endpoints
- `docs/PLANNING.md` — M5 (Debug & Dev Tools)

---

## 🔧 Supporting Tickets (Can be done in parallel or after critical path)

### **Ticket 8: Global Exception Handling & Validation**
**Session Goal:** Centralize error handling and add validation in one session.

**Scope:**
- Create global `@ControllerAdvice` exception handler
- Map exceptions to appropriate HTTP status codes:
  - `NotFoundException` → 404
  - `IllegalArgumentException` → 400
  - `ServiceException` → 500 or 400 depending on type
- Add consistent error response format (JSON with message, status, timestamp)
- Add bean validation annotations to request DTOs (`@NotNull`, `@NotBlank`, `@Min`, etc.)
- Add validation tests
- Create error catalog document (optional, in `docs/ERROR_CODES.md`)

**Acceptance Criteria:**
- All exceptions return consistent JSON error responses
- Validation errors return 400 with clear messages
- Tests verify error responses
- No stack traces exposed in production

**References:**
- `docs/PLANNING.md` — M6 (Validation & Error Handling)

---

### **Ticket 9: API Documentation & Swagger UI**
**Session Goal:** Configure Swagger UI and ensure OpenAPI spec alignment in one session.

**Scope:**
- Verify springdoc-openapi-ui dependency is configured
- Add OpenAPI annotations to controllers if needed
- Test Swagger UI at `http://localhost:8080/swagger-ui.html`
- Compare generated spec with `docs/openapi/viron-api.json`
- Update OpenAPI spec if implementation differs (but prefer spec-first)
- Add API documentation section to README.md
- Create Postman collection from OpenAPI spec (optional)

**Acceptance Criteria:**
- Swagger UI loads and displays all endpoints
- Generated spec matches documented spec
- README.md has API documentation section
- Manual testing via Swagger UI works

**References:**
- `docs/PLANNING.md` — M5 item 44 (Swagger UI configuration)
- `docs/PLANNING.md` — M9 item 43 (OpenAPI sync)

---

### **Ticket 10: Testing & Coverage**
**Session Goal:** Enhance test coverage and add integration tests in one session.

**Scope:**
- Add test data builders for easier test setup
- Ensure unit tests exist for all services
- Ensure unit tests exist for all controllers
- Add integration tests using @SpringBootTest with TestRestTemplate
- Add cascade delete tests
- Add entity placement constraint tests
- Configure JaCoCo coverage thresholds in pom.xml
- Run `mvn test` and verify all tests pass
- Generate and review coverage report

**Acceptance Criteria:**
- Test coverage meets threshold (e.g., 80% line coverage)
- All critical paths tested
- Integration tests verify end-to-end flows
- Coverage report generated in `target/site/jacoco/`

**References:**
- `docs/PLANNING.md` — M7 (Testing)
- `pom.xml` — JaCoCo configuration

---

### **Ticket 11: Docker & Local Development Setup**
**Session Goal:** Finalize Docker configuration and local dev workflow in one session.

**Scope:**
- Verify `compose.yml` is properly configured
- Verify `Dockerfile` builds the application correctly
- Add `.dockerignore` file
- Test `docker compose up --build`
- Verify application connects to PostgreSQL container
- Update README.md with local development instructions
- Add Makefile or scripts for common tasks (optional):
  - `make build` → `mvn clean install`
  - `make run` → `docker compose up`
  - `make test` → `mvn test`
  - `make clean` → cleanup

**Acceptance Criteria:**
- `docker compose up --build` starts app and database
- Application is accessible at `http://localhost:8080`
- Database persists data between restarts
- README.md has clear setup instructions

**References:**
- `docs/PLANNING.md` — M5 item 55 (Docker Compose)
- `compose.yml` and `Dockerfile`

---

### **Ticket 12: CI/CD Pipeline**
**Session Goal:** Set up GitHub Actions workflow in one session.

**Scope:**
- Create `.github/workflows/ci.yml`
- Configure workflow to:
  - Checkout code
  - Setup Java 21
  - Run `mvn clean install`
  - Run tests with `mvn test`
  - Generate coverage report
  - Upload coverage to GitHub artifacts or Codecov (optional)
  - Run Spotless/Checkstyle if configured
- Add status badge to README.md
- Test workflow by pushing changes

**Acceptance Criteria:**
- CI workflow runs on push and PR
- Build and tests pass in CI
- Coverage report generated
- Badge shows build status

**References:**
- `docs/PLANNING.md` — M8 item 58 (CI pipeline)

---

### **Ticket 13: Code Quality & Formatting**
**Session Goal:** Add code quality tools in one session.

**Scope:**
- Add Spotless Maven plugin to `pom.xml`
- Configure Google Java Format or similar
- Add Checkstyle plugin with rules
- Configure Maven to fail on formatting violations
- Run `mvn spotless:apply` to format existing code
- Add formatting check to CI
- Document code style in README.md or CONTRIBUTING.md

**Acceptance Criteria:**
- Code follows consistent style
- `mvn spotless:check` passes
- CI enforces formatting
- Documentation explains how to format code

**References:**
- `docs/PLANNING.md` — M8 item 57 (static analysis & formatting)

---

## 📋 Optional Enhancement Tickets (Post-MVP)

### **Ticket 14: Pagination & Sorting**
Add pagination and sorting to list endpoints (optional for MVP).

**Scope:**
- Add `Pageable` parameter to repository methods
- Update service methods to accept pagination parameters
- Update controllers to accept `page`, `size`, `sort` query parameters
- Return `Page<DTO>` instead of `List<DTO>`
- Test pagination

**References:**
- `docs/MVP.md` — Completion Criteria (optional pagination)
- `docs/PLANNING.md` — M6 item 42

---

### **Ticket 15: Database Migrations with Flyway**
Add Flyway for version-controlled database migrations.

**Scope:**
- Add Flyway dependency
- Create initial migration scripts in `src/main/resources/db/migration/`
- Test migration on fresh database
- Document migration process

---

### **Ticket 16: Performance Optimization**
Add database indexes and optimize queries.

**Scope:**
- Add indexes to frequently queried columns (name, entity_id references)
- Analyze query performance
- Add query logging to identify N+1 queries
- Optimize with @EntityGraph or fetch joins if needed

**References:**
- `docs/PLANNING.md` — M8 item 54 (repository indexes)

---

## 🎯 Recommended Execution Order

For maximum efficiency with Claude AI agents:

1. **Session 1:** Ticket 1 (Models & DTOs) — Foundation
2. **Session 2:** Ticket 2 (Repositories) — Data layer
3. **Session 3:** Ticket 3 (Environment API) — First complete feature
4. **Session 4:** Ticket 4 (Grid API) + Ticket 5 (Location API) — Two related features (if time allows, otherwise split)
5. **Session 5:** Ticket 6 (Entity API) + Ticket 7 (Debug) — Complete core features
6. **Session 6:** Ticket 8 (Error Handling) + Ticket 9 (Swagger) — Polish core API
7. **Session 7:** Ticket 10 (Testing) — Ensure quality
8. **Session 8:** Ticket 11 (Docker) + Ticket 12 (CI/CD) + Ticket 13 (Code Quality) — DevOps

**Total: 8 agent sessions for complete MVP**

---

## 💡 Agent Session Tips

**For each session:**
1. Start by reading the ticket completely
2. Review referenced documentation files
3. Check current code state before making changes
4. Make changes incrementally and test as you go
5. Run linters and tests before completing
6. Use `report_progress` frequently to commit work
7. If stuck, break ticket into smaller pieces or ask for guidance

**Context preservation:**
- Each ticket is self-contained but references common docs
- Always check `docs/openapi/viron-api.json` for API contracts
- Always check `docs/MVP.md` for acceptance criteria
- Commit frequently so next session can pick up cleanly

---

## 🎉 Definition of Done (MVP)

MVP is complete when:
- ✅ All endpoints in tickets 1-7 are implemented and tested
- ✅ Swagger UI shows all endpoints and they work correctly
- ✅ Docker Compose starts application successfully
- ✅ All tests pass with adequate coverage (>80%)
- ✅ Manual testing via Postman or Swagger confirms functionality
- ✅ README.md is up-to-date with setup and usage instructions
- ✅ No critical security vulnerabilities (run `mvn dependency:check` if possible)

---

**Generated:** 2026-02-16  
**For Project:** Viron — Spatial Simulation Service  
**Spec Version:** As defined in `docs/openapi/viron-api.json`
