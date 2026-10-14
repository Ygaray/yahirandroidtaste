---
phase: 21-receipt-row-pill-selector-sheet-shape
plan: 03
subsystem: ui
tags: [compose, material3, receipt-row, semantics, api-surface]

requires:
  - phase: 21-receipt-row-pill-selector-sheet-shape
    provides: "21-01 and 21-02 lane-1 additive discipline, Dimens.SegmentedPill, api.txt baseline with 3 expected removed lines"
provides:
  - "public ReceiptRow(...) with the exact sketch Y-2 signature (D-01), stateless, no overload or shim"
  - "Dimens.ReceiptRow tokens (VerticalPadding, ColumnGap, LineGap, ValueMinWidth, CueHorizontalPadding, CueVerticalPadding, ChevronSize)"
  - "internal seams RECEIPT_ROW_MOTION_MS, RECEIPT_ROW_CHEVRON_EXPANDED_DEGREES, RECEIPT_ROW_DIMMED_ALPHA, rememberReceiptRowChevronRotation, rememberReceiptRowAlpha, receiptRowLine, receiptRowToneColor for plan 21-04"
  - "Cards registry entry ReceiptRow (PATTERN) with Default, Pressed / Selected, Disabled previews and ReceiptRowVariants; DOMAIN_VOCABULARY acknowledgment"
affects: [21-04-phase-gate, 22-receipt-outcome-sheet]

actuals:
  tokens: 42000
  tasks: 3
  commits: 3
plan_head_before: 91531768f3f26879ecb56ccfbc22f31e03677c5f

tech-stack:
  added: []
  patterns:
    - "Merge domain by nesting: merged clickable Row (weight 1f) is a sibling of the trailing action inside an outer summary Row"
    - "LineHeightStyle(Center, None) applied to row text only so a single line is exactly lineHeight tall"
    - "Private holder classes (ReceiptRowText, ReceiptRowPalette) keep private composables well under the parameter limit"

key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRow.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowLineHeightTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowSourceContractTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/theme/Dimens.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/CardsFamilyScreen.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/theme/DimensTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ToneFoundationInvariantsTest.kt
    - api.txt

key-decisions:
  - "R-01: ReceiptRow is Tier.PATTERN with an acknowledged DOMAIN_VOCABULARY entry; 'Receipt' is not added to the generic noun allowlist"
  - "R-03: receipt_row_chevron, receipt_row_value_column and receipt_row_cue are test-only tags documented as such in the KDoc"
  - "R-07: single-line text uses maxLines = 1 plus TextOverflow.Ellipsis, not softWrap = false"
  - "R-09: modifier order on the merged Row is tag, semantics, clickable, padding; trailingAction is the outer Row's second child"
  - "R-12: the dim alpha applies to the summary line (summary plus trailing action) only, not the divider or expanded content"
  - "Cue pill is a plain Box with background(CircleShape), not a Surface, so it adds no semantics of its own"

requirements-completed: [VROW-01, VROW-02]

