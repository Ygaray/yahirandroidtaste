package io.github.ygaray.yahirandroidtaste.model

/**
 * Per-item row render state for a grouped undo affordance's [UndoRowUiModel]
 * ([io.github.ygaray.yahirandroidtaste.component.OutcomeSheet], VUNDO-01, D-01). Mirrors
 * [KeyFieldState]'s sealed-interface-as-own-file convention.
 */
sealed interface UndoRowState {
    /** The row's underlying entry is still undoable — tapping the row invokes [onUndo]. */
    data class Available(val onUndo: () -> Unit) : UndoRowState

    /** The row's underlying entry was already undone — renders muted, non-clickable. */
    data object Undone : UndoRowState

    /**
     * The row's underlying entry has no undo adapter for this action (e.g. an entangled item, or
     * a mutation the app can't undo individually) — renders [reason] as static, non-clickable
     * text and is never counted toward a group's undoable total (VUNDO-01).
     */
    data class Unavailable(val reason: String) : UndoRowState
}

/**
 * One row of a grouped undo affordance — built by the CONSUMER from
 * [io.github.ygaray.yahirandroidtaste.feedback.UndoHistoryStore.group]/`.groupStatus` reads,
 * never inside this library itself (INV-01).
 *
 * @param id Stable identifier — mirrors the underlying
 *   [io.github.ygaray.yahirandroidtaste.feedback.UndoHistoryEntry.id].
 * @param label Caller-formatted display text for the row.
 * @param state The row's current render state.
 * @param undoneLabel Caller-localizable text rendered on a row whose [state] is
 *   [UndoRowState.Undone]; English default `"Undone"`, plain text (VI18N-04).
 */
data class UndoRowUiModel @JvmOverloads constructor(
    val id: String,
    val label: String,
    val state: UndoRowState,
    val undoneLabel: String = "Undone"
) {
    // Hand-written pre-v2.5 `copy` arity. Without it Metalava reports the shipped JVM
    // `copy(String, String, UndoRowState)` as a removed method once [undoneLabel] is appended
    // (the compiler only generates the full-arity copy). Delegates with the CURRENT undoneLabel so
    // a legacy three-argument copy never resets a caller's custom label. A body `var` (the
    // TagChipUiModel alternative) is rejected because it would cost Compose all-val stability.
    fun copy(id: String, label: String, state: UndoRowState): UndoRowUiModel =
        copy(id = id, label = label, state = state, undoneLabel = undoneLabel)

    // v2.4.x binary-compatibility shim (INC-2026-10-05-02 F1c): re-emits the v2.4.1 compiler-generated
    // static `copy$default` (v2.4.1 had no defaulted ctor parameter, so no default-ctor synthetic).
    // Mask bit i means v2.4.1 parameter i took its default. See HandledByUiModel for the full
    // rationale. Hidden from Kotlin and Java source: do not call it or document it as API.
    private companion object {
        @Deprecated("Binary compatibility with v2.4.x", level = DeprecationLevel.HIDDEN)
        @JvmStatic
        @JvmName("copy\$default")
        fun legacyCopyDefault(
            self: UndoRowUiModel,
            id: String?,
            label: String?,
            state: UndoRowState?,
            mask: Int,
            marker: Any?
        ): UndoRowUiModel = self.copy(
            id = if (mask and (1 shl 0) != 0) self.id else requireNotNull(id),
            label = if (mask and (1 shl 1) != 0) self.label else requireNotNull(label),
            state = if (mask and (1 shl 2) != 0) self.state else requireNotNull(state),
            undoneLabel = self.undoneLabel
        )
    }
}
