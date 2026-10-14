---
phase: 22-outcomesheet-receipt-style
plan: 03
subsystem: ui
tags: [compose, outcome-sheet, classic, parity, live-region, source-contract]

requires:
  - phase: 22-outcomesheet-receipt-style
    provides: "Widened state arms, OutcomeSheetColors (22-01); OutcomeSheet(style, colors) dispatch and OutcomeFixtures (22-02)"
provides:
  - "Classic Confirm honors confirmEnabled; Classic Warn failure paints colors.warnTone; Classic Failure.undo renders below the failure surface; refused undo surface is an Assertive live region"
  - "internal classicFailureSurfaceColors(tone, colors, scheme) pure seam"
  - "OutcomeFixtures success / failure / handledBy / undo / undoRows / refused / batchResults builders"
  - "Per-arm Classic tag and text inventories plus a v2.5.1-SHA-256 source contract"
affects: [22-04, 22-05, 24-docs]

actuals:
  tokens: 30000
  tasks: 2
  commits: 2
plan_head_before: e4221ac2c62d406876de3148bef335f4fba82ecd

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetClassicDeltasTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetLiveRegionTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetClassicParityTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetClassicParitySourceContractTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetFixtures.kt

key-decisions:
  - "The Warn seam is a pure internal function so container and content colors are unit-testable without pixel capture; the Error branch reads the scheme roles, never the OutcomeSheetColors error tone (different content color would change v2.5.1 pixels)"
  - "Classic ignores the Receipt-only Failure fields title, transcript, tip, secondaryAction and badge as well as the four D-06 fields (flagged below)"
  - "The live region sits on the refused Surface only, not merged; existing onNodeWithText queries still resolve"
  - "Source-contract pins are SHA-256 digests computed on the v2.5.1 text with a faithful port of SourceContractTestSupport.functionBody (literals would be 400-800 chars each)"

requirements-advanced: [VRCPT-01, VRCPT-02, VRCPT-03, VRCPT-04, VRCPT-05, VXR-01, VXR-02]

coverage:
  - id: D1
    description: "Disabled Classic Confirm never fires onConfirm while Cancel still fires; default fires once"
    requirement: VRCPT-02
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetClassicDeltasTest.kt#disabled classic confirm never fires onConfirm while cancel still fires"
        status: pass
    human_judgment: false
  - id: D2
    description: "Warn tone paints colors.warnTone (seam and LocalContentColor); Error keeps onErrorContainer"
    requirement: VRCPT-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetClassicDeltasTest.kt#classic Warn failure content color is the custom warn content color"
        status: pass
    human_judgment: false
  - id: D3
    description: "Classic Failure.undo renders below the failure surface with a 16dp inset; undo = null renders none"
    requirement: VXR-02
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetClassicDeltasTest.kt#classic Failure with undo renders the undo block below the surface with a 16dp inset"
        status: pass
    human_judgment: false
  - id: D4
    description: "Refused surface is an Assertive live region, exactly one node defines it, none when refused is null (Success and Failure)"
    requirement: VXR-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetLiveRegionTest.kt"
        status: pass
    human_judgment: true
  - id: D5
    description: "Defaulted call renders as v2.5.1: per-arm tag and text inventories derived from v2.5.1 hold for v2.5.1 arguments and with every Receipt-only field set; HandledByRow ignores prefixLabel and onDevice; unchanged functions equal their v2.5.1 SHA-256"
    requirement: VRCPT-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetClassicParityTest.kt"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetClassicParitySourceContractTest.kt"
        status: pass
    human_judgment: false

duration: 30min
completed: 2026-10-12
status: complete
---

# Phase 22 Plan 03: Classic deltas and parity proof Summary

**Classic gets exactly four behavior edits (confirm enabled, Warn tone, Failure.undo, Assertive live region) as one lane-2 commit, and everything else in Classic is pinned to v2.5.1 by per-arm inventories and SHA-256 source pins; `api.txt` is byte-identical.**

## Task Commits

| Task | Commit | Lane | Message |
| ---- | ------ | ---- | ------- |
| 1 (tracer) | c1d3cd5 | LANE 2, committed with `HUB_LANE_OVERRIDE=2` | feat(22-03): Classic confirm enabled, Warn tone, Failure.undo and Assertive live region |
| 2 | bdb9530 | LANE 1 | test(22-03): Classic parity inventories and the v2.5.1-derived source contract |
| SUMMARY | (this commit) | docs only | docs(22-03): summary |

Classifier output: task 1 `LANE 2 (mode=additive, baseline=v2.5.1)`, blocked until declared; task 2 `LANE 1 (mode=additive, baseline=v2.5.1)`.

## The lane-2 edit audit

`git diff --numstat v2.5.1 -- src/main/java/.../component/OutcomeSheet.kt` reports `57 8` (57 added, 8 removed). The eight removed lines are exactly the two from 22-02 plus the six this task named:

1. `    modifier: Modifier = Modifier` (22-02, gains a comma)
2. `private fun ProposedItemRow(item: ProposedItemUiModel) {` (22-02, now `internal`)
3. `        OutcomeSheetContent(outcome)` (now `OutcomeSheetContent(outcome, colors = colors)`)
4. `internal fun OutcomeSheetContent(outcome: VoiceOutcomeUiState, modifier: Modifier = Modifier) {` (split over lines with `colors` as the last defaulted parameter)
5. `                is VoiceOutcomeUiState.Failure -> FailureBody(outcome)` (now a block that adds the padded undo block)
6. `private fun FailureBody(failure: VoiceOutcomeUiState.Failure) {` (gains `colors: OutcomeSheetColors`)
7. `        color = MaterialTheme.colorScheme.errorContainer,` (now `surface.container`)
8. `        contentColor = MaterialTheme.colorScheme.onErrorContainer,` (now `surface.content`)

