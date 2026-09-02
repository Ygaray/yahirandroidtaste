---
phase: 05
slug: gardening-unify-coordinated-repin
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: validated
nyquist_compliant: true
wave_0_complete: false
created: 2026-09-01
validated: 2026-09-02
---

# Phase 05 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric (`libs.robolectric`) + Compose UI-test-junit4/manifest, all already wired in `build.gradle.kts` |
| **Config file** | `build.gradle.kts` (module root — no separate test config file) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests "*ComponentRegistry*"` (registry-scoped) or `--tests "*TextListBottomSheetEditMenuSourceContractTest*"` (WO-2-scoped) |
| **Full suite command** | `./gradlew testDebugUnitTest detekt apiCheck` |
| **Estimated runtime** | ~90 seconds (quick) / ~4 minutes (full suite incl. detekt + apiCheck) |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew testDebugUnitTest --tests "*ComponentRegistry*"` plus the directly-touched source-contract test class (`*TextListBottomSheetEditMenuSourceContractTest*` for WO-2 tasks).
- **After every plan wave:** Run `./gradlew testDebugUnitTest detekt` (full suite, zero-baseline detekt per CLAUDE.md).
- **Before `/gsd-verify-work`:** `./gradlew testDebugUnitTest detekt apiCheck` must be green — `apiCheck` must pass against the freshly-committed rebaselined `api.txt`, proving the intentional break is fully captured, not a residual mismatch.
- **Max feedback latency:** ~90 seconds (quick command).

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 05-01-* | 01 (WO-1) | 1 | GARD-01 | T-05-01, T-05-03 | N/A | unit + registry drift-guard | `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*"` | ✅ exists, no new file needed | ✅ green |
| 05-02-* | 02 (WO-2) | 2 | GARD-01 | T-05-01, T-05-02 | N/A | source-contract (retargeted) | `./gradlew testDebugUnitTest --tests "*TextListBottomSheetEditMenuSourceContractTest*"` | ✅ exists, edited this phase per Pitfall 1 (region-marker anchors moved to SheetHeaderMenu.kt) | ✅ green |
| 05-03-* | 03 (gate verification + human checkpoint) | 3 | GARD-01, GARD-02 | T-05-04, T-05-05, T-05-06 | N/A | Metalava apiCheck + registry-invariant confirmation | `./gradlew testDebugUnitTest detekt apiCheck` | ✅ tooling exists; `api.txt` rebaselined twice by prior waves | ✅ green (Task 1 automated verification); Task 2 is a checkpoint, not a test — see Manual-Only |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

**Execution evidence:** `./gradlew testDebugUnitTest detekt apiCheck` re-run independently by both
`gsd-code-reviewer` and `gsd-verifier` (forced `--rerun-tasks`, non-cached) during this phase's
execution — `BUILD SUCCESSFUL`, 43/43 tasks executed, zero detekt findings both times.
`ComponentRegistryDriftGuardTest` and the retargeted `TextListBottomSheetEditMenuSourceContractTest`
were also run in isolation and pass.

---

## Wave 0 Requirements

*None: existing infrastructure (Robolectric, Compose-UI-test, the source-contract pattern, `ComponentRegistryDriftGuardTest`, Metalava `apiCheck`) fully covers all phase requirements. No new framework install or shared fixture is needed. The one required test EDIT — retargeting `TextListBottomSheetEditMenuSourceContractTest` per Pitfall 1 — was completed as an in-scope phase task (05-02 Task 2), not a Wave-0 gap.*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Tag cut + both consumers (SecondBrain, CalTracker) repinned and re-verified at Gate-1, neither stranded | GARD-02 | Human-gated per repo CLAUDE.md/ECOSYSTEM.md §7 — the hub cannot autonomously tag or bump a consumer's coordinate; each consumer repin runs through that consumer's own channel | 1) Confirm hub-side `apiCheck`/tests green and `api.txt` rebaselined — DONE this phase. 2) Surface the cut-tag decision to the human for go-ahead — DONE (05-03-SUMMARY.md; `checkpoint:decision` correctly left unresolved, awaiting human go/hold). 3) After human confirms tag `v2.0.0` is cut, run `python3 ~/.claude/context/deps/repin_status.py reconcile --hub yahirandroidtaste` to confirm both pins moved. 4) Each consumer's own Gate-1 re-verification happens in that consumer's own channel (CalTracker via an intermediate `v1.10.0` catch-up hop first per D-05, then the gardening tag; SecondBrain via a single-tag move). |

This item is a legitimate, pre-classified Manual-Only verification — not a coverage gap. Nyquist
gap analysis (Step 3) found zero MISSING/PARTIAL requirements: every automated-command-eligible
task (GARD-01, both WO-1/WO-2) has a green, independently-re-run test; GARD-02's tag-cut/repin
behavior has no automated test because none is possible or appropriate from this hub-scoped repo —
it is correctly routed to this Manual-Only table instead.

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references (N/A — no gaps)
- [x] No watch-mode flags
- [x] Feedback latency < 90s
- [x] `nyquist_compliant: true` — finalizer gap analysis (2026-09-02) found zero MISSING/PARTIAL
      requirements; GARD-02's tag-cut/repin behavior is a pre-classified Manual-Only item, not a gap

**Approval:** validated 2026-09-02 by finalize-nyquist-gate (auto mode, `--auto` chain)
