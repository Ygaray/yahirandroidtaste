---
phase: 26-receipt-card-stacked-snackbar-deck
fixed_at: 2026-10-17T00:00:00Z
review_path: /home/yahir/Projects/Reusable/android/yahirandroidtaste/.planning/phases/26-receipt-card-stacked-snackbar-deck/26-REVIEW.md
iteration: 1
findings_in_scope: 8
fixed: 8
skipped: 0
status: all_fixed
---

# Phase 26: Code Review Fix Report

**Fixed at:** 2026-10-17
**Source review:** /home/yahir/Projects/Reusable/android/yahirandroidtaste/.planning/phases/26-receipt-card-stacked-snackbar-deck/26-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 8 (3 Warning, 5 Info; `fix_scope: all`)
- Fixed: 8
- Skipped: 0

**Verification ran in the main checkout** (`workflow.use_worktrees` is true in config, but the run was
directed to work in the main checkout, so no worktree or recovery sentinel was created). Gates run once
after the last commit, all with `--no-daemon`:
- `./gradlew testDebugUnitTest --tests '*FeedbackStack*' --tests '*FeedbackDeck*' --tests '*CatalogDocsDriftGuardTest' --tests '*ComponentRegistryDriftGuardTest' --tests '*LibraryInvariantsGuardTest'`: pass.
- `./gradlew detekt`: pass, 0 code smells, no baseline change.
- `./gradlew apiDump` then `git diff --exit-code api.txt`: unchanged (no public signature changes).

All commits rewrite unreleased Phase 25/26 code and were made with `HUB_LANE_OVERRIDE=2`. Nothing was
tagged, pushed or repinned.

## Fixed Issues

### WR-01: Fanned-height cap subtracts the window-level IME inset regardless of where the deck sits

**Status:** fixed: requires human verification (layout logic)
**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeck.kt`, `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckMetrics.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckLayoutTest.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckMetricsTest.kt`
**Commit:** 4607899
**Applied fix:** Took the reviewer's "better" option. The deck samples its distance to the window bottom
(`onGloballyPositioned`, only while collapsed so the sample cannot feed back into the fanned height; a
sample taken while fanned would oscillate for a top-anchored host). Only the keyboard part beyond that
distance counts against the cap, so a host already above the keyboard (imePadding, Scaffold) is not
reduced twice. `feedbackFannedCap(available, imeOverlap, floorPx)` also never lets the keyboard term go
below a 96dp floor (`FEEDBACK_FANNED_MIN_CAP`, itself limited to the available height); the 60 percent
share remains the only term that can go lower. `FeedbackDeck` gained an internal `imeInsets` parameter
(default `WindowInsets.ime`) as a test seam. KDoc corrected (a min, not a subtraction). New tests: six
metrics cases and three Robolectric cases with a non-zero IME inset (small host above the keyboard,
host already above the keyboard, keyboard covering the host).
**Needs a look on device:** the collapsed-only sampling means a keyboard that opens while the deck is
already fanned in an imePadding host is judged against the pre-keyboard position until the next
collapse; the floor bounds the damage but the cap can be tighter than ideal in that one case.

### WR-02: `FeedbackStackHost` effects are not keyed on `state`

**Status:** fixed: requires human verification (lifecycle logic)
**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHost.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostDeckTest.kt`
**Commit:** fdd8d66
**Applied fix:** `fanned`, `pointerDown` and `interactionTick` are now `remember(state)`, and the three
effects (`setFanned`, idle collapse, single-card fold) are keyed on `state`. A swapped-in state starts
collapsed with no pointer down, so what the user sees matches the pause the state holds. New test swaps
the state while fanned and checks the old state is unpaused, the new state is not paused, the deck is
collapsed, and a peek tap on the new state fans and pauses it. Confirmed the test fails with the
pre-fix `remember { }`.

### WR-03: A card arriving while the fanned column overflows is never scrolled into view

**Status:** fixed: requires human verification (scroll/lifecycle logic)
**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeck.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckLayoutTest.kt`
**Commit:** fa81029
**Applied fix:** `ScrollToNewestWhenFanned` now takes the newest item id and runs `LaunchedEffect(fanned,
newestId)`, so an arrival while fanned re-runs the settle-then-scroll-to-max. New capped-height test
shows a fourth card while fanned and asserts it is displayed. Confirmed the test fails with the effect
keyed on `fanned` only.

### IN-01: `onEnqueued` is invoked outside the `try`

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackStateTest.kt`
**Commit:** 7507105
**Applied fix:** `onEnqueued?.invoke(item)` is the first statement inside the `try`; the comment now
names it. New test: a throwing callback leaves `items` empty and the exception reaches the caller.

### IN-02: `feedbackDepthScale` is unbounded below

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckMetrics.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckMetricsTest.kt`, `API.md`
**Commit:** 0218998
**Applied fix:** Scale floors at `FEEDBACK_DEPTH_SCALE_MIN = 0.7f` (depth 0 to 2 values unchanged:
1.0, 0.94, 0.88). Metrics test covers depth 5, 20, 1000 and a negative depth. `API.md` now says "up to
three by default (the state's `maxVisible`; ...)"; the CatalogDocsDriftGuardTest still passes.

### IN-03: A spent card keeps the "openable" primary-colored dot

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt`
**Commit:** 716ebd5
**Applied fix:** `FeedbackCardRow` takes `openable` and the dot is primary only when the card can still
open, matching the chevron. No new test: the dot color is not exposed to semantics and the project has
no pixel assertions for it; the existing spent-card semantics test still passes.

### IN-04: Stale and imprecise documentation

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/FeedbackFamilyScreen.kt`, `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHost.kt`
**Commit:** 6568801
**Applied fix:** Feedback family KDoc lists the four rows. Host KDoc describes the cap as the smaller of
60 percent and what the keyboard leaves, counting only the overlapping part, newest in view, and notes
that a swapped state starts collapsed.

### IN-05: Minor test hygiene

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostSwipeTest.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackAssumptionProbeTest.kt`
**Commit:** 481cd2f
**Applied fix:** `firstFrame_withUnmeasuredAnchors_neverThrows` wraps its body in `try/finally` with
`state.dismissAll()` and `early.cancel()`. The probe test KDoc now says a failure after a BOM bump
means Compose changed, not the library. No `@Category` was added: there is no category runner config
in the project, and the KDoc plus the `Probe` class name carry the signal.

---

_Fixed: 2026-10-17_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
