# Contributing Guide

**TL;DR:** Use **Conventional Commits**, keep PRs to **one vertical slice**, and **never commit secrets**.

---

## 1) Branching & Commit Style

**Branch names**  
feat/import-spine  
fix/rules-null-check  
chore/ci-openapi  
docs/readme-obs  


**Conventional Commits**
- `feat(scope): add CSV presign endpoint`
- `fix(rules): handle leading whitespace in merchant`
- `chore(ci): add smoke test workflow`
- `docs(adr): oss-first policy`
- `refactor(sync): extract upsert mapper`
- `test(api): add import parser happy-path`

> Use `BREAKING CHANGE:` in the body when applicable.

**Commit hygiene**
- Prefer **small commits** with meaningful messages.
- Rebase/fixup on your **feature branch** is fine.
- **Never** rewrite history on `main`.

---

## 2) One-Slice PRs

A PR should deliver a **thin, shippable slice**:
- API + tests + docs (or)
- UI + tests + feature flag
- Infra change + Conftest policy + docs

**Do not** mix unrelated refactors, dependency bumps, or formatting with feature code.

Target size: ~200–400 lines changed. If bigger, explain why.

---

## 3) PR Checklist (copy into description)

- [ ] Small, focused PR (single slice)
- [ ] Unit tests added/updated
- [ ] Feature behind **Unleash** flag (if user-visible)
- [ ] Docs updated (`/docs` or ADR if needed)
- [ ] No secrets committed (checked with SOPS/age)
- [ ] Retention/TTL set for any new logs/metrics/traces
- [ ] Local smoke passed (`docker compose up` happy)
- [ ] CI green

**Review etiquette**
- Leave actionable comments, not drive-bys.
- If scope grows, request a follow-up PR.

---

## 4) Running Locally

**Backend**
```bash
./gradlew test
docker compose -f docker-compose.dev.yml up -d  # api + postgres
```

## Client (Flutter)
```bash
flutter pub get
flutter run  # choose device/emulator
```

## Observability
```bash
docker compose -f docker-compose.observability.yml up -d
# Prometheus :9090, Grafana :3000, Loki :3100, Tempo :3200
```

## 5) Tests & Quality Gates
- **Java**: JUnit + Mockito; aim for critical-path coverage.
- **Flutter**: widget tests for key screens; golden tests optional.
- **Static checks**: linters formatters must pass.
- **Performance guardrails**: p95 API < 300ms in smoke tests.

## Merging
- PR must be **green** on CI.
- Squash-merge with a clean conventional commit title.

## 6) Secrets & Config
- Use SOPS + age for any sensitive config.
- Never commit raw tokens/keys, `.pem`, `.p12`, `.jks`, or credentials.
- `.env` files are local only; use examples like `.env.example`.

## 7) Feature Flags
- All user-visible changes go behind an Unleash flag.
- Naming: `area.feature.rollout` (e.g., import.csv.v1).
- Include a kill-switch path in code.

## 8) Docs & ADRs
- Minor tweaks → /docs/*.
- Architectural decisions → /docs/adr/ADR-xxxx-*.md (use ADR template).
- Link PRs to ADRs where relevant.

## 9) Security & Privacy
- Report vulnerabilities privately: security@finsight.dev.
- PII must not appear in logs or traces.
- Add redaction for any new log fields.
- Follow retention policy: logs 14d, metrics 45d, traces 7d.

## 10) Issue Labels (triage)
- `type:bug`, `type:feat`, `type:chore`, `type:docs`
- `prio:p0/p1/p2`
- `area:api`, `area:client`, `area:infra`, `area:obs`
- `status:blocked`, `status:ready`, `status:in-review`

## 11) License & DCO
- By contributing, you agree your code is compatible with the project’s licenses.
- Use Signed-off-by if DCO is enabled:
```bash
git commit -s -m "feat(api): add import summary"
```

## 12) How to Get a PR Accepted
1. Open an Issue (or link one) describing the thin slice.
2.  Draft PR early; keep it small.
3. Add tests + docs.
4. Keep user-facing bits behind a flag.
5. Make CI happy.
6. Be responsive in review.  
**Thanks for helping build FinSight—small slices, fast feedback, stable releases.**