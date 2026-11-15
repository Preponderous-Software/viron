# Viron MVP Rebuild Plan

This document formalizes the process for rebuilding Viron from the ground up in alignment with the updated **PLANNING.md**, **MVP.md**, and `openapi/viron-api.json` specification.

---

## 🎯 Goals

- Align implementation **exactly** with the OpenAPI specification and MVP scope.
- Establish a **clean, maintainable, testable** Spring Boot 3.3 project.
- Preserve access to old code for reference without carrying over technical debt.
- Integrate project hygiene, tooling, and CI/CD from the start.

---

## 🛠 Steps

### 0. Preparation
Ensure your local repository is up to date:

    git status
    git pull --rebase

### 1. Freeze Current State
Create a branch to preserve the current implementation:

    git checkout -b chore/pre-rebuild-freeze
    git commit -am "chore: freeze current code before MVP rebuild"  # if needed
    git tag pre-rebuild-viron-<YYYYMMDD>

### 2. Create a Rebuild Branch
Create a long-lived feature branch for the rebuild:

    git checkout -b feat/mvp-rebuild

### 3. Handle Old Code
**Option A** – Move old code to `/legacy` for reference:

    mkdir -p legacy
    git mv src legacy/src || true
    git mv pom.xml legacy/pom.xml || true
    git commit -m "chore: move existing implementation to /legacy"

**Option B** – Remove entirely (still preserved via tag):

    git rm -r src || true
    git rm pom.xml || true
    git commit -m "chore: remove old implementation (kept in tag pre-rebuild-...)"

### 4. Scaffold New Spring Boot Project
Generate a fresh Maven Spring Boot 3.3 project (Java 21):

    curl https://start.spring.io/starter.zip \
      -d type=maven-project \
      -d language=java \
      -d bootVersion=3.3.2 \
      -d groupId=preponderous.viron \
      -d artifactId=viron \
      -d name=Viron \
      -d packageName=preponderous.viron \
      -d javaVersion=21 \
      -d dependencies=web,validation,lombok,testcontainers,postgresql \
      -o /tmp/viron.zip

    unzip -o /tmp/viron.zip -d .
    git add .
    git commit -m "chore: scaffold clean Spring Boot 3.3 project for MVP rebuild"

### 5. Add Project Hygiene & Tooling
Include:
- Spotless / Checkstyle
- JaCoCo for coverage
- springdoc-openapi-ui
- Optional: MapStruct, Flyway
- Dev Container + Dockerfile/Compose (if used)

Commit in small, clear steps:

    git commit -am "build: add spotless + jacoco + springdoc (+ config)"
    git commit -am "chore: add devcontainer + docker-compose for local"

### 6. OpenAPI-First Contract
- Place spec at `openapi/viron-api.json`.
- Add OpenAPI Generator to produce DTOs & interfaces (server stubs).
- Commit generated code:

    git commit -am "build: wire OpenAPI generator; generate DTOs & interfaces"

### 7. Implement MVP Features (Follow PLANNING.md Milestones)
- Follow **MVP.md** and **PLANNING.md** milestone order.
- Implement one resource at a time:
  - Controller → Service → Repository
  - Bean validation
  - Error model
  - Happy-path & edge-case tests

Commit in vertical slices:

    git commit -m "feat: environments endpoints (MVP)"
    git commit -m "feat: grids endpoints (MVP)"
    git commit -m "feat: locations endpoints (MVP)"
    git commit -m "feat: entities endpoints (MVP)"

### 8. Cherry-Pick Clean Code from Legacy (If Needed)
Only copy over code that fully aligns with the new spec:

    git cherry-pick <commit-sha>

### 9. CI/CD & Quality Gates
Add GitHub Actions workflow:
- Build
- Test
- JaCoCo report
- Formatting checks

Require checks on `feat/mvp-rebuild` via branch protection:

    git commit -m "ci: add GitHub Actions w/ test + coverage + formatting"

### 10. Merge to Main
When MVP is complete:
- Open PR with:
  - **Base**: `main`
  - **Compare**: `feat/mvp-rebuild`
- Include migration notes in PR description:
  - Endpoint changes
  - Data reset steps (if any)

---

## 📌 Notes
- Keep `/legacy` for reference until MVP is merged, then remove if unnecessary.
- Start tests immediately with the first feature.
- Keep debug tools behind a switch that is off by default so they don’t ship to production. As built, this is the `viron.debug.enabled` property (environment variable `VIRON_DEBUG_ENABLED`, default `false`), which `DebugController` is conditional on; no Spring profile is involved.
- This process ensures a **spec-aligned, maintainable** foundation without losing old work.
