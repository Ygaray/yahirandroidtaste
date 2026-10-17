---
phase: 27-open-from-the-sheet-undo-center-pill-fixes
plan: 02
subsystem: ui
tags: [compose, outcome-sheet, batch-rows, go-to-item, accessibility, source-compat]

requires:
  - phase: 27-01
    provides: "OpenTargetRow.kt, Success.target fields, onOpenTarget threading, Classic open path"
provides:
  - "BatchRowResultUiModel.target (appended after detail, nullable, default null)"
  - "Internal OpenTargetBatchList: one anatomy per row, chevron/merge/click/48dp only on rows with a target"
  - "hasOpenTarget() also true for a batch-only outcome"
  - "Receipt and Classic batch switch (open list only with a handler and at least one targeted row)"
affects: [27-03 undo center, 27-04 phase source audit, 29 docs]

actuals:
  tokens: 33000
  tasks: 3
  commits: 3

tech-stack:
  added: []
  patterns:
    - "Per-row target binding by value inside the row's own click lambda (no index, no shared lambda)"
    - "Both-styles tests flip the style state inside one composition (one setContent per test)"

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetBatchOpenTargetTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/BatchRowResultUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OpenTargetRow.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceipt.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
    - api.txt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetFixtures.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetOpenTargetLongLabelTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/V271CallSiteCanaryTest.kt

key-decisions:
  - "PD-9: all rows of an open list share the 12dp rhythm, hairline above and text styles; 48dp minimum only on a clickable row"
  - "PD-10: a target-less row reserves no chevron space (LOCKED D-03 wins over the mock's invisible slot)"
  - "OpenTargetRow and the batch row share private helpers (openTargetClick, hairlineAbove, OpenTargetChevron); the batch row tag is the existing documented outcome_sheet_batch_row literal, no new outcome_sheet_ literal"

patterns-established:
  - "Gate for the batch switch is one internal helper, List<BatchRowResultUiModel>.hasRowTarget(), used by hasOpenTarget, the Receipt body and the Classic open body"

requirements-completed: [FOPEN-01, FOPEN-02]

coverage:
  - id: D1
    description: "Each targeted batch row opens its own item with its own target exactly once per tap; a target-less row has no chevron, no click action and does nothing, in Receipt and Classic"
    requirement: FOPEN-02
    verification:
      - kind: unit
        ref: "OutcomeSheetBatchOpenTargetTest#receipt_mixedBatch_eachTargetedRowOpensItsOwnItem_targetlessRowIsInert"
        status: pass
      - kind: unit
        ref: "OutcomeSheetBatchOpenTargetTest#classic_mixedBatch_eachTargetedRowOpensItsOwnItem_targetlessRowIsInert"
        status: pass
      - kind: unit
        ref: "OutcomeSheetBatchOpenTargetTest#targetedRowBetweenTargetlessRows_leavesNeighboursInert"
        status: pass
    human_judgment: false
  - id: D2
    description: "Edges: equal targets, equal labels (order kept, no de-duplication), empty batch, single-row batch, all rows target-less"
    requirement: FOPEN-02
    verification:
      - kind: unit
        ref: "OutcomeSheetBatchOpenTargetTest#equalTargetsOnTwoRows_eachCallsTheHandlerOnItsOwnTap"
        status: pass
      - kind: unit
        ref: "OutcomeSheetBatchOpenTargetTest#equalLabels_keepListOrder_andEachOpensItsOwnTarget"
        status: pass
      - kind: unit
        ref: "OutcomeSheetBatchOpenTargetTest#emptyBatch_rendersNoList"
        status: pass
      - kind: unit
        ref: "OutcomeSheetBatchOpenTargetTest#singleRowBatch_withATarget_opens"
        status: pass
    human_judgment: false
  - id: D3
    description: "No handler, or no targeted row, renders the v2.7.1 list (same semantics tree); BatchResultsList keeps its pin and visibility; both source-contract tests pass unedited"
    requirement: FOPEN-02
    verification:
      - kind: unit
        ref: "OutcomeSheetBatchOpenTargetTest#noHandler_withTargetedRows_rendersTheV271List_bothStyles"
        status: pass
      - kind: unit
        ref: "OutcomeSheetBatchOpenTargetTest#allRowsWithoutTargets_rendersTheV271List"
        status: pass
      - kind: unit
        ref: "OutcomeSheetReceiptSourceContractTest and OutcomeSheetClassicParitySourceContractTest (unedited)"
        status: pass
    human_judgment: false
  - id: D4
    description: "Source-compat: BatchRowResultUiModel 2- and 3-argument construction, copy and destructuring still compile with a null target; api.txt additive"
    requirement: FOPEN-02
    verification:
      - kind: unit
        ref: "V271CallSiteCanaryTest#batchRow_v271Shapes_keepTheirDefaults"
        status: pass
      - kind: other
        ref: "bash tools/verify-api-source-additive.sh v2.7.1 prints API SOURCE-ADDITIVE OK; ./gradlew apiCheck passes"
        status: pass
    human_judgment: false
  - id: D5
    description: "Locked while in flight (dimmed, chevron kept, no click) then unlocked; error text for failed rows; no reserved chevron space; opaque keys never in the tree"
    requirement: FOPEN-02
    verification:
      - kind: unit
        ref: "OutcomeSheetBatchOpenTargetTest#inFlight_locksTargetedRows_thenUnlocks"
        status: pass
      - kind: unit
        ref: "OutcomeSheetBatchOpenTargetTest#targetlessRow_reservesNoChevronSpace"
        status: pass
      - kind: unit
        ref: "OutcomeSheetBatchOpenTargetTest#opaqueKeys_neverAppearInTheSemanticsTree"
        status: pass
    human_judgment: false
  - id: D6
    description: "Long German and Spanish targeted failed batch rows wrap at 360dp and font scale 2.0 with the chevron inside the window; row anatomy parity with the mock (FA-4)"
    requirement: FOPEN-02
    verification:
      - kind: unit
        ref: "OutcomeSheetOpenTargetLongLabelTest (4 new batch-row tests, NATIVE graphics)"
        status: pass
    human_judgment: true
    rationale: "STYLE.md section 7: only real-device screenshots count for layout sign-off; the Gate-1 tester owns the on-device re-check (mixed targeted/target-less rows, German, font scale 2.0) and FA-4"

