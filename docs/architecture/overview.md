# Architecture Overview

## What FinSight Is
FinSight is a privacy-first platform that combines **cashflow visibility** and **budget planning** in one place,
designed to scale from individuals to multi-adult/multi-child households with permission controls.

## Current State (Today)
The current focus is ingestion correctness:

- CSV upload/import into raw ingestion tables
- Job lifecycle state machine: RECEIVED → INGESTING → FINALIZED / FAILED / DUPLICATE
- Flyway-managed schema (baseline + future migrations)

### Current Components
- Spring app (`backend/finsight-app`)
- MVC upload flow (minimal UI)
- Raw ingestion entities + repositories
- Import service responsible for parsing + persistence

## Near-Term Next Steps
1. Unit tests that enforce idempotency + failure handling
2. CI pipeline (tests/build on push/PR)
3. Import Profiles (institution/account CSV dialect + mapping)
4. Processing pipeline: raw → normalized transactions

## Future (Planned)
- Household roles & permissions
- GUI transaction management
- Optional Plaid tier for automated imports (subscription to cover external costs)
- Visual dashboards optimized for clarity and low cognitive load
