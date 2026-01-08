# FinSight — Viability & Risk Check (v1)

**Date:** 2025-09-23  
**Owner:** Dustin Walker  
**Principles:** Offline-first • Privacy-first • OSS-first • Stability-first • Ship small slices that create real-world value

---

## 1) Snapshot (traffic-light)

| Area | Status | Notes |
|---|---|---|
| Product Core (CSV → normalize → auto-tag → period KPIs) | ✅ **Viable** | Scope tight; path clear. |
| Stack (Flutter + Spring + Postgres + OSS observability) | ✅ **Viable** | Boring-in-a-good-way; good foundations. |
| DevSecOps (CI, smoke, logs, SLOs, flags, retention) | ✅ **Viable** | Guardrails defined; enforcement planned. |
| Offline Sync correctness | ⚠️ **Moderate** | Server-authoritative plan; needs conflict UX. |
| Auth + Store Compliance | ⚠️ **Moderate** | Email/pass + JWT; Privacy/ToS + delete/export needed. |
| Runway / Income path | 🔥 **High** | Must enforce LifeOps triggers and income blocks. |

**Verdict:** Proceed to **D1 “Hello Import.”** Re-score after D1 before widening scope.

---

## 2) Biggest hurdles → concrete moves

### H1 — Offline Sync Correctness (⚠️)
- **Risk:** Silent overwrites, duplicate edits, user confusion.
- **Moves:** Server-authoritative, `updated_at` vector; 200-row batch caps; conflict banners; “Reprocess tags” button; integration tests for conflict cases.

### H2 — CSV Chaos & Duplicates (⚠️)
- **Risk:** Bank format variance, dup rows.
- **Moves:** Column mapping templates; checksum key `SHA256(user|account|occurred_at|amount|merchant|description_raw)`; import summary `{imported, skipped, failed[]}`; sample CSVs & error catalog.

### H3 — Auth & Stores (⚠️)
- **Risk:** Token refresh edges; Apple/Play privacy requirements.
- **Moves:** Keycloak email/pass with refresh; `/me/export`, `/me/delete`; draft **Privacy/ToS**; Android closed test first, then iOS.

### H4 — Feature Flags & Rollout (⚠️)
- **Moves:** Unleash early; every user-visible change behind a flag; canary 10%/30m; kill-switch policy; naming `area.feature.rollout`.

### H5 — Observability Cost Control (⚠️)
- **Moves:** TTLs (logs 14d, metrics 45d, traces 7d); monthly metric roll-ups to S3; Conftest in CI to fail on missing TTLs.

### H6 — Runway & Focus (🔥)
- **Moves:** LifeOps triggers: <60d runway → daily income block; <30d → emergency plan. Experiments: paid closed beta; CSV micro-gigs.

---

## 3) Go / No-Go Gates

### Gate D1 — “Hello Import” (GO when all true)
- CI/Smoke green on `main` (p95 read < **300ms**; 5xx < **0.5%** during smoke).
- `/imports/presign` + `/imports/commit` working on sample CSV; import summary returned; duplicates skipped.
- DB: Flyway baseline; partition strategy noted; required indexes present.
- Logs: JSON with `X-Correlation-Id` echoed.
- **Retention:** TTLs configured (logs **14d** / metrics **45d** / traces **7d** / CSV **30d**).
- Draft **Privacy/ToS** committed; `/me/export` stub exists.

### Gate D2 — “MVP Beta (Closed)”
- Auto-tag accuracy ≥ **70%** on test CSVs.
- Crash-free sessions ≥ **98%** (client), API error rate < **1%**.
- p95 read < **300ms** typical; 5k-row import avg < **10s**.
- 10–25 weekly actives complete “60-second success” scripts (Heather/Amanda pass).
- Account Export/Delete functional; store privacy labels ready.

**If a gate fails:** do not widen scope—**fix or shrink**.

---

## 4) Income experiments (value now)

| Experiment | What | Target | Kill rule |
|---|---|---:|---|
| Paid Closed Beta | $10 one-time early access (30 seats) + feedback form | ≥ **20** conversions, NPS ≥ **30** | < **10** conversions after 2 weeks |
| CSV Micro-gigs | 1–2 day CSV → normalized pipeline for small orgs | **$500**/gig; **2/mo** | If gigs slow D1/D2 progress |

---

## 5) Viability Score (quick rubric)

| Dimension | Weight | Current | Score |
|---|---:|---:|---:|
| Technical readiness (spine/CI/obs) | 0.30 | 0.8 | 0.24 |
| User validation (beta scripts, simplicity) | 0.30 | 0.5 | 0.15 |
| Financial runway (LifeOps status) | 0.20 | 0.4 | 0.08 |
| Compliance readiness (privacy/ToS/store) | 0.20 | 0.5 | 0.10 |
| **Total** | **1.00** |  —  | **0.57** |

**Interpretation:** 0.55–0.7 → **Proceed with focus** (fix reds before feature creep). Re-score after D1.

---

## 6) Action Checklist (this week)
- [ ] D1 spine merged (imports + list + OpenAPI + Flyway).  
- [ ] Conftest TTL policy enforced in CI.  
- [ ] Conflict banner + “Reprocess tags” in client.  
- [ ] Privacy/ToS draft committed; `/me/export` stub.  
- [ ] Choose client state mgmt (**Riverpod** vs **Bloc**) and stick to it.  
- [ ] Decide hosting for **Unleash** & **GlitchTip** (self-host vs tiny SaaS).

---

## 7) Re-evaluation rule
If **Viability Score < 0.5** or **Runway < 30 days**, **pause feature work** and execute income-first plan until ≥ **0.6** and ≥ **60 days** runway.
