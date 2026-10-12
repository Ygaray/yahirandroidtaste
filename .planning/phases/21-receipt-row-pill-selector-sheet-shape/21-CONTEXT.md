# Phase 21: Receipt row, Pill selector & sheet shape - Context

**Gathered:** 2026-10-12
**Status:** Ready for planning

<domain>
## Phase Boundary

This phase adds a stateless, accessible `ReceiptRow`. It adds a Pill style and per-option disable to `SegmentedOptionSelector`, and a `shape` param to `SheetScaffold`. Default selector and sheet calls render exactly as in v2.5.1, including the G-15-1 long-label layout. Requirements: VROW-01, VROW-02, VSEG-01, VSHEET-01.

</domain>

<decisions>
## Implementation Decisions

### Signatures & compat
- **D-01 [signatures]:** The signatures are exactly those in `.planning/seams/v2.6.0-SKETCH.md` §2 Y-2, Y-11 and Y-13. — **Reversibility:** one-way — signed off by both consumers.
- **D-02 [compat]:** Source compatibility only (P1): no hidden overloads or shims.
  - `SegmentedOptionSelector`: params are appended after `disabledReason`.
  - `SheetScaffold.shape`: inserted before the trailing `content` lambda, never after it.
  - Regenerate `api.txt` in the same commit.

### row-overflow
- **D-03 [row-overflow]:** When the title and value collide, the value wins and the title ellipsizes. Verify at 360dp with a long German title. _(ai-auto)_
- **D-04 [row-layout]:** Summary layout:
  - A `Row` of a title column (weight 1f): title, subtitle, `supportingContent`, cue pill.
  - A value column: wraps, min 56dp, end-aligned, holding value and `valueCaption`.
  - A chevron, only when `onClick != null && trailingAction == null`. It rotates 0→180° in 200ms.
  - `trailingAction`.

  `expandedContent` renders full width below the summary, never as a Row sibling. Title, subtitle and value are single-line with ellipsis.

  Other specs:
  - **Cue pill:** full radius on `warnTone`, `bodySmall`, 4dp × 8dp padding, 24dp tall, after a 4dp gap.
  - **`dimmed`:** 38% alpha plus strikethrough on title and subtitle, with a 200ms fade.
  - **Collapsed height:** with all lines and no cue, 92dp ±1dp.
- **D-05 [row-tone]:** `ContentTone.Error` resolves through `ToneDefaults.error()`. There is no `errorTone` param.

### row semantics
- **D-06 [row-semantics]:** The summary root uses `semantics(mergeDescendants = true)`.
  - With `onClick`, it also gets `clickable(role = Role.Button, onClickLabel)` and a `stateDescription` from `expandedStateLabel`/`collapsedStateLabel`.
  - `trailingAction` and `expandedContent` stay outside the merge.
  - The row is stateless; single-open state belongs to the consumer.
  - Tags: `receipt_row_summary`, `receipt_row_trailing_action`, `receipt_row_expanded_content`.

### pill-impl
- **D-07 [pill-impl]:** Pill is a separate private Row-based branch, because M3 `SegmentedButton` forces the check icon and its shape.
  - **Container:** one full-radius container with a 1dp `outlineVariant` outline.
  - **Selected segment:** `primary`/`onPrimary`, no check icon.
  - **Labels:** single-line `bodyMedium` with ellipsis, 40dp tall.
  - **Default path:** the Material path stays byte-unchanged.

  _(ai-auto)_
- **D-08 [disabled-options]:** A segment in `disabledOptions` renders at 38% alpha, exposes `enabled = false` and never fires `onSelect`. This applies in both styles. The whole-control `enabled`/`disabledReason` behavior is unchanged. The KDoc warns that long localized labels truncate in Pill.

### row-family
- **D-09 [row-family]:** `ReceiptRow` registers in the Cards family. Its gallery demo holds single-open state in demo state, not in the row. _(ai-auto)_

### Claude's Discretion
- How the Material branch gets `disabledOptions` (per-segment `enabled`), as long as an empty set changes nothing.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Decision record & requirements
- `.planning/seams/v2.6.0-SKETCH.md` §2 Y-2, Y-11, Y-13
- `.planning/REQUIREMENTS.md` — VROW-01, VROW-02, VSEG-01, VSHEET-01
- `.planning/ROADMAP.md` § Phase 21
- `.planning/v2.6-DECISION-MAP.md` § Phase 21

### Consumer spec
- `/home/yahir/Projects/AndroidApps/Personal/CalTracker_Android/.planning/phases/76-gate-2-gap-closure-voice-sheets-piece-weight-grammar-header/76-UI-SPEC.md` § Y-2 (the full row anatomy and S1 math), Y-11, Y-13, Y-14; Single-line rules

### Prior lessons
- `.planning/milestones/v2.5-phases/15-*/` — the G-15-1 long-label fix (`SegmentedOptionSelector` `reserveSegmentIconSlot`, `IntrinsicSize.Min`, `maxLines = 2`). Its tests must pass unedited.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- Phase 20 provides `ContentTone`, `ToneColors` and `ToneDefaults.warn()`.
- `component/SegmentedOptionSelector.kt` (the v2.5.1 G-15-1 layout) and `component/SheetScaffold.kt`.

### Established Patterns
- Robolectric bounds/semantics tests; a German long-label test at w360dp (`ApproachLadderCardLongLabelTest`, `SegmentedOptionSelectorTest`).

### Integration Points
- The Phase 22 Receipt `OutcomeSheet` passes `RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)` to `SheetScaffold(shape)`, and CT uses `ReceiptRow` inside `topLevelContent`.

</code_context>

<specifics>
## Specific Ideas

None beyond the decisions above.

</specifics>

<deferred>
## Deferred Ideas

None. This phase stays within its scope.

</deferred>
