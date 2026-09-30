package io.github.ygaray.yahirandroidtaste.feedback

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Session-durable, in-memory undo state machine — the architectural core of Phase 53
 * (UNDO-01/02/03). Centralizes append, the atomic consumed-guard, best-effort undo
 * execution, scoped clear, and the D-10 status-aware 50-cap eviction in ONE singleton
 * so first-consumer-wins is provable across the two independent dispatcher sites
 * (`FeedbackController`, `SheetFeedbackHost`) wired in plan 53-03.
 *
 * Pure Kotlin + `javax.inject` only — no Compose/Nav dependency — keeps
 * `:designsystem/feedback` extraction-ready.
 */
@Singleton
class UndoHistoryStore @Inject constructor() {

    private val _entries = MutableStateFlow<List<UndoHistoryEntry>>(emptyList())

    /** Exposed newest-first; stable insertion order is preserved for tied timestamps. */
    val entries: StateFlow<List<UndoHistoryEntry>> = _entries.asStateFlow()

    // ---- VUNDO-01 / D-01 additive grouping state (Phase 11) ----
    // Grouping lives ENTIRELY in these private maps -- UndoHistoryEntry's internal primary
    // constructor and UndoStatus's exactly-3-member enum stay byte-for-byte unchanged (no ABI
    // break). The existing _entries/entries/3-arg append/attemptUndo/clearSpent's ungrouped
    // semantics are untouched below.

    /** entryId -> groupId. The single source of truth for group membership (never duplicated). */
    private val groupIdByEntryId: MutableMap<String, String> = mutableMapOf()

    /** A group's label + its ONE atomic undo action, registered via [openGroup]. */
    private data class GroupMeta(val label: String, val undoAll: suspend () -> Unit)
    private val groupMeta: MutableMap<String, GroupMeta> = mutableMapOf()

    /**
     * Guards [attemptUndoGroup]'s whole claim -> run -> resolve critical section. This IS the
     * group claim's atomicity/releasability boundary (Pitfall 1) -- [UndoHistoryEntry.tryConsume]
     * is deliberately NEVER called from the group path, since its one-way `AtomicBoolean` CAS has
     * no release path and would permanently strand a claimed-then-Refused member.
     */
    private val groupUndoMutex = Mutex()

    /**
     * Appends one [UndoStatus.Available] entry for a [FeedbackEvent.WithUndo]-shaped
     * input and returns its id. Applies the D-10 status-aware 50-cap eviction so size
     * never exceeds 50 after this call.
     *
     * @param preview Optional snapshot preview payload (UNDO-04, D-03) — defaulted null
     *                so un-migrated producers keep compiling unchanged. Deliberately
     *                positioned BEFORE [undoAction] (not after) so [undoAction] stays the
     *                LAST parameter — every existing call site passes it as a trailing
     *                lambda (`store.append(message) { ... }`), and Kotlin's trailing-lambda
     *                syntax always binds to the last parameter.
     */
    fun append(message: String, preview: UndoPreview? = null, undoAction: suspend () -> Unit): String {
        val entry = UndoHistoryEntry(
            id = UUID.randomUUID().toString(),
            message = message,
            timestamp = System.currentTimeMillis(),
            undoAction = undoAction,
            preview = preview
        )
        // Newest-first: new entry is prepended; evictIfNeeded operates in this same
        // newest-first ordering so "oldest" always means "last in the list".
        _entries.update { current -> evictIfNeeded(listOf(entry) + current) }
        return entry.id
    }

    /**
     * Grouped append overload (VUNDO-01, D-01) -- records `entry.id -> groupId` in
     * [groupIdByEntryId] and runs through the SAME [evictIfNeeded] eviction path as the ungrouped
     * 3-arg [append]. Disambiguated from the ungrouped overload by Kotlin's overload resolution
     * whenever a caller names [groupId] explicitly (it carries no default value), e.g.
     * `store.append(msg, groupId = groupId) { ... }` -- no ambiguity exists between the two.
     *
     * @param groupId The group this entry belongs to. Must have a corresponding [openGroup] call
     *   for [attemptUndoGroup] to find its atomic undo action, though this method itself does not
     *   require one to have been registered yet.
     * @param undoAction The per-item undo action, reachable via the pre-existing, unchanged
     *   [attemptUndo] path independent of the group's own `undoAll` (see [attemptUndoGroup]'s
     *   KDoc for the documented residual concurrency gap between the two paths).
     */
    fun append(
        message: String,
        preview: UndoPreview? = null,
        groupId: String,
        undoAction: suspend () -> Unit
    ): String {
        val entry = UndoHistoryEntry(
            id = UUID.randomUUID().toString(),
            message = message,
            timestamp = System.currentTimeMillis(),
            undoAction = undoAction,
            preview = preview
        )
        groupIdByEntryId[entry.id] = groupId
        _entries.update { current -> evictIfNeeded(listOf(entry) + current) }
        return entry.id
    }

