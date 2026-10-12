---
phase: 22-outcomesheet-receipt-style
plan: 02
subsystem: ui
tags: [compose, outcome-sheet, receipt, tracer, pinned-bar, metalava]

requires:
  - phase: 22-outcomesheet-receipt-style
    provides: "OutcomeSheetStyle, OutcomeSheetColors, OutcomeSheetDefaults, Dimens.OutcomeReceipt, widened state arms (22-01)"
  - phase: 21-receipt-row-pill-selector-sheet-shape
    provides: "SheetScaffold(shape)"
provides:
  - "OutcomeSheet(style, colors) with the early-return Receipt dispatch"
  - "OutcomeSheetReceipt.kt: 24dp-corner frame, title row, badge, transcript, NeedsConfirmation body and the pinned Text/Filled bar"
  - "Pure seams outcomeBadgeIcon / outcomeBadgeColors; ProposedItemRow made internal"
  - "OutcomeFixtures shared test builders"
affects: [22-03, 22-04, 22-05, 24-docs]

actuals:
  tokens: 22000
  tasks: 3
  commits: 4
plan_head_before: 873e7313ef422a36a8914fecd6ffef5237b972da

key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceipt.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetFixtures.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptNeedsConfirmationTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptFrameTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptBarGeometryTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptLongLabelTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptSourceContractTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ToneFoundationInvariantsTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetSourceCompatV251Test.kt
    - api.txt

key-decisions:
  - "Colors are threaded as explicit parameters, not a CompositionLocal (keeps the internal test seams trivial)"
  - "P7: the literal Spacer(weight 1f) bar failed the long German 360dp test, so the documented escape hatch was applied (both slots weight(1f, fill = false), SpaceBetween, no Spacer); a lone end action uses Arrangement.End"
  - "No Explorer Receipt demo in Phase 22 (OQ-4); demos are Phase 24"

requirements-completed: [VRCPT-01, VRCPT-02]
requirements-advanced: [VTAG-01]

coverage:
  - id: D1
    description: "OutcomeSheet(NeedsConfirmation, style = Receipt) renders title, transcript, items and the pinned bar; confirm fires once, cancel fires once, a disabled confirm never fires while cancel still does; Classic and the 3-arg call have no bar or transcript"
    requirement: VRCPT-02
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptNeedsConfirmationTest.kt"
        status: pass
    human_judgment: false
  - id: D2
    description: "At 384dp the cancel starts at 16dp and the confirm ends at 368dp; the 1dp divider sits above both buttons; a 240dp host with a tall body keeps both buttons displayed and at least 40dp tall"
    requirement: VRCPT-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptBarGeometryTest.kt"
        status: pass
    human_judgment: false
  - id: D3
    description: "Long German Cancel/Confirm labels at 360dp keep both buttons inside the window with no ellipsized line; a 120-character title wraps to 2 lines ending ellipsized in 18sp/24sp SemiBold"
    requirement: VRCPT-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptLongLabelTest.kt"
        status: pass
    human_judgment: false
  - id: D4
    description: "Title row slots, transcript rule, items/top-level content, Success and Failure title rows and the badge icon/hue seams"
    requirement: VTAG-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptFrameTest.kt"
        status: pass
    human_judgment: false
  - id: D5
    description: "Dispatch order (guard, call, return, untouched legacy SheetScaffold line), exactly one public OutcomeSheet, shape passed to SheetScaffold, bar after the weighted body, no public declaration or resource-id flag in the Receipt file"
    requirement: VRCPT-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptSourceContractTest.kt"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ToneFoundationInvariantsTest.kt"
        status: pass
    human_judgment: false

duration: 45min
completed: 2026-10-12
status: complete
---

# Phase 22 Plan 02: OutcomeSheet Receipt dispatch and the NeedsConfirmation tracer Summary

**`OutcomeSheet(style = Receipt)` now renders a NeedsConfirmation end to end (24dp frame, title row, transcript, shared item rows, 1dp divider and a pinned Text/Filled bar) with real click, 384dp-edge and long-German proofs; Classic is untouched.**

## Accomplishments

- `OutcomeSheet` gained `style` and `colors` (appended after `modifier`, defaulted), an early-return dispatch above the textually unchanged legacy `SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier) {` line, and `ProposedItemRow` became `internal`. No hidden overload, no shim.
- `OutcomeSheetReceipt.kt` holds only `internal` and `private` declarations (no registry or vocabulary edit needed): `OutcomeSheetReceipt`, `OutcomeSheetReceiptContent`, `outcomeBadgeIcon`, `outcomeBadgeColors`, and private `ReceiptTitleRow`, `ReceiptBadge`, `ReceiptTranscript`, `ReceiptNeedsConfirmationBody`, `ReceiptNeedsConfirmationBar`, `ReceiptActionBar`, `ReceiptSuccessBody`, `ReceiptFailureBody`, `barArrangement`.
- The bar is a sibling after the `weight(1f, fill = false)` scroll body, so it is never starved (240dp host test).
- The Success and Failure Receipt bodies render **only the shared title row** until plans 22-04 and 22-05 append their remaining sections to those same composables; the Failure bar branch is `Unit` until 22-05.

## Task Commits

| Task | Commit | Lane | Message |
| ---- | ------ | ---- | ------- |
| 1 (tracer) | c483729 | LANE 2, committed with `HUB_LANE_OVERRIDE=2` | feat(22-02): OutcomeSheet Receipt style dispatch and the Receipt NeedsConfirmation frame |
| 2a (P7 fix) | 658de68 | LANE 2, committed with `HUB_LANE_OVERRIDE=2` | fix(22-02): Receipt action bar slots share the row so long German labels stay in the window |
| 2b | 4f17851 | LANE 1 | test(22-02): Receipt bar edges, frame slots and long German labels |
| 3 | 30f3d65 | LANE 1 | test(22-02): Receipt dispatch and frame source contracts |
| SUMMARY | (this commit) | docs only | docs(22-02): summary |

