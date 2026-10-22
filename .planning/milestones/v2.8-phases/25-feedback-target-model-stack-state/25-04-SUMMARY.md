---
phase: 25-feedback-target-model-stack-state
plan: 04
subsystem: feedback
tags: [compose, coroutines, undo, noncancellable, api-additive, metalava]
requires: ["25-02", "25-03"]
provides:
  - "suspend fun FeedbackStackState.emitTrackedWithUndo(store: UndoHistoryStore, event: FeedbackEvent.WithUndo): FeedbackResult"
affects: [25-05]
tech-stack:
  added: []
  patterns: ["append before show", "undo only through store.attemptUndo (shared CAS guard)", "NonCancellable around the store undo on the new path only", "spent watcher on store.entries via showInternal"]
key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackTrackedUndoTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoTracking.kt
    - api.txt
key-decisions:
  - "The new function never calls event.undoAction; a card Undo that loses the race still returns Undone and the store call is a CAS no-op"
  - "Spent card is kept on screen with spent = true, never auto-dismissed (D-08)"
  - "SnackbarHostState.emitTrackedWithUndo is untouched (D-09)"
requirements-completed: [FSTK-07, FTGT-02]
status: complete
commits: 2
plan_head_before: 0861639b88f29ce1e3c536f63f8aa423b57e4f51
actuals:
  tokens: 9000
  tasks: 2
  commits: 2
duration: 20min
completed: 2026-10-17
---

# Phase 25 Plan 04: Tracked undo on the feedback stack Summary

`FeedbackStackState.emitTrackedWithUndo(store, event)` appends the targeted entry to `UndoHistoryStore` before the card shows, runs the undo only through `store.attemptUndo` under `NonCancellable` when the card resolves Undone, and keeps a card whose entry was spent elsewhere.

## What was built

- `UndoTracking.kt` (additions only, zero removed lines): the new extension. Order: `store.append(..., target = event.target, ...)`, then `showInternal(event, entryId)` with a watcher that suspends on `store.entries.first { no Available entry with this id }` and sets `item.spent = true`, then `withContext(NonCancellable) { store.attemptUndo(entryId) }` only when the result is Undone. KDoc mirrors the Material3 function ("Order matters") and states the two cancellation windows.
- `FeedbackStackTrackedUndoTest.kt`: 17 tests: entry exists and matches `item.entryId` once the card is visible; Undone runs the lambda once and the entry loses its openable target but keeps its target; Opened, Dismissed and TimedOut never undo (entry Available, openable); both race orders (Undo Center first, card first) run the lambda exactly once; spent card on undone-elsewhere and failed-elsewhere (card kept); 4th arrival evicts a tracked card (Dismissed, lambda 0, entry Available); cancel before a result (CancellationException rethrown, card removed, no undo, entry still undoable later); cancel during the store undo (gated, job not completed until release, entry Undone, lambda once, job cancelled); throwing lambda (returns Undone, entry Failed); null target; field mapping and `baseMillis` 10000; Long card times out at exactly 10000 ms (`advanceTimeBy(9999)` then 1); one entry per call.
- `api.txt`: one added line for the new static method on `UndoTrackingKt`.

## Commits

- `2d2e0a9` feat(25-04): add FeedbackStackState.emitTrackedWithUndo (lane 1, additive; includes `api.txt`)
- `b2a0add` test(25-04): cover tracked stack path races, spent card and cancellation (lane 1)

## Verification

- Tracer gate: after the first commit's verify (apiDump + FeedbackStackTrackedUndoTest + UndoTrackingMaterialPathTest, then the additive script) passed end to end, expansion proceeded.
- `./gradlew testDebugUnitTest --tests` for FeedbackStackTrackedUndoTest, UndoTrackingMaterialPathTest, FeedbackStackStateTest, FeedbackStackStateTimeoutTest, UndoHistoryStoreTest, V271CallSiteCanaryTest, GeneratedSymbolDriftGuardTest plus `detekt apiCheck --no-daemon`: BUILD SUCCESSFUL, 0 failures, detekt at zero baseline.
- `bash tools/verify-api-source-additive.sh v2.7.1`: `API SOURCE-ADDITIVE OK` (removed 0, required_param_changes 0, signature_changes 0).
- Mutation check (recorded once, then restored): replacing `withContext(NonCancellable) { store.attemptUndo(entryId) }` with a direct `store.attemptUndo(entryId)` made `cancelDuringStoreUndo_undoStillFinishes_entryEndsUndone` fail (AssertionError at the test file line 294, 1 of 17 failed); with the wrapper restored all 17 pass. Committed code contains the wrapper.

## Deviations from Plan

None. The tests passed on first run against the implementation; no defect surfaced in `UndoTracking.kt`, so no `HUB_LANE_OVERRIDE` was needed.

## Notes

- Executed in a worktree named `agent-...`; directory-name-based drift guards (which walk up to a `yahirandroidtaste` directory) pass vacuously here, including `GeneratedSymbolDriftGuardTest`. Re-run the full suite in the main checkout at phase end.
- `FeedbackStackItem.spent` is now written by the tracked path; hiding Undo/open on a spent card is the host's job in plan 25-05.
- No tag, push or consumer repin. STATE.md and ROADMAP.md untouched (orchestrator-owned). No working-tree debris committed.

## Self-Check: PASSED

- FOUND: UndoTracking.kt (`fun FeedbackStackState.emitTrackedWithUndo`, one `withContext(NonCancellable)`, `store.append(` before `showInternal(`), FeedbackStackTrackedUndoTest.kt, api.txt (2 `emitTrackedWithUndo` lines)
- FOUND commits: 2d2e0a9, b2a0add
