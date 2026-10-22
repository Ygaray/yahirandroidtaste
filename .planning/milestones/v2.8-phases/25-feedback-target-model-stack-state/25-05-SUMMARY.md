---
phase: 25-feedback-target-model-stack-state
plan: 05
subsystem: feedback
tags: [compose, robolectric, registry, drift-guards, api-additive, metalava]
requires: ["25-03", "25-04"]
provides:
  - "@Composable fun FeedbackStackHost(state, onOpenTarget, modifier, undoLabel = \"Undo\", dismissLabel = \"Dismiss\")"
  - "@Composable fun rememberFeedbackStackState(maxVisible: Int = 3): FeedbackStackState"
  - "internal FeedbackStackState.BindAccessibilityTimeout() and FEEDBACK_STACK_*_TEST_TAG constants"
affects: [26]
tech-stack:
  added: []
  patterns: ["CAS winner alone navigates (resolve before onOpenTarget)", "conditional clickable so a non-openable card has no click action", "reference-counted accessibility binding from DisposableEffect"]
key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHost.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/RememberFeedbackStackStateTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/FeedbackFamilyScreen.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
    - API.md
    - README.md
    - api.txt
key-decisions:
  - "Dismiss accessibility action carries the caller's dismissLabel and resolves Dismissed; swipe, deck motion and pause stay in Phase 26"
  - "A card without a target, or a spent card, gets no clickable modifier at all (not enabled = false)"
  - "ComponentRegistry KDoc snapshot counts left untouched (would force a lane-2 commit; no test reads them)"
requirements-completed: [FSTK-01, FSTK-07]
status: complete
commits: 3
plan_head_before: bdb6e74a0c12e7c1402e489856889d584109240e
actuals:
  tokens: 18000
  tasks: 3
  commits: 3
duration: 45min
completed: 2026-10-17
---

# Phase 25 Plan 05: FeedbackStackHost and rememberFeedbackStackState Summary

Public `FeedbackStackHost` (brief D-09 signature) renders `FeedbackStackState` cards newest-first with a CAS-guarded body tap, Undo, and localizable `undoLabel`/`dismissLabel`, plus `rememberFeedbackStackState`, all registered, allowlisted and documented at 67 registered / 6 unregistered / 73 total.

## What was built

