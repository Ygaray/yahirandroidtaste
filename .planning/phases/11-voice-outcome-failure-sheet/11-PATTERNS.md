# Phase 11: Voice outcome & failure sheet - Pattern Map

**Mapped:** 2026-09-30
**Files analyzed:** 7 (4 new models, 2 new composables, 1 modified registry file)
**Analogs found:** 7 / 7

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `model/VoiceOutcomeUiState.kt` (new) | model (sealed UI state) | request-response (one-shot outcome render) | `model/KeyFieldState.kt` | exact |
| `model/HandledByUiModel.kt` (new) | model (data class) | transform (provenance projection) | `model/ApproachRungUiModel.kt` | exact |
| `model/UndoAffordanceUiModel.kt` + `UndoRowUiModel.kt` (new) | model (data class + sealed state) | transform (presentational projection over `UndoHistoryEntry`) | `model/ApproachRungUiModel.kt` + `model/TagChipUiModel.kt` (ABI-safety lesson) | exact |
| `model/ClarificationOptionUiModel.kt` (new) | model (data class) | transform | `model/ApproachRungUiModel.kt` | exact |
| `component/<OutcomeSheet>.kt` (new, name TBD) | component (sealed-state renderer, host+content) | request-response | `component/ApproachLadderCard.kt` (card shape) + `component/RecordingBottomSheetContent.kt` (host/content split, sheet-state enum) | role-match |
| `component/<ClarificationChoices>.kt` (new, name TBD) | component (standalone, pressable list) | event-driven (tap → callback) | `component/ApproachLadderCard.kt`'s `SegmentedOptionSelector` usage + `component/AppChip.kt` | role-match |
| `explorer/VoiceCommandFamilyScreen.kt` (modified — append) | route/registry (gallery wiring) | CRUD (append two `Entry` blocks + fixtures) | itself, existing `voiceCommandFamilyEntries` list (lines 43-132) | exact (same file, additive) |

## Pattern Assignments

### `model/VoiceOutcomeUiState.kt` (model, sealed UI state)

**Analog:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/KeyFieldState.kt` (full file, 31 lines)

**Sealed-interface convention to copy verbatim:**
```kotlin
package io.github.ygaray.yahirandroidtaste.model

/**
 * Render-only ... state for <X> ([component ref], REQ-ID). This describes HOW
 * the <component> should render ... — it is display state the consumer
 * computes and hoists in; the library never validates/executes ... (INV-01).
 */
sealed interface KeyFieldState {
    data object Empty : KeyFieldState
    data object Entered : KeyFieldState
    data object Validating : KeyFieldState
    data object Valid : KeyFieldState

    /**
     * @param reason Caller-formatted, human-readable failure reason shown as the field's
     *   supporting/error text.
     */
    data class Invalid(val reason: String) : KeyFieldState
}
```
**How to apply:** `VoiceOutcomeUiState` should be a `sealed interface` with `data class Success(...)` and `data class Failure(...)` arms only (per D-02/D-05/D-08 — `Success` carries optional `undo`/`editablePayload`, `Failure` carries optional `action`). Mirror `KeyFieldState.Invalid`'s pattern of a data class carrying a caller-formatted `reason: String` for the `Failure` arm. All fields `val`, no `var` — matches this repo's Compose-`STABLE` convention throughout `model/`.

---

### `model/HandledByUiModel.kt`, `ClarificationOptionUiModel.kt` (model, plain data classes)

**Analog:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/ApproachRungUiModel.kt` (full file, 31 lines)

