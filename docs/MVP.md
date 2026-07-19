# Viron MVP – Implementation Checklist

The **Minimum Viable Product (MVP)** for Viron establishes the core spatial simulation capabilities necessary to manage **environments**, **grids**, **locations**, and **entities** via a REST API.

This document aligns with the [`openapi/viron-api.json`](openapi/viron-api.json) specification and serves as the implementation guide.

> **Status:** The endpoints and DTOs below are implemented and tested; the checked boxes reflect the current implementation. The remaining gap is aligning entity creation to a request-body contract, tracked in [#135](https://github.com/Preponderous-Software/viron/issues/135).

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
- [x] `POST /api/v1/environments` – Create an environment (JSON body: `name`, `numGrids`, `gridSize`).
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
- [x] `GET /api/v1/locations/entity/{entityId}` – Retrieve the location of a specific entity.
- [x] `PUT /api/v1/locations/{locationId}/entity/{entityId}` – Add an entity to a location.
- [x] `DELETE /api/v1/locations/{locationId}/entity/{entityId}` – Remove an entity from a specific location.
- [x] `DELETE /api/v1/locations/entity/{entityId}` – Remove an entity from its current location.

---

### 4. **Entity Management**
- [x] `GET /api/v1/entities` – Retrieve all entities.
- [x] `GET /api/v1/entities/{id}` – Retrieve a specific entity by ID.
- [x] `POST /api/v1/entities` – Create a new entity (request body: `{ "name": "..." }`).
- [x] `DELETE /api/v1/entities/{id}` – Delete an entity.

---

### 5. **Debug Utilities**
- [x] `POST /api/v1/debug/create-sample-data` – Create an environment with grids, locations, and sample entities for testing.
- [x] `POST /api/v1/debug/create-world-and-place-entity/{environmentName}` – Create a world and place a random entity.

---

## 🧩 DTO Requirements

- [x] **EnvironmentDTO** – Public representation of an environment.
- [x] **CreateEnvironmentRequest** – Request body for creating environments (name, number of grids, grid size).
- [x] **UpdateEnvironmentNameRequest** – Request body for updating environment names.
- [x] **GridDTO** – Public representation of a grid.
- [x] **LocationDTO** – Public representation of a location.
- [x] **EntityDTO** – Public representation of an entity.

---

## ✅ Completion Criteria

- All endpoints in [`openapi/viron-api.json`](openapi/viron-api.json) implemented and tested.
- DTOs returned in responses, avoiding direct exposure of internal entity models.
- Unit and integration tests covering all controllers and repositories.
- Endpoints verified via Postman or Swagger UI.
- API returns proper HTTP status codes and error messages.
- Debug endpoints operational for development testing.
- (Optional for MVP) Pagination and sorting applied to list endpoints.

---
