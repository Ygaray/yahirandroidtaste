---
phase: 21-receipt-row-pill-selector-sheet-shape
plan: 01
subsystem: ui
tags: [compose, material3, bottom-sheet, sheetscaffold, api-surface]

requires:
  - phase: 20-tone-foundation-atoms
    provides: lane-1 additive path and the apiDump discipline this plan reuses
provides:
  - "SheetScaffold(shape: Shape = BottomSheetDefaults.ExpandedShape) before the trailing content lambda, forwarded by name to ModalBottomSheet"
  - "Source contract SheetScaffoldShapeTest and recording-Shape probe SheetScaffoldShapeProbeTest"
  - "Sheets gallery shaped-sheet demo button (24dp top corners)"
affects: [22-receipt-outcome-sheet, 21-04-phase-gate]

actuals:
  tokens: 5600
  tasks: 3
  commits: 3
plan_head_before: 6014927d1d2da8f7bdfedc6165246b40cf800179

tech-stack:
  added: []
  patterns:
    - "Whitespace-insensitive source contract on a public composable signature (declaration order, default, forwarding, single function)"
    - "Probe-gated recording Shape to prove a caller parameter reaches a Material container under Robolectric without pixels"

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/SheetScaffoldShapeTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/SheetScaffoldShapeProbeTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/SheetScaffold.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/SheetsFamilyScreen.kt
    - api.txt

key-decisions:
  - "Signature copied verbatim from sketch Y-13 (D-01); shape sits directly before the trailing content lambda, no overload or shim (D-02)"
  - "Default is BottomSheetDefaults.ExpandedShape, the same expression ModalBottomSheet defaults to, so defaulted callers render as v2.5.1"

patterns-established:
  - "Tracer slice: signature -> forward -> api.txt -> source contract -> gallery demo, one gradle invocation at a time"

requirements-completed: [VSHEET-01]

coverage:
  - id: D1
    description: "SheetScaffold accepts shape (default BottomSheetDefaults.ExpandedShape) before the trailing content lambda and forwards it by name to ModalBottomSheet"
    requirement: VSHEET-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/SheetScaffoldShapeTest.kt#shape sits between contentWindowInsets and the trailing content lambda with the M3 default"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/SheetScaffoldShapeTest.kt#shape is forwarded by name to ModalBottomSheet exactly once"
        status: pass
    human_judgment: false
  - id: D2
    description: "A caller-supplied shape is consulted by the real ModalBottomSheet (recording Shape probe)"
    requirement: VSHEET-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/SheetScaffoldShapeProbeTest.kt#caller supplied shape is consulted by the real sheet"
        status: pass
    human_judgment: false
  - id: D3
    description: "No overload or shim added; api.txt changed by exactly one rewritten SheetScaffold line; existing sheet tests unedited and green"
    requirement: VSHEET-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/SheetScaffoldShapeTest.kt#exactly one SheetScaffold function exists so no overload or shim was added"
        status: pass
      - kind: other
        ref: "./gradlew apiCheck detekt"
        status: pass
    human_judgment: false
  - id: D4
    description: "Sheets gallery shows the shaped-sheet demo button next to the generic one and the SheetScaffold registry entry is unchanged"
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/SheetScaffoldShapeTest.kt#gallery keeps SheetScaffold as a Sheets primitive and shows both demo buttons"
        status: pass
    human_judgment: false
  - id: D5
    description: "The rendered 24dp top corners of the shaped sheet look right on a device"
    verification: []
    human_judgment: true
    rationale: "Corner radius is never asserted from pixels in this harness (captureToImage times out, R-14); visual shape is a Gate-1 device check owned by the tester."

duration: 22min
completed: 2026-10-12
status: complete
---

# Phase 21 Plan 01: SheetScaffold shape parameter Summary

**`SheetScaffold(shape = BottomSheetDefaults.ExpandedShape)` inserted before the trailing content lambda and forwarded by name to `ModalBottomSheet`, pinned by a source contract and a recording-Shape probe, with a 24dp-top-corner gallery demo and a one-line api.txt change.**

## Performance

- **Duration:** ~22 min
- **Tasks:** 3 (tracer + probe + gallery/gate)
- **Files:** 5 (2 created, 3 modified)

## Accomplishments

