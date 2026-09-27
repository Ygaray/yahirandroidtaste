---
phase: 08-micbutton-hardening
reviewed: 2026-09-27T00:00:00Z
depth: standard
files_reviewed: 3
files_reviewed_list:
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/MicButton.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/MicButtonGestureTest.kt
  - api.txt
findings:
  critical: 1
  warning: 2
  info: 4
  total: 7
status: issues_found
---

# Phase 08: Code Review Report

**Reviewed:** 2026-09-27T00:00:00Z
**Depth:** standard
**Files Reviewed:** 3
**Status:** issues_found

## Summary

Reviewed `MicButton.kt`'s gesture/dispatch logic and the mid-press hardening it just gained
(`rememberUpdatedState` for `enabled`/`onTap`/`onDisabledTap`), the new
`MicButtonGestureTest.kt` regression suite, and `api.txt`'s regenerated signature for the
additive parameterization.

The `rememberUpdatedState` wiring itself is correctly reasoned and correctly implemented: the
`pointerInput(Unit)` coroutine is genuinely never restarted by an `enabled`/callback-identity
change mid-press, and the release branch reads the latest values, so the callback-identity-swap
bug this phase targets is fixed and is proven by a real Compose-injected gesture test. `api.txt`
matches the source signature exactly — no API drift.

However: **this raw `Surface` + `pointerInput`/`detectTapGestures` gesture reproduction carries
no accessibility semantics at all** (no `clickable`, no `onClick` semantics action, no
`focusable()`, no `Role.Button`). That was true before this phase and remains true after it; a
"hardening" pass over exactly this composable's press/release path is the natural place to have
caught it, but it wasn't touched. This is treated as this review's one blocking finding because
it makes the component's core interaction (activating a mic) unreachable for a TalkBack or
keyboard/D-pad user — not a corner case, but a total path failure for that whole user segment,
in a component destined to ship into multiple consumer apps via this shared library.

The new test file is solid but has two coverage gaps relative to what the phase's own KDoc
claims: it proves the `onTap` identity swap survives mid-press, but never exercises the
`enabled` flip mid-press (the literal reason `latestEnabled` exists) nor the `onDisabledTap`
identity swap (claimed to have "the same rationale" as `onTap`'s, in the code comment, but
never asserted).

## Critical Issues

### CR-01: MicButton exposes no accessibility semantics — TalkBack/keyboard users cannot activate it

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/MicButton.kt:99-129`
**Issue:** The visible control is a plain `Surface` with a raw
`Modifier.pointerInput(Unit) { detectTapGestures(onPress = …) }`. Unlike
`Modifier.clickable`/`Modifier.combinedClickable` (or the `Surface(onClick = …)` overload),
`detectTapGestures` registers **no** Compose semantics: no `Role.Button`, no exposed
`SemanticsActions.OnClick`, and the `Surface` itself adds no `focusable()`. The inner `Icon`'s
`contentDescription` gives TalkBack something to *read*, but there is no accessibility action a
screen reader's "double-tap to activate" (or a keyboard/D-pad "Enter") can invoke — TalkBack
reports the node as not clickable and the gesture never reaches `detectTapGestures`, which only
listens for real touch events. This is a complete, unconditional loss of the mic control for
assistive-technology and keyboard users; there is no fallback path.

This is exactly the kind of regression the FAB-replacement comment in the KDoc (lines 44-54)
warns about reintroducing for *touch* gesture ownership — but the fix that avoided a second
*touch* gesture owner also silently dropped the *accessibility* action path that
`FloatingActionButton`'s internal `clickable` used to provide for free. Accessibility actions are
dispatched by the `AccessibilityService` calling `performAction(ACTION_CLICK)` on the semantics
node — a completely separate code path from the raw pointer-event stream that caused the
original double-owner bug — so restoring it does not reintroduce that bug.
**Fix:** Add a semantics-only click action alongside the existing single-owner `pointerInput`
(do not add `clickable`/`combinedClickable`, which *would* reintroduce a second touch-gesture
owner):
```kotlin
modifier = modifier
    .size(56.dp)
    .semantics(mergeDescendants = true) {
        role = Role.Button
        onClick {
            if (latestEnabled) latestOnTap() else latestOnDisabledTap()
            true
        }
    }
    .focusable(interactionSource = interactionSource)
    .indication(interactionSource, ripple())
    .pointerInput(Unit) { /* unchanged */ }
