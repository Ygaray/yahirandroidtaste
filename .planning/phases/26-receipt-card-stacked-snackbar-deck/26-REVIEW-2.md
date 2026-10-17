---
phase: 26-receipt-card-stacked-snackbar-deck
reviewed: 2026-10-17T00:00:00Z
depth: standard
scope: gap-closure 26-06 only (git diff aee7103..HEAD)
files_reviewed: 3
files_reviewed_list:
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostDeckTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostSemanticsTest.kt
findings:
  critical: 0
  warning: 2
  info: 3
  total: 5
status: resolved
---

# Phase 26: Code Review Report (gap-closure 26-06)

**Reviewed:** 2026-10-17
**Depth:** standard
**Files Reviewed:** 3
**Status:** issues_found

## Summary

The production change is small and sound: `CollapsedBackTouchBlocker` is a card-wide `Box` with an empty `pointerInput(Unit)`, placed after the `Surface` inside the card root, and it exists only while `collapsedBack` is true. I traced it against each risk area and found no defect in the source.

- **Swipe on back cards and the front card.** The root `Box` carries the draggable, the slop observer and the tap owner. The blocker is a child, so the root is an ancestor in the hit path and still receives every event. A collapsed back card has its drag disabled by `enabled = !collapsedBack || displaced`, and the front card gets no blocker.
- **Mid-gesture displacement.** A card pushed to the back mid-drag keeps the hit path it was given at the down event. The blocker therefore cannot interrupt that drag.
- **Single tap owner.** The blocker has no tap gesture and no `clickable`, and it consumes nothing. `FeedbackStackSourceContractTest` still sees exactly one `detectTapGestures(` and no `clickable(`.
- **Pause observer and long-hold rule.** The Initial-pass observers are on ancestors, so they still see every touch.
- **TalkBack and semantics.** The blocker adds no semantics node and no click handler. TalkBack's Undo and Dismiss run as semantic actions on the merged node and the Undo child, not as touch, so they do not depend on the blocker.
- **Fanned deck and re-collapse.** `collapsedBack` is read at composition time and the host recomposes on fan, so the blocker leaves the tree when the deck fans and returns when it collapses.
- **Layout.** `matchParentSize` does not feed the root's size, and the Undo's visibility is unchanged, so the row, clamp and fan spring are unaffected.
- **Detekt.** `UnusedPrivateMember` is satisfied because the composable is used. No new rule hit is evident in the diff. The detekt config is syntax-only with a handful of rules, and the summary reports detekt executed green.

The concerns are in the tests. Two of the new tests are labelled as regression coverage for the blocker but pass without it, and the riskiest blocker interaction has no test.

## Warnings

### WR-01: The touch-slop-band test is vacuous in Robolectric, so it guards nothing

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostDeckTest.kt:287-303` (helper at `:145-197`)

**Issue:**
- The 26-06 SUMMARY mutation check records that `peekTapOnTheUndosTouchSlopBand_fansTheDeck_andNeverUndoes` passes with the blocker call commented out.
- The helper's own comment explains why. In Robolectric the Undo node's bounds already start about 1.9dp below the card top, because the node includes the minimum interactive size. The band point is then clamped to `stripTop + 0.5dp`. That point is outside the button's node bounds, and Robolectric does not widen the hit region beyond them, so the click never reaches the button.
- The test name and the in-test comment claim "only the touch-slop widening of the button reaches here". That is false in this environment. The test adds no protection and gives false confidence that the widening band is covered. The plan's own T-26-21 mitigation cites this band as covered.
- The device re-run is the only real proof, as the SUMMARY admits. The test should not read as protection.

**Fix:** Either delete the test, or rename it and fix its comment so it does not claim coverage. Optionally add a layout-level assertion that the Undo's unclipped bounds already overlap the strip. That pins the root-cause geometry and does fail if the geometry moves:

```kotlin
// peekTapOnTheUndosTouchSlopBand_... -> peekTapJustAboveTheUndoBounds_fansTheDeck (control only)
// Robolectric does not widen the touch region (FA-12): this is a control, the tester owns the real band.
```

### WR-02: The drag test passes without the blocker, and the real swipe-vs-blocker interaction is untested

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostDeckTest.kt:392-414`

**Issue:**
- `dragStartingOverTheBackCardsUndo_neitherFansNorUndoes` passes with the blocker removed (SUMMARY mutation (a)). Drag is disabled on a collapsed back card, and `pastSlop` suppresses the tap, so the assertions hold regardless of the blocker.
- The plan lists this as the guard for "the blocker does not break swipe". As written, it cannot fail if the blocker does break something. For example, it would still pass if the blocker consumed events and starved the root's draggable.
- The riskiest interaction has no test: a card that becomes a back card mid-drag (`displaced == true`, `collapsedBack == true`). In that state the blocker appears while the root's `anchoredDraggable` is still live. The code is probably fine, because the hit path is fixed at down. It is untested, and the covering `if (collapsedBack)` condition changes mid-gesture.
- There is also no test of a fresh down on a displaced, settling back card.

**Fix:**
- Rename the drag test to say it is a control and not a blocker guard, so it is not mistaken for coverage.
- Add one test. Start a swipe on the front card of a two-card deck. Mid-drag, emit a newer card so the dragged card becomes `collapsedBack` and `displaced`. Continue the drag past the threshold and assert `FeedbackResult.Dismissed`.

## Info

### IN-01: Keyboard and D-pad activation of the collapsed back Undo still bypasses the "pointer path is the peek tap and nothing else" rule

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt:250-252`, KDoc at `:77-80`

**Issue:** The blocker blocks pointer events only. The Undo stays focusable and enabled, so a hardware-keyboard or switch user who focuses it and presses Enter still runs Undo on a card visually buried in the deck. This is consistent with the "semantics path untouched" decision and A11Y-01. The KDoc says "on that card the pointer path is the peek tap and nothing else", so it should say explicitly that key-event activation is outside the blocker. The SELF-UAT already lists keyboard opening as a forwarded note, so this is not a regression.

**Fix:** Add one sentence to the KDoc, "key and switch activation of the Undo is intentionally not blocked", so a later reader does not assume full input isolation.

### IN-02: The `undoOf` helper is duplicated verbatim across two test files

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostDeckTest.kt:129`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostSemanticsTest.kt:384`

**Issue:** Both files define the identical private `undoOf(message)` matcher, which is an unmerged-tree node tagged `FEEDBACK_STACK_UNDO_TEST_TAG` with an ancestor `feedback_stack_item` holding the message. A tag rename would need two edits.

**Fix:** Move it to a small internal test-support function in the feedback test package, or accept the duplication, since the other helpers (`setHost`, `withUndo`, `notify`) are already per-file.

### IN-03: Edge claims in the plan have no test or evidence

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostDeckTest.kt`

**Issue:** The plan states "Edge: the block covers the whole card, so it does not depend on RTL, font scale 2.0, a long German label, a spent card, or no Undo". No test exercises RTL, a spent back card, or a back card without an Undo. This is acceptable because the card-wide blocker is structurally independent of those cases. Still, a body tap on a plain back card is covered only by the older `peekTap` test. That older test taps the horizontal centre, and the blocker now sits above the body too.

**Fix:** Optional. Add one assertion that a peek tap on a back card with no Undo still fans after the blocker, to pin the "harmless blocker" claim.

---

_Reviewed: 2026-10-17_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
