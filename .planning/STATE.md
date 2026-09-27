---
gsd_state_version: "1.0"
milestone: v2.0
milestone_name: Line Reunification
status: planning
last_updated: "2026-09-27T05:08:17.834Z"
last_activity: 2026-09-26
progress:
  total_phases: 0
  completed_phases: 0
  total_plans: 0
  completed_plans: 0
  percent: 0
---

# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-08-28)

**Core value:** The hub stays a coherent design system — not merely a safe, ever-growing pile of domain-agnostic components — as more consumers contribute.
**Current focus:** Phase 04 — Repin Bookkeeping Hardening (complete)

## Current Position

Phase: Not started (defining requirements)
Plan: —
Status: Defining requirements
Last activity: 2026-09-26 — Milestone v2.0 started

## Pending Human-Gated Obligation — Coordinated Repin (SC-4)

**v2.0.0 is published** (`com.github.Ygaray:yahirandroidtaste:v2.0.0`). A hub change is inert until each consumer repins. Run in EACH consumer's own channel (Mechanism B, Android/Gradle/JitPack per ~/.claude/context/workflows/repin.md), NOT from this hub run:

- **SecondBrain** — single-hop `v1.10.0 -> v2.0.0`. Breaking: `FilterBar` removed (migrate to `ChipBar(expandable = ExpandableConfig(...), rawContent = { ... })`); any `ComponentRegistry.Entry(...)` / `ComponentRow(...)` call sites must supply the now-required `tier`. Edit `gradle/libs.versions.toml`, `./gradlew --refresh-dependencies :app:dependencies | grep yahirandroidtaste` (must show v2.0.0), `assembleDebug`, reinstall + Gate-1 device re-verify.
- **CalTracker** — two-hop `v1.5.0 -> v1.10.0` (catch-up) `-> v2.0.0` (gardening) per D-05. Same Mechanism B; note CalTracker's hub surface excludes the Sheets family and does not use FilterBar (blast-radius grep = 0 files), so the break may be inert there — verify at compile.
- After both land: `repin_status.py reconcile` (Phase 4 tooling) to update the ECOSYSTEM.md repin matrix, then `/gsd-verify-milestone` (Gate-2) to drain UAT and close v1.0.

## Performance Metrics

**Velocity:**

- Total plans completed: 10
- Average duration: - min
- Total execution time: 0 hours

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 1 | 5 | - | - |
| 02 | 2 | - | - |
| 03 | 2 | - | - |
| 04 | 1 | - | - |

**Recent Trend:**

- Last 5 plans: -
- Trend: -

*Updated after each plan completion*

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- Roadmap: LEG → AUD → GARD is the load-bearing dependency spine; GOV and REPIN are independent hardening tracks sequenced between audit and gardening.
- Roadmap: GARD-02's coordinated repin is kept last (Phase 5) — the breaking change is coordinated once, after everything else is in place.

### Pending Todos

None yet.

### Blockers/Concerns

- **ACTIVE, awaiting decision:** Phase 5 (Gardening) is a human-gated coordinated repin of both consumers (SecondBrain + CalTracker) — do not tag or repin without the owner's explicit go-ahead (per CLAUDE.md). As of 2026-09-02, all 3 plans executed and hub-side gates are green; **v2.0.0 was cut + pushed** (operator-authorized 2026-09-02); the remaining consumer repin (SC-4) is deferred to each consumer's own channel. See the "Pending Human-Gated Obligation — Coordinated Repin" section above.
- **KNOWN ISSUE (project-local, tracked):** `metalavaCheckCompatibilityDebug` fails under `./gradlew build` — a Dagger-generated `UndoHistoryStore_Factory` leaked into the `api.txt` baseline (false "Removed class"). Does NOT affect the JitPack publish path (verified green pre-v2.0.0). Full write-up + reproduction + proposed fix: `.planning/KNOWN-ISSUES.md` (KI-2026-09-02-01). Routed here (this repo's own build config), not the control-plane log.
- GOV-03 residual risk (tracked, not fixed): `verify-api-additive.sh` shares
  `verify-additive-diff.sh`'s pre-fix architecture (stale cumulative baseline-vs-current
  comparison), proven live in `tools/test/test-verify-api-additive.sh` case (e) — but the
  Phase-3 VERIFICATION.md audit (2026-09-01) found the true root cause of why it's dormant
  TODAY is different and more severe than first recorded: `tools/hooks/pre-commit` exports
  `API_FILE` as an **absolute** path (`export API_FILE="${API_FILE:-$ROOT/api.txt}"`), and
  `verify-api-additive.sh`'s `git cat-file -e "$BASE:$API_FILE"` check cannot resolve an
  absolute path as a git object path — so the lane-3 API-break check silently no-ops on
  every real commit regardless of tag content (reproduced directly: `git cat-file -e
  "v1.10.0:$(pwd)/api.txt"` fails while the relative form `git cat-file -e "v1.10.0:api.txt"`
  succeeds — `v1.10.0` DOES already carry `api.txt`, contradicting the original "predates
  api.txt" theory). Pre-existing since commit `534ec10`, before Phase 3. Phase 5 must fix
  BOTH: (1) the absolute-vs-relative path bug in `tools/hooks/pre-commit`/
  `verify-api-additive.sh` so the check actually runs, AND (2) the same staged-delta
  comparison-basis fix `verify-additive-diff.sh` got this phase (`git show ":$API_FILE"` vs
  `git show "HEAD:$API_FILE"`, per 03-RESEARCH.md's Pattern-2 code excerpt) — fixing only one
  leaves the check either non-functional or freshly false-flagging.

- Pre-existing, unrelated to Phase 3: `./gradlew build`'s `metalavaCheckCompatibilityDebug` task
  fails (`Removed class ...UndoHistoryStore_Factory`, worker process exit 255) at the phase-03
  base commit (5c2ed5c) as well as post-merge — confirmed NOT introduced by 03-01 or 03-02 (neither
  touches `api.txt` or the `feedback` package). `./gradlew testDebugUnitTest detekt` passes clean.
  Needs investigation before the next tag-cut.

## Deferred Items

Items acknowledged and carried forward from previous milestone close:

| Category | Item | Status | Deferred At |
|----------|------|--------|-------------|
| v2 | GOV-04: automate tier-labeling enforcement in the drift-guard test | Deferred | Requirements definition |
| v2 | ECO-02: auto-repin tooling across all consumers | Deferred | Requirements definition |

## Session Continuity

Last session: 2026-09-01
Stopped at: Completed 04-01-PLAN.md — ECOSYSTEM.md repin-matrix block seeded (hub commit bfec0c9), reconcile proven idempotent, INC-2026-08-28-03 closed (control-plane commit 4cd1e86)
Resume file: None