```
(`androidx.compose.ui.semantics.semantics`/`Role`/`onClick` and
`androidx.compose.foundation.focusable` imports.) This restores TalkBack activation and
keyboard/D-pad focus + activation without touching the existing single-gesture-owner touch path.

## Warnings

### WR-01: No regression test for the scenario `latestEnabled` exists to fix — an `enabled` flip mid-press

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/MicButtonGestureTest.kt`
**Issue:** The KDoc and inline comments in `MicButton.kt` (lines 75-82) justify
`rememberUpdatedState(enabled)` specifically because `enabled` can flip *while a press is in
flight*, and the release must dispatch based on the latest value, not the value captured when
the gesture began. The test suite proves the analogous claim for `onTap`'s identity
(`tap_midPressCallbackIdentitySwap_firesOnlyLatestOnTap`) but never exercises an `enabled` flip
mid-press at all — the two `enabled` states are only ever tested as static, unchanging props.
If a future edit accidentally keyed `pointerInput` on `enabled` again (the exact regression the
comment warns about), this test suite would not catch it.
**Fix:** Add a test that starts a press while `enabled = true`, flips `enabled` to `false`
mid-press (via a `mutableStateOf` toggled between `down()` and `up()`, mirroring the existing
`swapToTapB` pattern), releases, and asserts `onDisabledTap` — not `onTap` — fires exactly once.

### WR-02: No regression test for `onDisabledTap` identity swap mid-press

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/MicButtonGestureTest.kt`
**Issue:** `MicButton.kt`'s comment (lines 83-88) explicitly claims the "same rationale, applied
to the two dispatch callbacks" — i.e. `onDisabledTap`'s identity is meant to be exactly as
swap-safe as `onTap`'s. Only `onTap`'s swap is covered by
`tap_midPressCallbackIdentitySwap_firesOnlyLatestOnTap`; there is no equivalent assertion for
`onDisabledTap`, so that half of the claimed guarantee ships unverified.
**Fix:** Add a disabled-mic counterpart of the existing mid-press swap test, asserting release
dispatches through the latest `onDisabledTap` closure, not the one captured at press-start.

## Info

### IN-01: Duplicated three-way `when` for `containerColor`/`contentColor`

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/MicButton.kt:89-98`
**Issue:** `containerColor` and `contentColor` each re-derive the same
`!enabled -> … ; isListening -> … ; else -> …` branch structure independently. Any future new
state (e.g. an error/permission-denied visual) has to be added in two places kept in sync by
convention only.
**Fix:** Collapse into one `when` returning a `Pair<Color, Color>` (or a small local data
class) and destructure once.

### IN-02: KDoc describes the interaction emit pair as "try/finally-shaped" but no `try`/`finally` exists

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/MicButton.kt:80-81, 114-127`
**Issue:** The comment states "…keeps the SAME gesture coroutine alive for the whole press, so
the `try`/`finally`-shaped emit pair below always completes" — but the actual code has no
`try`/`finally`; it's a plain sequential `emit(press)` → `tryAwaitRelease()` →
`emit(Release/Cancel)`. In the current design this is safe only because the `pointerInput(Unit)`
key never changes and any cancellation of that coroutine coincides with the node (and its
`interactionSource`) leaving composition entirely — but that safety argument isn't the one the
comment makes, and it's not obvious to a future reader who takes "try/finally-shaped" literally
and assumes exception-safety that isn't actually coded.
**Fix:** Either wrap the emit pair in an actual `try { … } finally { … }` for defense-in-depth,
or reword the comment to state the real invariant (coroutine lifetime == node lifetime, not
try/finally).

### IN-03: Test helper name `notSetUp` reintroduces domain framing into a domain-agnostic library test

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/MicButtonGestureTest.kt:49`
**Issue:** `private val notSetUp = "Microphone unavailable"` names the constant after a
domain-specific interpretation ("mic not set up", i.e. permission/config not granted) even
though `MicButton` itself (per its own KDoc, lines 32-34) "carries no permission logic and no
domain concept." The library-neutral name for this string is simply what it says:
disabled/unavailable.
**Fix:** Rename to something like `disabledDescription` or `micUnavailable` to match the
parameter it mirrors (`disabledDescription`) instead of implying a specific cause.

### IN-04: `pressAndHold_onDisabledMic_neverFiresOnTap` largely duplicates the prior test

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/MicButtonGestureTest.kt:107-131`
**Issue:** This test and
`tap_onDisabledMic_invokesOnDisabledTap_andNeverFiresOnTap` (lines 80-105) assert the same three
things for the same disabled-mic setup; the only difference is splitting `down()`/`up()` into
two `performTouchInput` calls with an intervening `waitForIdle()` instead of one call. The
distinct value (proving a hold doesn't fire early) could instead be a single extra assertion
inserted into the existing test rather than a full duplicate test class body.
**Fix:** Merge the hold-specific assertion into
`tap_onDisabledMic_invokesOnDisabledTap_andNeverFiresOnTap`, or rename this test to make clear
what incremental behavior it's proving beyond the other one.

---

_Reviewed: 2026-09-27T00:00:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