Classifier output for task 1 and 2a: `LANE 2 (mode=additive, baseline=v2.5.1)`; tasks 2b and 3: `LANE 1`.

## Removed-line audits

- `api.txt` (`git diff -U0`): exactly one removed line, the old 3-parameter `OutcomeSheet(` signature (line 321), with the rewritten line (adds `optional ...OutcomeSheetStyle style, optional ...OutcomeSheetColors colors`) as its counterpart. Nothing else changed.
- `OutcomeSheet.kt` vs `v2.5.1` (`--numstat` 12 added, 2 removed): the removed lines are the `modifier: Modifier = Modifier` parameter line (gains a comma) and the `private fun ProposedItemRow(` declaration line (now `internal`). Nothing else.
- Protected tests (`OutcomeSheetTest`, `FailureRoleSourceContractTest`, `DataClassBinaryCompatShimTest`) are byte-identical to `v2.5.1` (`git diff --exit-code v2.5.1` exit 0).
- `git diff --exit-code -- api.txt` after the final dump: exit 0.

## Outcomes recorded for later plans

- **P7: escape hatch used.** The literal D-05 row (`start`, `Spacer(weight 1f)`, `end`) failed `OutcomeSheetReceiptLongLabelTest` for the long German labels at 360dp (the long start label took the row and squeezed the end action). Both slots are now wrapped in `Box(Modifier.weight(1f, fill = false))` with `Arrangement.SpaceBetween` (a lone end action uses `Arrangement.End`, a lone start `Arrangement.Start`) and no `Spacer`. The short-label 16dp / 368dp edges still hold at 384dp. The Task 3 source contract asserts the escape-hatch shape (two `weight(1f, fill = false)` slots, `barArrangement`, no `Spacer(` inside `ReceiptActionBar`). Plans 22-04 and 22-05 reuse `ReceiptActionBar` with nullable `start`/`end` and get this behavior for free.
- **A3: confirmed.** `layoutInput.style` is readable from the title node's `TextLayoutResult` in this harness (18sp, 24sp, SemiBold asserted in `OutcomeSheetReceiptLongLabelTest`); no fallback to the source contract was needed.

## Cosmetic choices for the Phase 24 screencap review

- When no bar follows the scroll body (Success and Failure for now), the body gets 16dp bottom padding (`Dimens.HorizontalPadding`); with a bar it gets 0.
- When the title is null but `titleActions` exist, the actions stay end-aligned through a `Spacer(Modifier.weight(1f))` in the title row (this Spacer is in the title row only, not in the action bar).
- The NeedsConfirmation reversibility hint keeps the Classic `labelSmall` `onSurfaceVariant` look.
- Pure-badge hue rules are unit-tested (`outcomeBadgeColors`); no pixel capture was used anywhere.

## Verification

- Task 1 command (`apiDump`, tracer + `OutcomeSheetTest` + `FailureRoleSourceContractTest` + `VoiceI18nSourceCompatTest`, `detekt`, `apiCheck`): BUILD SUCCESSFUL in 58s; tracer class 6 tests, 0 failures.
- Task 2 command: first run failed only `long German bar labels keep both buttons inside the window` (P7, as the plan predicted); after the escape hatch BUILD SUCCESSFUL in 23s.
- Task 3 plan gate (`apiDump testDebugUnitTest detekt apiCheck`, full unfiltered suite): BUILD SUCCESSFUL in 1m 17s; 1032 tests, 22 skipped (pre-existing), 0 failures, 0 errors.
- No `captureToImage` in any new test; no `testTagsAsResourceId`, no `Color(0x` in `src/main`.

## Deviations from Plan

**1. [Process] Tracer red step not run separately.** Tests and implementation were written, then one gradle run executed the lot (one gradle at a time was the binding constraint; same as 22-01).

**2. [Plan contingency] P7 escape hatch applied**, committed separately as lane 2 (see above). This is the plan's own pre-planned branch, not an unplanned deviation.

**3. [Tooling] Commit ledger not persisted.** The sandbox refuses compound `git rev-parse --git-dir` redirects (same as 22-01). `plan_head_before` is the worktree base 873e731 and `commits: 4` was measured with `git rev-list --count 873e731..HEAD` before this SUMMARY commit.

**4. [Scope] `ToneFoundationInvariantsTest.phaseFiles`:** the previous last entry gained a trailing comma (one rewritten line) while the two new paths were appended; no other line changed.

**Total deviations:** none changing behavior or scope.

## Worktree note on the drift guards (flag for the orchestrator)

`ComponentRegistryDriftGuardTest` and `DomainVocabularyDriftGuardTest` walk up to the first directory named `yahirandroidtaste`, which is the MAIN checkout from this `agent-*` worktree, so their green result here says nothing about this worktree's sources. This plan adds no public top-level composable (every Receipt declaration is `internal` or `private`, and `OutcomeSheetReceipt.kt` has no column-0 `fun`), so nothing is at risk. `ToneFoundationInvariantsTest` and `OutcomeSheetReceiptSourceContractTest` resolve sources relative to the project directory and did run against this worktree. The orchestrator's single full `./gradlew testDebugUnitTest` in main after merge is the real confirmation.

## Self-Check: PASSED

- Files exist: the seven created and four modified files listed in the frontmatter.
- Commits c483729, 658de68, 4f17851 and 30f3d65 exist on `worktree-agent-a3b2f946e328cdc69`.
- STATE.md and ROADMAP.md untouched; nothing pushed or tagged; no device touched; no gradle process left running.
