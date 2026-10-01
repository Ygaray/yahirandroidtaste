---
phase: "14"
slug: "cut-v2-4-0"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
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
| 14-01-01 | 01 | 1 | SHIP-01 (§11 step 1) | — | Full suite + all 4 drift guards + zero-baseline detekt green at tagged commit | unit/static-analysis | `./gradlew testDebugUnitTest && ./gradlew detekt` | ✅ | ⬜ pending |
| 14-01-02 | 01 | 1 | SHIP-01 (§11 step 2) | — | Public API strictly additive vs `v2.3.0`, `api.txt` refreshed+committed | static-analysis (Metalava) | `cp api.txt /tmp/b.bak && git show v2.3.0:api.txt > api.txt && ./gradlew apiCheck; RC=$?; cp /tmp/b.bak api.txt; exit $RC` | ✅ | ⬜ pending |
| 14-01-03 | 01 | 1 | SHIP-01 (§11 step 3) | — | Tagged commit pushed to `origin` | scripted git check | `git ls-remote --tags origin v2.4.0` matches `git rev-parse v2.4.0^{}` | ✅ | ⬜ pending |
| 14-01-04 | 01 | 1 | SHIP-01 (§11 step 4) | — | JitPack resolves `v2.4.0` (pom + aar + builds-API cross-checked against the tagged commit SHA) | smoke (network) | `curl` triple against jitpack.io (pom, aar, builds-API JSON) | ✅ | ⬜ pending |
| 14-01-05 | 01 | 1 | SHIP-01 (ledger) | — | Full §11 ledger row produced as a durable artifact for the orchestrator (A14: never self-written to the ledger) | manual/scripted | produce `14-SHIP-LEDGER-ROW.md`; best-effort live message if a messaging tool is available | ⚠ mechanism unconfirmed (Open Question #1) | ⬜ pending |
| 14-01-06 | 01 | 1 | SHIP-02 | — | No stray tag beyond `v2.4.0` created; `git.create_tag` is `false` | config check | `grep '"create_tag"' .planning/config.json` shows `false` | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements. No new test file is needed — this phase authors zero new production or test code; it executes already-proven verification commands (all 4 drift-guard tests, detekt, Metalava `apiCheck`) against the final commit, then performs the tag/push/JitPack-confirm sequence.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| A14 orchestrator messaging of the §11 ledger row | SHIP-01 | No in-repo precedent for live agent-to-agent messaging from inside a GSD plan task (RESEARCH.md Open Question #1); the durable `14-SHIP-LEDGER-ROW.md` artifact is the fallback-always path | After the tag is confirmed resolvable, produce the ledger row (repo, tag, commit SHA, JitPack coordinate, contents summary, evidence path) in `14-SHIP-LEDGER-ROW.md`; if a live messaging tool is available in the execution context, also send it — but the file artifact is the authoritative, always-produced record the human/orchestrator session relays from |

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> These are finalized ONLY post-execution by the Nyquist finalizer (the `verify:post` →
> `validate-phase` hook, invoked by execute-phase `finalize_nyquist_validation` after Gate-1). Never
> set `nyquist_compliant: true` — or otherwise "sign off" compliance — at plan time, and do not let
> the plan-checker do so (INC-2026-07-27-01: a premature plan-time flip is what caused inconsistent
> COMPLIANT/PARTIAL milestone-audit states).

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 600s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time; the
      finalizer sets `true` iff its gap analysis finds zero gaps

**Approval:** pending — finalizer-owned, not set at plan time
