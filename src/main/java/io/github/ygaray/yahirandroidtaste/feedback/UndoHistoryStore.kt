package io.github.ygaray.yahirandroidtaste.feedback

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
     */
    fun clearSpent() {
        _entries.update { current ->
            current.filterNot { it.status == UndoStatus.Undone || it.status == UndoStatus.Failed }
        }
    }

    /**
     * D-10: on insert past the 50-entry cap, evict the oldest spent (Undone/Failed)
     * entry first; if none exist (all Available), fall back to evicting the oldest
     * Available entry. [current] is newest-first, so "oldest" is the last element.
     */
    private fun evictIfNeeded(current: List<UndoHistoryEntry>): List<UndoHistoryEntry> {
        if (current.size <= 50) return current

        val oldestSpentIndex = current.indexOfLast {
            it.status == UndoStatus.Undone || it.status == UndoStatus.Failed
        }
        return if (oldestSpentIndex >= 0) {
            current.filterIndexed { index, _ -> index != oldestSpentIndex }
        } else {
            current.dropLast(1) // D-10 fallback: all Available — evict oldest by insertion order
        }
    }
}
