# FinSight

Offline-first, privacy-first personal finance app (CSV import → auto-tag → period KPIs).

## Quick Start
1) Create a GitHub repo and push this starter (see commands below).
2) Run the LifeOps metrics exporter to track runway/time.
3) Start Sprint 1: D1 "Hello Import" (CSV → normalized → summary).

### LifeOps exporter
```bash
python tools/metrics_exporter.py --state config/lifeops_state.json --time-log logs/time_log.csv --port 9105
```

### Repo Conventions
- Release trains: 2 weeks
- Conventional commits
- Everything user-visible behind a feature flag
