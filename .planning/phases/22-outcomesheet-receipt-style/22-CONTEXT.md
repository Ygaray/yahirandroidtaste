# Phase 22: OutcomeSheet Receipt style - Context

**Gathered:** 2026-10-12
**Status:** Ready for planning

<domain>
## Phase Boundary

This phase adds an opt-in `OutcomeSheetStyle.Receipt` for confirm, success and failure. It includes:
- the `NeedsConfirmation`/`Success`/`Failure`/`FailureActionUiModel`/`HandledByUiModel` additions;
- the Y-14 stable tags;
- an action that stays usable while `inFlight` (SB);
- an Assertive live region on the refused surface (XR-178-01 a);
- `Failure.undo` (XR-178-01 b).

The 3-arg Classic call renders exactly as in v2.5.1. Requirements: VRCPT-01..05, VTAG-01, VXR-01, VXR-02.

</domain>

<decisions>
## Implementation Decisions

### Signatures & compat
- **D-01 [signatures]:** The signatures are exactly those in `.planning/seams/v2.6.0-SKETCH.md` §2 Y-3..Y-7, plus the accepted delta `SheetActionUiModel.activeWhileInFlight: Boolean = false` (teamlead, 2026-10-11). — **Reversibility:** one-way — signed off by CT and SB.
- **D-02 [compat]:** Source compatibility only (P1): no hidden 3-arg `OutcomeSheet` overload, no old-arity `copy`, no hidden ctors, no `copy$default` shims.
  - Data-class fields are appended with defaults.
  - `NeedsConfirmation`'s `confirmEnabled` and `titleActions` are inserted before `onConfirm`/`onCancel`. Both consumers use named args: SB `VoiceConfirmSheetModels.kt:29`, CT `VoiceOutcomeMapper.kt:315/435`.
  - The v2.5.0 shims already in the code stay as they are.
  - Regenerate `api.txt` with each API commit.
- **D-03 [new-enums]:** The new enums are `OutcomeSheetStyle { Classic, Receipt }`, `OutcomeBadge { Success, Reverted, Error, Warn }` and `FailureTone { Error, Warn }`. No existing enum gains constants.

### receipt-structure
- **D-04 [receipt-structure]:** Receipt rendering lives in new private composables in a new file (`OutcomeSheetReceipt.kt`), dispatched by `style`. The Classic arm composables stay byte-unchanged. Parity is proven structurally: the defaulted call runs the same code path, plus Robolectric state assertions (P3, no Roborazzi). _(ai-auto)_
- **D-05 [receipt-frame]:** The Receipt frame (Y-3):
  - `SheetScaffold(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))`.
  - The body has 16dp horizontal and 4dp top padding.
  - **Title row:** `Row(CenterVertically, spacedBy 8dp)` = optional 32dp badge → title (`OutcomeSheetDefaults.ReceiptTitleStyle`: `titleLarge` 18/24 SemiBold, `heading()`, weight 1f, maxLines 2) → `titleActions`.
  - **Transcript line:** `bodyMedium` italic `onSurfaceVariant`, 4dp top and 8dp bottom, shown iff a transcript or reason exists.
  - **Pinned bar** for NeedsConfirmation and Failure: a 1dp `outlineVariant` divider, then a `Row` with 16dp start/end, 12dp top and 16dp bottom padding: start Text action, `Spacer(weight 1f)`, end Filled. Keep the v2.4.1 `weight(1f, fill = false)` scroll body so the bar never starves.
  - **Success actions** sit at the end of the scroll content with 24dp above them, not pinned.

### classic-new-fields
- **D-06 [classic-new-fields]:** Classic ignores `badge`, `titleActions` and `startAction`/`endAction`. It honors `FailureTone.Warn` (painting `colors.warnTone`) and `Failure.undo`. The default `Error` tone and `undo = null` change nothing. _(ai-auto)_

### State additions
- **D-07 [confirm]:** While `confirmEnabled = false`, the confirm node is `enabled = false` and `onConfirm` never fires. In Receipt:
  - `reason` renders as the transcript;
  - the start action is `cancelLabel` (Text);
  - the end action is `confirmLabel` (Filled; `severity` picks the hue).

  The KDoc says `items` may be empty when `topLevelContent` renders the rows.
