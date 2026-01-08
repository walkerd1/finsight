# OSS-First Substitutions — Cheat Sheet

**Date:** 2025-09-21  
**Owner:** Dustin Walker  
**Related:** ADR-0003 — OSS-First Policy & Vendor-Neutral Design
**Status**: Draft v1  
**Purpose**: Pick open-source, self-hostable defaults. If we use a managed/vendor service, we keep a thin adapter and document a clean exit.

## Stack by layer
|Layer|Default Choice|Why|OSS Alt / Fallback|Notes|  
|:---|---|---|---|---|  
|**VCS**|**GitHub**|Network effect, Actions|Gitea / GitLab CE|Mirror repo weekly to keep exit ramp.|
|**CI/CD**|**GitHub Actions**|Fast start, good ecosystem|Woodpecker CI / Drone / Tekton|Keep workflows portable (no vendor-locked actions).|
|**IaC**|**OpenTofu**|Terraform-compatible, OSS|—|Modules live in `/infra/tofu`.|
|**Secrets & Config**|**SOPS + age**|Git-ops friendly encryption|External Secrets Operator|No plain secrets in repo.|
|**AuthN/Z**|**Keycloak**|Mature SSO, roles, JWT|—|Wrap IdP calls in `/internal/platform/auth`.|
|**Runtime (dev)**|**Docker + Compose**|Simple local parity|Podman|Compose files are living docs.|
|**Runtime (prod)**|**ECS + RDS (managed)**|Pragmatic to ship MVP|k3s/microk8s (later)|Keep infra neutral via OpenTofu.|
|**Ingress / Gateway**|**Traefik**|Simple, TLS, dynamic|Envoy / NGINX|Config in `/deploy/traefik`.|
|**DB**|**Postgres**|Rock solid, FTS built-in|—|Monthly partitions for `transactions`.|
|**Cache / Queue**|**Redis**|Ubiquitous, fast|—|Add only when profiling demands it.|
|**Object Storage**|**S3**|Cheap, durable|MinIO|Keep client behind `/internal/platform/objects`.|
|**Search**|**Postgres FTS**|Good enough to start|OpenSearch|Add only if queries demand relevance/scaling.|
|**Observability**|**Prometheus + Grafana + Loki + Tempo (+ OTel)**|End-to-end OSS|—|TTLs: logs 14d, metrics 45d, traces 7d.|
|**Error Tracking**|**GlitchTip**|Sentry-compatible, OSS|—|DSNs via SOPS.|
|**Feature Flags**|**Unleash**|Lightweight OSS|—|Require flags for user-visible changes.|
|**Registry / Artifacts**|**GHCR (now)**|Simple, free|Harbor|Keep last 10 images/branch; tags forever.|
|**Mobile Build/Release**|**Fastlane**|OSS automation for Play/App Store|—|Store creds encrypted with SOPS.|
|**Desktop Packaging**|**Flutter Desktop**|Already in stack|Tauri|Consider later if app size/footprint matters.|
|**Docs Site**|**MkDocs (Material)**|Clean, fast, OSS|Docusaurus|Lives at `/docs/site`.|
|**Analytics (site)**|**Plausible**|Privacy-friendly|Matomo|Optional; never for in-app PII.|
|**Chat (internal)**|**Slack (free)**|Frictionless to start|Mattermost / Zulip|Discord for community; strict boundary.|
|**Policy as Code**|**OPA / Conftest**|Enforce infra rules|—|CI must fail if TTLs/labels missing.|

## Rules of thumb

- Start cheap & boring. Add components only when a real bottleneck appears.
- Adapter first. Any vendor SDK gets wrapped in `/internal/platform/<service>` so swapping is trivial.
- Exit ramps documented. Each ADR that approves a vendor also lists the OSS fallback and migration steps.
- Retention required. Every log, metric, trace, bucket, and CI artifact must have a TTL. CI fails if missing.
- Flags everywhere. User-visible behavior is behind Unleash flags; kill-switch is mandatory.

## Directory conventions
```bash
/infra/tofu/                # OpenTofu modules & envs
/deploy/traefik/            # Ingress config
/internal/platform/auth/     # Keycloak adapter
/internal/platform/objects/  # S3/MinIO adapter
/internal/flags/             # Unleash wiring
/internal/telemetry/         # OTel/Micrometer setup
/docs/adr/                   # Architecture decisions
```

## Retention knobs (env defaults)
```ini
RETENTION_LOG_DAYS=14
RETENTION_METRICS_DAYS=45
RETENTION_TRACES_DAYS=7
RETENTION_TX_HOT_MONTHS=24
RETENTION_IMPORT_CSV_DAYS=30
REGISTRY_KEEP_IMAGES_PER_BRANCH=10
```

## Enforcement (policy-as-code)
- CI runs **Conftest** on IaC/manifests to ensure every log/metric/trace/bucket/artifact has a **TTL** and resources are **tagged**.
- Vendor SDKs must be wrapped in `/internal/platform/<service>` to preserve the swap-out path.


## Exception process
If we choose a proprietary/managed tool:
1. Write an ADR: value case, cost, and OSS fallback.
2. Identify the adapter package path.
3. Add a migration checklist (how to exit in ≤1 week).
4. Ensure retention/TTL and tags are applied from day one.