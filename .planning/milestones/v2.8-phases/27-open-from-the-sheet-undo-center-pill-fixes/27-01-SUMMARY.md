---
phase: 27-open-from-the-sheet-undo-center-pill-fixes
plan: 01
subsystem: ui
tags: [compose, outcome-sheet, go-to-item, accessibility, source-compat]

requires:
  - phase: 25
    provides: FeedbackTarget (opaque key plus localized openLabel)
provides:
  - "VoiceOutcomeUiState.Success.target, targetLabel, targetDetail (appended after endAction)"
  - "onOpenTarget on both OutcomeSheet overloads"
  - "Internal OpenTargetRow: one merged Role.Button node, label-less, chevron, locked while in flight"
  - "Classic go-to-item path through new functions inside the single scaffold"
affects: [27-02 batch rows, 27-03 undo center, 27-04 phase source audit, 29 docs]

actuals:
  tokens: 36000
  tasks: 3
  commits: 3

tech-stack:
  added: []
  patterns:
    - "Classic extension through new functions plus an if/else inside the one existing scaffold lambda (pinned functions stay byte-equal)"
    - "Tree-parity tests: flip outcome or handler under ONE call site and compare SemanticsDump trees"

key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OpenTargetRow.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetOpenTargetTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OpenTargetRowTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetOpenTargetLongLabelTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceipt.kt
    - api.txt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetFixtures.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/V271CallSiteCanaryTest.kt

key-decisions:
  - "PD-1: row locked (alpha 0.38, chevron kept, no click action, no Role.Button) while Success.inFlight"
  - "PD-2: Classic open path is an if/else inside the existing scaffold lambda, not a second scaffold call site"
  - "PD-3: test tags open_target_row and open_target_chevron (not outcome_sheet_ prefixed, to stay out of the API.md drift guard)"
  - "PD-4/5/6/7/8: no line cap on title and detail; blank targetLabel falls back to summary, blank openLabel to the title; no debounce; no docs edits; onOpenTarget threaded as a trailing defaulted parameter"

patterns-established:
  - "Shared internal row (OpenTargetRow) used by both styles, with SuccessOpenTargetRow as the no-op-unless-target wrapper"

requirements-completed: [FOPEN-01]

coverage:
  - id: D1
    description: "Success with target and onOpenTarget shows a label-less chevron row in Receipt and Classic that calls onOpenTarget(target) once per tap, through both OutcomeSheet overloads"
    requirement: FOPEN-01
    verification:
      - kind: unit
        ref: "OutcomeSheetOpenTargetTest#receipt_targetRow_tapOpensOnce_viaHoistedOverload"
        status: pass
      - kind: unit
        ref: "OutcomeSheetOpenTargetTest#classic_targetRow_tapOpensOnce_viaDefaultStateOverload"
        status: pass
    human_judgment: false
  - id: D2
    description: "Without target or without handler the sheet renders exactly as v2.7.1 (same code path); v2.7.1 call shapes of OutcomeSheet and Success compile with null defaults"
    requirement: FOPEN-01
    verification:
      - kind: unit
        ref: "OutcomeSheetOpenTargetTest#noHandler_withTarget_rendersTheSameTreeAsNoTarget_classic"
        status: pass
      - kind: unit
        ref: "V271CallSiteCanaryTest#success_v271Shapes_keepTheirDefaults"
        status: pass
    human_judgment: false
  - id: D3
    description: "Long German and Spanish row text wraps at 360dp and font scale 2.0 with the chevron inside the window"
    requirement: FOPEN-01
    verification:
      - kind: unit
        ref: "OutcomeSheetOpenTargetLongLabelTest (4 tests, NATIVE graphics)"
        status: pass
    human_judgment: true
    rationale: "STYLE.md section 7: only real-device screenshots count for layout sign-off; Gate-1 tester owns the on-device re-check (including FA-1 lock appearance)"

duration: 38min
completed: 2026-10-17
status: complete
plan_head_before: 65bc8cd3bf9a15cf447b8aae588049d931fbca62
commits: 3
---

# Phase 27 Plan 01: Open from the sheet, single target Summary

