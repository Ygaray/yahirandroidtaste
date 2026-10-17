# Phase 26: Receipt-card stacked snackbar deck (UI) - Context

**Gathered:** 2026-10-17
**Status:** Ready for planning
**Mode:** ai (every gray area took research's recommendation, Source: ai-auto; recommendations were set by the ui-designer seat that owns YAT)

<domain>
## Phase Boundary

Receipt-card stacked snackbar deck (UI) — milestone v2.8 (release v2.8.0, additive only). FSTK-02..06, A11Y-01. Deck and card are internal; the FeedbackStackHost signature from Phase 25 doesn't change. Install anchors before any requireOffset (the SwipeableActionRow first-frame crash keeps 14 card tests @Ignored).

</domain>

<decisions>
## Implementation Decisions

Locked milestone decisions D-01..D-17 live in `.planning/research/v2.8-BRIEF.md` and apply here unchanged. The phase decisions below are numbered P26-D-xx in prose but use the parser shape.

### card-layout
- **D-01 [card-layout]:** wrap + capped Undo (the Receipt action-row lesson), measured heights, clamp collapsed back cards to the front card's height

### undo-button
- **D-02 [undo-button]:** DynamicActionButton Text emphasis (the Receipt bar's own text action)

### long-hold
- **D-03 [long-hold]:** suppress; pause-on-touch exists so the user can hold to read

### pause-scope
- **D-04 [pause-scope]:** whole deck, reason set {pointer, fanned}, 800 ms floor

### swipe
- **D-05 [swipe]:** anchoredDraggable, front card only when collapsed, every card when fanned; threshold ~72dp or fling

### peek-target
- **D-06 [peek-target]:** separate pointer and semantics paths (MicButton pattern); peek strip ≥ 24dp visual, 48dp touch via expanded hit area

### live-region
- **D-07 [live-region]:** only the arriving card announces; keep both Dismiss forms and verify on device in Phase 28 (designer decides if TalkBack lists two)

### fanned-overflow
- **D-08 [fanned-overflow]:** cap at ~60% of available height, scroll inside; host respects IME + nav insets

### Claude's Discretion
- Internal naming, file split and test layout, within the hub conventions (CLAUDE.md, docs/STYLE.md).

</decisions>

<canonical_refs>
## Canonical References

- .planning/ROADMAP.md (§ Phase 26)
- .planning/REQUIREMENTS.md
- .planning/v2.8-DECISION-MAP.md (§ Phase 26) — source of the decisions below
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
