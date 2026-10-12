# Phase 16: A11y + Failure enrichment - Context

**Gathered:** 2026-10-05
**Status:** Ready for planning

<domain>
## Phase Boundary

Add accessibility to the approach-ladder rows (minimum interactive size + selected semantics on the chosen cap rung) and enrich `VoiceOutcomeUiState.Failure` (action `role`, optional `body` content slot, optional `semanticsPrefix`). Covers VA11Y-01, VFAIL-01..03. Strictly additive; existing callers unchanged.

</domain>

<decisions>
## Implementation Decisions

### selected-semantics
- **D-01 [selected-semantics]:** `selected = (rung.id == maxTierId)` — announce the rung the user actually chose as the cap, NOT `isCapped` (which is the excluded, above-cap rows). Resolution (human); corrects the brief's shorthand wording. Mirrors `PresetChip`'s `selected = isSelected`. — **Reversibility:** reversible — semantics-only, internal.

### failure-a11y
- **D-02 [failure-a11y]:** `Failure.semanticsPrefix` is announced by setting a combined `contentDescription` (prefix + reason) on the error surface's merged semantics node. Resolution (human). SB-175 is the confirming consumer and validates the announcement shape at integration.

### role-scope
- **D-03 [role-scope]:** Apply `Role.RadioButton` + `selected` to rungs only when the cap is actually selectable (`onMaxTierChange != null`) — a cap-less ladder must not announce non-interactive rows as an empty radio group. _(source: ai-auto)_

### min-size-visual
- **D-04 [min-size-visual]:** Add `Modifier.minimumInteractiveComponentSize()` to the per-rung interactive wrapper (`CapControl`'s Row); accept the slight row-height growth on short rows as standard a11y, and flag it as a Gate-2 visual check (brief §6 Q4). _(source: ai-auto)_

### Claude's Discretion
- Failure `body` slot placement inside the error surface Column (recommended: after `handledBy`/reason, before the optional action, so it rides the existing scroll region). Whether a `liveRegion` is added is left open for SB-175's a11y validation.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Decision source
- `.planning/v2.5-DECISION-MAP.md` § Phase 16
- `.planning/cross-repo/RECONVENE-BRIEF-R-v1.1.md` (cb5f047) §2 (d) + §6 Q4

### Requirements / roadmap
- `.planning/REQUIREMENTS.md` — VA11Y-01, VFAIL-01..03
- `.planning/ROADMAP.md` § Phase 16

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `DynamicActionButton.kt:67` — public `ActionButtonRole { Destructive, Save, Neutral }` reused for `FailureActionUiModel.role` (no new enum). `NeedsConfirmation.severity` (`VoiceOutcomeUiState.kt:116`) is the precedent for a defaulted `ActionButtonRole` field.
- `PresetChip.kt:94,100-102` + `AppChip.kt:128` — `minimumInteractiveComponentSize()` + `selected` semantics precedent.
- `Success.editableContent` (`VoiceOutcomeUiState.kt:48`) — precedent for a nullable `@Composable` body slot.

### Established Patterns
- `CapControl` (`ApproachLadderCard.kt:187-204`) already owns the single merged-semantics node — the natural, internal seam for `selected`/`role`/min-size (no public API change).
- Failure body rides `OutcomeSheetContent`'s `weight(1f, fill=false)` scroll region (`OutcomeSheet.kt:92-103`) — a body slot won't starve on a tall sheet; no pinned footer needed.

### Integration Points
- `OutcomeSheet.kt:302` — hardcoded `role = Neutral` → wire to `action.role` (append `role` to `FailureActionUiModel`, default Neutral).
- `VoiceOutcomeUiState.Failure` (`:67-71`) — append `body`/`semanticsPrefix` after `action` (preserve `componentN()`/`copy()` ordinals).

</code_context>

<specifics>
## Specific Ideas

No specific requirements beyond the decisions above.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 16-a11y-failure-enrichment*
*Context gathered: 2026-10-05*
