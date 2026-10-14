# Phase 23: ProviderKeyCard lifecycle & Undo Center groups - Context

**Gathered:** 2026-10-12
**Status:** Ready for planning

<domain>
## Phase Boundary

This phase gives `ProviderKeyCard` the full key lifecycle (sketch §1): a per-provider saved-key summary that shows the fingerprint only, plus Save/Update/Clear/Cancel. It also lets `UndoCenterScreen` render grouped entries with one "Undo all" (XR-178-01 c). Default calls of both render exactly as in v2.5.1. Requirements: VKEY-01, VKEY-02, VUNDO-02.

</domain>

<decisions>
## Implementation Decisions

### Signatures & compat
- **D-01 [signatures]:** `ProviderKeyCard` and its new types (`SavedKeyUiModel`, `ProviderKeyActions`, `ProviderKeyLabels`, `ProviderKeyDefaults`) are exactly as in `.planning/seams/v2.6.0-SKETCH.md` §1. The new params are appended after `providerLabel`. — **Reversibility:** one-way — signed off by CT and SB.
- **D-02 [compat]:** Source compatibility only: no hidden overloads or shims. Regenerate `api.txt` per API commit.

### Card behavior
- **D-03 [card-behavior]:** The card stays stateless and presentational, with no persistence, validation or network (INV-01).
  - The key field follows `selectedProviderId`.
  - If `savedKeys[selected] != null && !editingSavedKey`, the field is replaced by the summary: a check icon plus `"{savedPrefix} · {fingerprint}"`, then Update | Clear.
  - Otherwise you get the field plus Save, and Cancel when `onCancelEdit != null`.
  - `inFlight` disables every action. `saveEnabled` gates Save.
  - Providers present in `savedKeys` get `labels.savedMarker` appended in the dropdown.
  - The raw `keyValue` never renders while the summary shows.
- **D-04 [tags]:** `provider_key_saved_summary` and `provider_key_action_{save,update,clear,cancel}`.

### key-buttons
- **D-05 [key-buttons]:** Actions are `DynamicActionButton`s: Save = `Save` role, Clear = `Destructive`, Update/Cancel = `Neutral`. Each has a 48dp touch target. _(ai-auto)_

### undo-group-api
- **D-06 [undo-group-api]:** `UndoCenterScreen` gains `groups: List<UndoGroupRowUiModel> = emptyList()` and `onUndoGroup: ((String) -> Unit)? = null`, inserted before the trailing `onClear` so trailing-lambda call sites still compile. The new `UndoGroupRowUiModel(groupId, label, memberIds: List<String>, status: UndoGroupStatus, undoAllLabel: String = "Undo all")` goes in `feedback/` and reuses the existing `UndoGroupStatus`.
  - The screen stays stateless.
  - Each group row (label, member count, status, "Undo all" → `onUndoGroup(groupId)`) is followed by its member rows, indented.
  - Ungrouped entries stay flat.
  - Empty `groups` gives today's flat list.

  _(ai-auto)_ — **Reversibility:** costly — SB adopts the shape on its next repin.

### Claude's Discretion
- Group row styling within hub tokens. Whether "Undo all" hides when `onUndoGroup == null` or when the status says nothing is undoable.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Decision record & requirements
- `.planning/seams/v2.6.0-SKETCH.md` §1 (the ProviderKeyCard lifecycle)
- `.planning/REQUIREMENTS.md` — VKEY-01, VKEY-02, VUNDO-02
- `.planning/ROADMAP.md` § Phase 23
- `.planning/v2.6-DECISION-MAP.md` § Phase 23

### Consumer context
- `/home/yahir/Projects/AndroidApps/Personal/SecondBrain/.planning/cross-repo/178-XR-01.md` item (c)
- CT `app/src/main/java/com/caltracker/app/ui/settings/SettingsScreen.kt:641` (today's dead dropdown + own Save/Configured rows); SB `feature/settings/voiceagent/VoiceAgentProviderCards.kt:67` (one card + own Save row; must stay unchanged)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `component/ProviderKeyCard.kt` (masked field + reveal toggle, `KeyFieldState`), and `ProviderOptionUiModel`.
- `feedback/UndoHistoryStore.kt` already tracks groups (`openGroup`, `group`, `groupIdOf`, `groupLabel`, `groupStatus`, `attemptUndoGroup`). `feedback/UndoGroupTypes.kt` has `UndoGroupStatus`. `feedback/UndoCenterScreen.kt` renders a flat list today.

### Integration Points
- SB's Undo Center screen maps `UndoHistoryStore` groups → `UndoGroupRowUiModel` and wires `onUndoGroup` → `attemptUndoGroup`.

</code_context>

<specifics>
## Specific Ideas

- Fingerprint example: "Saved · sk-ant-…abcd".

</specifics>

<deferred>
## Deferred Ideas

None. This phase stays within its scope.

</deferred>