**All-`val` data class + KDoc convention:**
```kotlin
package io.github.ygaray.yahirandroidtaste.model

/**
 * One rung of a command-approach tier ladder
 * ([io.github.ygaray.yahirandroidtaste.component.ApproachLadderCard], VAPPR-01/02/03) — mirrors
 * [ListItemUiModel]'s all-`val` immutable shape (D-04) so this class stays Compose-inferred
 * STABLE and introduces no `copy()`/`var` ABI trap (see [TagChipUiModel] for that lesson).
 *
 * [offlineCapable] is an APP-derived prop (D-06) — the library never depends on any
 * voice-action-engine `TierPolicy` type; the consumer maps its engine's own tier metadata into
 * this UI-only shape at the call site (no hub-to-hub edge, L7).
 *
 * @param id Stable identifier emitted via callbacks — never rendered as text.
 * @param label Caller-formatted display text ...
 */
data class ApproachRungUiModel(
    val id: String,
    val label: String,
    val rank: Int,
    val enabled: Boolean = true,
    val offlineCapable: Boolean = false,
    val description: String? = null
)
```
**How to apply:** `HandledByUiModel(tier: String, approach: String? = null, provider: String? = null, model: String? = null, escalationCount: Int? = null)` — required tier, all else optional-and-defaulted (D-04). `ClarificationOptionUiModel(id: String, label: String)` per D-07 — copy the "never rendered / opaque id" KDoc framing from `ApproachRungUiModel.id`'s own doc comment (id is "emitted via callbacks... never rendered as text"; here: "opaque — the library never interprets it").

---

### `model/UndoAffordanceUiModel.kt` / `UndoRowUiModel.kt` (model, presentational projection)

**Analog (shape convention):** `model/ApproachRungUiModel.kt` (same all-`val` pattern as above)

**Analog (ABI-safety lesson — READ BEFORE TOUCHING `UndoHistoryEntry`):** `src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt` lines 38-56 (per RESEARCH.md, verbatim-quoted): documents that adding a 6th primary-constructor parameter was REJECTED because it would make `copy()`'s old-arity overload a non-additive `RemovedMethod` `apiCheck` break; the fix was moving the new field OUTSIDE the primary constructor as a body `var`.

**How to apply:** Do NOT add fields to `feedback/UndoHistoryEntry.kt`'s primary constructor (currently 7 params, `internal` ctor, `feedback/UndoHistoryEntry.kt:47-54`). Instead author a brand-new public `UndoRowUiModel(id: String, label: String, state: UndoRowState)` with `sealed interface UndoRowState { data object Available; data object Undone; data class Unavailable(val reason: String) }`, and `UndoAffordanceUiModel(allLabel: String, rows: List<UndoRowUiModel>, onUndoAll: (() -> Unit)? = null, refused: UndoRefusedUiModel? = null)` with `UndoRefusedUiModel(reason: String, changedItem: String? = null)`. The consumer builds these FROM its own read of `UndoHistoryStore.entries` / `UndoHistoryEntry.status`. Null-hides-the-control convention (`onUndoAll == null` hides "Undo all" entirely) mirrors `ApproachLadderCard`'s documented D-05 "hideable-by-null-prop, never shown-disabled" rule (`component/ApproachLadderCard.kt:29-32`).

---

### `component/<OutcomeSheet>.kt` (component, sealed-state renderer)

