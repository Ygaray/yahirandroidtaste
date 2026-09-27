---
phase: 08-micbutton-hardening
fixed_at: 2026-09-27T16:00:00Z
review_path: .planning/phases/08-micbutton-hardening/08-REVIEW.md
iteration: 1
findings_in_scope: 7
fixed: 7
skipped: 0
status: resolved
---

# Phase 08: Code Review Fix Report

**Fixed at:** 2026-09-27T16:00:00Z
**Source review:** .planning/phases/08-micbutton-hardening/08-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 7
- Fixed: 7
- Skipped: 0

**Verification:** Ran inside an isolated worktree (`.claude/worktrees/rf-08-*`, branch
`gsd-reviewfix/08-*`) checked out from `main`. Full gate
`./gradlew testDebugUnitTest detekt apiCheck publishReleasePublicationToMavenLocal` was green after
all fixes: `BUILD SUCCESSFUL` (67 tasks, 25 executed). `MicButtonGestureTest` (5 test methods after
the IN-04 merge) all pass. `apiCheck` passed with **zero diff** to `api.txt` — the accessibility
additions are modifier-only and do not change `MicButton`'s public signature. Commits were made on
`gsd-reviewfix/08-*` inside the worktree, then fast-forwarded onto `main` and the worktree torn down
as part of this agent's cleanup; this is reproducible by re-running the same gate command from a
clean `main` checkout.

## Fixed Issues

### CR-01: MicButton exposes no accessibility semantics — TalkBack/keyboard users cannot activate it

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/MicButton.kt`
**Commits:** `22b0a59`, `2111de7`
**Applied fix:** Added a semantics-only click action to the `Surface`'s modifier chain —
`.semantics(mergeDescendants = true) { role = Role.Button; onClick { ... } }` plus
`.focusable(interactionSource = interactionSource)` — placed above the existing single-owner
`pointerInput(Unit)` gesture. Deliberately did **not** add `clickable`/`combinedClickable`, which
would have reintroduced the second touch-gesture owner this phase's own hardening pass removed; the
semantics `onClick` action dispatches via `AccessibilityService.performAction(ACTION_CLICK)`, a
separate code path from the `pointerInput` touch stream. A second commit (`2111de7`) fixed a
missed import (`androidx.compose.ui.semantics.role` — an extension property distinct from the
`Role` enum class) that the first commit's `./gradlew compileDebugKotlin` verification caught
before it could ship broken.

### WR-01: No regression test for an `enabled` flip mid-press

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/MicButtonGestureTest.kt`
**Commit:** `34bad8e`
**Applied fix:** Added `tap_midPressEnabledFlip_firesOnlyOnDisabledTap`: presses while
`enabled = true`, flips `enabled` to `false` mid-press via a `mutableStateOf`, releases, and asserts
`onDisabledTap` fires exactly once while `onTap` never fires. Uses `composeRule.onNode(hasClickAction())`
as the node matcher instead of content-description, since flipping `enabled` also swaps the
rendered icon's `contentDescription` via `Crossfade` — a description-based matcher would fail to
re-find the node after the flip. The CR-01 semantics click action made this matcher available.

### WR-02: No regression test for `onDisabledTap` identity swap mid-press

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/MicButtonGestureTest.kt`
**Commit:** `969db54`
**Applied fix:** Added `tap_midPressDisabledTapCallbackIdentitySwap_firesOnlyLatestOnDisabledTap`,
mirroring the existing `onTap` mid-press swap test: presses on a disabled mic, swaps the
`onDisabledTap` closure identity mid-press, releases, and asserts the release dispatches through the
latest closure, never the stale one.

### IN-01: Duplicated three-way `when` for `containerColor`/`contentColor`

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/MicButton.kt`
**Commit:** `3ed4c81`
**Applied fix:** Collapsed the two independent `when` blocks into one `when` returning
`Pair<Color, Color>`, destructured into `containerColor`/`contentColor`. Behavior-equivalent — the
same three states resolve to the same color pairs. This repo's `tools/hooks/pre-commit` classified
this as a "lane 2" (non-additive/behavior-changing source edit) since it modifies pre-existing
lines rather than only appending; declared via the hook's own documented
`HUB_LANE_OVERRIDE=2` escape after verifying `./gradlew compileDebugKotlin` and the full
`MicButtonGestureTest` suite both passed unchanged.

### IN-02: KDoc describes the emit pair as "try/finally-shaped" but no `try`/`finally` exists

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/MicButton.kt`
**Commit:** `f729683`
**Applied fix:** Reworded the comment to state the real invariant (the `pointerInput(Unit)`
coroutine's lifetime is tied to the node's lifetime, so the only cancellation path also tears down
`interactionSource`) instead of implying an exception-safety guarantee that isn't actually coded.
Comment-only change. Also declared lane 2 via `HUB_LANE_OVERRIDE=2` (modifies a pre-existing
comment block).

### IN-03: Test helper name `notSetUp` reintroduces domain framing

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/MicButtonGestureTest.kt`
**Commit:** `1441d06`
**Applied fix:** Renamed `notSetUp` to `disabledDescription` (matching the composable's own
`disabledDescription` parameter) across its declaration and all three usages. Verified via
`./gradlew testDebugUnitTest --tests MicButtonGestureTest`.

### IN-04: `pressAndHold_onDisabledMic_neverFiresOnTap` largely duplicates the prior test

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/MicButtonGestureTest.kt`
**Commit:** `12c7bef`
**Applied fix:** Merged the hold-specific assertion (proving a hold doesn't fire early) into
`tap_onDisabledMic_invokesOnDisabledTap_andNeverFiresOnTap` as an extra assertion after a standalone
`down()` call, then removed the now-redundant `pressAndHold_onDisabledMic_neverFiresOnTap` test.
`MicButtonGestureTest` now has 5 test methods (down from 6), all passing.

## Skipped Issues

None — all seven findings were fixed.

---

_Fixed: 2026-09-27T16:00:00Z_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
