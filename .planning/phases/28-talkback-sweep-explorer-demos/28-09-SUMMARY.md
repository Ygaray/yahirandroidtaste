---
phase: 28-talkback-sweep-explorer-demos
plan: 09
subsystem: accessibility
tags: [talkback, live-region, compose-semantics, outcome-sheet, gap-closure]
requires:
  - phase: 28-06
    provides: "PD-4 pre-recorded recipe for the empty-live-region-event fix"
  - phase: 28-07
    provides: "Gate-1 finding C-1 (refused-undo live region not spoken)"
provides:
  - "Refused-undo Surface is one merged Assertive live-region node carrying the full message"
affects: [28-gate-1-rerun]
tech-stack:
  added: []
  patterns: ["live-region node uses mergeDescendants = true so its change event has non-empty text"]
key-files:
  created: []
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetClassicParitySourceContractTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetLiveRegionTest.kt
key-decisions:
  - "Applied PD-4 as recorded: semantics(mergeDescendants = true) { liveRegion = Assertive } on the refused Surface only; no announceForAccessibility"
  - "Replaced exactly one pinned contract assertion (the no-merge line, 22 decision A9) with a recorded rationale; kept the one-live-region count"
requirements-completed: [A11Y-02]
status: complete
commits: 1
plan_head_before: 8b6f20cf2a805ff7ca511d2ad3e2850d04cd975e
actuals:
  tokens: 6000
  tasks: 2
  commits: 1
duration: 25min
completed: 2026-10-17
---

# Phase 28 Plan 09: Merge descendants into the refused-undo live region Summary

**The refused-undo Surface now merges its descendants, so the Assertive live-region node itself carries the full "Couldn't undo: ..." message instead of firing an empty-text event.**

## What changed

- `OutcomeSheet.kt`: the refused `Surface` semantics went from `.semantics { liveRegion = Assertive }` to `.semantics(mergeDescendants = true) { liveRegion = Assertive }`, with a comment citing Gate-1 C-1. The `.testTag("outcome_sheet_undo_refused")` is still directly before it, and there is still exactly one `liveRegion = LiveRegionMode.` in the file.
- `OutcomeSheetLiveRegionTest`: three new Robolectric tests read the tagged node from the merged tree: full message as its own `Text` with a changedItem suffix, no suffix without one, and exactly one Assertive region with no `OnClick` action.
- `OutcomeSheetClassicParitySourceContractTest`: the one deliberate edit. The pinned string is now `.testTag("outcome_sheet_undo_refused").semantics(mergeDescendants=true){liveRegion=LiveRegionMode.Assertive}`, the `assertFalse(... mergeDescendants)` line is removed, the exactly-one-live-region count is kept, and a comment above the test records the rationale (22 decision A9's evidence trigger fired in Gate-1 cycle 1; PD-4 of 28-06 pre-recorded this edit).

## Execution record

- Task 1 (tracer): RED first. The two text tests failed on the old code with `ComparisonFailure` (the node's own Text was empty); the click/count test passed as a baseline. After the fix the targeted run passed: LiveRegionTest 8 tests, 0 failures; ClassicParity contract 16 tests, 0 failures; `*OutcomeSheet*`, `*Receipt*`, `LibraryInvariantsGuardTest`, `Phase28HygieneSourceContractTest` green. Commit `6406901`.
- Task 2 (forced fresh battery, one Gradle invocation at a time, `--no-daemon`):
  - `./gradlew testDebugUnitTest --rerun-tasks --no-build-cache --no-daemon`: exit 0, 35 of 35 tasks executed, 1869 tests, 22 skipped, 0 failures, 0 errors (179 result files).
  - `./gradlew apiDump --no-daemon` then `git diff --exit-code api.txt`: api.txt byte-identical.
  - `./gradlew detekt --no-daemon --rerun-tasks`: exit 0, zero baseline kept.
  - `tools/test/run-all.sh`: every script reports FAIL=0 (the BLOCKED lines in its output are the precommit-hook test's own expected cases).
  - `git diff --stat 8b6f20c HEAD`: only the three named files.

## Deviations from Plan

None in behavior. One process note: the repo pre-commit hook classified the edit as lane 2 (source-line rewrite) and blocked the first commit; I committed with `HUB_LANE_OVERRIDE=2` as the plan authorizes (internal edit, no public signature changed, api.txt unchanged). `--no-verify` was not used.

## Not proven here

Device behavior is NOT proven by this plan. That TalkBack actually speaks the refusal message after "Bottom Sheet" on the TESTER is re-verified by the Gate-1 re-run (speech-overlay method in 28-07-SELF-UAT.md). If the merged node is still silent on the device, the next gap cycle must choose another route (this plan deliberately adds no `announceForAccessibility`). No adb device was touched.

## Self-Check: PASSED

- Modified files exist and the commit `6406901` is present on `worktree-agent-a0f1fb2076c6e70aa`.
- STATE.md and ROADMAP.md were not modified; nothing tagged, pushed or repinned.
