---
phase: "23"
slug: "providerkeycard-lifecycle-undo-center-groups"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-10-12"
---

# Phase 23 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric 4.16.1 + Compose ui-test (`createComposeRule`), `@Config(sdk = [35])`; text geometry uses `qualifiers="w360dp-h800dp"` + `@GraphicsMode(NATIVE)` |
| **Config file** | `build.gradle.kts` (`isIncludeAndroidResources = true`) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests "io.github.ygaray.yahirandroidtaste.component.ProviderKey*" --tests "io.github.ygaray.yahirandroidtaste.feedback.Undo*"` |
| **Full suite command** | `./gradlew apiDump testDebugUnitTest detekt apiCheck` then `bash tools/test/run-all.sh` |
| **Estimated runtime** | ~300 seconds |

No pixel capture (`captureToImage` times out here): semantics, bounds and source-contract proofs only. One gradle invocation at a time.

---

## Sampling Rate

- **After every task commit:** that task's `--tests` filters in ONE gradle invocation (`apiDump` first when public API changed), then `apiCheck` and `detekt` when `src/main` changed
- **After every plan wave:** `./gradlew apiDump testDebugUnitTest detekt apiCheck`
- **Before `/gsd-verify-work`:** full unfiltered suite + detekt + apiCheck green
- **Max feedback latency:** 600 seconds

---

## Per-Task Verification Map

Filled in by the planner from the PLAN.md tasks (see the plans' `<verify>` blocks); the requirement-to-test map is in `23-RESEARCH.md` § Validation Architecture.

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 23-01-01 | 01 | 1 | VKEY-01, VKEY-02 | T-23-01 | raw key never in semantics while summary shows | Robolectric + tree scan | see 23-01-PLAN.md | ❌ W0 | ⬜ pending |
| 23-02-01 | 02 | 2 | VUNDO-02 | — | N/A | Robolectric + pure seam | see 23-02-PLAN.md | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `ProviderKeyCardDefaultParityTest` and `UndoCenterScreenDefaultParityTest` golden dumps, authored against UNMODIFIED code and committed before any `src/main` edit
- [ ] Shared test support (unmerged-tree dumper, raw-key scan, bounds helper)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| On-device look of the saved-key summary + action row and the Undo Center group row | VKEY-01, VKEY-02, VUNDO-02 | Pixel capture is unavailable under Robolectric here; Gate-1 runs on the TESTER via a throwaway harness and uat-run | Gate-1 (execute stage), never waived |

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`; the Nyquist finalizer owns them post-execution.

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 600s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant`

**Approval:** pending
