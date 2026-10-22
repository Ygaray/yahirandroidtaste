---
phase: 26-receipt-card-stacked-snackbar-deck
plan: 03
subsystem: ui
tags: [compose, feedback-stack, gestures, pointer-input, robolectric, accessibility]

requires:
  - phase: 26-receipt-card-stacked-snackbar-deck
    provides: "Plan 26-01 pause API and probe outcomes A1, A2, A2b, A5; plan 26-02 FeedbackDeck and FeedbackCard"
provides:
  - "FeedbackCard with one detectTapGestures owner, no clickable, a separate semantics open path and long-hold suppression"
  - "FeedbackDeck deck-wide non-consuming Initial-pass pointer observer (onPointerChange)"
  - "FeedbackStackHost fan state, state.setFanned mirror, idle collapse, one-card auto-collapse and DisposableEffect pause reset"
  - "FeedbackStackHostDeckTest (25 gesture tests) and FeedbackStackSourceContractTest (5 contracts)"
affects: [26-04, 26-05]

requirements-completed: [FSTK-03, FSTK-04, FSTK-06]

actuals:
  tokens: 8200
  tasks: 3
  commits: 3

plan_head_before: ca8304a45102d878081b5e3f6b847d61afbcd0f9

tech-stack:
  added: []
  patterns:
    - "MicButton split: one pointerInput tap owner plus a separate semantics onClick, callbacks through rememberUpdatedState, pointerInput keyed on the item id only"
    - "Non-consuming Initial-pass observer with a finally block so a pause reason cannot stick"
    - "Host-local fanned state mirrored into long-lived state by a LaunchedEffect, with a DisposableEffect reset"

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostDeckTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackSourceContractTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeck.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHost.kt

key-decisions:
  - "Peek decision stays on the card (probe A5 confirmed): the strip tap reaches the back card, so no deck-level arithmetic fallback was needed"
  - "The slop guard is a non-consuming tracker beside the single detectTapGestures owner, not a second tap owner and not a custom detector"
  - "Idle-collapse tests run on mainClock (probe A1 confirmed) against the production state.idleCollapseMillis of 4000 ms"

duration: 19min
completed: 2026-10-17
status: complete

coverage:
  - id: D1
    description: "A body tap on a targeted, unspent card resolves Opened first and calls onOpenTarget exactly once (double tap included); a no-target card, a spent card, a peek tap, a long-hold release and a drag release never open"
    requirement: FSTK-03
    verification:
      - kind: unit
        ref: "FeedbackStackHostDeckTest#bodyTap_onFrontCard_collapsed_opensOnce, cardWithoutTarget_tapDoesNothing, spentCard_tapDoesNothing, longHold_thenRelease_doesNotOpen_andDoesNotFan, dragPastSlop_thenRelease_doesNotOpen"
        status: pass
    human_judgment: false
  - id: D2
    description: "Tapping a peek strip fans the collapsed deck without opening; every fanned card body opens and every Undo resolves Undone; the fan re-collapses after 4000 ms idle, restarts on activity, never collapses under a held finger and folds at one card"
    requirement: FSTK-04
    verification:
      - kind: unit
        ref: "FeedbackStackHostDeckTest#peekTap_fansDeck_neverOpens_andPausesTimers, fannedDeck_everyCardBodyOpens_andUndoOnAnyCardResolvesUndone, idleCollapse_reCollapsesAfterTheIdleDelay_notBefore, pointerActivityWhileFanned_restartsTheIdleWait, noCollapseWhileAPointerIsDown, fannedDeck_collapsesWhenOnlyOneCardRemains"
        status: pass
    human_judgment: false
  - id: D3
    description: "A press anywhere in the deck (body, strip, Undo) pauses every timer through the deck-wide observer and clears on release, cancellation, card resolution mid-press and host disposal"
    requirement: FSTK-06
    verification:
      - kind: unit
        ref: "FeedbackStackHostDeckTest#pointerDown_setsPause_releaseClears, pointerDownOnUndo_setsPause, pointerDownOnPeekStrip_setsPause, cardResolvedMidPress_pauseClearsOnRelease, hostLeavesComposition_whileFanned_clearsPause, hostLeavesComposition_midPress_clearsPause"
        status: pass
    human_judgment: false
  - id: D4
    description: "TalkBack path is separate from the pointer path: Role.Button and onClick exist only when openable, always open (even on a collapsed back card) and the open label reaches the semantics verbatim"
    requirement: FSTK-03
    verification:
      - kind: unit
        ref: "FeedbackStackHostDeckTest#semanticsClick_onCollapsedBackCard_opens_andDoesNotFan, openLabel_isUsedVerbatim_withDiacriticsAndEmoji, blankMessageAndBlankOpenLabel_doNotCrash"
        status: pass
    human_judgment: false
  - id: D5
    description: "No public API change; source contracts pin one gesture owner, no clickable, a non-consuming observer, resolve-before-open and no logging"
    verification:
      - kind: unit
        ref: "FeedbackStackSourceContractTest (5 tests); apiDump plus git diff --exit-code api.txt; tools/verify-api-source-additive.sh v2.7.1"
        status: pass
    human_judgment: false
  - id: D6
    description: "Feel of the gesture on a real finger (tap versus the 14dp strip, long-hold-to-read, fan and idle re-collapse pacing) is not asserted by Robolectric"
    human_judgment: true
    rationale: "Gate-1 tester owns on-device behavior; the 14dp strip touch-target feel is the open FA-1 follow-up for the ui-designer seat"
