---
phase: 17-approachladdercard-router-on-off-toggle
reviewed: 2026-10-05T00:00:00Z
depth: standard
files_reviewed: 6
files_reviewed_list:
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderRouterCompatTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ApproachLadderCardGalleryDemoTest.kt
  - api.txt
findings:
  critical: 0
  warning: 2
  info: 2
  total: 4
status: resolved
---

# Phase 17: Code Review Report

**Reviewed:** 2026-10-05
**Depth:** standard
**Files Reviewed:** 6
**Status:** issues_found

## Summary

The router toggle is a small, correct, append-only change. The four new params are appended last, so the
existing positional call shapes (the 6-param v2.4.x shape and the 11-positional Phase-15 shape) still bind.
The `require` pairing matches the offline-only pair. `index == 1` maps to ON and the options are ordered
`[Off, On]`, so the segment order and the emitted value agree. The hidden v2.4.1 shim is unchanged in the
diff, and it still delegates with `router = null`, so no pairing exception fires and no toggle renders.
`api.txt` reflects exactly the appended public descriptor, and the hidden shim is correctly absent from it.
I found no correctness, security, or binary-compat defects. The remaining findings concern maintainability
and documentation drift.

## Warnings

### WR-01: API.md does not document the new router parameters

**File:** `API.md:222-250` (the "Appending label fields to the voice models and composables" section)
**Issue:** `API.md` is the declared public-surface reference (CLAUDE.md: "Read `API.md` for the public
surface"). It describes the v2.5.0 appended-params / binary-compat story for `ApproachLadderCard` but does
not mention `router`, `onRouterChange`, `routerOnLabel`, or `routerOffLabel` anywhere (`grep -i router
API.md` is empty). Consumers wiring the card from `API.md` and `INTEGRATION.md` will not discover the
toggle or its `require` pairing contract. The pairing contract is a throw-on-violation behavior, so
undocumented use is a runtime crash risk.
**Fix:** Add `router` / `onRouterChange` (hidden when both are null; must be paired, otherwise
`IllegalArgumentException`) and the two label params to the `ApproachLadderCard` entry. Extend the v2.5.0
paragraph to note that the router params are appended after the label params and keep the v2.4.1 shim
valid.

### WR-02: Two private overloads of `ApproachLadderCardFixture` both have all-default parameters

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt:312-333`
**Issue:** The 2-param wrapper and the 3-param router-aware fixture have the same name and every parameter
defaulted. Calls such as `ApproachLadderCardFixture()` (line 126) and `ApproachLadderCardFixture(initialOfflineOnly = true)`
(line 130) resolve only because Kotlin's tie-break prefers the candidate that uses fewer default
arguments. That rule is subtle: any future edit (for example giving the wrapper a non-default parameter, or
adding a parameter to both) can turn the registry cells into "overload resolution ambiguity" compile
errors, or silently change which overload a cell reaches. The comment justifies keeping the 2-param shape
so the synthetic accessor descriptor stays stable. That is a valid ABI reason, but it argues for giving the
new fixture a distinct name, not an overload. The `initialRouter = true` call at line 375 is the only
call that disambiguates by name alone.
**Fix:** Rename the router-aware fixture (for example `ApproachLadderCardRouterFixture`) and have the
2-param wrapper delegate to it. This keeps the old accessor descriptor and removes reliance on
default-count overload ranking.

## Info

### IN-01: Compat test name and comment overstate what is asserted

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderRouterCompatTest.kt:74-105`
**Issue:** The test `v2_4_1 descriptor defaults router to null and renders no router toggle` never asserts
the shim card's absence of a router toggle directly. It infers it from a global count of `1` router toggle
(the sibling direct card) and `1` "Router off, selected". If a future change made the shim card leak a
router, the counts would go to 2 and fail, so the inference is sound. But the assertion is
count-by-inference rather than scoped to the shim card, which makes a failure message hard to read.
Tagging each card in a `Box(Modifier.testTag(...))` and asserting `hasAnyAncestor` scoping, as
`ApproachLadderCardGalleryDemoTest` already does, would make the intent explicit.
**Fix:** Wrap the shim invocation in a tagged `Box` and assert
`onAllNodesWithTag("approach_ladder_card_router_toggle")` has no match under it.

### IN-02: New router KDoc omits the D-05 hideable-by-null-prop reference

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt:82`
**Issue:** The class-level KDoc (lines 32-35) states the "every control is hideable-by-null-prop" rule
and lists the offline and cap pairs, but not the router pair. The `@param router` line says it hides the
toggle, which is correct, but the paragraph that enumerates hideable controls is now incomplete.
**Fix:** Add the `router` / `onRouterChange` pair to the sentence in lines 32-35 so the class summary
and the params agree.

---

_Reviewed: 2026-10-05_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
