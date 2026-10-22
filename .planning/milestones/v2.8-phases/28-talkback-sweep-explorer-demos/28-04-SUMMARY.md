---
phase: 28-talkback-sweep-explorer-demos
plan: 04
subsystem: explorer
tags: [compose, explorer, demo, feedback-stack, undo-center, german, registry-render-test]
requires:
  - phase: 28-03
    provides: ExplorerTheme scheme pin (the demos render under it)
provides:
  - "FeedbackStackStageDemos(): English and German 360dp stacked-snackbar stage with independent state and an Opened readout"
  - "UndoCenterTargetRowsDemos(): English and German go-to-item rows (openable, no-target, Undone)"
  - "Explorer-only demo_feedback_* and demo_undo_targets* tags"
  - "FeedbackStackDemoRegistryRenderTest (15) and UndoCenterTargetRowsDemoRegistryRenderTest (6)"
affects: [28-05, 28-07]
tech-stack:
  added: []
  patterns: ["localized(german, en, de) side-by-side copy helper in the guard-scanned ReleaseDemos.kt", "demo container tag on the outer Column so readouts and controls are scoped by ancestor"]
key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/FeedbackStackDemoRegistryRenderTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/UndoCenterTargetRowsDemoRegistryRenderTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ReleaseDemos.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/FeedbackFamilyScreen.kt
key-decisions:
  - "PD-1 to PD-5 of the plan applied as written (localized() helper, per-button fixture keys, German result words, old demo replaced, Short/Long only assert a card is pushed)"
  - "The tag for each Undo Center demo sits on an outer Column that also holds the Opened readout, instead of on the Box holding the screen, so the two readouts (English and German) are scoped by ancestor"
status: complete
plan_head_before: e1e207c26bc094500cc4ca77cd36eadb04341612
commits: 3
actuals:
  tokens: 45000
  tasks: 3
  commits: 3
---

# Phase 28 Plan 04: Explorer stacked-snackbar stage and Undo Center target rows Summary

The Explorer now shows the stacked snackbar on a 360dp fixed-height stage and the Undo Center go-to-item rows, each in English and German and each reached by a registry render test. No library source and no public API changed.

## What was built

- **FeedbackStackHost stage (Tasks 1 and 2).** `FeedbackStackStageDemos()` renders two stages (`demo_feedback_stack`, `demo_feedback_stack_german`), each with its own `FeedbackStackState`, seven buttons (Note with target, Note with Undo and target, Plain note, Burst x4, Short note, Long note with Undo, Dismiss all), a `Last result` readout and an `Opened: key` readout (neutral word `none` / `keins`). Fire, burst and Undo events are Indefinite; Short and Long use real timers. The German stage passes `Rückgängig`, `Verwerfen` and the open label `Eintrag öffnen` and renders German result words. `FeedbackStackHostVariants()` in `FeedbackFamilyScreen.kt` now delegates; the old 240dp inline demo and `burstEvents` were removed from that file.
- **Undo Center target rows (Task 3).** `UndoCenterTargetRowsDemos()` renders row A (Available with a target, opens), row B (Available, no target) and row C (Undone, keeps its target, does not open) through the second `UndoCenterScreen` overload, English and German, with an `Opened: key` readout. Undo on row A makes it lose the chevron and the click action. It is appended after `UndoGroupsDemos()` in `UndoCenterScreenVariants()`.
- **Tests.** `FeedbackStackDemoRegistryRenderTest` has the 15 named tests of the plan (neutral initial state, fire, body tap, Undo card, plain note, burst keeps exactly 3 and dismisses the oldest, body tap after the burst opens `demo-burst-4`, swipe, Undo, dismiss all, Short/Long, German labels, German key readout, independent stages, 360dp bounds). `UndoCenterTargetRowsDemoRegistryRenderTest` has the 6 named tests.

## Verification

