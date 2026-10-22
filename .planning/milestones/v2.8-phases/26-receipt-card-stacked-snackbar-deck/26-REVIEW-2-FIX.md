---
phase: 26-receipt-card-stacked-snackbar-deck
fixed_at: 2026-10-17T00:00:00Z
review_path: .planning/phases/26-receipt-card-stacked-snackbar-deck/26-REVIEW-2.md
iteration: 1
findings_in_scope: 5
fixed: 5
skipped: 0
status: all_fixed
---

# Phase 26: Code Review Fix Report (gap-closure 26-06)

**Fixed at:** 2026-10-17
**Source review:** .planning/phases/26-receipt-card-stacked-snackbar-deck/26-REVIEW-2.md
**Iteration:** 1

**Summary:**
- Findings in scope: 5
- Fixed: 5
- Skipped: 0

Fixes were applied directly in the main checkout (no worktree, per the run's project notes). Each
commit used `HUB_LANE_OVERRIDE=2` with the body line "rewrites unreleased Phase 25/26 code; no
v2.7.1 signature changes" and listed only its own paths.

## Fixed Issues

### WR-01: The touch-slop-band test is vacuous in Robolectric

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostDeckTest.kt`
**Commit:** d638d53
**Applied fix:** Making it bite is not achievable: Robolectric hit-tests against node bounds only, and the
Undo's bounds already start about 2dp below the card top, so a point above them never reaches the button.
Renamed to `peekTapJustAboveTheUndoBounds_fansTheDeck_control`, added a KDoc saying it is a control and that
the real slop band (T-26-21) is proven only by the device re-run, fixed the misleading in-test comment, and
corrected the `undoPointInTheStrip` KDoc. Status: fixed (requires human device re-run for the real band).

### WR-02: The drag test passes without the blocker, and the swipe-vs-blocker interaction is untested

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostDeckTest.kt`
**Commit:** ace65c9
**Applied fix:**
- Renamed `dragStartingOverTheBackCardsUndo_neitherFansNorUndoes` to `..._control` with a KDoc stating it is not a blocker guard.
- Added `cardPushedToTheBackMidDrag_stillDismisses_andNeitherFansNorUndoes` (the reviewer's case: a front card is dragged,
  a newer arrival makes it `collapsedBack` + `displaced`, the same finger continues past the threshold, result is
  `Dismissed`, no Undo run, no fan). Mutation check: it still passes if the blocker consumes events, because the hit
  path is fixed at the down event; its KDoc says so.
- Added `freshDragOnADisplacedSettlingBackCard_dismissesThroughTheBlocker` (the reviewer's "fresh down on a displaced,
  settling back card"; the clock is frozen so the card stays displaced, then a new drag starts over the Undo's strip).
  Mutation check: with the blocker changed to consume every pointer event, this test fails (along with 15 others in the
  class); with the real blocker it passes. This is the test that bites.

### IN-01: Keyboard and D-pad activation of the collapsed back Undo is outside the blocker

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt`
**Commit:** 1e3a48a
**Applied fix:** Added a KDoc sentence to `FeedbackCard` that the blocker stops pointer input only and key and
switch activation of the Undo is intentionally not blocked. Comment only; no API change.

### IN-02: The `undoOf` helper is duplicated across two test files

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackTestSupport.kt` (new),
`src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostDeckTest.kt`,
`src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostSemanticsTest.kt`
**Commit:** e515e65
**Applied fix:** Moved the matcher into an internal `SemanticsNodeInteractionsProvider.undoOf(message)` extension in the
new `FeedbackStackTestSupport.kt` (the one new file, as the reviewer suggested), removed both private copies and the
now-unused `hasAnyAncestor` imports.

### IN-03: Edge claims in the plan have no test

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostDeckTest.kt`
**Commit:** 9a4b4ba
**Applied fix:** Added `peekTapOnABackCardWithoutAnUndo_offCentre_stillFansAndNeverOpens` and
`peekTapOnASpentBackCard_stillFansAndNeverOpens`, both tapping the strip off the horizontal centre. RTL and font scale 2.0
are not exercised (the blocker is card-wide and structurally independent of them, as the reviewer noted).

## Verification

Ran in the main checkout, one Gradle invocation at a time with `--no-daemon`:
- `./gradlew testDebugUnitTest --tests '*FeedbackStack*' --tests '*FeedbackDeck*' --tests '*CatalogDocsDriftGuardTest' --tests '*ComponentRegistryDriftGuardTest' --tests '*LibraryInvariantsGuardTest' --no-daemon`: 258 tests, 0 failures.
- `./gradlew detekt --no-daemon`: exit 0, zero-baseline unchanged.
- `./gradlew apiDump --no-daemon` then `git diff --exit-code api.txt`: unchanged.
- The mutation check on the blocker was reverted (`git diff` on `FeedbackCard.kt` clean) before the final run.

Not tagged, pushed or repinned; the tester device was not touched.

---

_Fixed: 2026-10-17_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