    /**
     * Registers [groupId]'s label and its ONE atomic undo action (VUNDO-01, D-01) — the action
     * [attemptUndoGroup] runs exactly once for the whole group, never once per member.
     */
    fun openGroup(groupId: String, label: String, undoAll: suspend () -> Unit) {
        groupMeta[groupId] = GroupMeta(label, undoAll)
    }

    /** Returns [groupId]'s members in the store's existing newest-first order (VUNDO-01, D-01). */
    fun group(groupId: String): List<UndoHistoryEntry> =
        _entries.value.filter { groupIdByEntryId[it.id] == groupId }

    /** The groupId [entryId] belongs to, or `null` if [entryId] is ungrouped or unknown. */
    fun groupIdOf(entryId: String): String? = groupIdByEntryId[entryId]

    /** [groupId]'s registered label, or `null` if no [openGroup] call has registered one. */
    fun groupLabel(groupId: String): String? = groupMeta[groupId]?.label

    /**
     * [groupId]'s current lifecycle status (VUNDO-01, D-01), computed from [entries]' live
     * member statuses -- never stored as a field.
     */
    fun groupStatus(groupId: String): UndoGroupStatus = computeGroupStatus(groupId, _entries.value)

    /**
     * The group-status computation shared by [groupStatus] (against the live [entries]) and
     * [clearSpent]/[evictIfNeeded] (against their own in-flight `current` list, never a stale
     * read of [entries] mid-update) — takes [source] explicitly rather than reading [_entries]
     * internally.
     */
    private fun computeGroupStatus(groupId: String, source: List<UndoHistoryEntry>): UndoGroupStatus {
        val members = source.filter { groupIdByEntryId[it.id] == groupId }
        if (members.isEmpty()) return UndoGroupStatus.Empty
        val availableCount = members.count { it.status == UndoStatus.Available }
        return when {
            availableCount == members.size -> UndoGroupStatus.Undoable
            availableCount == 0 -> UndoGroupStatus.FullyResolved
            else -> UndoGroupStatus.PartiallyResolved
        }
    }

    /**
     * Atomically attempts to undo [groupId] as ONE unit (VUNDO-01, D-01). The whole claim -> run
     * -> resolve section runs inside [groupUndoMutex] -- this `Mutex` IS the atomicity boundary;
     * [UndoHistoryEntry.tryConsume] is deliberately NEVER called here (Pitfall 1), since its
     * one-way `AtomicBoolean` CAS has no release path and would permanently strand a
     * claimed-then-[UndoGroupResult.Refused] member.
     *
     * On success, [groupMeta]'s registered `undoAll` runs exactly ONCE (never once per member)
     * and every claimed member is marked [UndoStatus.Undone]. On a typed
     * [UndoGroupRefusedException], NO status change is written at all -- the claimed entries were
     * read as [UndoStatus.Available] and D-01 requires they remain so, which holds automatically
     * since nothing was ever written; a retried [attemptUndoGroup] on the same group can still
     * succeed. Any other exception marks every claimed member [UndoStatus.Failed] (mirrors
     * [attemptUndo]'s existing best-effort-never-crash convention).
     *
     * Documented residual gap (RESEARCH.md Pitfall 4): a concurrent per-item [attemptUndo] on a
     * grouped entry is NOT mutually excluded against a live [attemptUndoGroup] on that entry's
     * group -- [attemptUndo] stays unchanged per D-01. A consumer wanting to close this race
     * should represent every row of an in-flight group as [io.github.ygaray.yahirandroidtaste.model.UndoRowState.Unavailable]
     * (reusing the existing generic entangled-item state) for the duration of its own
     * [attemptUndoGroup] call, rather than this store adding any new locking primitive -- this is
     * a consumer-side UI-projection choice, not a store enforcement gap.
     */
    @Suppress("TooGenericExceptionCaught")
    suspend fun attemptUndoGroup(groupId: String): UndoGroupResult = groupUndoMutex.withLock {
        val meta = groupMeta[groupId] ?: return@withLock UndoGroupResult.NothingToUndo
        val claimed = group(groupId).filter { it.status == UndoStatus.Available }
        if (claimed.isEmpty()) return@withLock UndoGroupResult.NothingToUndo

        try {
            meta.undoAll()
            val claimedIds = claimed.map { it.id }.toSet()
            _entries.update { current ->
                current.map { if (it.id in claimedIds) it.withStatus(UndoStatus.Undone) else it }
            }
            UndoGroupResult.Undone(count = claimed.size)
        } catch (e: CancellationException) {
            throw e
        } catch (e: UndoGroupRefusedException) {
            UndoGroupResult.Refused(reason = e.reason, changedItem = e.changedItem)
        } catch (e: Exception) {
            val claimedIds = claimed.map { it.id }.toSet()
            _entries.update { current ->
                current.map { if (it.id in claimedIds) it.withStatus(UndoStatus.Failed) else it }
            }
            UndoGroupResult.Failed
        }
    }

