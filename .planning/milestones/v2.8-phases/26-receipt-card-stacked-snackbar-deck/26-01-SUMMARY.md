---
phase: 26-receipt-card-stacked-snackbar-deck
plan: 01
subsystem: ui
tags: [compose, coroutines, kotlin-time, feedback-stack, virtual-clock, robolectric]

requires:
  - phase: 25-feedback-target-model-stack-state
    provides: FeedbackStackState with lock-guarded resolve, enqueue eviction and the accessibility timeout scaler
provides:
  - "internal FeedbackPauseState(pointer, fanned) in a StateFlow, read by every card timer"
  - "pause-aware runTimer with an 800 ms remainder floor, on an injectable TimeSource"
  - "internal seams: pauseState, timeSource, idleCollapseMillis, setPointerDown, setFanned, resetPause"
  - "FeedbackStackPauseTest (18 virtual-clock tests) and FeedbackStackAssumptionProbeTest (11 Compose probes with recorded outcomes)"
affects: [26-02, 26-03, 26-04, 26-05]

actuals:
  tokens: 8800
  tasks: 3
  commits: 3

plan_head_before: 6b4a6a7f7f4d4145c8e70d185417deb20384ff92

tech-stack:
  added: []
  patterns:
    - "Pause as a deck-wide StateFlow of reasons; each timer waits for not-paused, then races its remainder against the next pause"
    - "Clock seam: internal var timeSource defaulting to TimeSource.Monotonic, tests inject testScheduler.timeSource"

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackPauseTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackAssumptionProbeTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt

key-decisions:
  - "Pause scope is the whole deck: one FeedbackPauseState in one StateFlow, no per-card pause"
  - "Remainder on interruption is max(800 ms, remaining - elapsed), applied per card at the moment the pause starts"
  - "All new members are internal or private, so api.txt is byte-identical"

patterns-established:
  - "Probe tests as a canary: assumptions about Compose are pinned as passing tests with a Recorded outcomes KDoc that later plans read"

requirements-completed: [FSTK-06]

coverage:
  - id: D1
    description: "Card timers pause while a pointer is down or the deck is fanned and resume the remaining time, with the 800 ms floor, on an injectable clock"
    requirement: FSTK-06
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackPauseTest.kt (18 tests)"
        status: pass
    human_judgment: false
  - id: D2
    description: "Timeout values unchanged (Short 4 s, Long 10 s, Indefinite never) through the existing scaler; Phase 25 suites green and unedited"
    requirement: FSTK-06
    verification:
      - kind: unit
        ref: "FeedbackStackStateTimeoutTest, FeedbackStackStateTest, FeedbackStackTrackedUndoTest, RememberFeedbackStackStateTest, FeedbackStackHostTest"
        status: pass
    human_judgment: false
  - id: D3
    description: "Five Wave 0 Compose assumptions pinned with recorded outcomes and fallbacks, including the exact 72dp fling wiring"
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackAssumptionProbeTest.kt (11 tests)"
        status: pass
    human_judgment: false
  - id: D4
    description: "No public API change"
    verification:
      - kind: other
        ref: "./gradlew apiDump && git diff --exit-code api.txt; bash tools/verify-api-source-additive.sh v2.7.1 prints API SOURCE-ADDITIVE OK"
        status: pass
    human_judgment: false

duration: 19min
completed: 2026-10-17
status: complete
---

# Phase 26 Plan 01: Pause-aware card timer and Wave 0 probes Summary

**Deck-wide pause (pointer or fanned) for the feedback stack card timers, with an 800 ms remainder floor on an injectable TimeSource, proven on virtual time, plus 11 Compose probes whose recorded outcomes steer plans 26-02 to 26-05.**

## Performance

- **Duration:** 19 min
- **Started:** 2026-10-17T10:28:46Z
- **Completed:** 2026-10-17T10:47:19Z
- **Tasks:** 3
- **Files modified:** 3 (1 main, 2 new tests)

## Accomplishments

