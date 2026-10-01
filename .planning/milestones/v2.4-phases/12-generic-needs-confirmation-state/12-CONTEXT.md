# Phase 12: Generic needs-confirmation state - Context

**Gathered:** 2026-09-29
**Status:** Ready for planning

<domain>
## Phase Boundary

Deliver ONE domain-neutral needs-confirmation state, rendered inside the Phase 11 outcome sheet, that covers both SecondBrain's risk confirm and CalTracker's weak-match single/batch confirm from props — a reason string, single-or-batch proposed item(s), and confirm/cancel — with no per-consumer library change.

</domain>

<decisions>
## Implementation Decisions

Resolved in `ai` mode (`source: ai-auto`); updated per **R1 GO-WITH-CHANGES**. **[additivity]** is provisional (depends on Phase 11); **[confirm-crossrepo]** is a reconvene item.

**✅ UNGATED (R1, 2026-09-29):** both SecondBrain and CalTracker confirmed the confirm-shape against their real call-sites. Planning may proceed — but still AFTER Phase 11 lands the sealed `VoiceOutcomeUiState` (the additivity dependency, D-02). Sequence: P10 → P11 → P12.

### per-item-metadata
- **D-01 [per-item-metadata]:** Carry the per-item metadata that differs between consumers (SB risk tier vs CT match score) as an OPAQUE presentational per-item slot (e.g. `trailingContent: @Composable () -> Unit`, or a nullable/defaulted field the library never interprets) — never a library-defined risk/score/severity enum. — **Reversibility:** one-way — the confirm prop shape freezes into the `v2.4.0` API; a library enum would leak one consumer's domain (drift-guard RED).

### additivity
- **D-02 [additivity]:** Phase 12 is purely additive — a new `NeedsConfirmation` subtype of the sealed `VoiceOutcomeUiState` + one `when` branch, reusing `SheetScaffold` + `DynamicActionButton` — CONTINGENT on Phase 11 having shipped the sealed `VoiceOutcomeUiState` param. Verify that seam exists before planning. _(provisional — refresh at execution; depends on Phase 11)_

### confirm-crossrepo
- **D-03 [confirm-crossrepo]:** Model single-or-batch as one uniform `List<ProposedItemUiModel>` (size 1 = single) + a `SelectionMode` (AllOrNothing | PerItem). Validate the exact `ProposedItemUiModel` field set against SB's `MutationGate`/`VoiceConfirmGate` and CT's `VoiceResultSheet` Proposed/ProposedBatch call-sites at the A13 reconvene before authoring. _(cross-repo — reconvene item)_

### confirm-scope
- **D-04 [confirm-scope]:** The confirm state must ALSO cover per-item EDIT before confirm (CT Phase 70 multi-item sheet has per-row edit/delete; VAE GATE-01 has `Admit(amended)`), per-item REMOVE, and a "Confirm all (N)" action. The opaque per-item slot may carry the edit UI, but the state model itself needs an item-level amended/removed representation. — **Reversibility:** one-way — freezes into the `v2.4.0` API. _(R1 orchestrator change 1)_

### confirm-sb-props (SB's real call-site, R1)
- **D-05 [confirm-sb-props]:** SB always sends a SINGLE item (size 1 — its gate is sequential; the batch case is CT, still pending). Add these OPTIONAL confirm props: (1) a `title` separate from the reason/body (e.g. "Delete card?" / "Merge tags?" / "Allow this change?"); (2) a per-confirm confirm-button VERB (default "Confirm"; e.g. "Delete" / "Merge" / "Allow"); (3) a style/severity (Destructive = red, per the indicative-buttons convention — all 4 SB subjects are destructive); (4) a reversibility hint (Undoable / Irreversible, or free text) that stays visible. **Behavior:** dismiss / outside-tap / back = decline; the host may WITHDRAW the sheet while it is showing (SB's gate auto-holds after 120 s) — handle without crashing or flashing. **Privacy:** `ProposedItemUiModel.toString()` must NOT print item names (SB T-166-05, same spirit as VAE TEL-04) — SB passes finished strings, so YAT never sees raw subjects. _(R1 — SB shape answer; CT batch answer still pending)_

### confirm-ct-props (CT's real call-site, R1)
- **D-06 [confirm-ct]:** CT must-haves on the confirm shape: (1) a TOP-LEVEL (sheet-level) content slot IN ADDITION to the per-item slots — CT's `target_date` is ONE shared value for all N items (a single date-picker row), which per-item slots can't host; (2) `SelectionMode` must include a WHOLE-BATCH mode (edit/remove rows, then ONE "Confirm all (N)" via `onConfirmAllProposed`) — CT is NOT per-item-confirm; keep `PerItem` as another option; (3) an OPTIONAL per-item reason/confidence cue on `ProposedItemUiModel` ("weak match, check this one") — thresholds stay app-side, only the cue is rendered. Already covered: weak single = size 1; batch = size N; per-row edit/delete via the item-level amended/removed representation + opaque slot (CT injects its `AmountEditor` + `ItemCorrectionDropdown`). _(R1 — CT VoiceResultSheet.kt / VoiceLogUiState.kt)_

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

## Runtime Decisions

### 2026-09-30 — D-02 [additivity] provisional refreshed (ai-auto, dependency Phase 11 now complete)

The provisional D-02 additivity decision is RESOLVED against Phase 11's real output. Phase 11 shipped `sealed interface VoiceOutcomeUiState` (`src/main/.../model/VoiceOutcomeUiState.kt`) with exactly `Success` and `Failure` arms and an explicit KDoc contract that the top-level type stays stable and that **Phase 12 adds `NeedsConfirmation` as a NEW sealed subtype + one `when` branch, never a reshape of either arm**. Seam verified live in `src/main` at HEAD (not from the plan text). → **Plan Phase 12 as a purely additive 3rd arm** reusing `SheetScaffold` + `DynamicActionButton`; do not reshape `Success`/`Failure`. The additivity contingency in D-02 is satisfied. _(milestone master provisional-refresh, source: ai-auto)_

---

*Phase: 12-generic-needs-confirmation-state*
*Context gathered: 2026-09-29*
