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
 */
data class UndoRowUiModel(val id: String, val label: String, val state: UndoRowState)