- `FeedbackStackState.runTimer` replaces the plain `delay` timer: it waits until no pause reason holds, marks the clock, races the remainder against the next pause, and on interruption sets `remaining = max(800 ms, remaining - elapsed)`. A card the pause never touches times out at exactly its scaled timeout.
- New internal API for the host and the deck: `pauseState` (read-only StateFlow), `timeSource`, `idleCollapseMillis` (4000 ms), `setPointerDown`, `setFanned`, `resetPause`. `scaledMillis` is still read once at enqueue; Indefinite still skips the timer; `resolve`, `enqueue`, `dismissAll` and the accessibility binding are untouched.
- 18 pause tests on the virtual clock cover the floor, per-card floor, untouched card, second pause, fanned reason, both reasons, enqueue-while-paused, empty deck, expiry-instant tie, equal remainders, resolve-while-paused, eviction-while-paused, `resetPause`, concurrent flips, scaler composition and Indefinite across pause cycles.
- 11 probe tests pin the Compose behavior the gesture plans depend on (below).

## Task Commits

1. **Task 1: Pause-aware card timer on an injectable clock** - `2ceefed` (feat, lane-2 override, tracer)
2. **Task 2: Wave 0 probes A1-A6** - `a09a640` (test)
3. **Task 3: Pause contract matrix** - `3d4e750` (test)

**Plan metadata:** the docs commit that adds this SUMMARY.

## Files Created/Modified

- `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt` - `FeedbackPauseState`, pause/clock/idle seams, `runTimer`, KDoc timer bullet
- `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackPauseTest.kt` - virtual-clock pause contract
- `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackAssumptionProbeTest.kt` - Compose probes with the Recorded outcomes KDoc

## Probe results

Copied from the probe test KDoc (the committed source of truth). Environment: Compose BOM 2026.04.01, Robolectric sdk 35, density 1.0, touch slop 16 px.

- **A1 CONFIRMED with a caveat.** `LaunchedEffect` delay and `rememberCoroutineScope()` delay are driven by `mainClock`. `advanceTimeBy(n)` rounds up to whole frames (`advanceTimeBy(3999)` already fires a 4000 ms delay); use `ignoreFrameDuration = true` for exact boundaries. Plan 26-03 can test idle collapse on `mainClock`; no fallback needed.
- **A2 CONFIRMED.** `detectTapGestures(onLongPress, onTap)`: hold past the long-press timeout gives one long press and no tap; short press gives one tap. The probe uses `mainClock.advanceTimeBy` between `down` and `up`.
- **A2b FALSIFIED.** Moving past the slop inside the node does not cancel the tap when nothing consumes the move. Fallback: the draggable's consumption cancels the tap (A2c); plans 26-03 and 26-04 must not rely on slop alone.
- **A2c CONFIRMED (new probe).** With `anchoredDraggable` ahead of `detectTapGestures` on one node, a drag past slop consumes the move and neither `onTap` nor `onLongPress` fires.
- **A3 CONFIRMED.** `AnchoredDraggableState(initial, DraggableAnchors { Settled at 0f })` has a non-NaN offset of 0 before composition; `updateAnchors` with three anchors keeps offset 0 and `currentValue` Settled.
- **A3b CONFIRMED with a caveat.** The 72dp positional threshold works on top of the touch slop (slop 16 px: a 90 px drag settles End, 80 and 60 settle back). It applies only when release velocity is nonzero and below the velocity threshold; exactly zero velocity picks the closest anchor (50 percent), so tests must keep moving (16 ms events).
- **A3c CONFIRMED.** A fast flick (8 moves of 4dp at 16 ms, 32dp total) settles End far below 72dp. `swipeWithVelocity` over 30dp did not settle (harness limitation): do not use it for flicks.
- **Fling wiring:** `val fling = AnchoredDraggableDefaults.flingBehavior(state = state, positionalThreshold = { 72.dp.toPx() })` (composable; `toPx` via `LocalDensity.current`), then `Modifier.anchoredDraggable(state, Orientation.Horizontal, flingBehavior = fling)`. `anchoredDraggableFlingBehavior(...)` is internal to foundation and not callable.
- **A5 CONFIRMED.** Hit-testing follows `placeWithLayer`: a tap at y 7dp reaches only the back card (layer scale 0.94 about top center); y 30dp reaches only the front card (zIndex 1).
- **A5b CONFIRMED.** A point in the unscaled footprint but outside the scaled back card (x 2dp, y 7dp) reaches neither card.
- **A6 FALSIFIED.** `onAllNodesWithTag("card")` lists cards in zIndex order (lowest first), not composition order: the first-composed card with the higher zIndex is listed last. Fallback for plan 26-02: give each card `traversalIndex = index.toFloat()` from its first commit and edit the Phase 25 test `newestCardComesFirst` to compare the TraversalIndex property instead of list position (A11Y-01 forces that edit; say so in that commit).

