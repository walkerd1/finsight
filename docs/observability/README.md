# Observability

**Goal:** See problems before users do, keep costs low, and delete noise on schedule.  
**Stack:** OpenTelemetry → Prometheus (metrics) • Loki (logs) • Tempo (traces) • Grafana (dashboards)

## Retention (defaults)
- **Logs:** 14 days  
- **Metrics:** 45 days  
- **Traces:** 7 days  
- **Monthly roll-ups:** export key metrics (p50/p95/p99, error rates) to S3/Parquet for 12 months

Configure via env:
```ini
RETENTION_LOG_DAYS=14
RETENTION_METRICS_DAYS=45
RETENTION_TRACES_DAYS=7
```

## What we measure (MVP)
### API (Spring/Micrometer)

- `http_server_requests_seconds{status,method,uri}` — latency, error rate  
- `finsight_import_seconds` — CSV import duration (histogram)  
- `finsight_rules_applied_total` — tagging actions
- `db_query_seconds{statement}` — DB latency (sampled)  
- `jvm_*`, `process_*` — health

### Client (Flutter, OTel)

- `app_start_duration_ms`  
- Custom events: `import.started/succeeded/failed`, `sync.pull/push`

## Alerts (first line)

- High error rate: 5xx > 0.5% for 10m → warn; 30m → page/rollback  
- Latency: p95 > 300ms for 30m → rollback flag
- Import failures: > 2% failures over 15m → investigate CSV parser
- DB health: connection saturation > 80% for 10m → scale/limit

Prometheus rules (sketch):
```yaml
groups:
- name: finsight-slo
  rules:
  - alert: ApiErrorRateHigh
    expr: sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
          / sum(rate(http_server_requests_seconds_count[5m])) > 0.005
    for: 10m
    labels: {severity: warning}
    annotations: {summary: "5xx > 0.5% (10m)"}
  - alert: ApiLatencyP95High
    expr: histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))
          > 0.3
    for: 30m
    labels: {severity: critical}
```

## Logging (Loki)

- JSON logs with `timestamp`, `level`, `message`, `trace_id`, `corr_id`, `user_id_hash`? (optional; **no raw PII**)
- Scrub PII at source.
- Labels: `{app="api", env="staging|prod"}`
- Query example:
```logql
{app="api", env="prod"} |= "ERROR" | json | line_format "{{.trace_id}} {{.message}}"
```

## Tracing (Tempo)

- Sample 1–5% in prod (`parentbased_traceidratio` sampler)
- Propagate `traceparent` through API → DB calls → S3 operations

## Dashboards (Grafana quick start)

Create panels for:
1. Red/Green board: error rate, p95 latency, RPS
2. Import health: success %, duration, top failure reasons
3. DB: cpu, connections, slow queries
4. Client: crash-free sessions, time to first screen

## Cost control

- Retention as above; CI fails if TTLs missing (Conftest).
- Logs at info in prod; debug only behind a flag and for short windows.
- Export monthly CSV/Parquet trend snapshots to S3; delete raw TSDB per policy.

## Run locally (dev)

```bash
docker compose -f docker-compose.observability.yml up -d
# exposes: Prometheus :9090, Grafana :3000, Loki :3100, Tempo :3200
```

## Naming & labels

- Metrics: finsight_* prefix for app custom metrics
- Labels: `env`, `service`, `version`, `region`
- Correlation header: `X-Correlation-Id` (echo in logs + metrics)

## Incident notes

- Rollback if p95 > 300ms or 5xx > 0.5% for 30m.
- Postmortems live in `/docs/postmortems/` with a 1-page template.