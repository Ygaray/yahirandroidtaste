---
phase: 26-receipt-card-stacked-snackbar-deck
reviewed: 2026-10-17T00:00:00Z
depth: standard
files_reviewed: 17
files_reviewed_list:
  - API.md
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/FeedbackFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeck.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckMetrics.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHost.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckLayoutTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckMetricsTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackAssumptionProbeTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostDeckTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostLongLabelTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostSemanticsTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostSwipeTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackPauseTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackSourceContractTest.kt
findings:
  critical: 0
  warning: 3
  info: 5
  total: 8
status: resolved
---

# Phase 26: Code Review Report

**Reviewed:** 2026-10-17
**Depth:** standard
**Files Reviewed:** 17 (API.md plus 6 main sources, 10 test files; `UndoTracking.kt` was read as the caller of `showInternal`)
**Status:** issues_found

## Summary

The deck, swipe, pause and semantics work is carefully built. Resolution is a single claim under a lock, so the tap, timer, swipe and eviction races end in exactly one winner. The `AnchoredDraggableState` is seeded at construction and every offset read goes through the NaN guard. Pointer observation uses the Initial pass and never consumes. The pause state survives host disposal. The one-way dependency and ComponentRegistry invariants hold: every new composable (`FeedbackDeck`, `FeedbackCard`, `FeedbackCardRow`) is `internal` or `private`, and the public surface (`FeedbackStackHost`, `rememberFeedbackStackState`, `FeedbackStackState`) keeps its signatures.

Static review found no BLOCKER defects. The three warnings are layout and lifecycle gaps that the tests do not cover:
- the keyboard cap is applied regardless of where the host sits;
- the host's pause and fan effects are not keyed on `state`;
- an arrival while the fanned column overflows is never scrolled into view.

The info items are small hygiene and documentation drift. Gradle was not run, as instructed.

## Warnings

### WR-01: Fanned-height cap subtracts the window-level IME inset regardless of where the deck sits

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeck.kt:139-152` (formula in `FeedbackDeckMetrics.kt:79-83`)
**Issue:** `fanHeightCap` computes `min(0.6 * maxHeight, maxHeight - ime.getBottom())`. `WindowInsets.ime` is the raw window-level inset. It is not reduced when a parent applies `imePadding()` or consumes the inset, and it knows nothing about the host's position.
- If the host's constraint already excludes the keyboard (the Scaffold or `imePadding` case), the keyboard is subtracted twice.
- If the host sits in a small region (a sheet, a fixed-height box such as the Explorer's 240dp demo, or the top of the screen), `maxHeight - imeBottom` can reach 0.
- At 0 the fanned deck is laid out 0px tall while the cards are still in state and `fanned`/pause is still true. The user sees nothing, and the timers stay paused until the idle collapse.
- No test covers the IME path. `fannedDeck_isCappedAtSixtyPercent_andScrollsInside` runs with zero IME.

The KDoc also says "60 percent of the available height minus the keyboard inset", but the implementation is a `min`, not a subtraction.

**Fix:** Compute the overlap with the IME using the deck's own position, or drop the IME term and let the host's parent (Scaffold / `imePadding`) own it. A bounded floor also avoids the zero-height state.
```kotlin
// FeedbackDeckMetrics.kt
internal fun feedbackFannedCap(available: Int, imeBottom: Int): Int {
    if (available == Constraints.Infinity) return Int.MAX_VALUE
    val share = (available * FEEDBACK_FANNED_MAX_HEIGHT_FRACTION).toInt()
    // Never collapse the column to nothing: keep at least one card-height of room.
    val keyboardRoom = (available - imeBottom).coerceAtLeast(FEEDBACK_FANNED_MIN_CAP_PX)
    return minOf(share, keyboardRoom).coerceAtLeast(0)
}
```
Better, subtract only the part of the IME that actually overlaps the host: `imeBottom - distanceFromHostBottomToWindowBottom`, taken from `onGloballyPositioned`. Add a Robolectric test with a non-zero IME inset and a small host.

### WR-02: `FeedbackStackHost` effects are not keyed on `state`; swapping the state leaves a visually fanned deck unpaused

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHost.kt:79-97`
**Issue:** `fanned`, `pointerDown` and `interactionTick` are `remember { }` without `state` as a key. `LaunchedEffect(fanned)` and `LaunchedEffect(state.items.size)` also omit it. If the caller passes a different `FeedbackStackState`, which happens when `rememberFeedbackStackState(maxVisible)` changes (the KDoc admits this creates a new state), the sequence is:
1. `DisposableEffect(state)` resets the old state's pause.
2. `fanned` stays `true`, so the deck keeps rendering fanned.
3. `LaunchedEffect(fanned)` does not re-run, so the new state never receives `setFanned(true)`.
4. The new state's cards keep counting down while the user sees a fanned (supposedly paused) deck.
A stale `pointerDown` can leak the same way.

