---
phase: 26-receipt-card-stacked-snackbar-deck
plan: 04
subsystem: ui
tags: [compose, feedback-stack, swipe, anchored-draggable, robolectric, source-contract]

requires:
  - phase: 26-receipt-card-stacked-snackbar-deck
    provides: "Plan 26-01 probe outcomes A2c, A3, A3b, A3c and the fling wiring; plan 26-03 card gestures, collapsedBack and the deck-wide pointer pause observer"
provides:
  - "Swipe-to-dismiss on FeedbackCard: seeded AnchoredDraggableState, stationary draggable root, translated and faded Surface, settle resolves Dismissed only"
  - "internal FEEDBACK_SWIPE_THRESHOLD (72dp) and feedbackSwipeAlpha(offsetPx, widthPx) in FeedbackDeckMetrics"
  - "FeedbackStackHostSwipeTest (20 tests) and 7 new crash-rule source contracts"
affects: [26-05]

requirements-completed: [FSTK-05]

actuals:
  tokens: 14000
  tasks: 3
  commits: 3

plan_head_before: cf9690ff2eadc87f72321b07afd0dea41f527fa3

tech-stack:
  added: []
  patterns:
    - "Seeded anchors plus positive-width updateAnchors plus NaN-guarded offset read inside graphicsLayer or derivedStateOf only"
    - "Stationary draggable root with the translation on the visible Surface inside it"
    - "Per-item AnchoredDraggableState keyed on the item id; a displaced card stays draggable until it settles"

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostSwipeTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckMetrics.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackSourceContractTest.kt

key-decisions:
  - "Probe A3 was CONFIRMED, so the card uses the seeded two-argument AnchoredDraggableState plus the NaN guard (not the guard alone)"
  - "Threshold wiring is AnchoredDraggableDefaults.flingBehavior(state, positionalThreshold = 72dp.toPx()) built in the card composition, exactly the recorded 'Fling wiring' line"
  - "The root Box uses propagateMinConstraints = true so a collapsed back card is still clamped to the front card height (the Surface used to receive the fixed constraints directly)"
  - "The PressTravel slop tracker from plan 26-03 is kept: the back-card draggable is disabled while collapsed, so a drag on a peek strip still needs it (dragOnPeekStrip_doesNotFan)"

duration: about 35min
completed: 2026-10-17
status: complete

coverage:
  - id: D1
    description: "A horizontal swipe, left or right, removes exactly the swiped card with Dismissed; the other cards stay and the target is never opened"
    requirement: FSTK-05
    verification:
      - kind: unit
        ref: "FeedbackStackHostSwipeTest#swipeLeft_onFrontCard_dismissesOnlyThatCard, swipeRight_dismissesToo, swipedCard_leavesTheTree_afterSettle"
        status: pass
    human_judgment: false
  - id: D2
    description: "A swipe never undoes: the Undo lambda is not run and the tracked store entry stays Available"
    requirement: FSTK-05
    verification:
      - kind: unit
        ref: "FeedbackStackHostSwipeTest#swipe_neverRunsUndo_andTrackedEntryStaysAvailable; FeedbackStackSourceContractTest#swipeSettleCallsOnlyTheDismissCallback"
        status: pass
    human_judgment: false
  - id: D3
    description: "72dp threshold, fast flick, sub-threshold return, vertical drag and the fanned column scroll behave as specified; collapsed deck swipes the front card only, fanned deck every card"
    requirement: FSTK-05
    verification:
      - kind: unit
        ref: "FeedbackStackHostSwipeTest#belowThreshold_returnsToSettled_andCardStays, aboveThreshold_slowDrag_dismisses, fastFlickBelowThreshold_dismisses, verticalDrag_doesNotDismiss, fannedColumnScroll_doesNotDismiss_andHorizontalSwipeDoesNotScroll, collapsedBackCardStrip_swipeDoesNothing, fannedDeck_everyCardIsSwipeable"
        status: pass
    human_judgment: false
  - id: D4
    description: "First frame cannot throw; an arrival mid-swipe, a racing timer and a second finger do not move the swipe to another card or resolve a card twice"
    requirement: FSTK-05
    verification:
      - kind: unit
        ref: "FeedbackStackHostSwipeTest#firstFrame_withUnmeasuredAnchors_neverThrows, arrivalMidSwipe_swipeStaysOnTheSameCard, swipedCard_resolvesDismissedExactlyOnce_whenTheTimerRaces, secondFingerDown_doesNotBreakTheSwipe, swipePausesTheDeck_andReleaseClears; FeedbackStackSourceContractTest (7 new contracts)"
        status: pass
    human_judgment: false
  - id: D5
    description: "No public API change"
    verification:
      - kind: other
        ref: "./gradlew apiDump && git diff --exit-code api.txt; bash tools/verify-api-source-additive.sh v2.7.1 prints API SOURCE-ADDITIVE OK signature_changes=0"
        status: pass
    human_judgment: false
  - id: D6
    description: "Feel of the swipe on a real finger (drag resistance, the 0.2 fade, fling pacing, swipe versus tap on the 14dp strip) is not asserted by Robolectric"
    human_judgment: true
    rationale: "Gate-1 tester owns on-device behavior; Robolectric injects synthetic 16 ms moves"
