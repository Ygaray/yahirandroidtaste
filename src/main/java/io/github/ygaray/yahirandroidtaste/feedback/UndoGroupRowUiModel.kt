package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.runtime.Immutable

/**
 * One group row of the Undo Center (VUNDO-02, D-06): a header showing [label], the number of
 * member entries present in the list and a status, with a single "Undo all" action. The screen
 * stays stateless, so a consumer builds this projection from [UndoHistoryStore] on every
 * recomposition and nothing here is stored by the library.
 *
 * The member count and the status words are English-only library text under this shape; only the
 * Undo all button label is caller-overridable through [undoAllLabel].
 *
 * @param groupId The store group id. Passed back to `onUndoGroup` when Undo all is tapped.
 * @param label The group's display label, typically `UndoHistoryStore.groupLabel(groupId)`.
 * @param memberIds The entry ids that belong to the group, matched against the screen's `entries`.
 *   Ids that are not present in `entries` are ignored, so a stale id is safe.
 * @param status The caller's projection of `UndoHistoryStore.groupStatus(groupId)`. Drives the
 *   status text and whether Undo all is shown (only for Undoable and PartiallyResolved).
 * @param undoAllLabel The text of the Undo all button. Defaults to `"Undo all"`.
 */
@Immutable
data class UndoGroupRowUiModel(
    val groupId: String,
    val label: String,
    val memberIds: List<String>,
    val status: UndoGroupStatus,
    val undoAllLabel: String = "Undo all"
)
