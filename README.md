# FinSight

FinSight is a **privacy-first personal finance platform** that unifies **cashflow + budgeting** into one system — designed to scale from individuals to **multi-adult / multi-child households** with clear permission controls for visibility and responsibility.

The goal: a **single place to see what matters** — what money is coming in, what’s going out, what’s planned, what’s real, and what’s actually available — without subscription traps, spreadsheet hell, or shame-based UX.

## Current Status (What Exists Today)
FinSight is currently focused on building a **robust ingestion foundation**:

- **CSV-based transaction import** (initial ingestion pipeline)
- Import job lifecycle tracking (e.g., RECEIVED → INGESTING → FINALIZED / FAILED / DUPLICATE)
- **Hash-based deduplication** (file- and/or row-level as the model evolves)
- **Raw ingestion tables** + deterministic schema via **Flyway**
- Minimal UI for upload/testing (Spring MVC template)

> The system is intentionally being built correctness-first: idempotency, failure handling, and clear state transitions before expanding features.

## Planned Capabilities (Trajectory)
FinSight will grow in order of need, with each layer built on top of the ingestion and data model foundation:

- **Import Profiles** (institution/account-specific CSV dialect + mapping)
- Automated processing pipeline from raw → normalized transactions
- Household roles & permissions (visibility + responsibility controls)
- Optional GUI transaction entry/editing
- Optional automated imports via **Plaid** (subscription tier to cover third-party costs)
- Visual dashboards designed to be **fun to look at**, easy to use, and supportive for **neurodivergent users**

## Tech Stack
Current stack (evolves as needed):
- **Java + Spring Framework**
- **MySQL**
- **Maven**
- **Flyway** (schema migrations)
- **GitHub** (version control + CI/CD)

## Repository Layout
- `backend/finsight-app/` — Spring application
- `backend/finsight-app/src/main/resources/db/migration/` — Flyway migrations
- `api/openapi.yaml` — API spec (evolving)
- `docs/` — architecture notes, ADRs, legal

## Getting Started (Local Dev)
Prereqs:
- Java (matching project config)
- MySQL

Run the Spring app:
1. Configure `application.properties` (DB connection)
2. Start the app:
   - `./mvnw spring-boot:run` (from `backend/finsight-app`)

Flyway migrations run automatically on startup (based on Spring config).

## Documentation
- Docs index: `docs/README.md`
- Architecture: `docs/architecture/`
- ADRs: `docs/adr/`
- Legal: `docs/legal/`

## License
See `LICENSE`.

---

If you're a recruiter: this repository is public to demonstrate **real-world engineering**:
data modeling, ingestion pipelines, schema migration discipline, and test-driven robustness.