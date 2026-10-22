# Phase 25: Feedback target model & stack state - Context

**Gathered:** 2026-10-17
**Status:** Ready for planning
**Mode:** ai (every gray area took research's recommendation, Source: ai-auto; recommendations were set by the ui-designer seat that owns YAT)

<domain>
## Phase Boundary

Feedback target model & stack state — milestone v2.8 (release v2.8.0, additive only). FTGT-01, FTGT-02, FSTK-01, FSTK-07. Guards to update in the same commit: ComponentRegistry (FeedbackStackHost as PATTERN), INTENTIONALLY_UNREGISTERED (rememberFeedbackStackState), DomainVocabularyDriftGuardTest PRIMITIVE_NOUN_ALLOWLIST ("Feedback"), API.md/README.md registry counts (CatalogDocsDriftGuardTest). Add a V271 call-site compile canary.

</domain>

<decisions>
## Implementation Decisions

Locked milestone decisions D-01..D-17 live in `.planning/research/v2.8-BRIEF.md` and apply here unchanged. The phase decisions below are numbered P25-D-xx in prose but use the parser shape.

### event-shape
- **D-01 [event-shape]:** add `supportingText: String? = null` (last param) to Notify and WithUndo in Phase 25; no tone field; dot = primary with a target, onSurfaceVariant without

### target-validation
- **D-02 [target-validation]:** KDoc rule plus render-time fallback to `message`; no throwing init (hub types don't throw on bad input)

### entry-target
- **D-03 [entry-target]:** `val target: FeedbackTarget? = null` appended after `consumedGuard`, plus `openableTarget` non-null only while Available; new `store.append` overload with a required `target`

### result-type
- **D-04 [result-type]:** enum (matches SnackbarResult / UndoStatus; sealed buys nothing for exhaustive whens)

### cancellation
- **D-05 [cancellation]:** remove + rethrow; tracked entry stays Available in the store

### timer-owner
- **D-06 [timer-owner]:** inside `show()` (virtual-time testable); starts at enqueue; Indefinite never times out; the host also pushes `LocalAccessibilityManager` scaling into the state so a manually constructed state still scales

### programmatic-dismiss
- **D-07 [programmatic-dismiss]:** add `dismissAll()`; additive and cheap

### spent-card
- **D-08 [spent-card]:** keep the card, hide Undo + chevron; a losing Undo tap still resolves Undone (store CAS no-op)

### undo-cancel
- **D-09 [undo-cancel]:** `withContext(NonCancellable)` around the store undo on the new path only

### Claude's Discretion
- Internal naming, file split and test layout, within the hub conventions (CLAUDE.md, docs/STYLE.md).

</decisions>

<canonical_refs>
## Canonical References

- .planning/ROADMAP.md (§ Phase 25)
- .planning/REQUIREMENTS.md
- .planning/v2.8-DECISION-MAP.md (§ Phase 25) — source of the decisions below
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