duration: 36min
completed: 2026-10-17
status: complete
plan_head_before: c5cfcede851204baca70e6556e81a38220f9fc5f
commits: 3
---

# Phase 27 Plan 02: Open from the sheet, batch rows Summary

**Each `BatchRowResultUiModel` can carry its own `FeedbackTarget`; with a handler and at least one targeted row, Receipt and Classic render an `OpenTargetBatchList` where only targeted rows get a chevron, a merged `Role.Button` node and a click, and every other batch renders the unchanged v2.7.1 list.**

## Performance

- **Duration:** about 36 min
- **Tasks:** 3 (1 tracer, 2 auto)
- **Files:** 1 created, 8 modified (including the regenerated `api.txt`)

## Accomplishments

- `BatchRowResultUiModel.target` appended after `detail` with KDoc (the caller clears it after Undo, brief D-14). `api.txt` regenerated; `tools/verify-api-source-additive.sh v2.7.1` prints `API SOURCE-ADDITIVE OK` (removed 0, signature_changes 0).
- `OpenTargetRow.kt`: `OpenTargetBatchList` plus a private `OpenTargetBatchRow`. Rows keep list order and bind their own target by value. The tag is the existing documented `outcome_sheet_batch_row`; the chevron reuses `open_target_chevron`. `hasOpenTarget()` is now also true for a batch-only outcome. The single row and the batch row share private helpers (`openTargetClick`, `hairlineAbove`, `OpenTargetChevron`).
- `ReceiptSuccessBody` and the Classic `SuccessBodyOpenTarget` each switch one batch line: open list when (handler and a row has a target), else the literal v2.7.1 `BatchResultsList(it)` call. `BatchResultsList`, the pinned Classic functions and both source-contract tests are untouched.
- Tests: `OutcomeSheetBatchOpenTargetTest` (17, both styles), 4 long-label batch tests, canary `batchRow_v271Shapes_keepTheirDefaults`, fixture `batchResultsWithTargets`.

## Task Commits

1. Task 1 (tracer): `6a565d7` feat(27-02): per-row open targets for batch results in the Receipt style
2. Task 2: `47da597` feat(27-02): per-row open targets for batch results in the Classic style
3. Task 3: `2645035` test(27-02): pin the batch open-target anatomy, edges, locks, long labels and the BatchRowResultUiModel shapes