Pure additions: five imports, the `FailureBody` KDoc sentence, the `val surface = ...` line, `classicFailureSurfaceColors` plus its KDoc, the `.semantics { liveRegion = LiveRegionMode.Assertive }` line after the refused testTag, and `enabled = confirmation.confirmEnabled,` between `onClick` and `modifier` of the Confirm button. The `SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier) {` line is untouched. The existing KDoc line of `FailureBody` was kept verbatim; the new sentence is a separate line.

## Verification

- Task 1 command (focused classes, `OutcomeSheetTest`, `FailureRoleSourceContractTest`, `OutcomeSheetReceiptNeedsConfirmationTest`, `apiDump`, detekt, apiCheck): first run failed on one test-source compile error (`onAllNodes` was wrongly imported as a top-level function; it is a rule member), then BUILD SUCCESSFUL in 23s: deltas 9 tests, live region 5, `OutcomeSheetTest` 49 (unedited), 0 failures.
- Task 2 plan gate (`apiDump testDebugUnitTest detekt apiCheck`, full unfiltered suite): BUILD SUCCESSFUL in 1m 24s; 1070 tests, 22 skipped (pre-existing), 0 failures, 0 errors. Parity test 9 tests, source contract 15.
- `git diff --exit-code -- api.txt` after every `apiDump`: exit 0. `api.txt` was byte-identical in this plan (no public symbol changed; `OutcomeSheetContent` and `classicFailureSurfaceColors` are `internal`).
- `git diff --exit-code v2.5.1 -- OutcomeSheetTest.kt FailureRoleSourceContractTest.kt DataClassBinaryCompatShimTest.kt`: exit 0.
- No `captureToImage` in any new test; one `setContent` per test method; no gradle process left running.

## Inventories (derived from v2.5.1, not copied from HEAD)

The expected tag and ordered text constants were written from reading v2.5.1's `testTag` literals and `Text` calls (listed in the test KDoc) before the first run, and held on the first run. Source-contract digests were computed by piping `git show v2.5.1:.../OutcomeSheet.kt` through a line-for-line Python port of `stripComments` and `functionBody`; the Kotlin test then reproduced the same digests on HEAD, which also confirms the port is faithful. The extraction starts at `fun`, so no visibility is pinned (22-04 changes `UndoAffordanceBody` and `BatchResultsList` and appends its own pin).

## Decisions flagged for the owner

- **Flagged interpretation (D-06 scope):** D-06 names only `badge`, `titleActions` and the start/end actions as ignored by Classic. This plan ALSO has Classic ignore the Receipt-only Failure fields `title`, `transcript`, `tip`, `secondaryAction` and `badge`, reading "Classic keeps today's card" (Y-6). Proven by `Failure with every Receipt only field set renders the same inventories`. If the owner wants any of them in Classic (for example `Failure.title` replacing the headline), that is a follow-up lane-2 change.
- **A9 (Gate-2 item):** the Assertive live region is proven structurally (semantics key and value, exactly one node). What TalkBack actually speaks, and whether it speaks the refused text once when the surface appears, is a Gate-2 TalkBack read-out check on a device; nothing here asserts speech.
- **Classic Failure.undo is never locked:** a Failure has no `inFlight`, so the shared undo block is rendered with `locked = false`.

## Deviations from Plan

**1. [Process] Red step not run separately.** Tests and implementation were written, then one gradle invocation ran everything (one gradle at a time was the binding constraint; same as 22-01 and 22-02).

**2. [Tooling] Commit ledger not persisted.** The sandbox refuses compound `git rev-parse --git-dir` redirects (same as 22-01 and 22-02). `plan_head_before` is the worktree base e4221ac and `commits: 2` was measured with `git rev-list --count e4221ac..HEAD` before this SUMMARY commit.

**3. [Rule 1] Test compile fix.** `onAllNodes` was imported as a top-level function in `OutcomeSheetLiveRegionTest`; it is a `ComposeTestRule` member. Import removed.

**Total deviations:** none changing behavior or scope.

## Worktree note on the drift guards (flag for the orchestrator)

`ComponentRegistryDriftGuardTest` and `DomainVocabularyDriftGuardTest` walk up to the first directory named `yahirandroidtaste`, which is the MAIN checkout from this `agent-*` worktree, so their green result here says nothing about this worktree's sources. This plan adds no public top-level composable (`classicFailureSurfaceColors` is a non-composable `internal fun`; `OutcomeSheetContent` was already internal), so nothing is at risk and no registry or vocabulary allowlist edit is needed. The orchestrator's single full `./gradlew testDebugUnitTest` in main after merge is the real confirmation.

## Deferred

- README.md, API.md and catalog counts (Phase 24).
- Receipt Success and Failure bodies, the shared undo block as `internal` (22-04, 22-05).

## Self-Check: PASSED

- Files exist: four created and two modified files listed in the frontmatter.
- Commits c1d3cd5 and bdb9530 exist on `worktree-agent-aa5dc3190b0845012`.
- STATE.md and ROADMAP.md untouched; nothing pushed or tagged; no device touched.