---

# Phase 26 Plan 04: Swipe to dismiss Summary

**Each feedback card can be swiped away in either direction: a seeded `AnchoredDraggableState` on a stationary root, a translated and faded Surface inside it, a 72dp positional threshold plus fling, and a settle that resolves `Dismissed` and nothing else (no Undo, no open).**

## Performance

- **Duration:** about 35 min
- **Tasks:** 3 (1 tracer, 2 auto), each committed individually
- **Files:** 4 (1 new test, 3 modified)

## Accomplishments

- **Tracer (Task 1):** `FeedbackCard`'s root is now a `Box` carrying `fillMaxWidth`, the item test tag, the merged semantics, the two plan 26-03 pointer inputs (slop tracker, single `detectTapGestures`), `onSizeChanged` and `anchoredDraggable`, in that order. Only the `Surface` inside it carries `graphicsLayer { translationX; alpha }`. The state is `remember(item.id) { AnchoredDraggableState(Settled, DraggableAnchors { Settled at 0f }) }`; `onSizeChanged` widens it to Start, Settled and End only when the width is positive; the offset is read only as `drag.offset.takeUnless { it.isNaN() } ?: 0f`. `LaunchedEffect(drag)` waits for `settledValue != Settled`, then calls `latestOnDismiss()` (the host's `state.resolve(item, Dismissed)`). `FEEDBACK_SWIPE_THRESHOLD = 72.dp` and `feedbackSwipeAlpha` live in `FeedbackDeckMetrics.kt`.
- **Matrix (Task 2):** 20 Robolectric tests (16 gesture and race tests, 4 plain alpha tests). A card pushed to the back of a collapsed deck while it is being dragged stays draggable through `enabled = !collapsedBack || displaced`, where `displaced` is a `derivedStateOf` on the NaN-guarded offset.
- **Contracts (Task 3):** `FeedbackStackSourceContractTest` grew from 5 to 12 tests: no `requireOffset` in any feedback file, no reveal-row reuse, anchors seeded at construction, widening behind a positive-width guard, every offset read NaN-guarded and inside a `graphicsLayer` or `derivedStateOf` block, translation placed inside the draggable root, and the settle effect calling only the dismiss callback.

## Task Commits

1. **Task 1 (tracer): swipe a card away with seeded anchors; Dismissed only** - `9acc680` (lane-2 override)
2. **Task 2: swipe matrix, displaced-card enablement and the fade helper** - `5fb5759` (lane-2 override)
3. **Task 3: guard the swipe crash rule and the dismiss-only settle** - `69848f7` (test-only, the hook classified it lane 1)

**Plan metadata:** the docs commit that adds this SUMMARY.

## Branches taken from earlier plans

- **A3 CONFIRMED:** the seeded constructor is used (anchors present at construction, NaN guard as a second line of defense). The "no anchors, guard only" fallback was not needed.
- **Fling wiring used:** `AnchoredDraggableDefaults.flingBehavior(state = drag, positionalThreshold = { with(density) { FEEDBACK_SWIPE_THRESHOLD.toPx() } })`, created in composition and passed as `flingBehavior` to `Modifier.anchoredDraggable(drag, Orientation.Horizontal, enabled, flingBehavior = fling)`. `anchoredDraggableFlingBehavior` was not used (internal to foundation).
- **A2c CONFIRMED (relied on):** with the draggable on the same node a drag past slop does not produce a tap; `dragPastSlop_thenRelease_doesNotOpen` stays green with both the tracker and the draggable.

## Deviations from Plan

1. **[Note] `feedbackSwipeAlpha` landed in Task 1, not Task 2.** The tracer's fade needs it, so the helper was added with the first commit; its four plain tests are in Task 2 as planned.
2. **[Test adjustment] Fast flick uses the probe A3c sequence, not `swipeWithVelocity`.** The plan said to use the distances proven by A3c; probe A3c itself recorded that `swipeWithVelocity` does not settle in this harness, so `fastFlickBelowThreshold_dismisses` sends 8 moves of 4dp every 16 ms (32dp in total).
3. **[Rule 1 - Bug avoided] `propagateMinConstraints = true` on the root Box.** Wrapping the Surface in a plain Box would have stopped back cards being clamped to the front card height in a collapsed deck (the Surface used to receive the fixed constraints directly). Caught in design; `FeedbackDeckLayoutTest` (11) confirms the clamp.
4. **[Note] Tracer RED run skipped as a Gradle run** (same call as plans 26-01 to 26-03): the test referenced the new members, so the red state was a compile error and the first Gradle run was the combined compile and run.
5. **[Note] PressTravel tracker kept.** The plan 26-03 summary said it could be removed once the draggable existed. It cannot: a back card in a collapsed deck has its draggable disabled, so a drag on a peek strip still relies on the tracker (`dragOnPeekStrip_doesNotFan`).
6. **[Note] Host KDoc untouched.** `FeedbackStackHost`'s public KDoc still says the swipe is not part of the surface; updating public docs is left to the documentation plan so this plan changes no public text.
7. **[Note] Duplicate edit during Task 2** (an inserted `displaced` block appeared twice after a retried tool call) was removed before any test run or commit.

