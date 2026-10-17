---
phase: 26-receipt-card-stacked-snackbar-deck
plan: 06
subsystem: ui
tags: [compose, snackbar-deck, hit-testing, pointer-input, gap-closure, robolectric]

requires:
  - phase: 26-receipt-card-stacked-snackbar-deck
    provides: FeedbackCard, FeedbackStackHost, collapsed deck and peek-tap fan (plans 26-01 to 26-05)
provides:
  - A private CollapsedBackTouchBlocker above the content of a collapsed back card, so a peek tap over its Undo fans the deck
  - Regression tests that tap the Undo's own bounds inside the strip (2- and 3-card decks) plus over-blocking controls
  - Semantics guard tests: the collapsed back card's Undo stays enabled, clickable and a custom action
affects: [phase-26 Gate-1 re-run (cycle 2 of 3), v2.8.0 tag (Yahir), phase-28 sweep]

actuals:
  tokens: 4600
  tasks: 2
  commits: 3

tech-stack:
  added: []
  patterns:
    - "Semantics-free, non-consuming sibling layer (Modifier.pointerInput(Unit) { }) above content to take the pointer hit while ancestors still see the events"

key-files:
  created: []
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostDeckTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostSemanticsTest.kt

key-decisions:
  - "A collapsed back card keeps rendering its Undo and only loses touch on it (a card-wide transparent blocker above the content), instead of hiding, disabling or re-routing the Undo"
  - "The blocker covers the whole back card, not the Undo's bounds, so RTL, font scale and touch-slop widening do not matter"

patterns-established:
  - "Regression tests for a hit-test defect tap the offending node's own bounds (root-level click) and assert the geometry overlap first, so they cannot pass vacuously"

requirements-completed: [FSTK-04]

coverage:
  - id: D1
    description: "A peek tap that lands on a collapsed back card's Undo fans the deck and never runs Undo (two-card deck)"
    requirement: FSTK-04
    verification:
      - kind: unit
        ref: "FeedbackStackHostDeckTest#peekTapOverTheBackCardsUndo_fansTheDeck_andNeverUndoes"
        status: pass
    human_judgment: false
  - id: D2
    description: "The same holds for the middle and the deepest card of a three-card deck and for the strip band above the Undo's bounds"
    requirement: FSTK-04
    verification:
      - kind: unit
        ref: "FeedbackStackHostDeckTest#threeCardDeck_peekTapOverTheMiddleCardsUndo_fansTheDeck_andNeverUndoes"
        status: pass
      - kind: unit
        ref: "FeedbackStackHostDeckTest#threeCardDeck_peekTapOverTheDeepestCardsUndo_fansTheDeck_andNeverUndoes"
        status: pass
      - kind: unit
        ref: "FeedbackStackHostDeckTest#peekTapOnTheUndosTouchSlopBand_fansTheDeck_andNeverUndoes"
        status: pass
    human_judgment: false
  - id: D3
    description: "The block is bound to the collapsed state: after the fan the same Undo works, and a collapsed front card's Undo works; a drag over the Undo neither fans nor undoes"
    requirement: FSTK-04
    verification:
      - kind: unit
        ref: "FeedbackStackHostDeckTest#afterTheFan_theBackCardsUndo_respondsToTouchAgain"
        status: pass
      - kind: unit
        ref: "FeedbackStackHostDeckTest#collapsedFrontCardsUndo_stillRespondsToTouch"
        status: pass
      - kind: unit
        ref: "FeedbackStackHostDeckTest#dragStartingOverTheBackCardsUndo_neitherFansNorUndoes"
        status: pass
    human_judgment: false
  - id: D4
    description: "A11Y-01 untouched: the collapsed back card's Undo is enabled, clickable, a separate child and a custom action; semantic click and custom action resolve it Undone without fanning"
    requirement: FSTK-04
    verification:
      - kind: unit
        ref: "FeedbackStackHostSemanticsTest#collapsedBackCard_undoChildStaysEnabledAndClickable"
        status: pass
      - kind: unit
        ref: "FeedbackStackHostSemanticsTest#collapsedBackCard_undoSemanticClick_resolvesUndone_withoutFanning"
        status: pass
      - kind: unit
        ref: "FeedbackStackHostSemanticsTest#collapsedBackCard_undoCustomAction_resolvesUndone"
        status: pass
    human_judgment: false
  - id: D5
    description: "On the tester, a real tap on the visually blank peek strip at the x position of a back card's Undo fans the deck and runs no Undo"
    requirement: FSTK-04
    verification: []
    human_judgment: true
    rationale: "Real-device hit-testing (touch-slop widening, Samsung input) is the Gate-1 tester's lane; Robolectric did not reproduce the widening (FA-12). The orchestrator re-runs SC3 on the tester."

plan_head_before: aee7103e40e12e33a7441e95989569cee452cfac

duration: 35min
completed: 2026-10-17
status: complete
---

