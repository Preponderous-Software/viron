# PLANNING.md — Viron MVP Issue Plan (Milestone-Oriented)

This plan enumerates GitHub issues to implement the Viron MVP as defined in `openapi/viron-api.json`, grouped into milestones with concise descriptions.

---

## Labels
- `area:env`, `area:grid`, `area:location`, `area:entity`, `area:debug`, `area:core`, `area:testing`, `area:devexp`, `area:docs`
- `type:feat`, `type:bug`, `type:refactor`, `type:test`, `type:docs`, `type:ci`

---

## Milestones

### **M1 – Foundations**
Establish the core building blocks: project structure, models, DTOs, mapping, repositories.

1. **Project scaffolding & conventions** — Set up packages, style, logging, and error model.  
2. **Domain models** — Implement Environment, Grid, Location, Entity with relationships.  
3. **DTO: EnvironmentDTO** — Public representation of an environment.  
4. **DTO: GridDTO** — Public representation of a grid.  
5. **DTO: LocationDTO** — Public representation of a location.  
6. **DTO: EntityDTO** — Public representation of an entity.  
7. **Request DTO: CreateEnvironmentRequest** — Input contract for environment creation.  
8. **Request DTO: UpdateEnvironmentNameRequest** — Input contract for environment rename.  
9. **Mapping layer** — Map domain models to DTOs and back.  
10. **Repositories** — CRUD operations for all core models.

---

### **M2 – Service Layer**
Implement core service logic to support the REST API.

11. **EnvironmentService** — Create, retrieve, update name, delete (cascade).  
12. **GridService** — Lookups by ID, environment, or entity.  
13. **LocationService** — Manage locations, add/remove entities, lookups.  
14. **EntityService** — Create, retrieve, delete entities and handle detach.

---

### **M3 – Read APIs**
Deliver GET endpoints for all resources.

15. **GET /environments** — Retrieve all environments.  
16. **GET /environments/{id}** — Retrieve environment by ID.  
17. **GET /environments/name/{name}** — Retrieve environment by name.  
18. **GET /environments/entity/{entityId}** — Get environment containing an entity.  
22. **GET /grids** — Retrieve all grids.  
23. **GET /grids/{id}** — Retrieve grid by ID.  
24. **GET /grids/environment/{environmentId}** — Retrieve grids in an environment.  
25. **GET /grids/entity/{entityId}** — Get grid containing an entity.  
26. **GET /locations** — Retrieve all locations.  
27. **GET /locations/{id}** — Retrieve location by ID.  
28. **GET /locations/environment/{environmentId}** — Retrieve locations in environment.  
29. **GET /locations/grid/{gridId}** — Retrieve locations in a grid.  
30. **GET /locations/entity/{entityId}** — Get location of an entity.  
34. **GET /entities** — Retrieve all entities.  
35. **GET /entities/{id}** — Retrieve entity by ID.

---

### **M4 – Write APIs**
Implement creation, update, and deletion endpoints.

19. **POST /environments** — Create environment with grids and locations (JSON body: `name`, `numGrids`, `gridSize`).  
20. **DELETE /environments/{id}** — Delete environment with related data.  
21. **PATCH /environments/{id}/name** — Update environment name (JSON body: `name`).  
31. **PUT /locations/{locationId}/entity/{entityId}** — Add entity to location.  
32. **DELETE /locations/{locationId}/entity/{entityId}** — Remove entity from a specific location.  
33. **DELETE /locations/entity/{entityId}** — Remove entity from its current location.  
36. **POST /entities** — Create a new entity.  
37. **DELETE /entities/{id}** — Delete an entity.

---

### **M5 – Debug & Dev Tools**
Add debug utilities and developer tooling.

38. **POST /debug/create-sample-data** — Create sample environment with data.  
39. **POST /debug/create-world-and-place-entity/{environmentName}** — Create env and place entity.  
44. **Swagger UI configuration** — Enable API docs UI.  
45. **Postman collection** — Export endpoints for testing.  
55. **Docker Compose & local run** — Setup for local app + DB.  
56. **Makefile / Gradle tasks** — Streamline dev commands.  
60. **Operational debug toggles** — Enable/disable debug endpoints.  
62. **Example seed script** — Script to populate and display test data.

---

### **M6 – Validation & Error Handling**
Ensure consistent validation and error responses.

40. **Validation rules** — Input constraints with proper status codes.  
41. **Global exception handler** — Centralized error mapping to JSON.  
42. **Pagination & sorting** — Apply to list endpoints (**optional for MVP**).  
61. **Error catalog** — Markdown reference for codes and messages.

---

### **M7 – Testing**
Implement unit, integration, and validation tests.

46. **Test data builders** — Helpers for creating test entities.  
47. **Unit tests: services** — Test core service logic.  
48. **Unit tests: controllers** — Verify endpoint responses.  
49. **Integration tests** — End-to-end repository and controller flow.  
51. **Cascade delete tests** — Ensure deletes propagate correctly.  
52. **Entity placement constraint tests** — Ensure one-location rule.  
53. **Validation tests** — Ensure bad inputs return correct errors.  
50. **Coverage gate (JaCoCo)** — Enforce coverage threshold.

---

### **M8 – Performance & Hardening**
Optimize and ensure stability.

54. **Repository indexes** — Add indexes for performance.  
57. **Static analysis & formatting** — Code style enforcement.  
58. **CI pipeline** — Build, test, coverage in GitHub Actions.

---

### **M9 – Documentation & Release**
Finalize docs and tag MVP release.

43. **OpenAPI sync** — Ensure spec matches implementation.  
59. **README updates** — Add endpoint and usage details.  
63. **Release notes: MVP 0.1.0** — Changelog and tag release.

---

## Definition of Done (per milestone)
- All issues in milestone implemented & tested.
- OpenAPI/Swagger matches actual behavior.
- Passes CI with coverage thresholds met.
- Documentation updated.
