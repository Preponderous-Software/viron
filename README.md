# Viron

**Viron** is your **foundational spatial simulation service** — the bedrock on which worlds are built.  
It manages **environments**, **grids**, **locations**, and **entities** through a clean REST API so you can skip the boilerplate and focus on *fun, emergent gameplay*.  

It serves as a reusable backend component for simulation-based games, AI experiments, and virtual world applications.

---

## 🚀 Why Viron Exists

Every game and simulation needs a shared understanding of **where things are** and **how they relate**.  
Without Viron, developers waste weeks reinventing grid systems, spatial queries, and entity placement logic for each new project.  

With Viron:
- Creating an entire environment takes seconds.
- Populating it with entities is a single API call.
- Debugging is instant and visual.
- Multiplayer is a breeze because the world is already consistent for every client.

---

## 🌍 A Day in the Life with Viron

You sit down with your morning coffee.  
In five minutes, you’ve:
1. **Spawned an entire planet** via `POST /api/v1/environments`.  
2. **Populated it with hundreds of plants and creatures** with one request.  
3. **Watched the simulation come alive** as agents move, interact, and adapt.  
4. **Debugged a gameplay issue** in seconds by querying exactly what’s in a problem area.  

Instead of building coordinate math and entity managers from scratch, you’re free to **design mechanics, test wild ideas, and ship faster**.  

---

## 🎯 Purpose

Viron abstracts away low-level spatial data management so client applications can focus on simulation logic, rendering, and game mechanics.

Core responsibilities:
- Managing hierarchical spatial structures (environments → grids → locations).
- Tracking entity placement and movement.
- Providing clean, testable REST APIs.
- Offering debug tools for rapid development and testing.

---

## 📦 Features (MVP Scope)

The MVP implements the endpoints defined in `docs/openapi/viron-api.json` and documented in `docs/MVP.md`.

**Environment Management**
- Create, retrieve, rename, and delete environments.
- Create grids as squares (`gridSize`) or with independent dimensions (`numRows`/`numColumns`).
- Query environments by ID, name, or contained entity.

**Grid Management**
- Retrieve grids by ID or environment.
- Find the grid containing a specific entity.
- Rename a grid.

**Location Management**
- Retrieve locations by ID, grid, or environment.
- Place an entity at a location, move a placed entity to an adjacent location, and remove it
  from a location or from wherever it currently stands.
- Retrieve the entities a location holds, report whether it holds any at all, and find the
  locations in a grid that hold none.
- Retrieve the locations adjacent to a location within its grid.

**Entity Management**
- Create, retrieve, rename, and delete entities.
- Query entities by environment, grid, or location, and list the entities that are not placed
  anywhere.

**Debug Utilities**
- Generate sample environments, grids, locations, and entities.
- Quickly create a world and place an entity for testing.
- Disabled by default — set `VIRON_DEBUG_ENABLED=true` (property `viron.debug.enabled`) to
  register `/api/v1/debug/**`. Left off, those endpoints are not mapped at all, so debug
  tooling does not ship to production.

> For detailed endpoint definitions and request/response formats, see `docs/MVP.md` and `docs/openapi/viron-api.json`.

---

## 🛠 Tech Stack

- Java 21
- Spring Boot 3
- Lombok
- MapStruct (model ↔ DTO mapping)
- Spring Security (OAuth2 resource server / JWT)
- PostgreSQL (persistence layer), pooled with HikariCP
- Maven (build tool)
- Docker + Docker Compose (deployment)
- Swagger/OpenAPI (API documentation)
- JaCoCo (test coverage)
- Planned: Flyway (future migrations); schema currently comes from `db-scripts/setup/`, with
  changes to an already-created schema kept as hand-run scripts in `db-scripts/migrations/`

---

## 📂 Project Structure