coverage:
  - id: D1
    description: "Collapsed anatomy is 92dp with every line and no cue, 72dp without the supporting line, 120dp and 100dp with the cue; the cue pill is 24dp with a 4dp gap"
    requirement: VROW-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowTest.kt#tracer - a collapsed row with every line and no cue is 92dp tall"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowTest.kt#the cue pill is 24dp tall and sits 4dp below the supporting line"
        status: pass
    human_judgment: false
  - id: D2
    description: "A single line styled through receiptRowLine is exactly 24 / 20 / 16dp for titleMedium / bodyMedium / bodySmall"
    requirement: VROW-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowLineHeightTest.kt#single lines styled by receiptRowLine are as tall as their line height"
        status: pass
    human_judgment: false
  - id: D3
    description: "Chevron shows only when onClick is set and there is no trailing action; value column keeps a 56dp minimum and end alignment; expanded content is full width below the summary; divider adds 1dp"
    requirement: VROW-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowTest.kt#onClick without a trailing action shows one 24dp chevron"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowTest.kt#expanded content is full width and below the summary"
        status: pass
    human_judgment: false
  - id: D4
    description: "dimmed strikes through the title and subtitle but not the value; tones map Default, Muted, Warn, Error through the documented sources"
    requirement: VROW-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowTest.kt#a dimmed row strikes through the title and subtitle but not the value"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowTest.kt#a warn subtitle uses the warn tone and an error title uses the error tone"
        status: pass
    human_judgment: true
    rationale: "The 38% alpha value itself is a source-contract pin (no pixel capture); the rendered dim look is a Gate-1 device check owned by the tester. Timing is proven in plan 21-04."
  - id: D5
    description: "The summary is one merged node with Role.Button, the caller onClickLabel and the state labels; trailing action and expanded content are separate nodes and the trailing click never reaches the row"
    requirement: VROW-02
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowTest.kt#the trailing action is a separate node and its click never reaches the row"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowTest.kt#without onClick the summary has no role no state description and no click action"
        status: pass
    human_judgment: false
  - id: D6
    description: "Source contracts: exact Y-2 signature, six tags once each, modifier order, summary never mentions trailingAction or expandedContent, stateless, single-line rules"
    requirement: VROW-02
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowSourceContractTest.kt#the ReceiptRow declaration is the sketch Y-2 signature in order"
        status: pass
    human_judgment: false

duration: 35min
completed: 2026-10-12
status: complete
---

# Phase 21 Plan 03: ReceiptRow Summary

**Stateless, accessible `ReceiptRow` with the exact sketch Y-2 signature: a 92dp merged-button summary (title column, end-aligned value column, chevron, separate trailing action), full-width expanded content, a Cards registry entry and `api.txt`, all proven structurally.**

## Accomplishments

- `ReceiptRow(...)` added with the 21 parameters of D-01 in order; no overload, no shim, no state (D-02, D-06).
- Summary anatomy: outer `Row` holding a weighted merged `Row` (title column, value column, chevron) and, as its second child, the trailing action. The title column is the only weighted child, so the value is measured first and keeps its width; the title ellipsizes (D-03).
- One merged semantics node (`receipt_row_summary`); with `onClick` it is a `Role.Button` with the caller label and a `stateDescription` from the expanded or collapsed label; with `onClick = null` it has no role, state description or click action (D-06).
- `dimmed` fades the summary line to 38% through the `rememberReceiptRowAlpha` seam and strikes through the title and subtitle; the chevron rotates through `rememberReceiptRowChevronRotation`. Both seams and the constants are `internal` for plan 21-04's timing proof.
- Registry: `ReceiptRow` in Cards as `Tier.PATTERN`, previews for Default / Pressed-Selected (expanded) / Disabled (dimmed), Focused is N/A; seven-case `ReceiptRowVariants`; `DOMAIN_VOCABULARY["ReceiptRow"]`.
- `ToneFoundationInvariantsTest` now also scans `ReceiptRow.kt`, `SegmentedOptionSelector.kt` and `SheetScaffold.kt` (imports, color literals) and `ReceiptRow` for the declaration shape.

## Task Commits

| Task | Commit | Lane | Message |
| ---- | ------ | ---- | ------- |
| 1 | a273dea | LANE 1 (additive, baseline v2.5.1) | feat(21-03): ReceiptRow component, Cards entry and vocabulary acknowledgment |
| 2 | f78bd24 | LANE 1 (additive, baseline v2.5.1) | test(21-03): ReceiptRow line height, geometry, chevron matrix, tones, dimmed and expanded content |
| 3 | c96e05b | LANE 1 (additive, baseline v2.5.1) | test(21-03): ReceiptRow semantics, merge domain and source contracts |

No commit needed `HUB_LANE_OVERRIDE=2`; no committed `ReceiptRow.kt` line was rewritten after Task 1.

## Assumption outcomes and observed values

