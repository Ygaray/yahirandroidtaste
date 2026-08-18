package com.example.secondbrain.yahirandroidtaste.feedback

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Lifecycle status of an [UndoHistoryEntry] — Phase 53 (UNDO-01/02/03).
 *
 * - [Available]: the underlying action has not yet been undone; the entry is live.
 * - [Undone]: the undo action was executed successfully.
 * - [Failed]: the undo action was attempted but threw (e.g. stale/already-purged state);
 *   best-effort — the app never crashes, the entry is simply marked Failed.
 */
enum class UndoStatus { Available, Undone, Failed }

/**
 * A single session-durable undo record — one per [FeedbackEvent.WithUndo] emission.
 *
 * Carries its own atomic consumed-guard ([tryConsume]) so [UndoHistoryStore.attemptUndo]
 * can guarantee first-consumer-wins across two independent callers (the snackbar's
 * `ActionPerformed` path and the Undo Center's tap) without any per-call-site locking.
 *
 * [status] is immutable per instance (gap-closure fix for 53-05 SC#3 / RESEARCH.md
 * Pitfall 3): a status transition (Available -> Undone/Failed) is expressed as a brand
 * new [UndoHistoryEntry] instance via [withStatus], never an in-place field mutation.
 * `data class` gives this genuine structural equality — a status-changed copy compares
 * `!=` to its predecessor, so the list [UndoHistoryStore] rebuilds around it is
 * structurally distinct too, and `MutableStateFlow` reliably emits (Compose recomposes)
 * instead of conflating the "same" list. [consumedGuard] is threaded through every
 * [withStatus] copy so the first-consumer-wins guard survives the replacement — it is
 * never reset back to unconsumed.
 *
 * Pure Kotlin — no Compose/Android import — keeps `:designsystem/feedback` extraction-ready.
 *
 * IN-01: the primary constructor is `internal` so [UndoHistoryStore.append] (the only
 * place a fresh, untripped-guard entry may legitimately come into existence) is the sole
 * construction site outside this file. Callers in other modules can only obtain instances
 * via the store and can only transition status via [withStatus] — they can never construct
 * a same-`id` entry with a fresh, untripped [consumedGuard], which would otherwise defeat
 * the store's first-consumer-wins CAS guarantee.
 *
 * [preview] (Phase 58, UNDO-04, D-03) is an optional snapshot payload captured by the
 * producer at emit time and carried verbatim — never re-resolved — so the Undo Center's
 * long-press peek survives the underlying card/photo being purged. Nullable/defaulted so
 * every existing producer keeps compiling unchanged, and threaded automatically through
 * [withStatus]'s `copy()` like every other constructor property.
 */
data class UndoHistoryEntry internal constructor(
    val id: String,
    val message: String,
    val timestamp: Long,
    val undoAction: suspend () -> Unit,
    val preview: UndoPreview? = null,
    val status: UndoStatus = UndoStatus.Available,
    private val consumedGuard: AtomicBoolean = AtomicBoolean(false)
) {

    /** Returns true only for the FIRST caller to claim this entry; false for every caller after. */
    fun tryConsume(): Boolean = consumedGuard.compareAndSet(false, true)

    /**
     * Returns a new [UndoHistoryEntry] carrying [newStatus], preserving this entry's id,
     * message, timestamp, undo action, and (critically) its already-tripped [consumedGuard]
     * reference — the replacement is a status transition, not a fresh, unconsumed entry.
     */
    fun withStatus(newStatus: UndoStatus): UndoHistoryEntry = copy(status = newStatus)
}
