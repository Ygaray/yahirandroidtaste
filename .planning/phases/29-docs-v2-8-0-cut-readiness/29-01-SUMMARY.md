---
phase: 29-docs-v2-8-0-cut-readiness
plan: 01
subsystem: docs
tags: [api-docs, drift-guard, robolectric, long-string, feedback-stack]
status: complete
requires:
  - phase: 25-feedback-target-model-stack-state
    provides: FeedbackTarget, FeedbackResult, FeedbackStackState, tracked-undo path
provides:
  - "API.md '### Additions in v2.8.0' (target-and-stack half)"
  - "CatalogDocsDriftGuardTest v2.8.0 symbol guard (section slice + api.txt declaration)"
  - "Spanish wrapped handled-by pill coverage at 360dp / font scale 2.0 (Robolectric half of D-03)"
affects: [29-02, 29-03, 29-05, 29-06]
plan_head_before: 732ea62be9e6e34393729d60ada10de728c74fa9
actuals:
  tokens: 6500
  tasks: 2
  commits: 2
tech-stack:
  added: []
  patterns:
    - "Docs guard slices the API.md section by exact heading and matches whole-word symbols inside it only"
key-files:
  created: []
  modified:
    - API.md
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/CatalogDocsDriftGuardTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/HandledByPillShapeTest.kt
key-decisions:
  - "Docs follow the shipped source: UndoHistoryStore.append(groupId) is documented as String? = null (the plan text said a bare groupId)"
requirements-completed: [SHIP-05]
duration: 12 min
completed: 2026-10-18
---

# Phase 29 Plan 01: v2.8.0 docs tracer and Spanish pill coverage Summary

A drift guard that ties the new `### Additions in v2.8.0` section of `API.md` to `api.txt` (five symbols, whole-word, inside the section only), the first half of that section written from the source, and a Spanish wrapped handled-by pill case with a failing CircleShape control.

## Accomplishments

- **Tracer (Task 1, commit 4fcd21f).** `CatalogDocsDriftGuardTest` gained `newInV280` (FeedbackTarget, FeedbackResult, FeedbackStackState, FeedbackStackHost, rememberFeedbackStackState), the exact heading `### Additions in v2.8.0`, a section slicer (fails on zero or duplicate headings, ends at the next `###` or `##`), and the test `every symbol new in v2_8_0 is named in the API md v2_8_0 section and declared in api txt`. RED was recorded before any docs were written; GREEN after the section landed.
- **API.md section.** Eight rows: FeedbackTarget, FeedbackEvent.Notify and WithUndo (appended `target`, `supportingText`), FeedbackResult, FeedbackStackState with rememberFeedbackStackState, FeedbackStackState.emitTrackedWithUndo, SnackbarHostState.emitTrackedWithUndo, UndoHistoryEntry (`target`, `openableTarget`), UndoHistoryStore.append (target overload). The `FeedbackStackHost` row got one closing pointer sentence. Counts 67/6/73, the Surface table, section 11 heading and tag tables are untouched. No `feedback_stack_*` / `open_target_*` tag is mentioned (PD-2).
- **Spanish pill (Task 2, commit f2aab98).** `measureWrappedGermanPill()` became a shared `measureWrappedPill(prefix, tier, provider)`; the German tests keep their names and assertions. Added `wrappedLongSpanishPill_atFontScale2_keepsEveryTextCornerInsideTheShape` and `controlCircleShape_failsTheSameCornerCheck_whenWrapped_inSpanish`, each asserting `textLineCount > 1` first.

## RED output (first guard run, before the docs)

```
CatalogDocsDriftGuardTest > every symbol new in v2_8_0 is named in the API md v2_8_0 section and declared in api txt FAILED
    java.lang.AssertionError: API.md needs exactly one '### Additions in v2.8.0' heading expected:<1> but was:<0>
11 tests completed, 1 failed
```

## Verification

| Check | Result |
|-------|--------|
| `CatalogDocsDriftGuardTest` | tests=11 failures=0 errors=0, new testcase present |
| `HandledByPillShapeTest` | tests=10 failures=0 errors=0, all four named testcases present |
| `V271CallSiteCanaryTest` | tests=12 failures=0 |
| `./gradlew detekt` | BUILD SUCCESSFUL (zero baseline) |
| `grep -c '^### Additions in v2.8.0$' API.md` | 1 |
| `git diff --stat 55f5288 HEAD -- src/main` | empty |
| `git diff --stat c1681ff HEAD -- api.txt` | empty |
| `git tag --list 'v2.8*'` | empty |
| `git show --stat` per task commit | exactly the planned files |
| Added lines scanned for home paths, 100.x addresses, ts.net | none |

## Deviations from Plan

None - plan executed exactly as written.

## Discrepancies between the plan text and the source (for the milestone master)

1. **`UndoHistoryStore.append` signature.** The plan wrote `append(message, preview, groupId, target, undoAction)`. The source (UndoHistoryStore.kt L123-129) has `groupId: String? = null` and `target: FeedbackTarget?` with no default. The docs describe the source, including that `groupId` is recorded only when non-null.
2. **Extra source-true behavior documented beyond the plan rows** (all read from FeedbackStackState.kt, UndoTracking.kt): timers are scaled by the accessibility manager's recommended timeout; `maxVisible` is constant per call site (changing it drops the cards); without a host an `Indefinite` card never returns until cancel or `dismissAll()`; a cancelled caller after the Undo tap still completes the undo under `NonCancellable`. Plan 29-02 need not repeat these.
3. **Eviction wording.** "A `WithUndo` entry stays in the Undo Center" holds on the tracked path only; a plain `show(WithUndo)` creates no history entry. The row says "a `WithUndo` entry of an evicted card", which is accurate for the tracked path.
4. No disagreement found between 29-CONTEXT.md Runtime Decisions and the source for the symbols covered here (POL-03 and TalkBack lines belong to 29-02).

## Issues Encountered

None. The pre-commit hook classified both commits as LANE 1 (docs and tests only) and passed without overrides.

## Next Phase Readiness

Ready for 29-02 (rest of the v2.8.0 section: Success, BatchRowResultUiModel, OutcomeSheet and UndoCenterScreen `onOpenTarget`, POL-01/02/03, compatibility scope, INTEGRATION.md). The slicer in the guard is reusable for further per-symbol tests. Working tree debris and the orchestrator's `.planning/STATE.md` edits were left untouched by task commits.

## Self-Check: PASSED

- 4fcd21f and f2aab98 exist in `git log`; the three modified files exist on disk.
- All acceptance criteria of both tasks re-run and passing (see Verification table).