**Analog 1 (card shape + null-prop-hides convention):** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt` (full file, 178 lines)

**Imports pattern** (lines 1-22): standard Compose/Material3 imports, `theme.Dimens`, `theme.expressive`, plain model import — no ViewModel, no Hilt.

**Core pattern** (lines 55-101): required content params first, then `modifier: Modifier = Modifier` last; a `Surface(shape = MaterialTheme.expressive.cardShapeLarge, color = MaterialTheme.colorScheme.surfaceContainer)` wrapping a `Column(padding = Dimens.HorizontalPadding)`; derived effective-state computed IN the composable from plain primitives, never from an engine type:
```kotlin
@Composable
fun ApproachLadderCard(
    ladder: List<ApproachRungUiModel>,
    offlineOnly: Boolean? = null,
    onOfflineOnlyChange: ((Boolean) -> Unit)? = null,
    maxTierId: String? = null,
    onMaxTierChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val effectiveOfflineOnly = offlineOnly == true
    val capRank = maxTierId?.let { id -> ladder.firstOrNull { it.id == id }?.rank } ?: Int.MAX_VALUE
    Surface(
        shape = MaterialTheme.expressive.cardShapeLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.testTag("approach_ladder_card_surface")
    ) { /* ... */ }
}
```
**Null-prop-hides convention (D-05, lines 29-32 KDoc):** "Every control is hideable-by-null-prop, never shown-disabled: a `null` [offlineOnly]/[onOfflineOnlyChange] pair hides the toggle entirely." Apply this exact pattern to the outcome sheet's optional `handledBy`/`action`/`undo` slots: `failure.handledBy?.let { HandledByRow(it) }`, `failure.action?.let { DynamicActionButton(...) }`.

**Swap-seam sub-composable pattern** (lines 153-177, `CapControl`): a `private` sub-composable wraps content in a conditionally-clickable `Row` — `Modifier.clickable` applied ONLY when the callback is non-null, never a disabled clickable node. Reuse this exact shape for any tappable row inside the outcome sheet (e.g. a per-item Undo row).

**Analog 2 (host/content split + sheet-state enum):** `src/main/java/io/github/ygaray/yahirandroidtaste/component/RecordingBottomSheetContent.kt` lines 1-80 — a designsystem-native enum (`RecordingSheetUiState`) mirrors a ViewModel's state without referencing the ViewModel; the pure content composable takes all state as hoisted params (`uiState`, callbacks), no ViewModel/Hilt/permission code inside the library composable. Use `SheetScaffold` (per RESEARCH.md `component/SheetScaffold.kt:50-69`) as the chrome host if the outcome renderer ships as a `ModalBottomSheet`.

**Loud-failure surface — error color roles (NOT `AttentionCue`):**
Source: `theme/Color.kt:36-43` (verified in RESEARCH.md) — `ErrorRed`, `ErrorRedContainer`, `OnErrorRedContainer` (+ `*Dark`), consumed via `MaterialTheme.colorScheme.errorContainer` / `.onErrorContainer` at verified call sites `component/RecordingBottomSheetContent.kt:244-246` and `modifier/SwipeableActionRow.kt:278`. Anti-pattern: `component/AttentionCue.kt:28-30` KDoc explicitly states it is "never a failure signal" — do not reuse it here.

---

### `component/<ClarificationChoices>.kt` (component, standalone pressable list)

**Analog:** `component/AppChip.kt` (lines 88-175 per RESEARCH.md) for the pressable option token, plus `ApproachLadderCard`'s use of `SegmentedOptionSelector` (`component/ApproachLadderCard.kt:88-97`) as the nearest "compact selectable row" precedent:
```kotlin
if (offlineOnly != null && onOfflineOnlyChange != null) {
    SegmentedOptionSelector(
        selectedIndex = if (offlineOnly) 1 else 0,
        options = listOf("Online", "Offline only"),
        onSelect = { index -> onOfflineOnlyChange(index == 1) },
        modifier = Modifier.fillMaxWidth().padding(top = Dimens.ContentSpacing).testTag("...")
    )
}
```
**How to apply:** Build as its own top-level standalone composable (NOT nested in the outcome sheet's `when`), taking `question: String, options: List<ClarificationOptionUiModel>, onSelect: (String) -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier`. Render each option as an `AppChip(label = option.label, isSelected = false, onClick = { onSelect(option.id) })` in a `Row`, per D-07's explicit chips-vs-buttons discretion grant.

---

### `explorer/VoiceCommandFamilyScreen.kt` (modified — append registry entries)

**Analog:** itself — the existing `voiceCommandFamilyEntries` list, `explorer/VoiceCommandFamilyScreen.kt:43-132` (full excerpt read).

**Entry-append pattern** (exact shape to copy, lines 74-106 for one full entry):
```kotlin
ComponentRegistry.Entry(
    name = "ModelSelectCard",
    family = ExplorerFamilies.VOICE_COMMAND,
    states = listOf(
        ComponentRegistry.StateCell("Default", render = { ModelSelectCardFixture(selectedModelId = fixtureModels.first().id) }),
        ComponentRegistry.StateCell("Pressed / Selected", render = { /* ... */ }),
        ComponentRegistry.StateCell("Disabled", render = { /* unavailable-state fixture, not literal enabled=false */ }),
        ComponentRegistry.StateCell("Focused")   // N/A cells still declared, just with no render lambda
    ),
    content = { ModelSelectCardVariants() },
    tier = ComponentRegistry.Tier.PATTERN
)
```
**Fixture-wrapper pattern** (lines 171-181, `ProviderKeyCardFixture`): a `private @Composable` fixture function hoists local `remember { mutableStateOf(...) }` state so gallery States-matrix cells can be interactive previews, not static renders.

**How to apply:** Append two new `ComponentRegistry.Entry(...)` blocks to `voiceCommandFamilyEntries` (one for the outcome/failure sheet, one for the clarification-choices composable) — same file, same list, same `Default`/`Pressed-Selected`/`Disabled`/`Focused` 4-cell states matrix (per CONTEXT.md's "full 4-cell states matrix" requirement) — plus matching `private` fixture-wrapper composables, following the `ProviderKeyCardFixture`/`ModelSelectCardFixture`/`ApproachLadderCardFixture` convention exactly.

## Shared Patterns

### Null-prop-hides-the-control (D-05 convention)
**Source:** `component/ApproachLadderCard.kt` lines 29-32 (KDoc) + lines 88-97 (implementation)
**Apply to:** Every optional field across `VoiceOutcomeUiState.Success`/`Failure` (handledBy, undo, editablePayload, action) and `UndoAffordanceUiModel.onUndoAll` — a `null` value/callback pair hides the control entirely; never render it in a disabled/greyed state.

### Loud failure — error color roles, never `AttentionCue`
**Source:** `theme/Color.kt:36-43`; usage convention at `component/RecordingBottomSheetContent.kt:244-246`, `modifier/SwipeableActionRow.kt:278`
**Apply to:** `Failure` body surface and the undo-refused nested substate — both must use `MaterialTheme.colorScheme.errorContainer`/`.onErrorContainer`, never `AttentionCue` (`component/AttentionCue.kt:28-30` forbids this use), never muted/neutral body text (VOUT-03, Pitfall 3).

### All-`val`, Compose-`STABLE` model shape; never edit a frozen `internal`-ctor class
**Source:** `model/ApproachRungUiModel.kt` (shape); `model/TagChipUiModel.kt:38-56` (the ABI lesson, verbatim in RESEARCH.md)
**Apply to:** Every new model file in this phase — `VoiceOutcomeUiState`, `HandledByUiModel`, `UndoAffordanceUiModel`/`UndoRowUiModel`, `ClarificationOptionUiModel`. Explicitly forbidden: adding a field to `feedback/UndoHistoryEntry.kt`'s primary constructor.

### Naming-guard pre-clearance (`DomainVocabularyDriftGuardTest` + `ComponentRegistryDriftGuardTest`)
**Source:** `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt` lines 297-341 (per RESEARCH.md, verbatim quoted allowlist)
**Apply to:** Every new public top-level composable name in this phase. `Outcome`, `Handled`, `Clarification` are NOT currently in `PRIMITIVE_NOUN_ALLOWLIST` or `DOMAIN_VOCABULARY` — the plan must either pick an already-allowlisted head token (`Card`, `Sheet`, `Content`, `Badge`, `Row`, `Bar`, etc.) or widen `PRIMITIVE_NOUN_ALLOWLIST` in the SAME commit, mirroring Phase 10's precedent of adding `Provider`/`Model`/`Approach` (`DomainVocabularyDriftGuardTest.kt:320-326`). `Undo` is ALREADY allowlisted (line 314) — no change needed for `UndoRow`/`UndoAllButton`-style names. Every new public composable must also be registered in `ComponentRegistry`'s Voice Command family list XOR allowlisted in `INTENTIONALLY_UNREGISTERED`.

## No Analog Found

None — all 7 files/file-groups have a strong existing-codebase analog (see table above). The undo cross-repo shape validation (D-03) is a planning/reconvene concern, not a missing-pattern concern.

## Metadata

**Analog search scope:** `src/main/java/io/github/ygaray/yahirandroidtaste/{model,component,explorer,feedback,theme}/`
**Files scanned:** `KeyFieldState.kt`, `ApproachRungUiModel.kt`, `TagChipUiModel.kt`, `ApproachLadderCard.kt`, `RecordingBottomSheetContent.kt`, `AppChip.kt`, `DynamicActionButton.kt`, `AttentionCue.kt`, `SheetScaffold.kt`, `theme/Color.kt`, `VoiceCommandFamilyScreen.kt`, `UndoHistoryEntry.kt`, `UndoHistoryStore.kt`, `DomainVocabularyDriftGuardTest.kt` (all previously verified in RESEARCH.md this session; `KeyFieldState.kt`, `ApproachRungUiModel.kt`, `ApproachLadderCard.kt`, `DynamicActionButton.kt`, `VoiceCommandFamilyScreen.kt` re-read directly this pass for exact excerpts)
**Pattern extraction date:** 2026-09-30