**Fix:** Key the per-state bookkeeping on the state, or reset local flags when it changes.
```kotlin
var fanned by remember(state) { mutableStateOf(false) }
var pointerDown by remember(state) { mutableStateOf(false) }
var interactionTick by remember(state) { mutableIntStateOf(0) }
LaunchedEffect(state, fanned) { state.setFanned(fanned) }
LaunchedEffect(state, state.items.size) { if (state.items.size <= 1) fanned = false }
```

### WR-03: A card arriving while the fanned column overflows is never scrolled into view

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeck.kt:121-133`
**Issue:** `ScrollToNewestWhenFanned` runs only when `fanned` flips (`LaunchedEffect(fanned)`). With the deck fanned and capped at 60 percent, a new arrival is appended at the bottom, where the newest card sits. The scroll offset is left where it was, so the new card is below the fold and invisible. The arrival is also announced by the Polite live region, so sighted and screen-reader users disagree. `arrivalWhileFanned_appendsNewestAtBottom` runs uncapped, so it cannot catch this.

**Fix:** Re-run the scroll when the newest item changes while fanned. Pass `items.firstOrNull()?.id` (or `items.size`) as a key and scroll to `scroll.maxValue` after the next layout.
```kotlin
LaunchedEffect(fanned, items.firstOrNull()?.id) {
    if (fanned) { /* same settle + scrollTo(maxValue) */ }
}
```
Add a capped-height test that shows a fourth card while fanned and asserts it is displayed.

## Info

### IN-01: `onEnqueued` is invoked outside the `try`, contradicting the comment that guards leaks

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt:171-176`
**Issue:** The in-code comment says everything after `enqueue` lives inside the `try`, so a throwing step still frees the slot. `onEnqueued?.invoke(item)` runs before the `try`. If a future caller's callback throws, the card stays in `items` forever with no timer and no awaiting caller. The only current caller (`UndoTracking.kt:94`) passes a trivial setter, so this is latent.
**Fix:** Move `onEnqueued?.invoke(item)` to the first line inside the `try`.

### IN-02: `feedbackDepthScale` is unbounded below; a large `maxVisible` produces zero or negative scale

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckMetrics.kt:28-29`
**Issue:** `1f - 0.06f * depth` reaches 0 at depth 17 and goes negative beyond that. `maxVisible` is a public, unbounded constructor and `rememberFeedbackStackState` parameter. A negative `scaleY` about a top-center origin mirrors the card upward out of the deck. Alpha is already clamped, scale is not. Also, `API.md` and the KDoc say "up to three" without noting that the count is configurable.
**Fix:** Clamp the scale, e.g. `(1f - FEEDBACK_DEPTH_SCALE_STEP * depth.coerceAtLeast(0)).coerceAtLeast(0.7f)`, and add a metrics test for depth 20.

### IN-03: A spent card keeps the "openable" primary-colored dot

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt:273-277`
**Issue:** `dotColor` uses `item.target != null`, while openability uses `openTarget` (target and not spent). A spent card hides the chevron and Undo and is inert, but its dot is still the primary tone that signals a live target. That is a small visual inconsistency.
**Fix:** Pass `openable` into `FeedbackCardRow` and use `if (openable) primary else onSurfaceVariant`, if the design intends the dot to mirror interactivity.

### IN-04: Stale and imprecise documentation

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/FeedbackFamilyScreen.kt:172-177`; `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHost.kt:31-32`
**Issue:**
- The `FeedbackFamilyScreen` KDoc still says "ConfirmationDialog and UndoCenterScreen (2 rows)". `feedbackFamilyEntries` now holds four entries, including `AttentionCue` and `FeedbackStackHost`.
- The `FeedbackStackHost` KDoc says the cap is "60 percent of the available height minus the keyboard inset", but the implementation is `min(60%, available - ime)` (see WR-01).
**Fix:** Update both comments.

### IN-05: Minor test hygiene

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostSwipeTest.kt:445`; `FeedbackStackAssumptionProbeTest.kt`
**Issue:**
- `firstFrame_withUnmeasuredAnchors_neverThrows` creates `CoroutineScope(Dispatchers.Unconfined)` and never cancels it. If an assertion fails before `dismissAll()`, the pending `show` coroutine leaks into later tests.
- `FeedbackStackAssumptionProbeTest` tests Compose behavior, not library code. It is labeled a BOM-bump canary, so it is intentional, but `probeA2b` and `probeA6` pin framework quirks (a tap still fires after a drag past the slop; semantics children come back in zIndex order). They will fail on a legitimate Compose change and be read as library regressions. Consider a `@Category` or a naming convention that makes the canary status obvious in CI output.
**Fix:** Wrap the scope in `try/finally { early.cancel() }`.

---

_Reviewed: 2026-10-17_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