---

# Phase 26 Plan 03: Card gestures, peek fan and deck-wide pause Summary

**The Phase 25 clickable card is replaced by the MicButton split (one `detectTapGestures` owner plus a separate semantics click), a peek-strip tap fans the deck, a long hold neither opens nor fans, a non-consuming Initial-pass observer pauses every timer while any pointer is down, and a fanned deck re-collapses after 4000 ms idle, at one card, and on host disposal.**

## Performance

- **Duration:** about 19 min (11:09Z to 11:28Z)
- **Tasks:** 3 (1 tracer, 2 auto), each committed individually
- **Files:** 5 (2 new tests, 3 modified main files)

## Accomplishments

- **Tracer (Task 1):** `FeedbackCard` gained `collapsedBack` and `onPeekTap`. The root now carries `semantics(mergeDescendants = true)` with `Role.Button` and `onClick` only when the card is openable (always opens, even on a collapsed back card), the Undo-then-Dismiss custom actions in the Phase 25 order, and exactly one `pointerInput(item.id)` running `detectTapGestures`. Callbacks, the target and `collapsedBack` are read through `rememberUpdatedState`. The host owns `fanned`, mirrors it with `LaunchedEffect(fanned) { state.setFanned(fanned) }` and resets pause in a `DisposableEffect(state)`. Resolve-before-open is untouched.
- **Pointer pause (Task 2):** `FeedbackDeck` takes `onPointerChange` and observes the first down on the Initial pass with `requireUnconsumed = false`, loops on Initial-pass events until nothing is pressed, never consumes, and reports `false` in `finally`. The host feeds it to `state.setPointerDown` and keeps `pointerDown` plus `interactionTick`. `onLongPress = { }` makes the release after the long-press timeout a no-op (D-03) while the press still pauses the deck.
- **Idle collapse (Task 3):** `LaunchedEffect(fanned, interactionTick, pointerDown)` waits `state.idleCollapseMillis` when fanned and no pointer is down, restarting on every pointer event; `LaunchedEffect(state.items.size)` folds the deck at one card or none.
- **Tests:** 25 Robolectric gesture tests and 5 source contracts, all green.

## Task Commits

1. **Task 1 (tracer): peek tap fans the deck; single-owner card gestures** - `4a865d4` (lane-2 override)
2. **Task 2: long-hold suppression and the deck-wide pointer pause observer** - `f14ecc1` (lane-2 override)
3. **Task 3: idle collapse, auto-collapse, disposal cleanup, source contract** - `13e4887` (the hook classified it lane 1; the commit body still carries the lane-2 override wording the plan prescribed)

**Plan metadata:** the docs commit that adds this SUMMARY.

## Probe branches taken (from 26-01)

- **A5 CONFIRMED:** the strip tap reaches the back card, so the peek decision stays on the card. The deck-level arithmetic fallback (`onPeekTap` on `FeedbackDeck`) was NOT used.
- **A2 CONFIRMED:** `onLongPress = { }` on `detectTapGestures` is enough; no manual press-duration measurement.
- **A2b FALSIFIED (applied here):** a drag past slop that nothing consumes still produces a tap, and plan 26-04's draggable does not exist yet. See deviation 1.
- **A1 CONFIRMED:** the idle-collapse tests drive `LaunchedEffect` delay with `mainClock` (autoAdvance off, `advanceTimeBy(..., ignoreFrameDuration = true)`), against the production `idleCollapseMillis` of 4000 ms. No shortened delay was needed.

## Deviations from Plan

