---
phase: 25-feedback-target-model-stack-state
plan: 03
subsystem: feedback
tags: [compose, coroutines, state-machine, api-additive, metalava, virtual-time]
requires: ["25-01", "25-02"]
provides:
  - "FeedbackResult enum { Opened, Undone, Dismissed, TimedOut }"
  - "FeedbackStackState(maxVisible = 3): composition-free show(event), dismissAll(), maxVisible"
  - "internal FeedbackStackItem, resolve, showInternal(event, entryId, watch), timeoutScaler, attach/detachAccessibility"
affects: [25-04, 25-05]
tech-stack:
  added: []
  patterns: ["one CompletableDeferred.complete() per card is the only exactly-once guard", "timer runs inside show() on the caller's dispatcher", "reference-counted binding by manager identity"]
key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackResult.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackStateTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackStateTimeoutTest.kt
  modified:
    - api.txt
key-decisions:
  - "Eviction loops (while size > maxVisible) instead of a single removal, so the cap holds even if the list is ever over by more than one"
  - "Accessibility bindings live in a private list under the existing lock; the scaler is recomputed from the last remaining manager after every attach/detach"
  - "No logging, no Context, no undo lambda on the item (T-25-16)"
requirements-completed: [FSTK-01]
status: complete
commits: 3
plan_head_before: 2f2444c53e6db2bdd370fe28b2bbd9827d37ec0d
actuals:
  tokens: 14000
  tasks: 3
  commits: 3
duration: 30min
completed: 2026-10-17
---

# Phase 25 Plan 03: FeedbackResult and FeedbackStackState Summary

A composition-free `FeedbackStackState` whose `show(event)` suspends until the card resolves to exactly one of Opened, Undone, Dismissed or TimedOut, with the 3-card cap, per-card timer, cancellation, `dismissAll()` and a reference-counted accessibility binding, all proven on virtual time.

## What was built

- `FeedbackResult.kt`: the four-value enum with per-value KDoc (enum, not sealed, D-04).
- `FeedbackStackState.kt`:
  - `show` delegates to `showInternal`, a `coroutineScope` that enqueues under the lock, launches the timer (none for Indefinite), optionally launches a `watch` coroutine, and awaits the item's `CompletableDeferred`; `finally` cancels timer and watcher and resolves Dismissed (a no-op unless the caller was cancelled).
  - `resolve(item, result)` completes the deferred and removes the item inside one critical section; `true` only for the first resolver.
  - Cap: newest-first list, oldest evicted with Dismissed; `maxVisible` below 1 reads back as 1.
  - Timer: Short 4000, Long 10000, Indefinite none; `timeoutScaler` is read once at enqueue and receives `containsControls = hasUndo || target != null`. `DismissScreen` is message-only, Short.
  - `attachAccessibility` / `detachAccessibility` (internal): reference-counted by identity, flags `containsIcons = true, containsText = true`, scaler cleared when the last binding goes.
  - The state never invokes an event's `undoAction`.
- Tests: `FeedbackStackStateTest` (22) and `FeedbackStackStateTimeoutTest` (21): 50x8 parallel-resolver race, four racing pairs plus dismissAll-then-tap, cap and freed slot, cancel (job and async), mapping of all three events, undo lambda run count 0, 3999/+1 and 9999/+1 boundaries, enqueue-start per card, Indefinite for ten virtual minutes, scaler seam values, doubled and tripled scaler, replaced scaler, attach/detach counting.

## Commits

- `0856978` feat(25-03): add FeedbackResult and FeedbackStackState core (lane 1, includes `api.txt`)
- `9206bb1` test(25-03): cover FeedbackStackState exactly-one, cap, timers, cancellation (lane 1)
- `a3774e9` feat(25-03): reference-counted accessibility scaling binding on FeedbackStackState (lane 1, additions only)

## Verification

- `./gradlew testDebugUnitTest --tests '*FeedbackStackStateTest' --tests '*FeedbackStackStateTimeoutTest' --tests '*V271CallSiteCanaryTest' --tests '*UndoHistoryStoreTest' detekt apiCheck --no-daemon`: BUILD SUCCESSFUL (22 + 21 + 8 + 33 tests, 0 failures; detekt zero baseline; apiCheck green).
- `bash tools/verify-api-source-additive.sh v2.7.1`: `API SOURCE-ADDITIVE OK` (removed 0, required_param_changes 0, signature_changes 0).
- `git diff v2.7.1 -- api.txt | grep -c '^-[^-]'` is 5 (unchanged from plans 25-01 and 25-02); the attach/detach members are internal, so `api.txt` did not change in task 3.
- Tracer gate: after the tracer commit the verify (tests, apiDump, additive script) had just passed end to end before expansion.

## Deviations from Plan

None of substance. One plan-text nit: Task 1's criterion `grep -q 'enum class FeedbackResult' api.txt` cannot match, because Metalava renders Kotlin enums as `public enum FeedbackResult {` (line 835 of `api.txt`, with all four constants listed); the intent (enum is in the public API) is met. No defect surfaced in `FeedbackStackState.kt` from the test matrix, so no `HUB_LANE_OVERRIDE` was needed.

## Notes

- Executed in a worktree named `agent-...`; directory-name-based drift guards (which walk up to a `yahirandroidtaste` directory) pass vacuously here. They are not part of this plan; re-run the full suite in the main checkout at phase end.
- `FeedbackStackState` is public API now (`api.txt` lists it); `rememberFeedbackStackState`, the host and the tracked-undo path (`emitTrackedWithUndo`) follow in plans 25-04 and 25-05.
- No tag, push or consumer repin. STATE.md and ROADMAP.md untouched (orchestrator-owned). No working-tree debris committed.

## Self-Check: PASSED

- FOUND: FeedbackResult.kt, FeedbackStackState.kt, FeedbackStackStateTest.kt, FeedbackStackStateTimeoutTest.kt, api.txt (class FeedbackStackState, `public enum FeedbackResult` with four constants)
- FOUND commits: 0856978, 9206bb1, a3774e9
