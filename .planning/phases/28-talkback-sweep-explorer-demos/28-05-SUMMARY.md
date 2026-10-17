---
phase: 28-talkback-sweep-explorer-demos
plan: 05
subsystem: explorer
tags: [compose, explorer, demo, outcome-sheet, go-to-item, german, hoisted-sheet-state, registry-render-test]
requires:
  - phase: 28-04
    provides: localized() copy helper in ReleaseDemos.kt and the FeedbackTarget demo fixtures
provides:
  - "GoToItemDemo (8 triggers) and GoToItemSheetDemos(): go-to-item OutcomeSheet demos, single and mixed batch, Receipt and Classic, English and German"
  - "Explorer-only demo_goto_* tags, the demo_goto_opened readout and demo_hoisted_sheet_show"
  - "GoToItemSheetDemosRegistryRenderTest (14) and HoistedSheetDemoRegistryRenderTest (5)"
affects: [28-07]
tech-stack:
  added: []
  patterns: ["own sheet slot and own hoisted SheetState per demo group so no two ModalBottomSheets stack", "hide() then record the key in invokeOnCompletion (the hide-then-navigate order)"]
key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/GoToItemSheetDemosRegistryRenderTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/HoistedSheetDemoRegistryRenderTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ReleaseDemos.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
key-decisions:
  - "PD-1 to PD-5 of the plan applied as written (one enum for the 2 x 2 x 2 matrix, hide then open, one readout, neutral fixtures, manual-clock hoisted test)"
  - "Batch rows are found by the shared outcome_sheet_batch_row tag plus a descendant text, in the unmerged tree, so targeted (merged, clickable) and target-less rows are read the same way"
status: complete
plan_head_before: d61eb7dacbb5f9960c7520e45d72cfcc792107a2
commits: 3
actuals:
  tokens: 55000
  tasks: 3
  commits: 3
---

# Phase 28 Plan 05: Go-to-item sheet demos and the hoisted-sheet proof Summary

The Explorer now shows the OutcomeSheet go-to-item rows (a Success with one target and a mixed batch) in Receipt and Classic, in English and German, and the v2.7.0 hoisted `sheetState` demo is tagged and test-proven to close through `hide()`. No library source and no public API changed.

## What was built

- **Go-to-item demos (Tasks 1 and 2).** `GoToItemDemo` has 8 entries (single and batch, Receipt and Classic, English and German; tags `demo_goto_single_receipt`, `..._german`, `demo_goto_single_classic`, `..._german`, `demo_goto_batch_receipt`, `..._german`, `demo_goto_batch_classic`, `..._german`). `GoToItemSheetDemos()` owns one slot and its own hoisted `rememberModalBottomSheetState(skipPartiallyExpanded = true)`, separate from the three existing sheet slots, so no two sheets stack and `ReceiptDemosRegistryRenderTest` ("nine sheet demos") is untouched and green. It is called right after `ReceiptOutcomeSheetDemos()` in `OutcomeSheetVariants()`. `onOpenTarget` runs `scope.launch { sheetState.hide() }.invokeOnCompletion { shown = null; readout = "Opened: <key>" }`, so the key is read only after the slide finished (the library never hides or navigates). The readout outside the sheet (`demo_goto_opened`) starts at `Opened: none` and reads `Geöffnet: <key>` after a German demo.
- **Fixtures.** Single: `demo-item-1`, title `Item A`, detail `List, 3 items`. Mixed batch: A (succeeded, target `demo-batch-a`), B (succeeded, no target), C (failed with a detail, target `demo-batch-c`), D (failed with a detail, no target). German copy uses `Eintrag A` to `Eintrag D`, the open labels `Eintrag öffnen` and `Eintrag A/C öffnen`, and a long Item C label that wraps at 360dp. Open labels are only exposed as click labels, never drawn.
- **Hoisted demo proof (Task 3).** `DEMO_TAG_HOISTED_SHEET_SHOW = "demo_hoisted_sheet_show"` chained onto the Show button of `ReceiptConfirmHandledByVariant` (one modifier line, nothing else changed). `HoistedSheetDemoRegistryRenderTest` shows the sheet, clicks Log it or Cancel on a stopped clock, asserts the confirm button is STILL composed 50 ms later (so the slot clears after the animation, not at once), and gone after the 2 000 ms budget; and that the sheet can be shown again.
- **Tests.** `GoToItemSheetDemosRegistryRenderTest` has the 14 named tests of the plan; `HoistedSheetDemoRegistryRenderTest` has the 5 named tests.

