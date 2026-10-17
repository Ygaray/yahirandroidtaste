---
phase: 27-open-from-the-sheet-undo-center-pill-fixes
fixed_at: 2026-10-17T00:00:00Z
review_path: .planning/phases/27-open-from-the-sheet-undo-center-pill-fixes/27-REVIEW.md
iteration: 1
findings_in_scope: 6
fixed: 6
skipped: 0
status: all_fixed
---

# Phase 27: Code Review Fix Report

**Fixed at:** 2026-10-17
**Source review:** .planning/phases/27-open-from-the-sheet-undo-center-pill-fixes/27-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 6 (fix_scope: all)
- Fixed: 6
- Skipped: 0

**Verification ran in the main checkout** (no worktree; the brief said to work on main). After each fix the
affected test classes were re-run; at the end the drift guards (`*DriftGuardTest`, `*ComponentRegistry*`),
the UndoCenterScreen / OpenTarget / HandledBy / `V271CallSiteCanaryTest` classes, `detekt` (zero baseline,
0 smells) and `apiCheck` all passed. No `./gradlew --stop`, no device, no tag, no push.

**Lane-2 note:** the repo pre-commit hook classes any deleted source line as a "lane 2" change and blocks
it on the fast path. Four commits touched only code added in this phase (`OpenTargetRow.kt`,
`HandledByDot.kt`, `HandledByPillShape.kt` are new files; `UndoHistoryOpenRow` is new in 27-03; no v2.7.1 line
was removed, checked against the `v2.7.1` tag). Those were committed with `HUB_LANE_OVERRIDE=2`, which
27-CONTEXT declares for this phase. Reviewer may want to eyeball that.

## Fixed Issues

### WR-01: Openable Undo Center row has no press feedback and is not focusable

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenOpenTargetTest.kt`
**Commit:** ce9eee3 (shared with WR-02, one change)
**Status:** fixed: requires human verification (interaction behavior; ripple and keyboard activation are not observable in Robolectric, check on the Gate-1 device)
**Applied fix:** Replaced the hand-rolled `semantics { onClick }` + `pointerInput { detectTapGestures }` with a single
`combinedClickable` (the review's alternative) driven by a remembered `MutableInteractionSource`. The row now gets
the press indication, a focus target and Enter/D-pad activation for free. The merged `Role.Button` node, the click
label (`openLabel`, else the message), the test tag and `rememberUpdatedState` stale-callback guard are kept. Tap
opens, a hold never opens or undoes, the nested Undo button still consumes its own pointer-down. This still
honors D-04 / PD-11 / FA-5 (merged Role.Button node, a hold shows the peek and never opens); only the
"no `clickable`" implementation note from 27-03 is superseded, because the peek now comes from `onLongClick`
(PD-11) so a clickable no longer kills hold-to-peek.

### WR-02: Peek on an openable row is unreachable without a touch long-press

**Files modified:** same as WR-01
**Commit:** ce9eee3
**Status:** fixed: requires human verification
**Applied fix:** `combinedClickable(onLongClick = ..., onLongClickLabel = "Preview")` exposes the `OnLongClick`
semantics action, so TalkBack and switch access can raise the peek. The peek is hidden on press Release/Cancel
(collected from the interaction source), and a long-click raised with no press in flight (an accessibility action)
hides itself after 3 s (`PEEK_ACTION_DISMISS_MS`) so it cannot stay up. New tests:
`openableRow_isFocusable_andExposesALongClickAction`, `longClickAction_showsThePeek_andHidesItAgainWithoutARelease`.
All 21 `UndoCenterScreenOpenTargetTest` tests pass, including the existing hold, tap, drag and Undo-tap cases.

### IN-01: Locked (in-flight) go-to-item rows are visually dimmed but not announced as disabled

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OpenTargetRow.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/OpenTargetRowTest.kt`
**Commit:** 64fbbfc
**Applied fix:** The non-enabled branch of the shared `openTargetClick` modifier now adds `semantics { disabled() }`
next to the 38% alpha, so it covers both the single row and locked batch rows. PD-1 still holds (no click action,
no role); `lockedRow_hasNoClickAction_noRole_butKeepsItsText` now also asserts `SemanticsProperties.Disabled`.

### IN-02: Handled-by pill cap is approximate when the theme's `bodySmall` line height is not in sp

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/HandledByPillShape.kt`
**Commit:** 88af094
**Applied fix:** Took the review's "document the limitation" option: a "Known limit (IN-02)" paragraph on
`handledByPillCapPx` and a pointer from the shape's KDoc claim. Measuring the real height would need a
`TextMeasurer` in the shape, which was out of proportion for a custom-typography-only edge that never throws.

### IN-03: Dot contrast ignores alpha of a caller-supplied fill

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/HandledByDot.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/HandledByDotContrastTest.kt`
**Commit:** fd98540
**Applied fix:** `handledByDotRing` now measures `fill.compositeOver(pill)`, so a translucent warn color is judged on
what is painted. Opaque fills are unaffected (the existing ramp, boundary and random-sample tests pass). KDoc and
the test header no longer say "documented, not tested"; new test
`translucentFill_isMeasuredAsPaintedOverThePill`.

### IN-04: `SuccessBodyOpenTarget` duplicates `SuccessBody`

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetOpenTargetTest.kt`
**Commit:** d5e8d1c
**Applied fix:** Took the review's interim option (the fold-in is blocked while the v2.5.1 byte-equality pin
stands): added `classicOpenTargetBody_withoutATarget_rendersLikeThePinnedClassicBody`, which renders
`OutcomeSheetContent` and `OutcomeSheetContentOpenTarget` for a target-less Success with handled-by, batch rows,
editor slot and undo, and asserts identical semantics trees. A slot added to only one body now fails the test.
No production code changed.

---

_Fixed: 2026-10-17_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
