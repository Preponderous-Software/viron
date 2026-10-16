# Viron OpenAPI Specification

This directory contains the **OpenAPI 3.0.3** specification for the Viron spatial simulation API.  
The specification defines endpoints, request/response formats, and DTO schemas for all core domain objects.

---

## 📂 Files

- **viron-api.json** – The complete OpenAPI definition for the Viron API, covering:
  - **Environments** – Create, read, update (including renaming), and delete environments.
  - **Grids** – Retrieve and rename grids, and find their relationships to environments and entities.
  - **Locations** – Manage locations, including entity placement, movement, and removal.
  - **Entities** – Access, create, rename, and delete entities in the simulation.
  - **Debug Utilities** – Endpoints for generating sample data and test scenarios.

---

## 🧩 Structure

The API is organized around **domain-specific controllers**:

- **EnvironmentController** – Handles environment-level operations, including creation, retrieval, renaming, and deletion.
- **GridController** – Handles grid-related retrieval, relationships, and renaming.
- **LocationController** – Manages spatial locations and entity placement, movement, and removal.
- **EntityController** – Manages entity creation, retrieval, renaming, and deletion.
- **DebugController** – Provides testing and demonstration endpoints.

Each path in the spec reflects a **clear mapping to a domain object**, ensuring maintainability and discoverability.

---

## 🗂 DTOs (Data Transfer Objects)

The specification uses DTOs for all request and response bodies.  
This ensures:
- **Consistency** in API contracts.
- **Clarity** for consumers of the API.
- **Separation** of internal models from public API.

Key DTOs include:
- `EnvironmentDTO`
- `CreateEnvironmentRequest`
- `UpdateEnvironmentNameRequest`
- `GridDTO`
- `UpdateGridNameRequest`
- `LocationDTO`
- `EntityDTO`
- `CreateEntityRequest`
- `UpdateEntityNameRequest`
- `ErrorResponse`

---

## 🚀 Usage

1. **View the spec**
   - Use any OpenAPI-compatible viewer such as [Swagger Editor](https://editor.swagger.io/) or [Insomnia](https://insomnia.rest/).

2. **Test the API**
   - Start the Viron application locally.
   - Use the endpoints defined in `viron-api.json` to interact with the service.
   - Send a JWT issued by the UserAuth service as `Authorization: Bearer <token>` on every request.
     Every endpoint except `/actuator/health` and the OpenAPI/Swagger documentation itself requires it;
     a request without a valid token is answered `401 Unauthorized` with an empty body. The spec
     declares this as the global `bearerAuth` security scheme, and every operation lists the shared
     `Unauthorized` (401) response; Swagger UI's **Authorize** control accepts the token.

3. **Generate clients or servers**
   - Use OpenAPI code generation tools (e.g., `openapi-generator-cli`) to scaffold API clients or server stubs.

---

## 📄 Related Documentation

- [`../MVP.md`](../MVP.md) – Minimum Viable Product checklist, aligned with this specification.
- [`../PLANNING.md`](../PLANNING.md) – Milestone and issue breakdown for implementing the MVP.
- [`../REBUILD_PLAN.md`](../REBUILD_PLAN.md) – Step-by-step plan for rebuilding the Viron codebase in alignment with this spec.

---

## ℹ️ Notes

- Pagination & sorting are **optional for MVP** and may not be present in the current `viron-api.json`.
- This specification is the **source of truth** for all endpoints and data contracts. All other documentation is derived from it.
