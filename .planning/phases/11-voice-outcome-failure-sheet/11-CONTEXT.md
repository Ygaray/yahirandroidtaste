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
- **D-01 [undo-shape]:** **REVISED at R1 by SecondBrain (the seam owner).** SB's Undo Center already IS this repo's `UndoHistoryStore`/`UndoHistoryEntry` (`feedback/`; SB `UndoCenterViewModel.kt`). So the A18 run-level undo shape must EXTEND that existing entry model ADDITIVELY — e.g. an optional run/group id + group status — NOT a new parallel store. The outcome sheet's undo affordance ("Undo all (N)" + per-item Undo) renders a group's entries and emits undo callbacks. "Undo all (N)" counts ONLY items undoable as part of the group; notify-only mutations (e.g. SB edits) are excluded or shown as `Unavailable("can't be undone")`. The existing per-action snackbar `WithUndo` path MUST keep working unchanged (SB stays on it until its Phase 178). — **Reversibility:** one-way — the additive entry-model extension freezes into the `v2.4.0` API. **Plan-time tension:** the current `UndoHistoryEntry` has an `internal` ctor + `suspend` lambda + `AtomicBoolean` (not a clean Compose-`STABLE` prop model), so the additive extension must expose a presentational, `STABLE`-friendly projection for the sheet WITHOUT breaking the entry's first-consumer-wins invariant — resolve in the plan. **CT (R1):** CT's voice writes are always CREATEs, so undo = delete the inserted row; a loud `Refused(reason)` suffices and CT needs NO restore payload — make `changedItem` OPTIONAL. _(R1 — SB + CT shape answers)_

### undo-placement
- **D-02 [undo-placement]:** Put the undo affordance as a field on the `Success` outcome + model the undo-refused/partial state as a nested undo substate — NOT a new top-level arm of the sealed `VoiceOutcomeUiState`. This keeps the top-level sealed type stable so Phase 12's `NeedsConfirmation` stays a one-branch additive add.

### undo-crossrepo
- **D-03 [undo-crossrepo]:** Validate the per-item `Unavailable(reason)` + top-level `Refused(reason, changedItem)` union against SecondBrain's and CalTracker's real undo call-sites at the A13 reconvene BEFORE authoring — a post-tag reshape is a breaking library change. _(cross-repo — reconvene item)_

### handled-by
- **D-04 [handled-by]:** The "handled by" UI model carries a REQUIRED tier label + OPTIONAL approach, provider, model, and escalation count — all optional beyond the tier so the shape grows additively. Apps fill these from VAE's committed TEL-01 `CommandTrace` (per-tier attempts, escalation reasons, provider/model). _(R1 orchestrator change 3)_

### success-editable
- **D-05 [success-editable]:** The sealed outcome type needs a committed-but-EDITABLE `Success` state — CT auto-logs a high-confidence single item, then allows in-place edits AFTER commit — distinct from the needs-confirmation (pre-commit) state. Model it as a `Success` variant that can carry an editable payload + edit callbacks (prop-driven; CT injects the editor). _(R1 — CT VoiceResultSheet.kt / VoiceLogUiState.kt)_

### batch-results
- **D-06 [batch-results]:** For batch outcomes, the sheet renders per-row result reporting — a partial-success summary ("Logged 2 of 3") from a `failedCount` / per-row `success|fail` status — plus a batch-write-in-flight LOCK state (disable actions while the write is running). All prop-driven. _(R1 — CT VoiceResultSheet.kt / VoiceLogUiState.kt)_

### failure-action
- **D-08 [failure-action]:** The Failure state carries an OPTIONAL action slot — a label + callback (e.g. "Open Settings" for a missing/invalid key; "Retry" ONLY when the app says the failure is retry-safe). Prop-driven and optional: the app decides whether and what to show; absent → no action rendered. Keeps VOUT-03 loud AND actionable without the library assuming any action is always safe. _(R1 — SB brief, 2026-09-30)_

### clarify-choices
- **D-07 [clarify-choices]:** A generic, prop-driven "clarification choices" composable (VCLAR-01, contract **A19**) — question text + a list of options, each `{ id: opaque String, label: String }`, + `onSelect(id)` + `onDismiss` (dismiss = cancel). When the model needs clarification ("Which list?"), the user resolves it by TAPPING an option, never by speaking again. Render it as a compact PRESSABLE choice surface (chips/buttons) — a Material snackbar holds only one action, so use a small choice bar or an outcome-sheet state, **your design call, easy to swap**. Visually informative, NOT an error. Apps map the engine's `Clarification` → these props (no engine dependency, L7). Domain-neutral; registered in `ComponentRegistry` (Voice Command family) with a full states matrix. _(§6.3, A19 — Yahir 2026-09-30, VAE 725d8d7)_

### Claude's Discretion
Loud-failure and undo-refused visual treatment: research is confident (not a gray area) — use the theme `error`/`errorContainer` roles (icon + headline + reason string, sticky), and explicitly NOT `AttentionCue` (its KDoc forbids use as a failure signal). For VCLAR-01, chips-vs-buttons and bar-vs-sheet-state is a swappable design call (Yahir reviews in the gallery at Gate-1).

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