- **D-08 [success]:** Receipt order: title row → handled-by pill → `editableContent` → existing `batchResults`/`undo` → actions row. `inFlight` disables every action except those with `activeWhileInFlight = true` (SB in-flight Cancel). `Reverted` uses `Icons.AutoMirrored.Filled.Undo`; `Warn` uses `PriorityHigh` with content and container swapped.
- **D-09 [failure]:** Receipt `Failure`:
  - the title row sits outside the card;
  - the card is a Phase 20 `ToneCard` (tone from `FailureTone` → `colors.errorTone`/`colors.warnTone`) holding `reason` (`bodyMedium` SemiBold, `onSurface`) plus `body`;
  - `tip` renders under the card;
  - `action` moves to the end of the pinned bar (Filled, keeping its role hue, with optional leading `icon`);
  - `secondaryAction` sits at the start.
- **D-10 [handled-by]:** The Receipt pill:
  - `surfaceContainerHighest`, full radius, 4dp × 12dp padding, `bodySmall` `onSurfaceVariant`;
  - a 6dp dot in `primary`, or `warnTone.content` when `onDevice`;
  - then `"{prefixLabel} "`, the tier in SemiBold `onSurface`, and `" · {approach/provider/model joined}"`.

  Classic `HandledByRow` is unchanged.

### failure-undo-placement
- **D-11 [failure-undo-placement]:** In Receipt, `Failure.undo` renders in the scroll content below the ToneCard and tip, above the pinned bar, using the same undo block as Success. In Classic it renders below the error surface. _(ai-auto)_

### a11y / tags
- **D-12 [live-region]:** The refused/partial undo surface gets `semantics { liveRegion = LiveRegionMode.Assertive }`. Semantics only, with no visual change.
- **D-13 [tags]:** The Y-14 Receipt tags are exactly as listed in REQUIREMENTS VTAG-01. The Receipt NeedsConfirmation bar keeps `outcome_sheet_confirmation_cancel`/`_confirm`; Success and Failure use `outcome_sheet_action_start`/`_end`. Classic tags are unchanged. Never set `testTagsAsResourceId`.

### Claude's Discretion
- How style and colors thread internally (CompositionLocal vs params) and the private file split, as long as Classic stays byte-unchanged.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Decision record & requirements
- `.planning/seams/v2.6.0-SKETCH.md` §2 Y-3..Y-7, Y-14
- `.planning/REQUIREMENTS.md` — VRCPT-01..05, VTAG-01, VXR-01, VXR-02
- `.planning/ROADMAP.md` § Phase 22
- `.planning/v2.6-DECISION-MAP.md` § Phase 22

### Consumer specs
- `/home/yahir/Projects/AndroidApps/Personal/CalTracker_Android/.planning/phases/76-gate-2-gap-closure-voice-sheets-piece-weight-grammar-header/76-UI-SPEC.md` § Y-3..Y-7, Y-14, plus the Receipt frame anatomy row. This is the UI contract.
- `/home/yahir/Projects/AndroidApps/Personal/SecondBrain/.planning/cross-repo/178-XR-01.md` — items (a) liveRegion and (b) `Failure.undo`

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `component/OutcomeSheet.kt` (Classic arms, `HandledByRow`, undo blocks, `BatchResultsList`), `model/VoiceOutcomeUiState.kt` (the `Failure` v2.5.0 shims at ~line 128), `model/HandledByUiModel.kt`.
- Phase 20: `ToneCard`, `ToneDefaults`, `ActionButtonEmphasis`. Phase 21: `SheetScaffold(shape)`.

### Established Patterns
- The v2.4.1 OutcomeSheet hotfix: the scroll body uses `weight(1f, fill = false)` so the action bar never starves.
- Existing `outcome_sheet_*` tags and `onAllNodesWithTag` indexed tests.

### Integration Points
- CT renders the rows through `topLevelContent` with `items = emptyList()`. SB stays on Classic, unchanged.

</code_context>

<specifics>
## Specific Ideas

- Do a long German ~360dp check on the Receipt action-bar labels and the pill before layout sign-off.

</specifics>

<deferred>
## Deferred Ideas

None. This phase stays within its scope.

</deferred>