viron/  
 ├── src/main/java/preponderous/viron/  
 │    ├── config/            # Spring configuration (security, database, OpenAPI)  
 │    ├── controllers/       # REST controllers (Environment, Grid, Location, Entity, Debug)  
 │    ├── database/          # JDBC access helpers  
 │    ├── dto/               # Data Transfer Objects for API requests/responses to keep internal models private  
 │    ├── exceptions/        # Exception types and the global exception handler  
 │    ├── factories/         # Creation logic for environments and entities  
 │    ├── mappers/           # MapStruct mappers between models and DTOs  
 │    ├── models/            # Internal domain models  
 │    ├── repositories/      # Data access layer  
 │    ├── services/          # Business logic  
 │    └── trace/             # Usage reporting (vendored trace client + the startup event)  
 ├── src/main/python/        # Python client SDK  
 ├── src/test/java/...       # Unit and integration tests  
 ├── src/test/python/...     # Python client SDK tests (pytest)  
 ├── db-scripts/             # SQL schema setup scripts, and migrations for existing databases  
 ├── docs/  
 │    ├── MVP.md             # Implementation checklist for MVP  
 │    ├── PLANNING.md        # MVP issue plan, grouped into milestones  
 │    ├── REBUILD_PLAN.md    # Process and conventions for the ground-up rebuild  
 │    ├── diagrams/          # Class-usage diagram (draw.io source and rendered PNG)  
 │    └── openapi/  
 │         └── viron-api.json  # API specification  
 ├── postman/                # Postman collection generated from the API specification  
 ├── pom.xml                 # Maven configuration  
 └── README.md               # This file  

---

## 🚀 Getting Started

### Prerequisites
- Java 21
- Maven 3.9+
- Docker & Docker Compose

### Installation
mvn clean install

### Configuration
Copy `sample.env` to `.env` and review the values before starting anything.  
`JWT_SECRET` is required — it has no default, so the service will not start until it is set.  
It must match the secret used by the UserAuth service that issues the tokens, and for `HS256`
it must be at least 32 bytes.

#### Usage reporting
On start-up the service sends **one** `startup` event to the trace usage-tracking service at
`https://trace.danielstephenson.dev`: the program name (`viron`), its version, and the tag
`service=true`. Nothing is sent per request, and nothing about users, hosts, addresses or data
is ever included. The event goes out on a background thread and is dropped if the trace server
is down or slow, so it can never delay start-up or a request.

It is on by default and configured by the `usage-reporting.*` properties in
`application.properties`, each with an environment override:

| Property | Environment variable | Default |
|---|---|---|
| `usage-reporting.enabled` | `USAGE_REPORTING_ENABLED` | `true` |
| `usage-reporting.endpoint` | `USAGE_REPORTING_ENDPOINT` | `https://trace.danielstephenson.dev` |
| `usage-reporting.key` | `USAGE_REPORTING_KEY` | the bundled program key |

Set `USAGE_REPORTING_ENABLED=false` to turn it off; every opt-out is listed under
[Usage reporting](#usage-reporting) below.

### Running Locally
docker-compose up --build  
API will be available at: http://localhost:9999

---

## 📜 API Documentation

Once running, you can view the interactive API docs:  
http://localhost:9999/swagger-ui.html  
or refer to the `docs/openapi/viron-api.json` file.

---

## 🧪 Testing

Run all Java unit and integration tests:  
mvn test

Install the Python client's dependencies (requires Python 3.8+):  
pip install -r requirements.txt

Run the Python client tests:  
pytest

`pytest.ini` puts the repository root on the path, so the client is imported the same way
from tests as it is from application code (`src.main.python.preponderous.viron...`). The
`pythonpath` setting it uses arrived in pytest 7.0, which is why `requirements.txt` asks for
that release or newer.

CI runs both suites: the Java build and tests, and the Python client tests on Python 3.8 and
3.12.

---

## Usage reporting

Usage reporting is on by default: on start-up the service sends one `startup` event (program
name `viron`, version, tag `service=true`) to the trace service at
`https://trace.danielstephenson.dev`. Nothing is sent per request, and nothing about users,
hosts, addresses or data is ever included.

Turn it off any of these ways:

- `USAGE_REPORTING_ENABLED=false` in the environment (`usage-reporting.enabled=false` in
  `application.properties`; `compose.yml` passes the variable through)
- `TRACE_USAGE_REPORTING=off` in the environment (also `false`, `0`, `no`; shared by every
  program that reports to trace, and it wins over the setting above)
- `DO_NOT_TRACK=1` in the environment (also `true`, `yes`; see
  [consoledonottrack.com](https://consoledonottrack.com))

One line is logged at start-up saying whether reporting is on and, if it is off, which switch
turned it off. Details: https://github.com/Stephenson-Software/trace#usage-reporting

---

## 📄 License

This project is licensed under the MIT License.  

**Copyright © 2022-2025 Daniel McCoy Stephenson. All rights reserved.**

---

## 📬 Contact

For inquiries, feature requests, or contributions, please open an issue or reach out via the official GitHub repository:  
https://github.com/Preponderous-Software/Viron 
