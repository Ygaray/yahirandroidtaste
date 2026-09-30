# Phase 12: Generic needs-confirmation state - Context

**Gathered:** 2026-09-29
**Status:** Ready for planning

<domain>
## Phase Boundary

Deliver ONE domain-neutral needs-confirmation state, rendered inside the Phase 11 outcome sheet, that covers both SecondBrain's risk confirm and CalTracker's weak-match single/batch confirm from props — a reason string, single-or-batch proposed item(s), and confirm/cancel — with no per-consumer library change.

</domain>

<decisions>
## Implementation Decisions

Resolved in `ai` mode (`source: ai-auto`). **[additivity]** is provisional (depends on Phase 11); **[confirm-crossrepo]** is a reconvene item.

### per-item-metadata
- **D-01 [per-item-metadata]:** Carry the per-item metadata that differs between consumers (SB risk tier vs CT match score) as an OPAQUE presentational per-item slot (e.g. `trailingContent: @Composable () -> Unit`, or a nullable/defaulted field the library never interprets) — never a library-defined risk/score/severity enum. — **Reversibility:** one-way — the confirm prop shape freezes into the `v2.4.0` API; a library enum would leak one consumer's domain (drift-guard RED).

### additivity
- **D-02 [additivity]:** Phase 12 is purely additive — a new `NeedsConfirmation` subtype of the sealed `VoiceOutcomeUiState` + one `when` branch, reusing `SheetScaffold` + `DynamicActionButton` — CONTINGENT on Phase 11 having shipped the sealed `VoiceOutcomeUiState` param. Verify that seam exists before planning. _(provisional — refresh at execution; depends on Phase 11)_

### confirm-crossrepo
- **D-03 [confirm-crossrepo]:** Model single-or-batch as one uniform `List<ProposedItemUiModel>` (size 1 = single) + a `SelectionMode` (AllOrNothing | PerItem). Validate the exact `ProposedItemUiModel` field set against SB's `MutationGate`/`VoiceConfirmGate` and CT's `VoiceResultSheet` Proposed/ProposedBatch call-sites at the A13 reconvene before authoring. _(cross-repo — reconvene item)_

### Claude's Discretion
Do NOT reuse `component/ConfirmationDialog.kt` (it is an `AlertDialog` with scalar title/body — can't host a batch list; the confirm state lives INSIDE the sheet).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone planning
- `.planning/ROADMAP.md` § Phase 12 — goal + success criteria
- `.planning/REQUIREMENTS.md` — VOUT-04 (A2/E1)
- `.planning/v2.4-DECISION-MAP.md` § Phase 12 — the decisions above
- `.planning/phases/11-voice-outcome-failure-sheet/11-CONTEXT.md` — the sealed outcome type this extends

### Research
- `.planning/research/FEATURES.md` — the `NeedsConfirmation(reason, items, selectionMode)` shape + three render paths
- `.planning/research/ARCHITECTURE.md` — additive sealed-subtype pattern; ConfirmationDialog anti-pattern
- `.planning/research/PITFALLS.md` — dual-consumer confirm-shape pitfall; opaque-slot guidance

### Cross-repo contract
- `~/Projects/Reusable/android/voice-action-engine/CROSS-REPO-SCOPE-CONTRACT.md` §6.3 + A2 + E1 (SB seam is `MutationGate`/`VoiceConfirmGate`, not `MutationTierPolicy`)
- `.planning/cross-repo/HANDOFF.md`

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `component/SheetScaffold.kt` (host) + `component/DynamicActionButton.kt` (confirm/cancel actions).
- `component/AlbumTitleConfirmSheet.kt`, `component/TagCreateSheet.kt` — in-sheet confirm precedents.

### Established Patterns
- `component/ConfirmationDialog.kt` is the WRONG tool here (dialog, scalar body) — precedent for the destructive/error emphasis only.
- All-`val` models; frozen constructors (the `TagChipUiModel.kt` `copy()`-ABI lesson).

### Integration Points
- Register the confirm state with the outcome sheet's states matrix (CATALOG-03); no new top-level composable if it renders as a sheet state.

</code_context>

<specifics>
## Specific Ideas

The single sharpest acceptance test: write BOTH consumers' confirm call-sites as two usages of the ONE proposed signature on paper before coding — if either needs a field the other must leave meaningless, iterate the shape first.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 12-generic-needs-confirmation-state*
*Context gathered: 2026-09-29*
