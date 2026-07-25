# Viron MVP – Implementation Checklist

The **Minimum Viable Product (MVP)** for Viron establishes the core spatial simulation capabilities necessary to manage **environments**, **grids**, **locations**, and **entities** via a REST API.

This document aligns with the [`docs/openapi/viron-api.json`](openapi/viron-api.json) specification and serves as the implementation guide.

> **Status:** The MVP endpoint surface is complete. Every endpoint and DTO below is implemented, tested, and present in the OpenAPI specification; the checked boxes reflect the current implementation.

---

## 🎯 Purpose

Deliver a working, tested API that supports:
- Creation and retrieval of simulation environments.
- Hierarchical spatial structures (environments → grids → locations).
- Entity management and placement within locations.
- Debug utilities for rapid testing and demonstration.

---

## 📌 Core Features

### 1. **Environment Management**
- [x] `GET /api/v1/environments` – Retrieve all environments.
- [x] `GET /api/v1/environments/{id}` – Retrieve a specific environment by ID.
- [x] `GET /api/v1/environments/name/{name}` – Retrieve a specific environment by name.
- [x] `GET /api/v1/environments/entity/{entityId}` – Get the environment containing a specific entity.
- [x] `POST /api/v1/environments` – Create an environment (JSON body: `name`, `numGrids`, and grid dimensions as either `gridSize` or both `numRows` and `numColumns`).
- [x] `PATCH /api/v1/environments/{id}/name` – Update environment name (JSON body: `name`).
- [x] `DELETE /api/v1/environments/{id}` – Delete an environment and all related entities, locations, and grids.

---

### 2. **Grid Management**
- [x] `GET /api/v1/grids` – Retrieve all grids.
- [x] `GET /api/v1/grids/{id}` – Retrieve a grid by ID.
- [x] `GET /api/v1/grids/environment/{environmentId}` – Retrieve all grids in an environment.
- [x] `GET /api/v1/grids/entity/{entityId}` – Retrieve the grid containing a specific entity.

---

### 3. **Location Management**
- [x] `GET /api/v1/locations` – Retrieve all locations.
- [x] `GET /api/v1/locations/{id}` – Retrieve a location by ID.
- [x] `GET /api/v1/locations/environment/{environmentId}` – Retrieve locations in an environment.
- [x] `GET /api/v1/locations/grid/{gridId}` – Retrieve locations in a grid.
- [x] `GET /api/v1/locations/grid/{gridId}/unoccupied` – Retrieve locations in a grid that hold no entities.
- [x] `GET /api/v1/locations/entity/{entityId}` – Retrieve the location of a specific entity.
- [x] `GET /api/v1/locations/{locationId}/entities` – Retrieve the IDs of the entities at a location.
- [x] `GET /api/v1/locations/{locationId}/occupied` – Report whether a location holds any entities.
- [x] `GET /api/v1/locations/{locationId}/neighbors` – Retrieve the locations adjacent to a location within its grid.
- [x] `PUT /api/v1/locations/{locationId}/entity/{entityId}` – Add an entity to a location.
- [x] `PUT /api/v1/locations/{locationId}/entity/{entityId}/move` – Move a placed entity to an adjacent, unoccupied location in the same grid.
- [x] `DELETE /api/v1/locations/{locationId}/entity/{entityId}` – Remove an entity from a specific location.
- [x] `DELETE /api/v1/locations/entity/{entityId}` – Remove an entity from its current location.

---

### 4. **Entity Management**
- [x] `GET /api/v1/entities` – Retrieve all entities.
- [x] `GET /api/v1/entities/{id}` – Retrieve a specific entity by ID.
- [x] `GET /api/v1/entities/environment/{environmentId}` – Retrieve entities in an environment.
- [x] `GET /api/v1/entities/grid/{gridId}` – Retrieve entities in a grid.
- [x] `GET /api/v1/entities/location/{locationId}` – Retrieve entities at a location.
- [x] `GET /api/v1/entities/unassigned` – Retrieve entities that are not placed at any location.
- [x] `POST /api/v1/entities` – Create a new entity (JSON body: `name`).
- [x] `PATCH /api/v1/entities/{id}/name` – Update entity name (JSON body: `name`).
- [x] `DELETE /api/v1/entities/{id}` – Delete an entity.

---

### 5. **Debug Utilities**

> Disabled by default. These endpoints are only registered when `viron.debug.enabled=true`
> (environment variable `VIRON_DEBUG_ENABLED`); otherwise `/api/v1/debug/**` is not mapped.

- [x] `POST /api/v1/debug/create-sample-data` – Create an environment with grids, locations, and sample entities for testing.
- [x] `POST /api/v1/debug/create-world-and-place-entity/{environmentName}` – Create a world and place a random entity.

---

## 🧩 DTO Requirements

- [x] **EnvironmentDTO** – Public representation of an environment.
- [x] **CreateEnvironmentRequest** – Request body for creating environments (name, number of grids, and grid dimensions as either `gridSize` or both `numRows` and `numColumns`).
- [x] **UpdateEnvironmentNameRequest** – Request body for updating environment names.
- [x] **GridDTO** – Public representation of a grid.
- [x] **LocationDTO** – Public representation of a location.
- [x] **EntityDTO** – Public representation of an entity.
- [x] **CreateEntityRequest** – Request body for creating entities (name).
- [x] **UpdateEntityNameRequest** – Request body for updating entity names.

---

## ✅ Completion Criteria

- All endpoints in [`docs/openapi/viron-api.json`](openapi/viron-api.json) implemented and tested.
- DTOs returned in responses, avoiding direct exposure of internal entity models.
- Unit and integration tests covering all controllers and repositories.
- Endpoints verified via Postman or Swagger UI.
- API returns proper HTTP status codes and error messages.
- Debug endpoints operational for development testing.
- (Optional for MVP) Pagination and sorting applied to list endpoints.

---