- `SheetScaffold` gained `shape: Shape = BottomSheetDefaults.ExpandedShape` directly after `contentWindowInsets` and before `content` (sketch Y-13, D-01); every existing call site compiles unchanged. A defaulted call uses the same expression `ModalBottomSheet` itself defaults to, so it renders as in v2.5.1.
- `SheetScaffoldShapeTest` pins declaration order, default, by-name forwarding (`contentWindowInsets=contentWindowInsets,shape=shape)` once) and that exactly one `SheetScaffold` function exists (no overload/shim); a fifth case checks the gallery registry entry and both demo buttons.
- `SheetScaffoldShapeProbeTest` passed: a recording `Shape` is consulted by the real sheet under Robolectric, so a caller shape demonstrably reaches `ModalBottomSheet`.
- The Sheets gallery shows "SheetScaffold — custom shape (24dp top corners)" next to the generic button (14 added lines, 0 removed); the registry entry keeps tier PRIMITIVE and its four canonical state labels.

SHAPE_PROBE: PASS

## Task Commits

| Task | Commit | Lane | Message |
| ---- | ------ | ---- | ------- |
| 1 | 4043cdb | LANE 1 (additive, baseline v2.5.1) | feat(21-01): SheetScaffold shape parameter |
| 2 | 3ec011a | LANE 1 (additive, baseline v2.5.1) | test(21-01): recording shape probe through SheetScaffold |
| 3 | 9e21bc8 | LANE 1 (additive, baseline v2.5.1) | feat(21-01): shaped SheetScaffold gallery demo |

## Verification

- Task 1: `./gradlew apiDump testDebugUnitTest --tests SheetScaffoldShapeTest --tests OutcomeSheetTest --tests AlbumTitleConfirmSheetComposeTest --tests "explorer.*"` BUILD SUCCESSFUL. The contract test failed on the pre-change source first (3 of 4 red), then went green.
- Task 2: `SheetScaffoldShapeTest` (4/4) and `SheetScaffoldShapeProbeTest` (1/1) pass.
- Task 3: `./gradlew testDebugUnitTest --tests SheetScaffoldShapeTest --tests "explorer.*" detekt apiCheck` BUILD SUCCESSFUL; detekt reported 0 code smells at the zero baseline; `SheetScaffoldShapeTest` 5/5.
- api.txt removed-line count vs v2.5.1: `git diff --numstat v2.5.1 -- api.txt` = `84 2` (2 removed lines: Phase 20's DynamicActionButton line plus this plan's SheetScaffold line). The only change from this plan is the single rewritten `SheetScaffold(` line adding `optional androidx.compose.ui.graphics.Shape shape` before `content`.
- `SheetScaffold.kt` vs v2.5.1: exactly one removed line (the `contentWindowInsets = contentWindowInsets` forwarding line, re-added with a trailing comma); everything else is additive.
- `OutcomeSheetTest.kt` and `AlbumTitleConfirmSheetTest.kt` are byte-identical to v2.5.1.
- `config/detekt*` unchanged vs v2.5.1; nothing pushed or tagged; no device work; no pixel capture.

## Deviations from Plan

**1. [Rule 3 - Blocking] Experimental opt-in on the probe test** - Found during: Task 2 | Issue: the probe did not compile (`SheetScaffold` call site in the test flagged the Material experimental API) | Fix: added `@OptIn(ExperimentalMaterial3Api::class)` on the probe class | Files: `SheetScaffoldShapeProbeTest.kt` | Verification: both test classes pass | Commit: 3ec011a (fixed before the first commit of the file).

**2. [Tooling] Commit ledger not persisted** - The `gsd-plan-head-before-21-01` ledger file could not be written because the sandbox refuses compound `git rev-parse --git-dir` redirects. `plan_head_before` is the known worktree base (6014927) and `commits: 3` was measured with `git rev-list --count 6014927..HEAD` before this SUMMARY commit.

**Total deviations:** 2 (1 auto-fixed blocking, 1 tooling note). **Impact:** none on scope or behavior.

## Self-Check: PASSED

- Files exist: SheetScaffold.kt, SheetsFamilyScreen.kt, SheetScaffoldShapeTest.kt, SheetScaffoldShapeProbeTest.kt, api.txt.
- Commits 4043cdb, 3ec011a, 9e21bc8 exist on `worktree-agent-a3c4ea42163716853`.
- All task acceptance criteria and plan verification commands green.
