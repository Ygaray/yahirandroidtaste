package io.github.ygaray.yahirandroidtaste.model

import androidx.compose.runtime.Composable

/**
 * One proposed item inside a [VoiceOutcomeUiState.NeedsConfirmation] (VOUT-04, D-01/D-04/D-06). A
 * [VoiceOutcomeUiState.NeedsConfirmation.items] list of size 1 is a single confirm (SecondBrain's
 * `MutationGate`/`VoiceConfirmGate` risk confirm, CalTracker's weak single match); size >1 is a
 * batch (CalTracker's `ProposedBatch`).
 *
 * [toString] intentionally omits [title]/[subtitle]/[confidenceCue] -- never print a subject's
 * own name (D-05 privacy, mirrors SecondBrain's own `PendingConfirmation.toString()` precedent,
 * T-166-05). [id] is treated as an opaque `String`, compared only via Kotlin's standard `String`
 * equality -- never normalized, case-folded, or reinterpreted (D-01, mirrors
 * [ClarificationOptionUiModel.id]'s established opaque-id convention).
 *
 * @param id Opaque stable identifier for this item. Required.
 * @param title The item's own display title. Required. Never printed by [toString].
 * @param subtitle An optional secondary line (e.g. a quantity). `null` renders nothing. Never
 *   printed by [toString].
 * @param confidenceCue An optional "weak match, check this one" cue (D-06; mirrors CalTracker's
 *   `needsAttention`). `null` renders nothing. Never printed by [toString].
 * @param amended Whether the caller has in-place-edited this item (D-04). Defaults to `false`.
 *   The library intentionally renders no visual treatment for this flag anywhere in
 *   [io.github.ygaray.yahirandroidtaste.component.OutcomeSheet] -- any visual indicator (e.g. an
 *   "Edited" label/icon) is the consumer's own responsibility, typically via [trailingContent].
 * @param onRemove Invoked when this row's own remove control is tapped. `null` hides the remove
 *   control entirely for this row (null-prop-hides, D-04).
 * @param trailingContent An opaque per-item slot the library never interprets (D-01) -- e.g.
 *   SecondBrain's risk badge or CalTracker's `AmountEditor`/`ItemCorrectionDropdown`. `null`
 *   renders nothing.
 */
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

/**
 * How a batch [VoiceOutcomeUiState.NeedsConfirmation] resolves to a confirm action (D-03/D-06).
 */
enum class SelectionMode {
    /**
     * Confirm acts on EVERY item currently in `items` (after any per-item removals). CalTracker's
     * real batch flow (edit/remove rows, then one "Confirm all (N)").
     */
    AllOrNothing,

    /**
     * Reserved for a future per-item include/exclude UI. No live consumer (SecondBrain or
     * CalTracker) currently exercises this -- deliberately left without a designed rendering
     * recipe this phase.
     */
    PerItem
}