## Verification (commands and results)

All with `--no-daemon`, one Gradle invocation at a time, from the worktree.

- Task 1: `apiDump` plus tests (BatchOpenTarget, OpenTarget, both source contracts, ReceiptSuccess, ClassicParity): failures 0. `apiCheck`: pass. Additive script: OK. Acceptance greps all as specified.
- Task 2: same set plus HoistedState: failures 0. `apiDump` then `git diff --exit-code api.txt`: no change. `OpenTargetBatchList(` count 1, `BatchResultsList(it)` count 2, `SheetScaffold(` count 1 in `OutcomeSheet.kt`.
- Task 3: BatchOpenTarget (17), LongLabel (8), V271Canary (11), OpenTarget, both source contracts: failures 0. Six guard classes (CatalogDocsDrift 10, DomainVocabularyDrift 2, ComponentRegistryDrift 1, ComponentStatesMatrix 2, ComponentPlaygroundIntegrity 5, LibraryInvariants 5): failures 0. `detekt`: 0 code smells.
- Plan gate: `*OutcomeSheetReceipt*`, `*OutcomeSheetTest`, `*OutcomeSheetClassicParityTest`, `*OutcomeSheetHoistedStateTest`, `*OpenTargetRowTest` plus the above: green. `git tag --list 'v2.8*'` prints nothing; nothing pushed; no debris path in any plan commit.
- Lane-2 lines (committed with `HUB_LANE_OVERRIDE=2`, reason "rewrites unreleased Phase 27 lines and one v2.7.1 batch line; no v2.7.1 signature removed or reordered"): tasks 1 and 2 (the `hasOpenTarget` line and the 27-01 row refactor in `OpenTargetRow.kt`, the Receipt batch line, the Classic open-body batch line). Task 3 was lane 1.

## Decisions Made

PD-9 and PD-10 as in the plan. **FA-4** (identical row anatomy, 48dp minimum only on clickable rows) is carried forward: confirm on the Gate-1 screenshot; changing it touches `OpenTargetBatchRow` only. In a mixed list a target-less row is shorter than a targeted one by design.

## Deviations from Plan

None - plan executed exactly as written, with these notes:

- The "run it red first" steps (tasks 1 and 2) were not run as separate Gradle invocations: before the field exists the test cannot compile, and in task 2 the test and the one-line production change were written in the same step. Each test went green on its first run.
- The both-styles tests flip the `style` state inside one composition per test (one `setContent` allowed per Robolectric test), so each plan-named test covers both styles; the Receipt-only tracer and the Classic pair keep their own hosts. The whole class runs with NATIVE graphics at 360dp by 800dp (needed for the real-font width check in `targetlessRow_reservesNoChevronSpace`).
- The plan's single long-label test name became four tests, one per style and language, each with the planned name as its suffix (one `setContent` per test).
- Added a small extra test, `succeededRowWithDetail_neverShowsTheDetail`, pinning the v2.7.1 text rule that the detail belongs to a failed row only.
- The drift guards (`*CatalogDocsDriftGuardTest`, `*ComponentRegistryDriftGuardTest`, `*DomainVocabularyDriftGuardTest`) were run from the worktree. They walk up to a directory named `yahirandroidtaste`, which from here resolves to the MAIN checkout, so they probably read the main checkout's sources and not these worktree sources. The orchestrator should re-run them from the main checkout after merge.

**Total deviations:** 0 auto-fixed. **Impact:** none.

## Issues Encountered

None. Behavioral verification on a device is not part of this plan; the Gate-1 tester owns the on-device re-check (a mixed targeted and target-less batch, German, font scale 2.0).

## Next Phase Readiness

Ready for 27-03 and 27-04. 27-04's phase-wide source contract can scan `OpenTargetRow.kt` for key reads (none exist: the opaque key is only handed back to the handler).

## Self-Check: PASSED

- Created file verified: `OutcomeSheetBatchOpenTargetTest.kt`.
- Commits `6a565d7`, `47da597`, `2645035` exist on `worktree-agent-a3f4e49b32d07d787`; `git rev-list --count` from the recorded base is 3.