## Verification

- Final combined run (single `--no-daemon` Gradle invocation, exit 0), all `failures="0"`: GoToItemSheetDemosRegistryRenderTest 14, HoistedSheetDemoRegistryRenderTest 5, ReceiptDemosRegistryRenderTest 14 (unedited), FeedbackStackDemoRegistryRenderTest 15, UndoCenterTargetRowsDemoRegistryRenderTest 6, OutcomeSheetHoistedStateTest 5, CatalogDocsDriftGuardTest 10, DomainVocabularyDriftGuardTest 2, ComponentRegistryDriftGuardTest 1, ComponentStatesMatrixTest 2, ComponentPlaygroundIntegrityTest 5, LibraryInvariantsGuardTest 5. The six guard classes were also run on their own with `--rerun-tasks` (fresh, not up-to-date) with the same result.
- `./gradlew detekt --no-daemon`: green at zero baseline (run after Task 2 and after Task 3).
- `./gradlew apiDump --no-daemon` then `git diff --exit-code api.txt`: unchanged.
- `git diff --stat c1681ff HEAD -- src/main/.../feedback src/main/.../model src/main/.../component/OutcomeSheet.kt` prints nothing; `git diff --stat d61eb7d HEAD` lists only the 2 explorer main files and the 2 new tests.

### ROADMAP criterion 4 (verified, not built)

- Registry counts unchanged: **67 registered, 6 unregistered, 73 total** (API.md header and `ComponentRegistryDriftGuardTest`/`CatalogDocsDriftGuardTest` green; `ComponentRegistry.kt` untouched).
- `FeedbackStackHost` is registered in `FeedbackFamilyScreen.kt` (`name = "FeedbackStackHost"`, family FEEDBACK).
- `rememberFeedbackStackState` is allowlisted in `ComponentRegistry.kt` with the reason "State holder remembered by callers and passed to FeedbackStackHost; non-visual, exercised indirectly via the FeedbackStackHost entry."
- `git diff v2.7.1 HEAD -- api.txt`: the only new top-level composables are `FeedbackStackHost` and `rememberFeedbackStackState`; the other additions are new overloads of the registered `UndoCenterScreen` and `OutcomeSheet` plus non-composable types (`FeedbackStackState`, `FeedbackResult`, `FeedbackTarget`) and added parameters.

## Deviations from Plan

**1. [Process] No separate red run for Task 2 and Task 3.** Task 1 was seen red (3 of 3 failed, trigger missing) and then green. For Task 2 the batch builders and tests were written together and the first run was green (14/14); for Task 3 the test and the one-line tag were written together and the first run was green (5/5). The mid-slide assertion in Task 3 would fail if the slot were cleared at once, so it carries the proof.

**2. [Process] Lane-2 override on Tasks 2 and 3.** Task 2 replaced one line of the Task 1 builder (`goToItemOutcome`), and Task 3 rewrote the Show button modifier line, so the pre-commit hook asked for lane 2. Both were committed with `HUB_LANE_OVERRIDE=2` and the reason in the commit body (Explorer-only, no library signature changed, api.txt unchanged).

**3. [Test shape] One `setContent` per test method.** Tests that visit several demos render once and click the next trigger while a sheet is up; the demo's single slot swaps the live sheet (which is also the behavior the demo documents). `render()` in the test class is idempotent to enforce that.

**4. [Plan wording] Test count.** The acceptance text says "the three tracer names plus the eleven names above (fourteen testcases)"; the file has exactly those 14. The German order and the target-less-rows-open-nothing checks were folded into `germanBatch_tappingAnItem_showsTheGermanReadout` and `batchRowsWithoutATarget_haveNoChevronAndNoClickAction` instead of adding extra test methods.

**Total deviations:** 0 auto-fixed bugs, 4 process or shape notes. **Impact:** none on library code or API.

## Known gaps

- No on-device behavior was checked (Gate-1 tester owns it): the real slide-down feel and the German 360dp look are covered only by Robolectric tree assertions.

## Self-Check: PASSED

- Files exist: GoToItemSheetDemosRegistryRenderTest.kt, HoistedSheetDemoRegistryRenderTest.kt, ReleaseDemos.kt (`enum class GoToItemDemo`), VoiceCommandFamilyScreen.kt (`GoToItemSheetDemos()` once, `DEMO_TAG_HOISTED_SHEET_SHOW`).
- Commits exist: 423682b, 9cb9f9e, 0cdb347 (`git rev-list --count d61eb7d..HEAD` = 3).
