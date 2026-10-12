---
phase: "22"
slug: "outcomesheet-receipt-style"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-10-12"
---

# Phase 22 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric 4.16.1 + Compose ui-test (`createComposeRule`), `@Config(sdk = [35])`, `GraphicsMode.NATIVE` for text metrics |
| **Config file** | `build.gradle.kts` (`testOptions { unitTests { isIncludeAndroidResources = true } }`) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests "io.github.ygaray.yahirandroidtaste.component.OutcomeSheet*" --tests "io.github.ygaray.yahirandroidtaste.model.*"` (batch all filters for a task into ONE invocation) |
| **Full suite command** | `./gradlew apiDump testDebugUnitTest detekt apiCheck` then `bash tools/test/run-all.sh` |
| **Estimated runtime** | quick ~120-240 s (single gradle invocation); full several minutes |

Host build lock: only ONE gradle invocation at a time, never parallel gradle. No pixel capture (`captureToImage` times out in this harness): structural, semantics and bounds proofs only.

---

## Sampling Rate

- **After every task commit:** that task's `--tests` filters in ONE gradle invocation (`apiDump` first only when the public API changed), then `apiCheck` and `detekt` when `src/main` changed.
- **After every plan:** `./gradlew apiDump testDebugUnitTest detekt apiCheck`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** ~240 seconds

---

## Per-Task Verification Map

Task IDs are finalized by the plans; the requirement-level map is:

| Requirement | Behavior | Test Type | Automated Command | File Exists | Status |
|-------------|----------|-----------|-------------------|-------------|--------|
| VRCPT-01 | Defaulted 3-arg call = Classic path; Classic ignores `badge`/`titleActions`/start/end; Receipt bar edges 16dp/368dp at 384dp, divider, bar not starved | Robolectric + source contract | `--tests "*OutcomeSheetClassicParityTest" --tests "*OutcomeSheetClassicParitySourceContractTest" --tests "*OutcomeSheetReceiptNeedsConfirmationTest" --tests "*OutcomeSheetTest" --tests "*FailureRoleSourceContractTest"` | ❌ W0 | ⬜ pending |
| VRCPT-02 | `confirmEnabled=false` disables confirm and `onConfirm` never fires (both styles) | Robolectric click | `--tests "*OutcomeSheetReceiptNeedsConfirmationTest" --tests "*OutcomeSheetClassicParityTest"` | ❌ W0 | ⬜ pending |
| VRCPT-03 | Receipt Success node order; inFlight matrix with `activeWhileInFlight`; badge seams | Robolectric + JUnit | `--tests "*OutcomeSheetReceiptSuccessTest"` | ❌ W0 | ⬜ pending |
| VRCPT-04 | Failure ToneCard holds reason+body, tip below, action end / secondary start; Classic Warn tone; Error default parity | Robolectric + pure seam | `--tests "*OutcomeSheetReceiptFailureTest" --tests "*OutcomeSheetClassicParityTest"` | ❌ W0 | ⬜ pending |
| VRCPT-05 | Pill text "Handled by Local grammar · offline", dot by `onDevice`, German 360dp | Robolectric NATIVE | `--tests "*OutcomeSheetReceiptSuccessTest" --tests "*OutcomeSheetReceiptLongLabelTest"` | ❌ W0 | ⬜ pending |
| VTAG-01 | Every applicable Y-14 tag per Receipt state; Classic tags unchanged; no `testTagsAsResourceId` in `src/main` | Robolectric + scan | `--tests "*OutcomeSheetReceiptTagsTest" --tests "*ToneFoundationInvariantsTest"` | ❌ W0 | ⬜ pending |
| VXR-01 | `liveRegion = Assertive` on `outcome_sheet_undo_refused`, exactly one such node | Robolectric semantics | `--tests "*OutcomeSheetLiveRegionTest"` | ❌ W0 | ⬜ pending |
| VXR-02 | `Failure.undo` renders undo-all + per-item rows in Classic and Receipt | Robolectric | Classic parity + Receipt failure classes | ❌ W0 | ⬜ pending |
| API | Signatures/defaults, v2.5.1 call shapes compile, arity pins updated, `toString` redaction, `apiCheck` green per commit | JUnit + Gradle | `--tests "*OutcomeReceiptModelsTest" --tests "*OutcomeSheetSourceCompatV251Test" --tests "*VoiceModelLabelDefaultsTest" --tests "*DataClassBinaryCompatShimTest" --tests "*VoiceI18nSourceCompatTest"` then `./gradlew apiDump apiCheck` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `OutcomeReceiptModelsTest`, `OutcomeSheetSourceCompatV251Test` (model package / component package)
- [ ] `OutcomeSheetReceiptNeedsConfirmationTest`, `OutcomeSheetReceiptSuccessTest`, `OutcomeSheetReceiptFailureTest`, `OutcomeSheetReceiptLongLabelTest`
- [ ] `OutcomeSheetClassicParityTest`, `OutcomeSheetClassicParitySourceContractTest`, `OutcomeSheetLiveRegionTest`
- [ ] `OutcomeSheetReceiptTagsTest`, `OutcomeSheetReceiptSourceContractTest`
- [ ] Edits to `VoiceModelLabelDefaultsTest` (3 arity-pin assertions) and `DimensTest` (new token group)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Visual look of the Receipt frame, pill and bar on the TESTER; Classic A/B pixel diff vs a real v2.5.1 build | VRCPT-01..05 | Pixel capture unavailable in the harness | Gate-1 tester agent via throwaway harness + `uat-run` (evidence_schema 3) on `adb -s R5CT10XNKQN`; owner screencap review is Phase 24 (Y-15) |
| TalkBack read-out of the Assertive refused-undo surface | VXR-01 | `liveRegion` mode is not visible to uiautomator | Gate-2 owner TalkBack pass |

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Frontmatter `status: draft` and `nyquist_compliant: false` are finalized only post-execution by the Nyquist finalizer.

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 240s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant: true`

**Approval:** pending
