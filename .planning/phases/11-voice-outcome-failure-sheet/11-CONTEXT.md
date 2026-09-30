# Phase 11: Voice outcome & failure sheet - Context

**Gathered:** 2026-09-29
**Status:** Ready for planning

<domain>
## Phase Boundary

Deliver a domain-neutral outcome/failure sheet rendered from props: the command outcome, a "handled by: tier/approach" indicator, loud/visible failure states, and a generic undo affordance ("Undo all (N)" + per-item Undo with an unavailable state, plus a loud undo-refused/partial state).

</domain>

<decisions>
## Implementation Decisions

Resolved in `ai` mode (`source: ai-auto`). The **[undo-crossrepo]** decision is a cross-repo item flagged for the R1 reconvene.

### undo-shape
- **D-01 [undo-shape]:** Model the VUNDO-01 (A18) undo affordance as a NEW all-`val` undo UI model — a list of per-item entries (id + label + status incl. an `Unavailable(reason)` case), a derived "Undo all (N)" where N = count of currently-undoable items, and `onUndoItem(id)`/`onUndoAll()` callbacks. Do NOT reuse `UndoHistoryEntry`/`UndoHistoryStore` (internal constructor + `suspend` lambda + `AtomicBoolean` guard make them unusable as a prop, and they lack the entangled/unavailable concept). — **Reversibility:** one-way — the undo prop shape freezes into the `v2.4.0` API.

### undo-placement
- **D-02 [undo-placement]:** Put the undo affordance as a field on the `Success` outcome + model the undo-refused/partial state as a nested undo substate — NOT a new top-level arm of the sealed `VoiceOutcomeUiState`. This keeps the top-level sealed type stable so Phase 12's `NeedsConfirmation` stays a one-branch additive add.

### undo-crossrepo
- **D-03 [undo-crossrepo]:** Validate the per-item `Unavailable(reason)` + top-level `Refused(reason, changedItem)` union against SecondBrain's and CalTracker's real undo call-sites at the A13 reconvene BEFORE authoring — a post-tag reshape is a breaking library change. _(cross-repo — reconvene item)_

### Claude's Discretion
Loud-failure and undo-refused visual treatment: research is confident (not a gray area) — use the theme `error`/`errorContainer` roles (icon + headline + reason string, sticky), and explicitly NOT `AttentionCue` (its KDoc forbids use as a failure signal).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone planning
- `.planning/ROADMAP.md` § Phase 11 — goal + 5 success criteria (incl. undo)
- `.planning/REQUIREMENTS.md` — VOUT-01, VOUT-02, VOUT-03, VUNDO-01 (A18)
- `.planning/v2.4-DECISION-MAP.md` § Phase 11 — the decisions above

### Research
- `.planning/research/ARCHITECTURE.md` — sealed `VoiceOutcomeUiState`; host/content split
- `.planning/research/FEATURES.md` — loud-failure UX; provenance indicator
- `.planning/research/PITFALLS.md` — silent-failure trap; additive-ABI

### Cross-repo contract
- `~/Projects/Reusable/android/voice-action-engine/CROSS-REPO-SCOPE-CONTRACT.md` §6.3 + A2/A18
- `.planning/cross-repo/HANDOFF.md`
- `CLAUDE.md` (root) — invariants, CATALOG-03

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `component/SheetScaffold.kt`: chrome-only sheet shell (hands `content` a `ColumnScope`, stays open on error) — the host for the outcome sheet.
- `theme/Color.kt` error roles (`ErrorRed`/`ErrorRedContainer`/`OnErrorRedContainer`, light+dark) for loud failures.
- `modifier/SwipeableActionRow.kt`, `modifier/RevealActionRow.kt` — reveal/undo interaction precedents.

### Established Patterns
- `component/AttentionCue.kt`: KDoc explicitly forbids using it as a failure signal — use error roles instead.
- Host + content split precedents: `component/RecordingBottomSheetContent.kt`, `component/ListCardBottomSheet.kt`.
- Sealed UI-state param so Phase 12 can extend additively (a `NeedsConfirmation` subtype).

### Integration Points
- Register the sheet in `ComponentRegistry` (CATALOG-03), full 4-cell states matrix, tier `PATTERN`.

</code_context>

<specifics>
## Specific Ideas

Loud failures are load-bearing for this repo's owner (failures must be visible, never swallowed) — Gate-1 self-UAT must drive the failure and undo-refused branches, not just the happy path.

</specifics>

<deferred>
## Deferred Ideas

The `NeedsConfirmation` state itself is Phase 12 (extends this sheet's sealed state).

</deferred>

---

*Phase: 11-voice-outcome-failure-sheet*
*Context gathered: 2026-09-29*
