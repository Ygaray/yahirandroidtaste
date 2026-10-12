---
phase: "23"
slug: "providerkeycard-lifecycle-undo-center-groups"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
status: validated
nyquist_compliant: true
wave_0_complete: true
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
| 23-01-01 | 01 | 1 | VKEY-01 | T-23-05 | Wave 0: default-card goldens captured from unmodified v2.5.1 code before any src/main edit | Robolectric golden (tree + bounds) + compile canary | `./gradlew cleanTestDebugUnitTest testDebugUnitTest --tests "*ProviderKeyCardDefaultParityTest" --tests "*ProviderKeyCardSourceCompatV251Test" --tests "*ProviderKeyCardTest" --tests "*VoiceBinaryCompatShimTest" --tests "*VoiceI18nSourceCompatTest" detekt` | ✅ | ✅ green |
| 23-01-02 | 01 | 1 | VKEY-01, VKEY-02 | T-23-01, T-23-07 | tracer: fingerprint-only summary, field not composed, Update/Clear fire once; api.txt regenerated | Robolectric + pure seam + apiCheck | `./gradlew apiDump testDebugUnitTest --tests "*ProviderKeyCardLifecycleTest" --tests "*ProviderKeyCardDefaultParityTest" ... detekt apiCheck` | ✅ | ✅ green |
| 23-01-03 | 01 | 1 | VKEY-01, VKEY-02 | T-23-03 | inFlight/saveEnabled disable actions and clicks never fire; per-provider switching; marker on menu rows | Robolectric click + semantics | `./gradlew apiDump testDebugUnitTest --tests "*ProviderKeyCardLifecycleTest" --tests "*ProviderKeyCardPerProviderTest" ... detekt apiCheck` | ✅ | ✅ green |
| 23-01-04 | 01 | 1 | VKEY-02 | T-23-01, T-23-04, T-23-08 | raw key found nowhere while summary shows (positive control in Field mode); long DE/ES labels stay in window; models hold no key | Robolectric tree scan + NATIVE geometry + source contract; full suite | `./gradlew apiDump testDebugUnitTest detekt apiCheck` | ✅ | ✅ green |
| 23-02-01 | 02 | 2 | VUNDO-02 | T-23-05 | Wave 0: flat Undo Center goldens captured from unmodified v2.5.1 code before the screen is edited | Robolectric golden (tree + bounds) + compile canary | `./gradlew cleanTestDebugUnitTest testDebugUnitTest --tests "*UndoCenterScreenDefaultParityTest" --tests "*UndoCenterScreenSourceCompatV251Test" --tests "*UndoCenterScreenTest" --tests "*UndoHistoryStoreTest" detekt` | ✅ | ✅ green |
| 23-02-02 | 02 | 2 | VUNDO-02 | T-23-07, T-23-11 | tracer: real store group renders header with label/count/status, Undo all calls onUndoGroup(groupId) once; api.txt regenerated | Robolectric + pure seam + apiCheck | `./gradlew apiDump testDebugUnitTest --tests "*UndoCenterRowsTest" --tests "*UndoCenterScreenGroupsTest" ... detekt apiCheck` | ✅ | ✅ green |
| 23-02-03 | 02 | 2 | VUNDO-02 | T-23-11 | Undo all only for Undoable/PartiallyResolved with a callback; indent; no peek on header; refusal keeps retry | Robolectric + real store end-to-end | `./gradlew apiDump testDebugUnitTest --tests "*UndoCenterRowsTest" --tests "*UndoCenterScreenGroupsTest" ... detekt apiCheck` | ✅ | ✅ green |
| 23-02-04 | 02 | 2 | VUNDO-02 | T-23-12 | long DE/ES Undo all stays in window; trailing-lambda and named call shapes compile; parameter order pinned | Robolectric NATIVE + compile canary + source contract | `./gradlew apiDump testDebugUnitTest --tests "*UndoCenterScreenLongLabelTest" --tests "*UndoCenterScreenSourceCompatV251Test" --tests "*UndoCenterScreenSourceContractTest" ... detekt apiCheck` | ✅ | ✅ green |
| 23-02-05 | 02 | 2 | VKEY-01, VKEY-02, VUNDO-02 | T-23-09, T-23-10 | phase gate: full suite, fresh api.txt (16 removed lines vs v2.5.1), run-all, protected tests byte-identical, no tag, Gate-1 handoff recorded (never waived) | Gradle + git audits | `./gradlew apiDump testDebugUnitTest detekt apiCheck` then `bash tools/test/run-all.sh` | n/a (gate) | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [x] `ProviderKeyCardDefaultParityTest` and `UndoCenterScreenDefaultParityTest` golden dumps, authored against UNMODIFIED code and committed before any `src/main` edit
- [x] Shared test support (unmerged-tree dumper, raw-key scan, bounds helper)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| On-device look of the saved-key summary + action row and the Undo Center group row | VKEY-01, VKEY-02, VUNDO-02 | Pixel capture is unavailable under Robolectric here; Gate-1 runs on the TESTER via a throwaway harness and uat-run | Gate-1 (execute stage), never waived |

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`; the Nyquist finalizer owns them post-execution.

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 600s
- [x] _(finalizer-only, post-execution)_ `nyquist_compliant`

**Approval:** validated 2026-10-12

---

## Validation Audit 2026-10-12

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |

All 9 per-task rows have automated tests that ran green in the unfiltered suite (1258 tests, 0 failures, 22 pre-existing skips), detekt and apiCheck green. The on-device look was covered by Gate-1 (23-02-SELF-UAT.md, all_pass, run uatr-20261012T172501Z-65a2a2aa); TalkBack read-out and hub-palette hex stay Gate-2.
