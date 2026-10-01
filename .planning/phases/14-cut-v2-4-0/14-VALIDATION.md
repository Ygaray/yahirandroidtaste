---
phase: "14"
slug: "cut-v2-4-0"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: validated
nyquist_compliant: true
wave_0_complete: true
created: "2026-09-30"
---

# Phase 14 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit (Robolectric/Compose-UI tests) via Gradle `testDebugUnitTest`; Detekt via `detekt`; Metalava via `apiCheck` |
| **Config file** | `build.gradle.kts` (Metalava block, `apiCheck`/`apiDump` tasks), `config/detekt-baseline.xml` |
| **Quick run command** | `./gradlew testDebugUnitTest` (unscoped — a `--tests`-filtered run does not exercise the full registry drift guard) |
| **Full suite command** | `./gradlew testDebugUnitTest && ./gradlew detekt && ./gradlew apiCheck` |
| **Estimated runtime** | ~3-5 minutes (full suite + apiCheck); JitPack build confirmation adds a few minutes of external wait |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew testDebugUnitTest && ./gradlew detekt && ./gradlew apiCheck`
- **After every plan wave:** Same full suite — this phase is single-wave, single-plan; no product code changes beyond the tag itself
- **Before `/gsd-verify-work`:** Full suite must be green AT THE EXACT COMMIT BEING TAGGED (re-run fresh — do not cite Phase 13's evidence)
- **Max feedback latency:** ~5-10 minutes (dominated by JitPack's external build latency for step 4)

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 14-01-01 | 01 | 1 | SHIP-01 (§11 step 1) | — | Full suite + all 4 drift guards + zero-baseline detekt green at tagged commit | unit/static-analysis | `./gradlew testDebugUnitTest && ./gradlew detekt` | ✅ | ✅ green |
| 14-01-02 | 01 | 1 | SHIP-01 (§11 step 2) | — | Public API strictly additive vs `v2.3.0`, `api.txt` refreshed+committed | static-analysis (Metalava) | `cp api.txt /tmp/b.bak && git show v2.3.0:api.txt > api.txt && ./gradlew apiCheck; RC=$?; cp /tmp/b.bak api.txt; exit $RC` | ✅ | ✅ green |
| 14-01-03 | 01 | 1 | SHIP-01 (§11 step 3) | — | Tagged commit pushed to `origin` | scripted git check | `git ls-remote --tags origin v2.4.0` matches `git rev-parse v2.4.0^{}` | ✅ | ✅ green |
| 14-01-04 | 01 | 1 | SHIP-01 (§11 step 4) | — | JitPack resolves `v2.4.0` (pom + aar + builds-API cross-checked against the tagged commit SHA) | smoke (network) | `curl` triple against jitpack.io (pom, aar, builds-API JSON) | ✅ | ✅ green |
| 14-01-05 | 01 | 1 | SHIP-01 (ledger) | — | Full §11 ledger row produced as a durable artifact for the orchestrator (A14: never self-written to the ledger) | manual/scripted | produce `14-SHIP-LEDGER-ROW.md`; best-effort live message if a messaging tool is available | ✅ (file artifact; no messaging tool was reachable — resolved per 14-VERIFICATION.md's accepted override) | ✅ green (manual-only, by design — see Manual-Only Verifications) |
| 14-01-06 | 01 | 1 | SHIP-02 | — | No stray tag beyond `v2.4.0` created; `git.create_tag` is `false` | config check | `grep '"create_tag"' .planning/config.json` shows `false` | ✅ | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements. No new test file is needed — this phase authors zero new production or test code; it executes already-proven verification commands (all 4 drift-guard tests, detekt, Metalava `apiCheck`) against the final commit, then performs the tag/push/JitPack-confirm sequence.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| A14 orchestrator messaging of the §11 ledger row | SHIP-01 | No in-repo precedent for live agent-to-agent messaging from inside a GSD plan task (RESEARCH.md Open Question #1); the durable `14-SHIP-LEDGER-ROW.md` artifact is the fallback-always path | **Resolved at execution:** no live messaging tool was reachable from the execution context; `14-SHIP-LEDGER-ROW.md` was produced as the authoritative record. `14-VERIFICATION.md` records an accepted override treating this as satisfying SHIP-01/A14 per the plan's own pre-approved Assumption A1 fallback. **Still outstanding:** a human or the milestone/control-plane orchestrator session must actually relay the file's contents onward to `yahir-gsd-control-plane-f2` (and from there to SecondBrain/CalTracker) — this repo cannot do that itself. |

---

## Validation Audit 2026-10-01

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |

All 6 per-task verifications (14-01-01 through 14-01-06) ran as fresh, live commands during
execution and are documented verbatim in `14-SHIP-GATE-EVIDENCE.md`; independently re-confirmed
by `14-VERIFICATION.md`. No automatable requirement is missing coverage. The single manual-only
item (14-01-05, A14 ledger messaging) was always a manual/scripted verification by design (not an
automatable gap) and is resolved per the accepted override in `14-VERIFICATION.md`.

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> These are finalized ONLY post-execution by the Nyquist finalizer (the `verify:post` →
> `validate-phase` hook, invoked by execute-phase `finalize_nyquist_validation` after Gate-1). Never
> set `nyquist_compliant: true` — or otherwise "sign off" compliance — at plan time, and do not let
> the plan-checker do so (INC-2026-07-27-01: a premature plan-time flip is what caused inconsistent
> COMPLIANT/PARTIAL milestone-audit states).

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 600s
- [x] _(finalizer-only, post-execution)_ `nyquist_compliant: true` — gap analysis found zero
      automatable gaps; all 6 per-task verifications ran and passed (5 automated + 1 manual-only
      by design, itself resolved at execution and tracked above)

**Approval:** verified 2026-10-01 — finalized by the execute-phase Nyquist finalizer (auto mode)