    /**
     * Best-effort undo (D-08/UNDO-03). Called by BOTH the snackbar path and the
     * center's tap — the [UndoHistoryEntry.tryConsume] CAS guard is what makes this
     * safe under concurrent callers: only the first caller to claim the entry runs
     * [UndoHistoryEntry.undoAction]; every subsequent caller (sequential or
     * concurrent) is a no-op.
     *
     * Catches [Exception] (not [Throwable]) so coroutine cancellation still
     * propagates — a caught failure marks the entry [UndoStatus.Failed], never
     * silently leaves it [UndoStatus.Available] and never crashes the app.
     *
     * Gap-closure fix (53-05 SC#3): the status transition REPLACES the entry in
     * `_entries` with a new, value-distinct instance ([UndoHistoryEntry.withStatus])
     * instead of mutating the existing instance's field in place. Because
     * [UndoHistoryEntry] is now a `data class`, the rebuilt list is genuinely
     * structurally different from the prior emission, so `MutableStateFlow` reliably
     * emits and an already-subscribed `UndoCenterScreen` recomposes.
     */
    @Suppress("TooGenericExceptionCaught")
    suspend fun attemptUndo(id: String) {
        val entry = _entries.value.firstOrNull { it.id == id } ?: return
        if (entry.status != UndoStatus.Available) return // already spent — no-op
        if (!entry.tryConsume()) return // lost the race — first-consumer-wins, no-op

        val newStatus = try {
            entry.undoAction()
            UndoStatus.Undone
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            UndoStatus.Failed
        }
        _entries.update { current ->
            current.map { if (it.id == id) it.withStatus(newStatus) else it }
        }
    }

    /**
     * Removes only [UndoStatus.Undone]/[UndoStatus.Failed] entries (D-08 scoped
     * clear). Every [UndoStatus.Available] entry is left untouched — a live,
     * undoable entry can never be lost to Clear. No-op when there is nothing spent
     * to remove (empty list, or all entries still Available).
     *
     * Group-aware (VUNDO-01/D-01, Pitfall 2): an ungrouped entry keeps this exact predicate
     * unchanged. A GROUPED entry is only removed once its ENTIRE group has resolved to
     * [UndoGroupStatus.FullyResolved] -- a partially-resolved group's spent members are never
     * dropped ahead of its still-Available members, so [group]/[groupStatus] reads never see an
     * incomplete member list mid-resolution.
     */
    fun clearSpent() {
        _entries.update { current ->
            current.filterNot { entry ->
                val groupId = groupIdByEntryId[entry.id]
                if (groupId == null) {
                    entry.status == UndoStatus.Undone || entry.status == UndoStatus.Failed
                } else {
                    computeGroupStatus(groupId, current) == UndoGroupStatus.FullyResolved
                }
            }
        }
    }

    /**
     * D-10: on insert past the 50-entry cap, evict the oldest spent (Undone/Failed)
     * entry first; if none exist (all Available), fall back to evicting the oldest
     * Available entry. [current] is newest-first, so "oldest" is the last element.
     *
     * Group-aware (VUNDO-01/D-01, Pitfall 2): the eviction TARGET is chosen by the identical,
     * unchanged oldest-spent-then-oldest-available selection. If that target entry is ungrouped,
     * exactly that one entry is evicted, same as before. If the target belongs to a group, EVERY
     * member sharing that groupId is evicted together in the same pass -- never stranding one
     * member while evicting another. The result may then drop below 50 by more than one entry,
     * which still satisfies the `<= 50` invariant (not `== 50`).
     */
    private fun evictIfNeeded(current: List<UndoHistoryEntry>): List<UndoHistoryEntry> {
        if (current.size <= 50) return current

        val oldestSpentIndex = current.indexOfLast {
            it.status == UndoStatus.Undone || it.status == UndoStatus.Failed
        }
        val targetIndex = if (oldestSpentIndex >= 0) oldestSpentIndex else current.lastIndex
        val targetEntry = current[targetIndex]
        val targetGroupId = groupIdByEntryId[targetEntry.id]
        return if (targetGroupId == null) {
            current.filterIndexed { index, _ -> index != targetIndex }
        } else {
            current.filterNot { groupIdByEntryId[it.id] == targetGroupId }
        }
    }
}
