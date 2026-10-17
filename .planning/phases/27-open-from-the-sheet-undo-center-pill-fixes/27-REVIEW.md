---
phase: 27-open-from-the-sheet-undo-center-pill-fixes
reviewed: 2026-10-17T00:00:00Z
depth: standard
files_reviewed: 22
files_reviewed_list:
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/HandledByDot.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/HandledByPillShape.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/OpenTargetRow.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceipt.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/BatchRowResultUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt
  - api.txt
  - src/test/java/io/github/ygaray/yahirandroidtaste/V271CallSiteCanaryTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/HandledByDotContrastTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/HandledByPillShapeTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OpenTargetHygieneSourceContractTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OpenTargetRowTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetBatchOpenTargetTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetFixtures.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetOpenTargetLongLabelTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetOpenTargetTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenOpenParityTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenOpenTargetLongLabelTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenOpenTargetTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenSourceContractTest.kt
findings:
  critical: 0
  warning: 2
  info: 4
  total: 6
status: resolved
---

# Phase 27: Code Review Report

**Reviewed:** 2026-10-17
**Depth:** standard
**Files Reviewed:** 22
**Status:** issues_found

## Summary

Reviewed the go-to-item row (Classic and Receipt), the per-row batch open list, the second
`UndoCenterScreen` overload, the handled-by pill shape and the dot contrast ring, plus the
`api.txt` delta and tests. I found no blockers. The library invariants hold: no consumer imports,
the opaque `FeedbackTarget.key` is never read in main code, new public parameters are appended with
defaults, and the new overloads cannot be called ambiguously from existing call sites. The
`UndoCenterScreen` overload delegates to the v2.7.1 overload (required `onOpenTarget` means no
recursion). Nested `Role.Button` semantics keep the Undo button as its own node, because a
merging child is not folded into its merging parent. The tests confirm this.

The real defects are interaction and accessibility gaps in the Undo Center open row, which
hand-rolls what `clickable` would give for free. The rest are smaller robustness items.

## Warnings

### WR-01: Openable Undo Center row has no press feedback and is not focusable

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt:471-491`
**Issue:** `UndoHistoryOpenRow` deliberately avoids `clickable` (to keep hold-to-peek working) and
instead combines `semantics { onClick }` with `pointerInput { detectTapGestures }`. That gives
TalkBack a click action, but a sighted user gets no ripple or pressed indication when tapping a row
that navigates away. A keyboard, D-pad or switch-access user also has no focus target, so the row
cannot be reached or activated without a screen reader. The sibling `OpenTargetRow` and
`OpenTargetBatchRow` use `clickable` and have both. The same list now contains rows that do and do
not respond visibly. The project treats interaction states as first-class requirements.
**Fix:** Keep one gesture owner, but add focus and indication. For example, use
`Modifier.indication(interactionSource, ripple())` driven from `onPress` by emitting
`PressInteraction.Press` and `Release` on a remembered `MutableInteractionSource`, and add
`Modifier.focusable(interactionSource = interactionSource)`. Alternatively use
`combinedClickable(onClick = ..., onLongClick = { showPeek = true })`, which provides indication,
focus and a long-click semantics action. Then clear `showPeek` on release through a separate
`pointerInput` that does not consume.

### WR-02: Peek on an openable row is unreachable without a touch long-press

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt:474-489`
**Issue:** On the v2.7.1 row the peek shows on any press and works for any pointer. On the openable
row it shows only from `onLongPress` inside `detectTapGestures`, and the semantics node exposes only
`onClick`, not `onLongClick`. TalkBack and keyboard users therefore lose the preview on exactly the
rows that now carry a target, so behavior diverges between rows in one list. PD-11 documents the
switch to long-press, but not the accessibility regression.
**Fix:** Add `onLongClick(label = ...) { showPeek = true; true }` to the row semantics, and clear it
on a next action or a timeout. Better, switch to `combinedClickable` as in WR-01, which exposes the
long-click action for free.

## Info

### IN-01: Locked (in-flight) go-to-item rows are visually dimmed but not announced as disabled

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OpenTargetRow.kt:144-158`
**Issue:** When `enabled` is false the modifier only applies `alpha(0.38f)`. No `disabled()`
semantics are set and the role is dropped, so TalkBack reads the title as plain text and a user
cannot tell the row is temporarily unavailable. PD-1 asks for no click action and no role, which
this meets, but it leaves the state conveyed by color alone.
**Fix:** Add `.semantics { disabled() }` in the non-enabled branch, after confirming
`OpenTargetRowTest.lockedRow_hasNoClickAction_noRole_butKeepsItsText` does not forbid it.

### IN-02: Handled-by pill cap is approximate when the theme's `bodySmall` line height is not in sp

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/HandledByPillShape.kt:34-37`
**Issue:** When `lineHeight` is Unspecified or in em, the cap is computed from a fixed 16sp, but the
pill's real one-line height comes from the font metrics. If those are larger, a one-line pill has
`height / 2 > cap`, so its radius is clamped below a full capsule. This contradicts the KDoc
promise that a one-line pill equals `CircleShape` at every font scale. The default theme's
`bodySmall` is in sp, so only a custom typography is affected, and nothing throws.
**Fix:** Document the limitation in the KDoc, or measure the real one-line height (for example with
a `TextMeasurer`) instead of falling back to a constant.

### IN-03: Dot contrast ignores alpha of a caller-supplied fill

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/HandledByDot.kt:20-28`
**Issue:** `wcagContrast` uses `luminance()`, which ignores alpha. `handledByDotColor` returns
`colors.warnTone.content`, which is caller-overridable. A translucent warn color that is visually
lighter than its opaque value can pass the 3:1 check, so no ring is drawn while the rendered dot
fails. The comment records this as FA-7, and the test file says "documented, not tested".
**Fix:** Composite the fill over the pill before measuring: `fill.compositeOver(pill)`, then
compare. That is one line and removes the caveat.

### IN-04: `SuccessBodyOpenTarget` duplicates `SuccessBody`

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt:578-628`
**Issue:** The new Classic body copies the structure of `SuccessBody` (headline, handled-by,
batch, editor, undo) so the pinned v2.5.1 functions stay byte-equal. A future change to
`SuccessBody`, such as a new slot, must be made in both places, and nothing enforces it. The
Classic content shell (`OutcomeSheetContentOpenTarget`) is likewise a copy of the scroll shell in
`OutcomeSheetContent`.
**Fix:** Once the byte-equality pin is no longer needed, fold `SuccessBodyOpenTarget` into
`SuccessBody` behind a nullable `onOpenTarget` parameter. Until then, add a parity test asserting
both bodies render identical semantics when no target is set.

---

_Reviewed: 2026-10-17_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