1. **[Rule 2 - Missing critical functionality] Drag release must not open or fan.** Found during Task 2: the plan requires `dragPastSlop_thenRelease_doesNotOpen` (T-26-10), but probe A2b showed `detectTapGestures` alone still taps after an unconsumed move, and the swipe draggable that would consume it only arrives in plan 26-04. Fix: a small non-consuming Initial-pass tracker (`PressTravel`, one boolean) on the card records whether the finger moved past `viewConfiguration.touchSlop`, and the tap lambda ignores a tap after such travel. It is a recorder, not a second tap owner (`detectTapGestures(` still appears once). Files: `FeedbackCard.kt`. Commit `f14ecc1`. Verification: mutation check below. Plan 26-04's draggable will consume drags itself, making the tracker redundant but harmless.
2. **[Test adjustment] Newest bottom check.** The tracer test's "newest card's bottom did not move" assumed the test root bottom-anchors the deck. The deck is bottom-anchored only inside its own bounds (the host sits at the top of the test root), so the test asserts the newest card stays on the deck's own bottom edge. Production behavior is unchanged.
3. **[Test adjustment] Idle window tolerance.** The idle tests use a 500 ms tolerance around the 4000 ms delay (still fanned at 3500 ms, collapsed by 4500 ms after a restarting tap) instead of exactly 3999 and 1 ms, because the exact start instant of the delay after an injected touch is frame-rounded. The mutation check below shows the windows still catch a doubled delay.
4. **[Note] Tracer RED run skipped as a Gradle run** (same call as plans 26-01 and 26-02): the first Gradle run was the compile-and-run of test plus implementation together. The one failure it surfaced was test-side (deviation 2).
5. **[Note] Lane classification.** Task 3 was classified lane 1 by the pre-commit hook; the override wording in its commit body is therefore unnecessary but harmless.
6. **[Note] Host KDoc untouched.** The `FeedbackStackHost` KDoc still says "The swipe gesture, deck motion and pause are not part of this surface yet"; updating public docs is left to the documentation plan so this plan changes no public text.

**Total deviations:** 1 auto-fixed (Rule 2) plus 2 test adjustments and 3 notes. **Impact:** none on scope or public API.

## Mutation checks (never committed)

- Slop guard disabled (`if (true || !travel.pastSlop)`): `dragPastSlop_thenRelease_doesNotOpen` and `dragOnPeekStrip_doesNotFan` failed (2 of 18 at the time); restored.
- Idle delay doubled: `idleCollapse_reCollapsesAfterTheIdleDelay_notBefore`, `pointerActivityWhileFanned_restartsTheIdleWait`, `noCollapseWhileAPointerIsDown` and `fannedOrder_isStableAcrossFanAndCollapse` failed (4 of 25); restored.
- `clickable() {}` added to the card: `cardAddsNoClickableModifier` failed; restored with `FeedbackCard.kt` matching its committed content.

## Verification (fresh runs on this worktree, `--no-daemon`)

- `./gradlew cleanTestDebugUnitTest testDebugUnitTest --tests '*FeedbackStack*' --tests '*FeedbackDeck*'`: BUILD SUCCESSFUL (FeedbackStackHostDeckTest 25, FeedbackStackSourceContractTest 5, FeedbackStackHostTest 12, FeedbackDeckLayoutTest 11, FeedbackStackHostLongLabelTest 7, FeedbackStackPauseTest 18, RememberFeedbackStackStateTest 6, all `failures="0"`).
- Guard classes (CatalogDocsDriftGuardTest, DomainVocabularyDriftGuardTest, ComponentRegistryDriftGuardTest, ComponentStatesMatrixTest, ComponentPlaygroundIntegrityTest, LibraryInvariantsGuardTest): green, but vacuous in a worktree (they scan the main checkout by directory name). The orchestrator must re-run them from `/home/yahir/Projects/Reusable/android/yahirandroidtaste` after the merge.
- `./gradlew apiDump` then `git diff --exit-code api.txt`: exit 0 after every task. `bash tools/verify-api-source-additive.sh v2.7.1`: `API SOURCE-ADDITIVE OK ... signature_changes=0`.
- `./gradlew detekt --no-daemon`: BUILD SUCCESSFUL at zero baseline.
- `git tag --list 'v2.8*'`: empty. Nothing pushed, tagged or repinned. No debris path in any commit (5 files, all named by the plan).

## Issues Encountered

None beyond the deviations above.

## Next Phase Readiness

- Plan 26-04 can put `anchoredDraggable` ahead of the card's tap owner on the same node (probe A2c); its drag consumption will cancel the tap natively. The `PressTravel` tracker can stay or be removed there.
- Plan 26-05 Gate-1 inputs: feel of the 14dp peek strip as a touch target (FA-1, ui-designer follow-up already recorded in plan 26-02), long-hold-to-read and idle re-collapse pacing on the tester device.
- FA-3 stays flagged: Unicode normalization forms of a label are not separately tested; the deck never compares text.

## Self-Check: PASSED

- Files exist: FeedbackCard.kt, FeedbackDeck.kt, FeedbackStackHost.kt, FeedbackStackHostDeckTest.kt, FeedbackStackSourceContractTest.kt.
- Commits `4a865d4`, `f14ecc1`, `13e4887` present (3 commits since the recorded base `ca8304a`).
- Acceptance literals re-checked: `detectTapGestures`, `rememberUpdatedState`, `Role.Button`, `state.setFanned`, `state.resetPause`, `state.resolve(item, FeedbackResult.Opened)` (1), `onLongPress`, `PointerEventPass.Initial` (2), `onPointerChange` (3), `finally` (1), `state.idleCollapseMillis`, `interactionTick` (4); `@Test` counts 25 and 5; every required test name present; result XMLs `failures="0"`.

---
*Phase: 26-receipt-card-stacked-snackbar-deck*
*Completed: 2026-10-17*
