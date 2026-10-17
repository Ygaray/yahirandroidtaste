---
phase: 25-feedback-target-model-stack-state
plan: 02
subsystem: feedback
tags: [compose, undo, api-additive, metalava, material3]
requires: ["25-01"]
provides:
  - "UndoHistoryEntry.target (last ctor param) and derived openableTarget (null unless Available)"
  - "UndoHistoryStore.append(message, preview, groupId, target, undoAction) with required target"
  - "SnackbarHostState.emitTrackedWithUndo forwards event.target into the store"
  - "UndoTrackingMaterialPathTest: first characterization test of the Material3 tracked path"
affects: [25-03, 25-04, 25-05]
tech-stack:
  added: []
  patterns: ["required (undefaulted) param on a new overload keeps old call shapes bound to old overloads", "derive openability from status inside the entry"]
key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoTrackingMaterialPathTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryEntry.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryStore.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoTracking.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryStoreTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/V271CallSiteCanaryTest.kt
    - api.txt
key-decisions:
  - "target is the LAST UndoHistoryEntry ctor param, so v2.7.1 positional copy and componentN are unchanged"
  - "New append overload keeps groupId: String? = null so a grouped entry can carry a target (Phase 27)"
  - "No NonCancellable or other logic added to the M3 path; the cancellation guard belongs to the stack path (D-09)"
requirements-completed: [FTGT-02]
status: complete
commits: 3
plan_head_before: 63615e250600d9a0288916a84039da38c97d97ca
actuals:
  tokens: 9000
  tasks: 2
  commits: 3
duration: 25min
completed: 2026-10-17
---

# Phase 25 Plan 02: Target on UndoHistoryEntry and the Material3 tracked path Summary

A `WithUndo` carrying a `FeedbackTarget` now lands in `UndoHistoryStore` with that same target through the existing `emitTrackedWithUndo`, and the entry's derived `openableTarget` goes null the moment it is Undone or Failed.

## What was built

- `UndoHistoryEntry.kt`: `val target: FeedbackTarget? = null` appended as the last constructor param, plus `val openableTarget get() = target?.takeIf { status == UndoStatus.Available }`. `withStatus` carries the target through `copy` untouched.
- `UndoHistoryStore.kt`: one new standalone `append(message, preview = null, groupId: String? = null, target: FeedbackTarget?, undoAction)` overload. `target` has no default, so every v2.7.1 call shape (including `append(m, groupId = g) { }` and the function-value form) still binds to the old overloads; both old bodies are byte-identical.
- `UndoTracking.kt`: the `store.append(...)` call now passes `target = event.target` (split across lines to respect the detekt line limit). Nothing else in the helper changed (still `actionLabel = "Undo"`, same `ActionPerformed` check).
- `UndoTrackingMaterialPathTest` (6 tests): target present in the store while the snackbar is up; ActionPerformed undoes once and `openableTarget` becomes null with `target` kept; Dismissed never undoes and stays openable; untargeted event behaves as v2.7.1; Undo Center tap racing the snackbar action runs undo exactly once; visible snackbar keeps the `Undo` label, duration and message.
- `UndoHistoryStoreTest` (+8, now 33): target stored, three untargeted overloads leave it null, null openable after Undone and after Failed, `withStatus`/`copy` keep it, grouped membership only for non-null groupId, concurrent attemptUndo runs once, eviction at 50 with targets.
- `V271CallSiteCanaryTest`: every v2.7.1 append shape yields `target == null` and `openableTarget == null`; entry `copy`/`withStatus` keep null.
- `api.txt` regenerated in commit A.

## Commits

- `04eed30` feat(25-02): carry FeedbackTarget on UndoHistoryEntry and add append overload (lane 1)
- `07c70ff` feat(25-02): forward FeedbackTarget through the Material3 tracked path (lane 2, `HUB_LANE_OVERRIDE=2`; one existing line rewritten, reason stated in the body)
- `4c1040b` test(25-02): cover target on UndoHistoryEntry and the new append overload (lane 1)

## Verification

- `testDebugUnitTest` for `UndoTrackingMaterialPathTest` (6), `UndoHistoryStoreTest` (33), `V271CallSiteCanaryTest` (8), `UndoCenterScreen*`: all green, 0 failures.
- `bash tools/verify-api-source-additive.sh v2.7.1`: `API SOURCE-ADDITIVE OK` (required_param_changes=0, signature_changes=0).
- `./gradlew detekt apiCheck --no-daemon`: green at zero baseline.
- `git diff v2.7.1 -- api.txt | grep -c '^-[^-]'` is 5 (Notify and WithUndo ctor/copy from 25-01, plus UndoHistoryEntry copy).
- Tracer gate: end-to-end verify re-run passed before the test-only task; expansion proceeded.

## Deviations from Plan

None. The plan was executed as written. One cosmetic choice: the lane-2 line was reformatted over several lines (still exactly one removed line) because the single-line form exceeded the detekt max line length.

## Notes

- Executed in a worktree named `agent-...`; directory-name-based drift guards that walk up to a `yahirandroidtaste` directory pass vacuously here. They are not part of this plan's checks; re-run the full suite in the main checkout at phase end.
- No tag, push or consumer repin. STATE.md and ROADMAP.md untouched (orchestrator-owned). No working-tree debris committed.

## Self-Check: PASSED

- FOUND: UndoTrackingMaterialPathTest.kt, UndoHistoryEntry.kt (openableTarget), UndoHistoryStore.kt (target: FeedbackTarget?,), UndoTracking.kt (target = event.target)
- FOUND commits: 04eed30, 07c70ff, 4c1040b
