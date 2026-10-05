# Phase 15: Voice-surface i18n label params - Context

**Gathered:** 2026-10-05
**Status:** Ready for planning

<domain>
## Phase Boundary

Make every hardcoded English label in the voice-command UI surface caller-overridable — via optional composable params (English defaults) and additive defaulted model fields — with no behavior change for existing callers. Covers VI18N-01..04. Strictly additive; cuts no tag.

</domain>

<decisions>
## Implementation Decisions

### api-guard
- **D-01 [api-guard]:** Adding a defaulted param/field to a symbol already shipped in v2.4.0 is source-compatible but trips the raw-line `tools/verify-api-additive.sh` as lane-3. Resolution (human): treat Metalava `apiCheck` as the authoritative additive gate, append every new param as the LAST parameter, and declare the raw-line guard's lane-3 as a known false-positive via `HUB_LANE_OVERRIDE=3` on these commits. — **Reversibility:** reversible — a guard-invocation/override choice, not a code contract.

### undo-label
- **D-02 [undo-label]:** The overridable "Undone" label lives on `UndoRowUiModel` as a defaulted `undoneLabel: String = "Undone"` (per-row); never convert `UndoRowState.Undone` (a `data object`) to a class — that would delete the public `INSTANCE` symbol (a lane-3 break). "Escalations:" → `HandledByUiModel`, "Couldn't undo:"/" changed since" → `UndoRefusedUiModel`, "Remove" → `ProposedItemUiModel.removeContentDescription`, all defaulted. _(source: ai-auto)_

### gallery
- **D-03 [gallery]:** Leave the Explorer gallery call sites on English defaults (byte-identical gallery); a localized showcase is optional, not required. _(source: ai-auto)_

### Claude's Discretion
- Exact param names beyond those the brief fixes, and the ordering of appended params among themselves (as long as each is appended after `modifier`/after existing fields to keep `apiCheck` green).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Decision source
- `.planning/v2.5-DECISION-MAP.md` § Phase 15 — the gray areas + resolutions behind these decisions
- `.planning/cross-repo/RECONVENE-BRIEF-R-v1.1.md` (cb5f047) §2 — the locked additive surface (per-param + model fields)

### Requirements / roadmap
- `.planning/REQUIREMENTS.md` — VI18N-01..04
- `.planning/ROADMAP.md` § Phase 15

### Hub guards
- `tools/verify-api-additive.sh`, `tools/classify-hub-change.sh`, `tools/hooks/pre-commit`, `tools/README-api-guard.md` — the additive guard + `HUB_LANE_OVERRIDE` path
- `api.txt` — the Metalava public dump (append-at-end discipline)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `ProviderKeyCard.kt:69,71` (`keyLabel`, `emptyProvidersReason`) and `ModelSelectCard.kt:46` (`emptyReason`) — existing caller-supplied-label precedent to mirror.
- `VoiceOutcomeUiState.NeedsConfirmation` — full caller-supplied label set (`confirmLabel`/`cancelLabel`) precedent.

### Established Patterns
- Null/default-param-hides and defaulted-field additions already used throughout; `showTagColors` (v2.3.0) is the append-as-last-param precedent.

### Integration Points
- Hardcoded literals: `ProviderKeyCard.kt:147`, `ModelSelectCard.kt:112`, `ClarificationBar.kt:92`, `ApproachLadderCard.kt:111,157,165,171`, `OutcomeSheet.kt:177,229,330,433`.
- Models: `HandledByUiModel.kt`, `UndoRowUiModel.kt`, `UndoRefusedUiModel.kt`, `ProposedItemUiModel.kt`.

</code_context>

<specifics>
## Specific Ideas

No specific requirements — English defaults preserved verbatim; consumers pass localized strings.

</specifics>

<deferred>
## Deferred Ideas

The offline-toggle label decision here (`onlineLabel`/`offlineOnlyLabel`) sets the precedent Phase 17's router-toggle labels will mirror (P17 [router-copy] is provisional on this phase's outcome).

</deferred>

---

*Phase: 15-voice-surface-i18n-label-params*
*Context gathered: 2026-10-05*