**A success sheet with `Success.target` and `onOpenTarget` now shows a label-less go-to-item row (title, optional detail, chevron, one merged `Role.Button` node labelled with the target's `openLabel`) in both Receipt and Classic, with Classic reached only through new functions inside the existing single scaffold.**

## Performance

- **Duration:** about 38 min
- **Tasks:** 3 (1 tracer, 2 auto)
- **Files:** 4 created, 6 modified (plus `api.txt` regenerated)

## Accomplishments

- `Success` gained `target`, `targetLabel`, `targetDetail` appended after `endAction` with KDoc (the caller clears `target` after Undo, brief D-14).
- `OpenTargetRow.kt` (all internal): `OpenTargetRow`, `SuccessOpenTargetRow`, `hasOpenTarget`, `openTargetTitle`, `openTargetDetail`, and the two tags. The row never reads the opaque key and never hides or dismisses the sheet.
- `onOpenTarget: ((FeedbackTarget) -> Unit)? = null` appended last on both `OutcomeSheet` overloads and threaded as a trailing defaulted parameter through `OutcomeSheetReceipt`, `OutcomeSheetReceiptContent` and `ReceiptSuccessBody`; the row is placed directly after the handled-by pill.
- Classic: an if/else inside the one existing `SheetScaffold` lambda calls the new `OutcomeSheetContentOpenTarget` / private `SuccessBodyOpenTarget`; the else arm keeps the literal `OutcomeSheetContent(outcome, colors = colors)`. All pinned Classic functions are untouched and both source-contract tests pass unedited.
- Tests: `OutcomeSheetOpenTargetTest` (25), `OpenTargetRowTest` (7), `OutcomeSheetOpenTargetLongLabelTest` (4), v2.7.1 canary (+2), fixture extended.

## Task Commits

1. Task 1 (tracer): `de85ed0` feat(27-01): add Success target fields, the shared open-target row and onOpenTarget on OutcomeSheet
2. Task 2: `55b5bc9` feat(27-01): Classic go-to-item row via new functions inside the single scaffold
3. Task 3: `1fb3e57` test(27-01): pin the open-target row, long German and Spanish geometry and the v2.7.1 shapes

## Verification (commands and results)

All run with `--no-daemon`, one Gradle invocation at a time, from the worktree.

- Task 1: `apiDump testDebugUnitTest` on OutcomeSheetOpenTargetTest, both source-contract tests, OutcomeSheetReceiptSuccessTest: failures 0. `apiCheck`: pass. `bash tools/verify-api-source-additive.sh v2.7.1`: `API SOURCE-ADDITIVE OK` (removed 0, signature_changes 0).
- Task 2: OutcomeSheetOpenTargetTest, both source-contract tests, OutcomeSheetClassicParityTest, HoistedStateTest, OutcomeSheetTest (49), SourceCompatV251Test: failures 0. `apiDump` then `git diff api.txt`: unchanged (no public symbol added). `detekt`: 0 code smells.
- Task 3: OpenTargetRowTest, OutcomeSheetOpenTargetTest, OutcomeSheetOpenTargetLongLabelTest, V271CallSiteCanaryTest, both source-contract tests: failures 0. Drift guards (CatalogDocsDrift, DomainVocabularyDrift, ComponentRegistryDrift, ComponentStatesMatrix, ComponentPlaygroundIntegrity, LibraryInvariants): failures 0. `detekt`: 0 code smells.
- Plan gate: `*OutcomeSheetReceipt*`, `*OutcomeSheetTest`, `*OutcomeSheetClassicParityTest`, `*OutcomeSheetHoistedStateTest` all green; `apiCheck` and the additive check green after the last commit; `git tag --list 'v2.8*'` prints nothing; no debris path in any plan commit; nothing pushed or tagged.
- Hook lane-2 lines (committed with `HUB_LANE_OVERRIDE=2`, reason "forwards a new defaulted onOpenTarget parameter; no v2.7.1 signature removed or reordered"): the `colors`/`sheetState` parameter lines that gained a trailing comma, the Receipt call line in the hoisted overload, the two forwarding lines in the Receipt file and the re-indented Classic content call (tasks 1 and 2). Task 3 was lane 1.

## Decisions Made

PD-1 to PD-8 as recorded in the plan were implemented unchanged (see key-decisions). Flagged assumptions carried forward: **FA-1** (row locked while `inFlight`; the ui-designer confirms on Gate-1 screenshots; revert is the single `enabled` argument), **FA-2** (no debounce; handler must be idempotent, stated in KDoc), **FA-3** (Unicode normalization and grapheme widths are not separately tested; blankness is Kotlin `isBlank`).

## Deviations from Plan

None - plan executed exactly as written, with these notes:

- The plan's "run it red first" step was not run as a separate Gradle invocation: before the fields exist the test cannot compile, so a red run would only show a compile error. The test was written before the implementation and went green on the first run.
- The drift guards were run from the worktree. They locate the repo by walking up to a directory named `yahirandroidtaste`, which here resolves to the main checkout, so their coverage of the worktree sources is not guaranteed. The orchestrator should re-run them from the main checkout after merge (`*CatalogDocsDriftGuardTest`, `*ComponentRegistryDriftGuardTest`, `*DomainVocabularyDriftGuardTest`).
- Where a test needs two renders for a parity or lock check, it flips state under one call site (one `setContent` per test) instead of two composes; Receipt and Classic variants of each check are separate test methods, so the plan's single "inBothStyles" names exist for the Receipt arm and the Classic arm carries a `_classic` suffix.

**Total deviations:** 0 auto-fixed. **Impact:** none.

## Issues Encountered

None. Behavioral verification on a device is not part of this plan; the Gate-1 tester owns the on-device re-check described in the plan's last truth.

## Next Phase Readiness

Ready for 27-02 (batch rows): `SuccessBodyOpenTarget` has the single batch-list line (`BatchResultsList(it)`) that 27-02 widens, and `OpenTargetRow` is reusable for per-row targets.

## Self-Check: PASSED

- Created files verified on disk: `OpenTargetRow.kt`, `OutcomeSheetOpenTargetTest.kt`, `OpenTargetRowTest.kt`, `OutcomeSheetOpenTargetLongLabelTest.kt`.
- Commits `de85ed0`, `55b5bc9`, `1fb3e57` exist on `worktree-agent-a94c239d5cde4e4d5`.