- `FeedbackStackDemoRegistryRenderTest` 15/15, `UndoCenterTargetRowsDemoRegistryRenderTest` 6/6, `UndoGroupDemoRegistryRenderTest` 6/6 (shared page still works), all failures="0".
- Guards failures="0": CatalogDocsDriftGuardTest (10), DomainVocabularyDriftGuardTest (2), ComponentRegistryDriftGuardTest (1), ComponentStatesMatrixTest (2), ComponentPlaygroundIntegrityTest (5), LibraryInvariantsGuardTest (5). Registry counts unchanged at 67 registered, 6 unregistered, 73 total (API.md header; `ComponentRegistry.kt` untouched).
- `./gradlew detekt --no-daemon` green at zero baseline (0 code smells).
- `./gradlew apiDump --no-daemon` then `git diff --exit-code api.txt`: api.txt unchanged. `git diff --stat e1e207c HEAD -- src/main` lists only `explorer/` files; nothing under `feedback/`.
- Every Gradle run was a single `--no-daemon` invocation; no `--offline`, no `--stop`; no device touched.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Test bug] Back-card bounds in the German 360dp test**
- **Found during:** Task 2
- **Issue:** `getUnclippedBoundsInRoot()` reports a scaled back card (scaled about its top centre) as a shifted left edge with the unscaled width, so the oldest-but-one cards read 363.68dp on the right edge although they are visually inside the stage.
- **Fix:** Cards are asserted with the visual `boundsInRoot` against the 16dp stage padding (within 1px); buttons and readouts keep `getUnclippedBoundsInRoot()` within 0..360dp; card texts are checked with `didOverflowHeight`.
- **Files:** `FeedbackStackDemoRegistryRenderTest.kt`. **Commit:** a42c29c.

**2. [Process] Task 1 removed `burstEvents` and the other old buttons from `FeedbackFamilyScreen.kt`**
- The plan said the move happens in Task 2, but the Task 1 delegation left the old private function unused, so it was deleted with the old body in Task 1 (the Task 2 grep `fun burstEvents` = 0 in the family file / 1 in ReleaseDemos.kt still holds).

**3. [Process] Lane-2 override on Task 2**
- Task 2 reshaped the Explorer-only stage added in Task 1 (line replacements, not pure additions), so the pre-commit hook asked for lane 2. Committed with `HUB_LANE_OVERRIDE=2` (reason in the commit body: Explorer-only code from this plan, no library signature changed, api.txt unchanged). Task 1 used the override the plan prescribed.

**4. [Process] Task 3 order and Task 2 red run**
- No separate red run was done for Task 2 (the tests would fail only because the tags are missing; the green run also validated compilation). In Task 3 the demo implementation was written before the test file; the six tests then passed on the first run, so they were not seen red.

**5. [Layout] Undo Center demo tag placement**
- The demo tag sits on an outer `Column` holding the readout and the 460dp screen box, not on the box alone (see key-decisions), so both readouts carry the same tag and are scoped by ancestor.

**Total deviations:** 1 auto-fixed test bug, 4 process or layout notes. **Impact:** none on library code or API.

## Known gaps (unchanged by this plan)

- The hard-coded English chrome of the Undo Center (`Undo`, `Preview`, `Undone`, `Clear history`, `Undo History`) stays English in the German demo (FA-3, recorded in 27-VERIFICATION Info, for v2.8.1).
- Short and Long note timer expiry is not asserted here (PD-5); it stays with the Phase 26 deck tests and the TESTER (Gate-1 G1-7).
- No on-device behavior was checked by this plan (Gate-1 tester owns that).

## Threat Flags

None. Only Explorer fixtures; T-28-13 to T-28-15 dispositions hold (LibraryInvariantsGuardTest green on the demo copy).

## Commits

- cfa131f: English stacked-snackbar stage with tagged readouts and registry render test (Task 1)
- a42c29c: complete the stacked-snackbar stage and add the German stage (Task 2)
- c992a09: Undo Center go-to-item row demos in English and German (Task 3)

## Self-Check: PASSED

- Created files exist: both registry render tests, plus the modified `ReleaseDemos.kt` and `FeedbackFamilyScreen.kt`.
- Commits cfa131f, a42c29c and c992a09 are on branch worktree-agent-addc84c52a337a697.
