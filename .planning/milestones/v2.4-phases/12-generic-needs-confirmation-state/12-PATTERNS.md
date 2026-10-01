# Phase 12: Generic needs-confirmation state - Pattern Map

**Mapped:** 2026-09-30
**Files analyzed:** 5 (2 extended existing files, 2 new model files, 1 extended gallery/fixture file; test file extension is a 6th)
**Analogs found:** 5 / 5 (all have strong in-repo analogs — this phase extends files that already contain the pattern to mirror)

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `model/VoiceOutcomeUiState.kt` (add `NeedsConfirmation` arm) | model | transform (sealed-state render input) | same file, `Success`/`Failure` arms (lines 15-40 pattern) + `UndoAffordanceUiModel.kt` (all-val, null-prop-hides) | exact (editing the file itself) |
| `model/ProposedItemUiModel.kt` (new) | model | transform | `model/UndoAffordanceUiModel.kt` / `UndoRowUiModel` (nested in same package) | role-match |
| `model/SelectionMode.kt` (new) | model/config (enum) | transform | `component/DynamicActionButton.kt`'s `ActionButtonDefaults.ActionButtonRole` enum | role-match |
| `component/OutcomeSheet.kt` (extend `when`, add 2 private composables) | component | request-response (props-in, render-out) | same file's `SuccessBody`/`UndoAffordanceBody`/`UndoRowItem`/`FailureBody` (private body-function pattern) | exact |
| `explorer/VoiceCommandFamilyScreen.kt` (extend `OutcomeSheetVariants()`) | component (gallery fixture) | request-response | same file's existing `OutcomeSheetVariants()` fixture buttons | exact |
| `src/test/.../OutcomeSheetTest.kt` (extend) | test | request-response | same file's existing `// ── VOUT-0N ──` test sections | exact |

## Pattern Assignments

### `model/VoiceOutcomeUiState.kt` (model, transform) — EXTEND

**Analog:** itself (the file being edited) — `Success`/`Failure` arms, and `model/UndoAffordanceUiModel.kt` for the all-`val`/null-prop-hides convention.

**Current shape to extend (verified live, full file read in RESEARCH.md):**
```kotlin
sealed interface VoiceOutcomeUiState {
    data class Success(
        val summary: String,
        val handledBy: HandledByUiModel? = null,
        val editableContent: (@Composable () -> Unit)? = null,
        val inFlight: Boolean = false,
        val batchResults: List<BatchRowResultUiModel> = emptyList(),
        val undo: UndoAffordanceUiModel? = null
    ) : VoiceOutcomeUiState

    data class Failure(
        val reason: String,
        val handledBy: HandledByUiModel? = null,
        val action: FailureActionUiModel? = null
    ) : VoiceOutcomeUiState
}
```
KDoc contract to preserve verbatim in spirit (lines 12-14): the top level must stay stable; add `NeedsConfirmation` as a NEW sealed subtype, never reshape `Success`/`Failure`.

**Core pattern — add new arm (illustrative, field-level is a planning decision per RESEARCH.md Code Examples):**
```kotlin
data class NeedsConfirmation(
    val reason: String,
    val items: List<ProposedItemUiModel>,
    val selectionMode: SelectionMode = SelectionMode.AllOrNothing,
    val title: String? = null,
    val severity: ActionButtonDefaults.ActionButtonRole = ActionButtonDefaults.ActionButtonRole.Neutral,
    val reversibilityHint: String? = null,
    val confirmLabel: String = "Confirm",
    val cancelLabel: String = "Cancel",
    val topLevelContent: (@Composable () -> Unit)? = null,
    val onConfirm: () -> Unit,
    val onCancel: () -> Unit
) : VoiceOutcomeUiState
```

**Null-prop-hides pattern to copy** (from `UndoAffordanceUiModel.kt` lines 15-16, 20-24): `null` on an optional callback/content field means "render nothing for this," not an error state — apply the same convention to `topLevelContent`, `onRemove` (on the new item model), etc.

---

### `model/ProposedItemUiModel.kt` (model, transform) — NEW FILE