- `FeedbackStackState.kt` (additions only): `rememberFeedbackStackState(maxVisible = 3)` and the internal `FeedbackStackState.BindAccessibilityTimeout()` (a `DisposableEffect(this, manager)` that attaches and detaches the `LocalAccessibilityManager`; plan 25-03's binding is reference-counted so host and remember never clear each other).
- `FeedbackStackHost.kt`: a `Column` of cards over `state.items` (`key(item.id)`). Body tap runs `if (state.resolve(item, Opened)) currentOnOpenTarget(target)`, so only the tap that resolved the card navigates. Undo calls `state.resolve(item, Undone)` and never touches an undo lambda. Click label is `openLabel.ifBlank { message }`. Leading dot is primary with a target, onSurfaceVariant without; optional second line for `supportingText`; chevron only when openable (`target != null && !spent`). Each card has one Dismiss custom action labelled `dismissLabel`.
- Registry: `FeedbackStackHost` PATTERN entry with four state cells (Default renders a live card) and a `FeedbackStackHostVariants` demo; `rememberFeedbackStackState` in `INTENTIONALLY_UNREGISTERED`; `"Feedback"` in `PRIMITIVE_NOUN_ALLOWLIST`.
- Docs: API.md and README.md counts 67/6/73, Feedback family 4, host row, allowlist row, `(6)` heading.
- Tests: `FeedbackStackHostTest` (10) and `RememberFeedbackStackStateTest` (6).

## Commits

- `fad787e` feat(25-05): add FeedbackStackHost and rememberFeedbackStackState (lane 1; includes `api.txt`)
- `f0ceb57` test(25-05): cover FeedbackStackHost taps, labels, spent cards and accessibility binding (lane 1)
- `f38b698` feat(25-05): register FeedbackStackHost and update catalog guards and docs (lane 1)

## Verification (fresh evidence)

- Tracer gate: `apiDump` plus `FeedbackStackHostTest` (tracer test, failures 0) and `verify-api-source-additive.sh v2.7.1` passed before expansion.
- Host tests: 10 tests, 0 failures; remember tests: 6 tests, 0 failures.
- Mutation check (recorded once, restored): changing `openable = target != null && !spent` to `target != null` made `spentCard_staysVisible_hidesUndoAndChevron_andIsInert` fail (1 of 10); with the original line restored all 10 pass. Committed code contains the original line.
- Guard run (CatalogDocsDriftGuardTest 10, DomainVocabularyDriftGuardTest 2, ComponentRegistryDriftGuardTest 1, ComponentStatesMatrixTest 2, ComponentPlaygroundIntegrityTest 5, LibraryInvariantsGuardTest 5, GeneratedSymbolDriftGuardTest 2): 0 failures, executed in a copy of the committed tree placed in a directory literally named `yahirandroidtaste` (see Notes).
- Full gate `./gradlew apiDump testDebugUnitTest detekt apiCheck --rerun-tasks --no-daemon` in that copy: BUILD SUCCESSFUL in 4m 47s, 44 of 44 tasks executed (none cached), 1446 tests, 22 skipped, 0 failures, 0 errors; detekt at zero baseline; the copy's regenerated `api.txt` is identical to the committed one.
- `bash tools/verify-api-source-additive.sh v2.7.1`: `API SOURCE-ADDITIVE OK` (removed_classes=0, removed_members=0, required_param_changes=0, signature_changes=0, added_classes=5, added_members=36, accepted_exceptions=0).
- `git diff v2.7.1 -- api.txt | grep -c '^-[^-]'` = 5 (the rewritten Notify/WithUndo/UndoHistoryEntry lines from plans 25-01 and 25-02); everything else is an addition.
- `bash tools/test/run-all.sh`: all cases pass (no FAIL lines).
- `git tag --list 'v2.8*'` is empty; nothing pushed; no debris path appears in any `v2.7.1..HEAD` commit.
- Counts recorded: 67 registered, 6 intentionally-unregistered, 73 total; Feedback family row says 4.

## Deviations from Plan

None to the code. One process deviation, no Rule 1-4 fix needed:

- Task 3 carries a precondition that the working directory be the main checkout named `yahirandroidtaste`. This plan ran in a worktree, where the directory-name-based guards scan the main checkout and pass vacuously. The edits were made in the worktree and the guards and full gate were then run against a copy of the committed tree (rsync, excluding `.git`, `build`, `.gradle`, `.claude`, `.planning`) placed in `.../scratchpad/verify/yahirandroidtaste`, so the guards scanned the new code and docs for real. Not verified here: the same guards inside the main checkout after merge. The orchestrator should re-run the guard command there once the wave is merged.

## Notes for Phase 26

- Rewriting any existing host or state line (not just appending) is a lane-2 commit: declare `HUB_LANE_OVERRIDE=2` with a reason.
- The host currently has no swipe, deck motion, pause, or merged-button semantics; append new params only after `dismissLabel`.
- `ComponentRegistry.kt` KDoc snapshot counts at the top were deliberately not edited.
- Hook output printed `LANE 1` on all three commits, so no override was needed.
- STATE.md and ROADMAP.md untouched (orchestrator-owned). No tag, push or consumer repin. No debris committed (`.planning/.jobs` is job-runner output and was not staged).

## Self-Check: PASSED

- FOUND: FeedbackStackHost.kt, FeedbackStackHostTest.kt, RememberFeedbackStackStateTest.kt, API.md and README.md contain `**67 registered public composables**`, api.txt contains FeedbackStackHost and rememberFeedbackStackState
- FOUND commits: fad787e, f0ceb57, f38b698
