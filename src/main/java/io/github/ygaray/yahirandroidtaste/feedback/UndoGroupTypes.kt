package io.github.ygaray.yahirandroidtaste.feedback

/**
 * Group-level lifecycle status for a set of [UndoHistoryEntry] members sharing a `groupId`
 * (VUNDO-01, D-01) — derived from member statuses via [UndoHistoryStore.groupStatus], never
 * stored as a field on any entry or a new [UndoStatus] member.
 */
sealed interface UndoGroupStatus {
    /** Every member of the group is still [UndoStatus.Available]. */
    data object Undoable : UndoGroupStatus

    /** At least one member is [UndoStatus.Available] and at least one is spent (Undone/Failed). */
    data object PartiallyResolved : UndoGroupStatus

    /** No member is [UndoStatus.Available] — every member has been undone or failed. */
    data object FullyResolved : UndoGroupStatus

    /** The groupId is unknown, or every member has been evicted. */
    data object Empty : UndoGroupStatus
}

/**
 * Result of an atomic [UndoHistoryStore.attemptUndoGroup] call (VUNDO-01, D-01).
 */
sealed interface UndoGroupResult {
    /** `undoAll` ran successfully exactly once; [count] claimed members were marked [UndoStatus.Undone]. */
    data class Undone(val count: Int) : UndoGroupResult

    /**
     * The consumer's `undoAll` threw [UndoGroupRefusedException] — nothing was written, so every
     * claimed member remains [UndoStatus.Available] and a retried
     * [UndoHistoryStore.attemptUndoGroup] on the same group can still succeed.
     */
    data class Refused(val reason: String, val changedItem: String? = null) : UndoGroupResult

    /** `undoAll` threw any other exception; every claimed member was marked [UndoStatus.Failed]. */
    data object Failed : UndoGroupResult

    /** The groupId is unknown, or every member is already spent — `undoAll` was never invoked. */
    data object NothingToUndo : UndoGroupResult
}

/**
 * Typed refusal exception a consumer's `undoAll` lambda throws to signal
 * [UndoGroupResult.Refused] (VUNDO-01, D-01) — "nothing was written" semantics, so
 * [UndoHistoryStore.attemptUndoGroup] writes no status change on this path and every claimed
 * member stays [UndoStatus.Available], available for a future retry.
 *
 * @param reason Caller-formatted, human-readable refusal reason.
 * @param changedItem Optional identifier of the item that changed since the group was opened —
 *   omitted for consumers whose undo has no restore payload (e.g. CT's create-only writes, D-01).
 */
class UndoGroupRefusedException(
    val reason: String,
    val changedItem: String? = null
) : Exception(reason)