**Total deviations:** 0 auto-fixed after the fact, 1 design-time Rule 1 avoidance, 2 test or placement adjustments and 4 notes. **Impact:** none on scope or public API.

## Mutation checks (never committed)

- **Settle effect calls `onUndo()` instead of `latestOnDismiss()`:** `swipe_neverRunsUndo_andTrackedEntryStaysAvailable` failed, together with 7 other swipe tests (8 of 20); restored.
- **Throwing accessor inserted (`s.requireOffset()` in a private function of `FeedbackCard.kt`):** `noFeedbackFileCallsTheThrowingOffsetAccessor` failed; restored (the file matches its committed content).
- **Extra, informational:** `enabled = !collapsedBack` without `|| displaced` made `arrivalMidSwipe_swipeStaysOnTheSameCard` fail (1 of 20); restored.

## FA-4 (flagged assumption)

The generic edge probe could not classify FSTK-05, so the manual list in the plan's must_haves is the resolution and each item has a named test: both directions, sub-threshold return, fast short flick, vertical drag, a swipe on a collapsed back strip, an arrival mid-swipe, a timer race, the first frame and a second finger. A mouse or keyboard has no swipe equivalent here; non-touch dismissal is the Dismiss accessibility action (already on every card, plan 26-05 documents and verifies it).

## Verification (fresh runs on this worktree, `--no-daemon`, build cache off for the final runs)

- `./gradlew testDebugUnitTest --tests '*FeedbackStack*' --tests '*FeedbackDeck*'`: BUILD SUCCESSFUL, all `failures="0"` (FeedbackStackHostSwipeTest 20, FeedbackStackSourceContractTest 12, FeedbackStackHostDeckTest 25, FeedbackStackHostTest 12, FeedbackDeckLayoutTest 11, FeedbackStackHostLongLabelTest 7, FeedbackStackPauseTest 18, FeedbackStackAssumptionProbeTest 11, plus the state, timeout, tracked-undo and remember suites).
- `./gradlew apiDump` then `git diff --exit-code api.txt`: exit 0 after every task. `bash tools/verify-api-source-additive.sh v2.7.1`: `API SOURCE-ADDITIVE OK ... signature_changes=0`.
- `./gradlew detekt --no-daemon`: BUILD SUCCESSFUL at zero baseline (src/test included).
- `git tag --list 'v2.8*'`: empty. Nothing pushed, tagged or repinned. Commits contain only the files named by the plan.
- Drift-guard classes (registry, catalog, domain vocabulary, library invariants) scan the main checkout by directory name and are vacuous in a worktree. The orchestrator must re-run them from `/home/yahir/Projects/Reusable/android/yahirandroidtaste` after the merge. No public composable was added, so the registry counts stay 67, 6, 73.
- Behavioral verification on a device was not attempted; it belongs to the Gate-1 tester.

## Issues Encountered

None beyond the deviations above.

## Next Phase Readiness

- Plan 26-05 can document the swipe (Dismissed only, front card while collapsed, every card while fanned, 72dp or a fling, the fade to 0.2) and should update the `FeedbackStackHost` KDoc, which still says the swipe is not part of the surface.
- Gate-1 inputs for the on-device tester: feel of the drag and fling on a real finger, swipe versus tap near the 14dp strip (FA-1), and a swipe while another card arrives.

## Self-Check: PASSED

- Files exist: FeedbackCard.kt, FeedbackDeckMetrics.kt, FeedbackStackHostSwipeTest.kt, FeedbackStackSourceContractTest.kt.
- Commits `9acc680`, `5fb5759`, `69848f7` present (3 commits since the recorded base `cf9690f`).
- Acceptance literals re-checked: `AnchoredDraggableState(` 1, `Settled at 0f` 2, `isNaN()` 1 or more, `settledValue` 1, `FEEDBACK_SWIPE_THRESHOLD` in both files, `fun feedbackSwipeAlpha` 1, `derivedStateOf` present, `@Test` counts 20 and 12, all seven named swipe tests and the four named contract tests present, result XMLs `failures="0"`.

---
*Phase: 26-receipt-card-stacked-snackbar-deck*
*Completed: 2026-10-17*