## Decisions Made

None beyond the plan; the implementation follows decision D-04 [pause-scope] and brief D-05 as written.

## Deviations from Plan

### Notes (no rule-based fixes were needed)

- **Probe set adjusted to observed behavior.** The plan's A2b (drag past slop gives zero taps) and A6 (list order) were falsified and pinned to the observed behavior with the named fallback, exactly as the plan's protocol prescribes. One probe was added (A2c) because A2b's falsification made the real tap-cancel mechanism worth pinning. The A3b drag amounts became 60 (back), 80 (back, borderline) and 90 (End) instead of 60/80 because the 16 px Robolectric slop eats part of the drag; A3c uses a manual 16 ms move sequence instead of `swipeWithVelocity`.
- **Tracer RED step skipped as a Gradle run.** The tracer test references members that did not exist yet, so the red state was a compile error; I went straight to the implementation instead of spending a Gradle run on it.
- **Task 3 needed no production change.** No test exposed a defect in `runTimer`, so `FeedbackStackState.kt` is untouched in that commit.
- **Commit-message lane wording.** The Task 1 commit was amended once to say "lane 2" in lowercase to match the acceptance criterion; the final hash is `2ceefed`.

**Total deviations:** 0 auto-fixed (Rules 1-3). **Impact:** none on scope.

## Verification (fresh runs on this worktree)

- `./gradlew testDebugUnitTest` for FeedbackStackPauseTest (18), FeedbackStackStateTimeoutTest (21), FeedbackStackStateTest (24), FeedbackStackTrackedUndoTest (18), RememberFeedbackStackStateTest (6), FeedbackStackHostTest (12) and FeedbackStackAssumptionProbeTest (11): all failures="0".
- `./gradlew apiDump` then `git diff --exit-code api.txt`: exit 0. `bash tools/verify-api-source-additive.sh v2.7.1`: `API SOURCE-ADDITIVE OK ... signature_changes=0`.
- `./gradlew detekt --no-daemon`: BUILD SUCCESSFUL at zero baseline.
- `git tag --list 'v2.8*'` prints nothing; nothing was pushed or repinned.
- **Mutation check (never committed):** `setPointerDown` made a no-op; 8 of 18 pause tests failed (including the tracer, the floor, the scaler and the concurrency test). The original `paused.update { it.copy(pointer = down) }` body was restored with `git checkout -- FeedbackStackState.kt` and is what the commits contain.

## Issues Encountered

- `AnchoredDraggableDefaults.flingBehavior` appeared to ignore the 72dp threshold. Root cause: Compose's `computeTarget` picks the closest anchor when the release velocity is exactly zero, and the first probe versions released with zero velocity. Fixed by releasing with a small nonzero velocity, which is also how a real finger behaves; recorded under A3b.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Plans 26-02 to 26-05 can use `setPointerDown`, `setFanned`, `resetPause`, `pauseState`, `timeSource` and `idleCollapseMillis`, and read the Recorded outcomes list in the probe test KDoc.
- Plan 26-02 must apply the A6 fallback (traversalIndex from the first commit, edit `newestCardComesFirst` with a stated reason). Plan 26-04 uses the recorded fling wiring verbatim.
- Threat T-26-01 still needs plan 26-03's observer `finally`, host `DisposableEffect` and auto-collapse; `resetPause()` and the resolve/eviction cleanup are in place and tested here.

## Self-Check: PASSED

- Files exist: FeedbackStackState.kt, FeedbackStackPauseTest.kt, FeedbackStackAssumptionProbeTest.kt.
- Commits `2ceefed`, `a09a640`, `3d4e750` present on `worktree-agent-add48815afb7ad3a6` (3 commits since the recorded base).

---
*Phase: 26-receipt-card-stacked-snackbar-deck*
*Completed: 2026-10-17*
