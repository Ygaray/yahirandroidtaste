---
phase: 05
slug: gardening-unify-coordinated-repin
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-09-01
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
| 05-01-* | 01 (WO-1) | 1 | GARD-01 | — / N/A | N/A | unit + registry drift-guard | `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*"` | ✅ exists, no new file needed | ⬜ pending |
| 05-02-* | 02 (WO-2) | 1 | GARD-01 | — / N/A | N/A | source-contract (retargeted) | `./gradlew testDebugUnitTest --tests "*TextListBottomSheetEditMenuSourceContractTest*"` | ✅ exists but MUST be edited this phase (Pitfall 1 — extraction moves the string anchors it scans for) | ⬜ pending |
| 05-03-* | 03 (rebaseline + tag surface) | 2 | GARD-01, GARD-02 | — / N/A | N/A | Metalava apiCheck + repin-readiness surfacing | `./gradlew apiCheck` | ✅ tooling exists; `api.txt` needs the `apiDump` rebaseline this phase produces | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

*None: existing infrastructure (Robolectric, Compose-UI-test, the source-contract pattern, `ComponentRegistryDriftGuardTest`, Metalava `apiCheck`) fully covers all phase requirements. No new framework install or shared fixture is needed. The one required test EDIT — retargeting `TextListBottomSheetEditMenuSourceContractTest` per Pitfall 1 — is a mandatory in-scope phase task, not a Wave-0 gap.*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Tag cut + both consumers (SecondBrain, CalTracker) repinned and re-verified at Gate-1, neither stranded | GARD-02 | Human-gated per repo CLAUDE.md/ECOSYSTEM.md §7 — the hub cannot autonomously tag or bump a consumer's coordinate; each consumer repin runs through that consumer's own channel | 1) Confirm hub-side `apiCheck`/tests green and `api.txt` rebaselined. 2) Surface the cut-tag decision to the human for go-ahead. 3) After human confirms tag `v2.0.0` is cut, run `python3 ~/.claude/context/deps/repin_status.py reconcile --hub yahirandroidtaste` to confirm both pins moved. 4) Each consumer's own Gate-1 re-verification happens in that consumer's own channel (CalTracker via an intermediate `v1.10.0` catch-up hop first per D-05, then the gardening tag; SecondBrain via a single-tag move). |

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
- [ ] Wave 0 covers all MISSING references (N/A — no gaps)
- [ ] No watch-mode flags
- [ ] Feedback latency < 90s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time; the
      finalizer sets `true` iff its gap analysis finds zero gaps

**Approval:** pending — finalizer-owned, not set at plan time
