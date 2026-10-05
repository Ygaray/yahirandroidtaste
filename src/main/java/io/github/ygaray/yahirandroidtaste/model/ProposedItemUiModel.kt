package io.github.ygaray.yahirandroidtaste.model

import androidx.compose.runtime.Composable
import kotlin.jvm.internal.DefaultConstructorMarker

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
 * @param removeContentDescription Caller-localizable accessibility description of this row's remove
 *   control; English default `"Remove"`, plain text. Set it on every item that supplies [onRemove]
 *   (VI18N-04). Never printed by [toString].
 */
data class ProposedItemUiModel @JvmOverloads constructor(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val confidenceCue: String? = null,
    val amended: Boolean = false,
    val onRemove: (() -> Unit)? = null,
    val trailingContent: (@Composable () -> Unit)? = null,
    val removeContentDescription: String = "Remove"
) {
    // Hand-written pre-v2.5 `copy` arity. Without it Metalava reports the shipped JVM seven-parameter
    // `copy` as a removed method once [removeContentDescription] is appended (the compiler only
    // generates the full-arity copy). Delegates with the CURRENT removeContentDescription so a legacy
    // seven-argument copy never resets a caller's custom label. A body `var` is rejected because it
    // would cost Compose all-val stability.
    fun copy(
        id: String,
        title: String,
        subtitle: String?,
        confidenceCue: String?,
        amended: Boolean,
        onRemove: (() -> Unit)?,
        trailingContent: (@Composable () -> Unit)?
    ): ProposedItemUiModel = copy(
        id = id,
        title = title,
        subtitle = subtitle,
        confidenceCue = confidenceCue,
        amended = amended,
        onRemove = onRemove,
        trailingContent = trailingContent,
        removeContentDescription = removeContentDescription
    )

    // v2.4.x binary-compatibility shims (INC-2026-10-05-02 F1c): re-emit the v2.4.1 compiler-generated
    // default-argument constructor and static `copy$default`. Mask bit i means v2.4.1 parameter i
    // took its default. See HandledByUiModel for the full rationale. Hidden from Kotlin and Java
    // source: do not call them or document them as API.
    @Deprecated("Binary compatibility with v2.4.x", level = DeprecationLevel.HIDDEN)
    constructor(
        id: String,
        title: String,
        subtitle: String?,
        confidenceCue: String?,
        amended: Boolean,
        onRemove: (() -> Unit)?,
        trailingContent: (@Composable () -> Unit)?,
        mask: Int,
        marker: DefaultConstructorMarker?
    ) : this(
        id = id,
        title = title,
        subtitle = if (mask and (1 shl 2) != 0) null else subtitle,
        confidenceCue = if (mask and (1 shl 3) != 0) null else confidenceCue,
        amended = if (mask and (1 shl 4) != 0) false else amended,
        onRemove = if (mask and (1 shl 5) != 0) null else onRemove,
        trailingContent = if (mask and (1 shl 6) != 0) null else trailingContent
    )

    private companion object {
        @Deprecated("Binary compatibility with v2.4.x", level = DeprecationLevel.HIDDEN)
        @JvmStatic
        @JvmName("copy\$default")
        fun legacyCopyDefault(
            self: ProposedItemUiModel,
            id: String?,
            title: String?,
            subtitle: String?,
            confidenceCue: String?,
            amended: Boolean,
            onRemove: (() -> Unit)?,
            trailingContent: (@Composable () -> Unit)?,
            mask: Int,
            marker: Any?
        ): ProposedItemUiModel = self.copy(
            id = if (mask and (1 shl 0) != 0) self.id else requireNotNull(id),
            title = if (mask and (1 shl 1) != 0) self.title else requireNotNull(title),
            subtitle = if (mask and (1 shl 2) != 0) self.subtitle else subtitle,
            confidenceCue = if (mask and (1 shl 3) != 0) self.confidenceCue else confidenceCue,
            amended = if (mask and (1 shl 4) != 0) self.amended else amended,
            onRemove = if (mask and (1 shl 5) != 0) self.onRemove else onRemove,
            trailingContent = if (mask and (1 shl 6) != 0) self.trailingContent else trailingContent,
            removeContentDescription = self.removeContentDescription
        )
    }

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