# Phase 26 Plan 06: Peek tap over a back card's Undo Summary

**A semantics-free card-wide touch blocker above a collapsed back card makes a peek tap over its Undo fan the deck instead of running Undo, pinned by tests that tap the Undo's own bounds (red before the fix).**

## Performance

- **Duration:** about 35 min
- **Completed:** 2026-10-17
- **Tasks:** 2
- **Files modified:** 3 (one source, two test files); `api.txt` byte-identical

## Accomplishments

- Closed the one Gate-1 failure of Phase 26 (`26-05-SELF-UAT.md` criterion 3, FSTK-04): the root cause was a hit-testable Undo whose touch region reaches into the 14dp peek strip, consuming the tap before the card's single tap owner (`detectTapGestures`, the only caller of `onPeekTap`) could fan the deck.
- Fix is one private composable (`CollapsedBackTouchBlocker`: a `Box` with `Modifier.pointerInput(Unit) { }`, no semantics, no consuming) and one conditional call (`if (collapsedBack)`) after the `Surface` in the card root. Compose gives the hit to the topmost sibling with pointer input and every ancestor still receives the events, so the root's tap owner sees an ordinary unconsumed tap.
- Ten new tests: seven in `FeedbackStackHostDeckTest` (the tracer plus six; the file now has 33) and three in `FeedbackStackHostSemanticsTest` (now 19).

## Task Commits

1. **Task 1 (tracer): peek tap over a back card's Undo fans a two-card deck** - `030634b` (fix): red-first test, then the blocker. Lane 1 (additive); no `HUB_LANE_OVERRIDE` needed.
2. **Task 2: expand the matrix, guard the semantics, mutation-check, full gate** - `e216c65` (test).

**Plan metadata:** the SUMMARY commit that follows these (docs).

## Red-first evidence

Run on the unfixed code before touching `FeedbackCard.kt`:

`./gradlew testDebugUnitTest --tests '*FeedbackStackHostDeckTest.peekTapOverTheBackCardsUndo_fansTheDeck_andNeverUndoes' --no-daemon`

Result: FAILED at `FeedbackStackHostDeckTest.kt:269`, `java.lang.AssertionError: the peek tap over the Undo must fan the deck`. The geometry precondition (Undo bounds overlap the strip by more than 2dp) held, so the click really landed on the Undo and the deck stayed collapsed, the device's symptom. After the fix the same command passed (BUILD SUCCESSFUL). The `aboveUndoTopDp = 2f` retry from the plan was not needed.

## Mutation check

Never committed; both restored with `git checkout -- src/main/.../FeedbackCard.kt`, then `git diff --quiet HEAD -- src/main` was clean.

(a) Commented out the `CollapsedBackTouchBlocker(...)` call, ran `FeedbackStackHostDeckTest` + `FeedbackStackHostSemanticsTest` (52 tests): **4 failed**, 48 passed.
- Failed (as required): `peekTapOverTheBackCardsUndo_fansTheDeck_andNeverUndoes`, `threeCardDeck_peekTapOverTheMiddleCardsUndo_...`, `threeCardDeck_peekTapOverTheDeepestCardsUndo_...`, `afterTheFan_theBackCardsUndo_respondsToTouchAgain`.
- Passed (as required): the three semantics tests, `collapsedFrontCardsUndo_stillRespondsToTouch`, `dragStartingOverTheBackCardsUndo_neitherFansNorUndoes`.
- **`peekTapOnTheUndosTouchSlopBand_...` also passed**, which the plan allowed for: Robolectric does not widen the touch region beyond the node bounds the way the device does (FA-12). The test stays as a belt-and-braces check; the device re-run is the real proof for the widening.

(b) Made the Undo's visibility depend on the collapsed flag (`showUndo = item.hasUndo && !spent && !collapsedBack`): **8 failed**, 44 passed.
- Failed: every Deck test that locates the collapsed back card's Undo (tracer, slop band, middle, deepest, after-the-fan, drag) plus `collapsedBackCard_undoChildStaysEnabledAndClickable` and `collapsedBackCard_undoSemanticClick_resolvesUndone_withoutFanning`.
- `collapsedBackCard_undoCustomAction_resolvesUndone` passed under this mutation, because the custom action lives on the merged card node and does not depend on the visible button. That is expected (the plan said "three semantics tests must fail"; two of three do, the third guards the custom action path, which this mutation does not remove).

## Runtime decision

Per D-06 [peek-target] amended ("separate pointer and semantics paths"): on a collapsed back card the pointer path is the peek tap and nothing else; the semantics path (open, Undo, Dismiss, the separate Undo child) is untouched. The Undo stays composed and rendered (D-01, D-02 unchanged) and loses touch only. Layout, body width, the clamp to the front card's height and the fan spring are unchanged because the Undo's visibility never depends on `collapsedBack`.