**Analog:** `model/UndoAffordanceUiModel.kt` (package `io.github.ygaray.yahirandroidtaste.model`, all-`val` data class, KDoc documenting null-hides semantics and the library's one-way-dependency discipline).

**Imports pattern** (mirror `UndoAffordanceUiModel.kt` line 1 + add Compose import for the slot):
```kotlin
package io.github.ygaray.yahirandroidtaste.model

import androidx.compose.runtime.Composable
```

**Core pattern** (field-level per RESEARCH.md, cross-checked against CT's `BatchItemState.needsAttention: Boolean` and `onDeleteBatchItem(Int)`):
```kotlin
data class ProposedItemUiModel(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val confidenceCue: String? = null,
    val amended: Boolean = false,
    val onRemove: (() -> Unit)? = null,
    val trailingContent: (@Composable () -> Unit)? = null
) {
    override fun toString(): String = "ProposedItemUiModel(id=$id, amended=$amended)"
}
```

**Privacy `toString()` override pattern — copy verbatim technique from SB's live `PendingConfirmation.toString()`** (cited in RESEARCH.md, `VoiceConfirmGate.kt:30-35`):
```kotlin
// SB's precedent (cross-repo, not in this codebase — technique to mirror, not code to import):
override fun toString(): String = "PendingConfirmation(id=$id, subject=${subject::class.simpleName})"
```
Apply the same discipline: never let the default `data class` auto-`toString()` print `title`/`subtitle`/`confidenceCue`.

**`copy()`-ABI lesson** — read `model/TagChipUiModel.kt` in full before finalizing field order/defaults; this file is cited in RESEARCH.md as the precedent for designing new public data classes additive-safe from day one (frozen all-`val` constructors, default values on every optional field so a future added field doesn't break existing call-sites' positional `copy()` usage).

---

### `model/SelectionMode.kt` (model/enum, transform) — NEW FILE

**Analog:** `component/DynamicActionButton.kt`'s `ActionButtonDefaults.ActionButtonRole` enum (verified live, `DynamicActionButton.kt:64-85`) — same "small, nested-or-file-local enum with no behavior, just a rendering switch" pattern.

**Core pattern:**
```kotlin
package io.github.ygaray.yahirandroidtaste.model

enum class SelectionMode {
    AllOrNothing,
    PerItem
}
```
Reference analog for enum style (`ActionButtonDefaults.ActionButtonRole`, verified `DynamicActionButton.kt:64-85`):
```kotlin
object ActionButtonDefaults {
    enum class ActionButtonRole { Destructive, Save, Neutral }
    @Composable
    fun colors(role: ActionButtonRole) = when (role) {
        ActionButtonRole.Destructive -> ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ActionButtonRole.Save -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
        ActionButtonRole.Neutral -> ButtonDefaults.textButtonColors()
    }
}
```
**Do not invent a new severity enum** — reuse this existing public `ActionButtonDefaults.ActionButtonRole` for `NeedsConfirmation.severity` (D-05's "destructive confirm" ask maps to `Destructive`).

---

### `component/OutcomeSheet.kt` (component, request-response) — EXTEND

**Analog:** itself — the existing exhaustive `when` + private body-function pattern (`SuccessBody`, `UndoAffordanceBody`, `UndoRowItem`, `BatchResultsList`, `FailureBody`, `HandledByRow`).

**Imports pattern** (already present in file; no new imports needed beyond what `ProposedItemUiModel`/`SelectionMode` require):
```kotlin
// existing OutcomeSheet.kt imports already cover Compose/Material3/Dimens/DynamicActionButton
```

**Core pattern — the exhaustive `when`** (verified live, `OutcomeSheet.kt:47-60`):
```kotlin
@Composable
fun OutcomeSheet(
    outcome: VoiceOutcomeUiState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier) {
        when (outcome) {
            is VoiceOutcomeUiState.Success -> SuccessBody(outcome)
            is VoiceOutcomeUiState.Failure -> FailureBody(outcome)
            // ADD: is VoiceOutcomeUiState.NeedsConfirmation -> NeedsConfirmationBody(outcome)
        }
    }
}
```
The compiler enforces this edit (exhaustive sealed `when`) — adding the arm to `VoiceOutcomeUiState.kt` will fail the build here until this branch is added.

**Private body-function pattern to mirror** (structure only — exact layout is illustrative per RESEARCH.md):
```kotlin
@Composable
private fun NeedsConfirmationBody(confirmation: VoiceOutcomeUiState.NeedsConfirmation) {
    Column(modifier = Modifier.fillMaxWidth().padding(Dimens.HorizontalPadding)) {
        confirmation.title?.let { Text(it, style = MaterialTheme.typography.headlineSmall) }
        Text(confirmation.reason, style = MaterialTheme.typography.bodyLarge)
        confirmation.reversibilityHint?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        confirmation.topLevelContent?.invoke()
        confirmation.items.forEach { item -> ProposedItemRow(item) }
        Row(modifier = Modifier.padding(top = Dimens.ContentSpacing)) {
            DynamicActionButton(
                label = confirmation.cancelLabel,
                role = ActionButtonDefaults.ActionButtonRole.Neutral,
                onClick = confirmation.onCancel,
                modifier = Modifier.testTag("outcome_sheet_confirmation_cancel")
            )
            DynamicActionButton(
                label = confirmation.confirmLabel,
                role = confirmation.severity,
                onClick = confirmation.onConfirm,
                modifier = Modifier.testTag("outcome_sheet_confirmation_confirm")
            )
        }
    }
}

@Composable
private fun ProposedItemRow(item: ProposedItemUiModel) {
    Row(modifier = Modifier.fillMaxWidth().testTag("outcome_sheet_confirmation_item")) {
        Column(Modifier.weight(1f)) {
            Text(item.title)
            item.subtitle?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
            item.confidenceCue?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) }
        }
        item.trailingContent?.invoke()
        item.onRemove?.let { onRemove -> IconButton(onClick = onRemove) { /* remove icon */ } }
    }
}
```

**Error handling pattern:** N/A — pure presentational composables, no try/catch; errors surface through `Failure` arm (unchanged), not this arm.

**Testing/testTag convention to copy:** every interactive element gets a `Modifier.testTag("outcome_sheet_<thing>")` string, matching `UndoRowItem`/`BatchResultsList`'s existing tags (e.g. `"outcome_sheet_confirmation_confirm"`, `"outcome_sheet_confirmation_cancel"`, `"outcome_sheet_confirmation_item"`).

---

### `explorer/VoiceCommandFamilyScreen.kt` (component/gallery fixture) — EXTEND

**Analog:** itself — the existing `OutcomeSheetVariants()` function (currently lines ~395-443) and the already-registered `OutcomeSheet` `ComponentRegistry.Entry` (lines 143-160).

**Core pattern:** add new fixture `VoiceOutcomeUiState.NeedsConfirmation` instances + "Show sheet" demo buttons INSIDE the existing `OutcomeSheetVariants()` — do **not** add a new `ComponentRegistry.Entry` (would trip the registry's duplicate-name `init` check at `explorer/ComponentRegistry.kt:136-145`). Cover at minimum: single-item confirm, batch confirm with `topLevelContent`, a destructive-severity confirm.

---

### `src/test/.../OutcomeSheetTest.kt` (test, request-response) — EXTEND

**Analog:** itself — existing `// ── VOUT-0N ── ...` sectioned, backtick-quoted-name test convention (verified partial read, `OutcomeSheetTest.kt:1-24`+).

**Core pattern:** add a new `// ── VOUT-04 ── ...` section with Robolectric + Compose UI test cases for: single confirm render + callback firing, batch confirm render (all rows + `topLevelContent` once) + `onRemove` firing, destructive-severity button color. Framework: `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()` — unchanged from existing tests in the file.

---

## Shared Patterns

### Private body-function pattern (sealed-state rendering)
**Source:** `component/OutcomeSheet.kt` — `SuccessBody` (lines 70-83), `UndoAffordanceBody` (94-131), `UndoRowItem` (148-187), `BatchResultsList` (190-219), `FailureBody` (229-254), `HandledByRow` (261-279)
**Apply to:** `NeedsConfirmationBody`, `ProposedItemRow` — both stay `private`, file-local, never separately registered in `ComponentRegistry`, never need a drift-guard allowlist entry (both guards scan only public top-level `@Composable` functions).

### Severity/role styling
**Source:** `component/DynamicActionButton.kt:64-85` (`ActionButtonDefaults.ActionButtonRole`, already public)
```kotlin
enum class ActionButtonRole { Destructive, Save, Neutral }
```
**Apply to:** `NeedsConfirmation.severity` field and both Confirm/Cancel `DynamicActionButton` calls in `NeedsConfirmationBody`. Do not invent a new `ConfirmSeverity` enum — `ConfirmationDialog.kt`'s separate `ConfirmStyle` enum is a different, NOT-reused primitive (confirmed wrong tool for this phase).

### All-`val`, frozen-constructor, null-prop-hides model convention
**Source:** `model/UndoAffordanceUiModel.kt` (full file, 26 lines), `model/TagChipUiModel.kt` (copy()/ABI lesson)
**Apply to:** `ProposedItemUiModel`, the `NeedsConfirmation` arm — every field a `val`, optional callbacks/content default to `null` meaning "render nothing," defaults chosen so future field additions don't break existing positional usage.

### Privacy-safe `toString()` override
**Source (cross-repo precedent, technique only):** SecondBrain's `VoiceConfirmGate.kt:30-35`, `PendingConfirmation.toString()` — `"PendingConfirmation(id=$id, subject=${subject::class.simpleName})"`
**Apply to:** `ProposedItemUiModel.toString()` — must omit `title`/`subtitle`/`confidenceCue`, print only `id` (+ optionally `amended`).

### `testTag` naming convention
**Source:** `component/OutcomeSheet.kt`'s existing tags on `UndoRowItem`/`BatchResultsList`/`FailureBody` elements
**Apply to:** every new interactive element in `NeedsConfirmationBody`/`ProposedItemRow`, prefixed `"outcome_sheet_confirmation_..."`.

## No Analog Found

None. Every new/modified file has a strong in-repo analog (see table above); this phase is purely additive on top of patterns Phase 11 already established in the same two files (`VoiceOutcomeUiState.kt`, `OutcomeSheet.kt`).

## Metadata

**Analog search scope:** `src/main/java/io/github/ygaray/yahirandroidtaste/{model,component,explorer}/`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/`
**Files scanned:** `VoiceOutcomeUiState.kt`, `OutcomeSheet.kt`, `ConfirmationDialog.kt`, `DynamicActionButton.kt`, `UndoAffordanceUiModel.kt`, `TagChipUiModel.kt`, `VoiceCommandFamilyScreen.kt`, `OutcomeSheetTest.kt`, `ComponentRegistry.kt` (all git-tracked, verified via `git ls-files`)
**Pattern extraction date:** 2026-09-30
