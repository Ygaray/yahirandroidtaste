---
phase: 28-talkback-sweep-explorer-demos
plan: 10
subsystem: feedback
tags: [a11y, talkback, snackbar-stack, gap-closure]
requires:
  - phase: 28-talkback-sweep-explorer-demos
    provides: "28-09 merged live-region fix; 28-07 Gate-1 cycle 1 finding (silent peek strip)"
provides:
  - "The middle card of a collapsed stacked-snackbar deck is invisible to accessibility (no silent TalkBack stop)"
affects: [28-gate-1-rerun]
tech-stack:
  added: []
  patterns: ["index/state-derived internal flag -> Modifier.semantics { invisibleToUser() }"]
key-files:
  created: []
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHost.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostSemanticsTest.kt
key-decisions:
  - "No label, description or fan action added to the strip (ui-designer ruling, 28-CONTEXT 2026-10-17); the card is hidden instead"
  - "Rule: hiddenFromAccessibility = !fanned && index > 0 && index < items.size - 1 (deck holds at most 3 cards, so exactly one middle card)"
requirements-completed: [A11Y-02]
status: complete
duration: ~25 min
completed: 2026-10-17
commits: 1
plan_head_before: ec0e2a0a6ce0beca282816550db42cce0c843d21
actuals:
  tokens: 9000
  tasks: 2
  commits: 1
---

# Phase 28 Plan 10: Hide the silent middle peek strip from accessibility Summary

**The middle card of a collapsed stacked-snackbar deck now carries `invisibleToUser`, so TalkBack no longer stops on it in silence; the front and oldest cards stay stops and a fanned deck hides nothing.**

## What changed

- `FeedbackCard` (internal) gained a trailing parameter `hiddenFromAccessibility: Boolean = false`. When true, the card root gets `.then(Modifier.semantics { invisibleToUser() })` right after the existing merged-semantics block. Layout, drawing, the tap owner and `CollapsedBackTouchBlocker` are untouched; the D-11 Undo child and the D-07 double Dismiss are untouched.
- `FeedbackStackHost` computes `hiddenFromAccessibility = !fanned && index > 0 && index < state.items.size - 1` at the `FeedbackCard(...)` call.
- No opt-in annotation was needed for `invisibleToUser` in this Compose BOM. No public declaration changed; `api.txt` is byte-identical after `apiDump`.

## Tasks and commits

| Task | Name | Commit |
|------|------|--------|
| 1 | Middle back card of a collapsed deck is not an accessibility stop | c125093 |
| 2 | Forced fresh battery (verification only, no commit) | - |

## Tests

Six cases were added to `FeedbackStackHostSemanticsTest` (all pass; suite 25 tests, 0 failures):
`collapsedDeckOfThree_middleCardIsInvisibleToUser_andFrontAndOldestAreNot`, `collapsedDeckOfThree_hasNoFocusableNodeWithAnEmptyLabel`, `collapsedDeckOfThreeInGerman_hasNoFocusableNodeWithAnEmptyLabel`, `collapsedDeckOfOneOrTwo_hidesNoCard`, `fannedDeckOfThree_hidesNoCard`, `tapOnTheHiddenMiddleStrip_stillFansTheDeck`.

Red phase: before the implementation, four tests failed for the right reason (the middle card was not flagged): the two three-card flag/label tests, the German one, and the tap test (which asserts the middle card is hidden before tapping). The 1/2-card and fanned cases passed already, as expected, since they guard against the flag leaking.

Note on the empty-label cases: on Robolectric the merged label of the middle card is NOT empty (the empty label and the clipping are a device fact), so those cases cannot fail on the old code on their own; the invisible-flag assertions are the red/green signal. The label cases still guard that every card remaining accessible has a non-empty label and that exactly two cards stay accessible in a collapsed deck of three.

`A11ySweepFeedbackCardTest` was not edited (it has no empty-label sweep helper; the cases live in the host semantics test where the multi-card builders are). It stays green (3 tests, 0 failures).

## Verification (commands and results)

- `./gradlew testDebugUnitTest --tests '*FeedbackStack*' --tests '*A11ySweepFeedbackCardTest' --tests '*FeedbackCard*' --tests '*FeedbackDeck*' --tests '*LibraryInvariantsGuardTest' --tests '*Phase28HygieneSourceContractTest' --no-daemon`: BUILD SUCCESSFUL.
- `./gradlew apiDump --no-daemon` then `git diff --exit-code api.txt`: exit 0, api.txt byte-identical.
- `./gradlew detekt --no-daemon`: BUILD SUCCESSFUL (zero baseline).
- `./gradlew testDebugUnitTest --rerun-tasks --no-build-cache --no-daemon` (35 of 35 tasks executed, forced fresh, whole unit suite): BUILD SUCCESSFUL.
- `bash tools/test/run-all.sh`: every script PASS, FAIL=0.
- The pre-commit hook classified the commit as LANE 1 (additive); no `HUB_LANE_OVERRIDE` was needed.

## Deviations from Plan

None. The plan listed `A11ySweepFeedbackCardTest.kt` among the files; it was only to be edited if it had an equivalent empty-label helper, which it does not, so it is unchanged.

## NOT proven by this plan

The TalkBack behavior itself (no silent focus stop in the collapsed deck, English and German, on the TESTER) is NOT proven here. No device was touched. It is re-verified by the Gate-1 re-run after this plan. Gate-2 should note the spoken order (front card, then the oldest strip) per threat T-28-42.

## Self-Check: PASSED

- Modified files present: FeedbackCard.kt, FeedbackStackHost.kt, FeedbackStackHostSemanticsTest.kt (committed in c125093).
- Commit c125093 exists on the worktree branch; `git rev-list --count ec0e2a0..HEAD` was 1 at SUMMARY time; working tree clean before this file.
