# ADR-0003: OSS-First Policy & Vendor-Neutral Design

**Status**: Accepted  
**Date**: 2025-09-21  
**Owner**: Dustin Walker  

## Context

We want FinSight’s pipeline and product to be portable, affordable, and resilient to vendor license changes or pricing shocks. Using open tooling where practical reduces long-term cost and lock-in while improving local/dev parity.

## Decision

1. Prefer clean, open-source, self-hostable tools (permissive or strong copyleft licenses).
2. When a proprietary/managed service offers clear net value (e.g., managed DB), we keep a **thin adapter boundary** and document an OSS fallback.
3. All infrastructure is defined as code and reproducible; no hand-tuned snowflakes.
4. Telemetry and artifacts must have explicit retention/TTL by default.
5. Every exception to this policy requires: (a) an ADR, (b) the adapter location, (c) the fallback plan with steps.

## Implications

- Faster local parity; easier vendor exits or migrations.
- Slightly more ops work up front (worth it for control/cost).
- Clear “off-ramps” reduce strategic risk and keep us compliant with our retention policy.

## Initial Stack (examples)

- **IaC**: **OpenTofu** (Terraform-compatible)
- **Secrets & Config**: **SOPS** + **age** (and/or External Secrets when on K8s)
- **Auth**: **Keycloak** (JWT, roles)
- **Observability**: **OpenTelemetry** + **Prometheus** (metrics) + **Grafana** (dashboards) + **Loki** (logs) + **Tempo** (traces)
- **Error Tracking**: **GlitchTip** (Sentry-compatible)
- **Feature Flags**: **Unleash**
- **API Gateway/Ingress**: **Traefik** (Envoy/NGINX acceptable)
- **Registry/Artifacts**: **GHCR*** now; **Harbor** later if multi-tenant/policy needs
- **Search**: **Postgres FTS** first; add **OpenSearch** only if needed
- **Chat/Coordination**: **Slack** (internal), **Discord** (beta); self-host options later (**Mattermost/Zulip**)
- **Object Storage**: **S3** now (abstraction layer); **MinIO** fallback
- **Runtime**: Containers + Compose (dev). ECS/RDS to start; k3s/microk8s later if needed.

## Enforcement

- **Policy-as-Code**: OPA/Conftest checks ensure:
    - retention/TTL is set on logs, metrics, traces, S3 buckets, CI artifacts
    - IaC resources are tagged and reproducible
    - No plain secrets in repo (SOPS-encrypted only)
- **Adapters**: Cloud/vendor SDKs are wrapped in /internal/platform/* packages.

## Risks

- Extra ops learning curve (mitigated by phased adoption).
- Some OSS tools require maintenance (mitigated by small, well-understood components and backups).

## Review & Lifecycle

- Review each quarter or before major infra changes.
- Any exception must include a rollback path and cost comparison.
- This ADR governs new tool adoption from this date forward.