Rejected alternatives (do not retry):
1. Not rendering the Undo on a collapsed back card: removes the separate focusable Undo child Gate-1 SC5 verified and re-lays the row out while the fan spring runs.
2. `enabled = false` on the Undo: Compose reports the node disabled (TalkBack announces it dimmed) and colours change; A11Y-01 needs identical semantics.
3. Routing the Undo button's click to `onPeekTap`: that lambda also serves TalkBack's double tap, so a screen-reader user activating the Undo would fan instead of undoing.
4. A second tap owner or an Initial-pass tap handler in the card: breaks the one-owner rule pinned by `FeedbackStackSourceContractTest` (T-26-10).

## Gate-1 inputs

For the orchestrator's cycle 2 of 3 (not run here, no device work): rebuild the throwaway harness against a fresh AAR (`clean publishReleasePublicationToMavenLocal`), TESTER `R5CT10XNKQN` over USB only. Tap the visually blank strip at the x position of the back card's Undo sliver in a collapsed two-card deck and over each back card of a three-card deck: the deck must fan, `UNDORUNS` must not change, the tracked entry must stay Available. Then re-check that Undo on a fanned card still runs. Restore density with `wm density 450` (never `wm density reset`). Specifically look at the touch-slop band just above the Undo's bounds, which Robolectric cannot exercise (FA-12).

## Files Created/Modified

- `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt` - private `CollapsedBackTouchBlocker` and its conditional call in the card root `Box`; `FeedbackCard` KDoc extended; signature unchanged.
- `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostDeckTest.kt` - helpers `undoOf`, `showTracked`, `undoPointInTheStrip`, `tapOverTheUndoInTheStrip`; tracer plus six tests (33 `@Test` in the file).
- `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostSemanticsTest.kt` - `undoOf`, `collapsedDeck` helpers and three tests (19 `@Test` in the file).

## Decisions Made

- Card-wide blocker rather than one scoped to the Undo's bounds: independent of RTL, font scale, a long German label, whether the card has an Undo at all, and the touch-slop widening.
- No change to `FeedbackStackHost.kt`, `FeedbackDeck.kt`, `FeedbackStackState.kt`, `api.txt` or `API.md`.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug in my own test helper] Slop-band point fell outside the strip**
- **Found during:** Task 2 (first run of the new matrix)
- **Issue:** `undoPointInTheStrip("Back", aboveUndoTopDp = 2f)` computed `undo.top - 2dp`, but the Undo node's bounds already start about 1.9dp below the card top in Robolectric (the node includes its minimum interactive size), so the point was at -0.12dp, outside the strip; the helper's own inside-the-strip assertion caught it.
- **Fix:** In band mode the point is `max(undo.top - aboveUndoTopDp, stripTop + 0.5dp)` and the helper additionally asserts the point lies above the Undo's bounds; KDoc updated.
- **Files modified:** `FeedbackStackHostDeckTest.kt`
- **Verification:** the Deck and Semantics suites pass; the band test passes under mutation (a) too (FA-12, recorded above).
- **Committed in:** `e216c65`

---

**Total deviations:** 1 auto-fixed (test helper geometry). **Impact on plan:** none on the fix; the slop-band test is weaker than the plan hoped because Robolectric does not widen the hit region, so the device re-run owns that band.

## Issues Encountered

- Worktree specifics: the plan assumes the main checkout. Here the drift-guard tests that scan the real tree (`CatalogDocsDriftGuardTest` and siblings) ran in the worktree copy, which is a full clone of the tracked tree, so they passed in the full gate; the orchestrator should re-run them after merge as instructed. The per-plan commit ledger file in the shared `.git/worktrees/` directory could not be written (the harness blocks writes outside the worktree), so `plan_head_before` is the base SHA given in the task prompt and `commits` was counted from it by hand (2 task commits plus this SUMMARY commit).

## Verification Results

- Fresh gate: `./gradlew testDebugUnitTest detekt apiCheck --rerun-tasks --no-daemon` BUILD SUCCESSFUL (3m48s); 1609 tests, 22 skipped (pre-existing `@Ignore`s), 0 failures, 0 errors; detekt and `metalavaCheckCompatibilityRelease` executed.
- `./gradlew apiDump --no-daemon` then `git diff --exit-code api.txt`: unchanged.
- `bash tools/verify-api-source-additive.sh v2.7.1`: `API SOURCE-ADDITIVE OK` (signature_changes=0, removed_members=0).
- `git tag --list 'v2.8*'`: empty. Nothing tagged, pushed or repinned.
- `grep -L 'failures="0" errors="0"'` over the feedback result files prints nothing.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Ready for the Gate-1 re-run of SC3 on the tester (cycle 2 of 3). If it passes, Phase 26 is unblocked for Gate-2; tagging v2.8.0 and the consumer bump stay Yahir's.

## Self-Check: PASSED

- `030634b` and `e216c65` found in `git log`; modified files exist; `api.txt` unchanged; no debris path in any plan commit (only the three `files_modified` paths plus this SUMMARY).