- A2: confirmed. With `LineHeightStyle(Center, None)` a single line is exactly `lineHeight` tall under NATIVE graphics at w360dp: titleMedium 24dp, bodyMedium 20dp, bodySmall 16dp (within 0.5dp). The per-Text `heightIn` fallback was not needed.
- A3: confirmed. `GetTextLayoutResult.layoutInput.style.color` carries the resolved color and `textDecoration` in this harness, so the tone colors and the strikethrough are asserted directly; the source-contract fallback was not needed.
- Collapsed summary heights (all within 1dp): 92 (all lines, no cue), 72 (no supporting line), 120 (all lines plus cue), 100 (no supporting line plus cue). Cue pill 24dp tall with a 4dp gap. Value column 56dp minimum. Chevron 24 x 24dp.
- PIXEL_PROBE: not run. No `captureToImage` anywhere in the plan; the dim alpha, container and cue colors are proven by source contract and text layout, not pixels.
- `git diff --numstat v2.5.1 -- api.txt` reports `111 3` (3 removed lines: DynamicActionButton, SheetScaffold, SegmentedOptionSelector). `ReceiptRow` added no removed line; `ReceiptRowKt` and `Dimens.ReceiptRow` are pure additions.
- Detekt: zero findings; `config/detekt-baseline.xml`, `config/detekt/detekt.yml` and `config/detekt-compose.yml` are unchanged against v2.5.1. `apiCheck` passes.

## Verification

- Task 1: `apiDump testDebugUnitTest` for `ReceiptRowTest`, `DimensTest` and `explorer.*`: BUILD SUCCESSFUL.
- Task 2: `ReceiptRowLineHeightTest` (1), `ReceiptRowTest` (22) and `DimensTest`: BUILD SUCCESSFUL.
- Task 3 gate (one invocation): `ReceiptRowTest` (31), `ReceiptRowLineHeightTest` (1), `ReceiptRowSourceContractTest` (9), `ToneFoundationInvariantsTest` (5), `explorer.*` (registry drift guard, states matrix, vocabulary guard, playground integrity, generated-symbol guard, tier test), `detekt` and `apiCheck`: BUILD SUCCESSFUL.
- Nothing pushed or tagged; no device work; `STATE.md` and `ROADMAP.md` untouched.

## Deviations from Plan

**1. [Process] Tracer red step skipped as a separate run.** The tracer tests were written before the component but I did not spend a gradle run on the expected compile failure (same as 21-02).

**2. [Tooling] Commit ledger not persisted.** The harness refuses writes to the shared git-dir path from the worktree, so no ledger file was written. `plan_head_before` is the known worktree base 9153176 and `commits: 3` was measured with `git rev-list --count 91531768..HEAD` before this SUMMARY commit.

**3. [Test fix] Compile errors in the first Task 3 run.** `SemanticsNode.isMergingSemanticsOfDescendants` is private (use `config.isMergingSemanticsOfDescendants`) and `getOrNull` needs its extension import; fixed in the test only (Rule 3).

**Total deviations:** 3 (none touched scope or behavior).

## Notes for plan 21-04 and Phase 24

- 21-04 appends the single-open accordion section to `ReceiptRowVariants()` (CardsFamilyScreen.kt) and adds motion, 360dp German and extreme-value tests; the seams and constants it needs are in place and covered by the source contract.
- README / API.md / `ComponentRegistry` KDoc counts are stale after this registration; deferred to Phase 24 as in Phase 20.

## Self-Check: PASSED

- Files exist: ReceiptRow.kt, Dimens.kt, CardsFamilyScreen.kt, DomainVocabularyDriftGuardTest.kt, DimensTest.kt, ToneFoundationInvariantsTest.kt, ReceiptRowTest.kt, ReceiptRowLineHeightTest.kt, ReceiptRowSourceContractTest.kt, api.txt.
- Commits a273dea, f78bd24, c96e05b exist on `worktree-agent-a2d948f8a3eb31452`.
- All task acceptance criteria and plan verification commands green.
