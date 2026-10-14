package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.runtime.Immutable

/**
 * One group row of the Undo Center (VUNDO-02, D-06): a header showing [label], the number of
 * member entries present in the list and a status, with a single "Undo all" action. The screen
 * stays stateless, so a consumer builds this projection from [UndoHistoryStore] on every
 * recomposition and nothing here is stored by the library.
 *
 * Every word on the header is caller-overridable: the Undo all button through [undoAllLabel], the
 * member count through [countLabel] and the status words through [undoableLabel],
 * [partlyResolvedLabel] and [resolvedLabel] (v2.7.1). The defaults are the English wording.
 *
 * @param groupId The store group id. Passed back to `onUndoGroup` when Undo all is tapped.
 * @param label The group's display label, typically `UndoHistoryStore.groupLabel(groupId)`.
 * @param memberIds The entry ids that belong to the group, matched against the screen's `entries`.
 *   Ids that are not present in `entries` are ignored, so a stale id is safe.
 * @param status The caller's projection of `UndoHistoryStore.groupStatus(groupId)`. Drives the
 *   status text and whether Undo all is shown (only for Undoable and PartiallyResolved).
 * @param undoAllLabel The text of the Undo all button. Defaults to `"Undo all"`.
 * @param countLabel Formats the number of members present, for plural rules the caller owns.
 *   `null` (the default) gives `"1 item"` / `"N items"`.
 * @param undoableLabel The status text of an [UndoGroupStatus.Undoable] group. Defaults to `"Undoable"`.
 * @param partlyResolvedLabel The status text of an [UndoGroupStatus.PartiallyResolved] group.
 *   Defaults to `"Partly resolved"`.
 * @param resolvedLabel The status text of an [UndoGroupStatus.FullyResolved] group. Defaults to
 *   `"Resolved"`. An [UndoGroupStatus.Empty] group shows only the count.
 */
@Immutable
data class UndoGroupRowUiModel(
    val groupId: String,
    val label: String,
    val memberIds: List<String>,
    val status: UndoGroupStatus,
    val undoAllLabel: String = "Undo all",
    val countLabel: ((count: Int) -> String)? = null,
    val undoableLabel: String = "Undoable",
    val partlyResolvedLabel: String = "Partly resolved",
    val resolvedLabel: String = "Resolved"
)
