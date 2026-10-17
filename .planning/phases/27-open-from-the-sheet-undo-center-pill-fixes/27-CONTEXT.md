# Phase 27: Open from the sheet & Undo Center, pill fixes (UI) - Context

**Gathered:** 2026-10-17
**Status:** Ready for planning
**Mode:** ai (every gray area took research's recommendation, Source: ai-auto; recommendations were set by the ui-designer seat that owns YAT)

<domain>
## Phase Boundary

Open from the sheet & Undo Center, pill fixes (UI) — milestone v2.8 (release v2.8.0, additive only). FOPEN-01..03, POL-01, POL-02. Classic SuccessBody/HandledByRow/BatchResultsList are SHA-pinned by OutcomeSheetClassicParitySourceContractTest: put go-to-item in NEW functions and keep `OutcomeSheetContent(outcome, colors = colors)` literal. Use an internal OpenTargetRow, never ReceiptRow (it announces Expanded/Collapsed).

</domain>

<decisions>
## Implementation Decisions

Locked milestone decisions D-01..D-17 live in `.planning/research/v2.8-BRIEF.md` and apply here unchanged. The phase decisions below are numbered P27-D-xx in prose but use the parser shape.

### sheet-row-content
- **D-01 [sheet-row-content]:** add `targetLabel`/`targetDetail` (amends D-15); falls back to summary

### row-placement
- **D-02 [row-placement]:** after the handled-by indicator, both styles (matches the mock)

### batch-rows
- **D-03 [batch-rows]:** whole list switches; rows without target get no chevron, no click, no reserved space

### undo-center-tap
- **D-04 [undo-center-tap]:** onTap + no-op onLongPress, merged Role.Button node; required-param overload; update the count contract deliberately _(provisional — refresh at execution; depends on Phase 25)_

### sheet-hide
- **D-05 [sheet-hide]:** callback only

### pill-shape
- **D-06 [pill-shape]:** capped-radius shape, Receipt pill only; Classic HandledByRow untouched

### dot-contrast
- **D-07 [dot-contrast]:** contrast ring; ToneDefaults.warn() unchanged; proven on a synthetic white-on-light scheme in a unit test

### Claude's Discretion
- Internal naming, file split and test layout, within the hub conventions (CLAUDE.md, docs/STYLE.md).

</decisions>

<canonical_refs>
## Canonical References

- .planning/ROADMAP.md (§ Phase 27)
- .planning/REQUIREMENTS.md
- .planning/v2.8-DECISION-MAP.md (§ Phase 27) — source of the decisions below
- .planning/research/v2.8-BRIEF.md — LOCKED decisions D-01..D-17 (Yahir 2026-10-17 + designer D-13..D-17); they bind every phase
- .planning/research/SUMMARY.md, .planning/research/ARCHITECTURE.md, .planning/research/PITFALLS.md, .planning/research/STACK.md, .planning/research/FEATURES.md
- `docs/design/snackbar-go-to-item/index.html — the approved mock (direction A)`
- `docs/STYLE.md, docs/STYLE.REVERTED.md — the hub's accepted look and rejected ideas`
- `API.md, CLAUDE.md (source-compat rules, ComponentRegistry, detekt zero-baseline)`

</canonical_refs>

<code_context>
## Existing Code Insights

Cited per decision in `.planning/v2.8-DECISION-MAP.md` and the research files (feedback/*.kt, component/OutcomeSheet*.kt, feedback/UndoCenterScreen.kt, modifier/SwipeableActionRow.kt, explorer/*). Concrete file:line maps are surfaced at plan time.

</code_context>

<specifics>
## Specific Ideas

Match the approved mock (direction A) and docs/STYLE.md; Yahir's picks: keep the › chevron on openable cards, pause timers while touched or fanned.

</specifics>

<deferred>
## Deferred Ideas

999.4 voice-surface polish → v2.8.1; combined "Undo & open"; share/MCP deep links; SB sheet-swap blink (app-side).

</deferred>

## Runtime Decisions

- **D-04 [undo-center-tap] refreshed after Phase 25 (ai-auto):** onTap + no-op onLongPress, merged Role.Button node; required-param overload; update the count contract deliberately. Refreshed against Phase 25: a row is openable iff entry.openableTarget != null (Phase 25 derives it null unless Available, so Undone/Failed rows lose the chevron and click); grouped entries carry targets through the Phase 25 append overload, and a group header itself never opens.
- **FA-1 / FA-5 confirmed (ui-designer, 2026-10-17):**
  - FA-1: a target row is dimmed and inert while the sheet is inFlight, the same as the sheet's other actions.
  - FA-5: a long hold in the Undo Center never opens the item but still shows the peek.
  - Internal source-line rewrites that forward the new defaulted params are declared lane 2 (as in Phase 26). The public API stays additive (apiCheck and verify-api-source-additive v2.7.1).
