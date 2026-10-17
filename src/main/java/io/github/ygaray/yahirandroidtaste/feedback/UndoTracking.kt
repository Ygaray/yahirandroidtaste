package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Single tracked-append sequence for [FeedbackEvent.WithUndo] — the ONE place
 * append + snackbar + [UndoHistoryStore.attemptUndo] are sequenced (UNDO-02).
 *
 * Both independent dispatcher sites (`FeedbackController` at the app root, and
 * `SheetFeedbackHost` for bottom-sheet z-ordering, Phase 50) call this same helper
 * so every `WithUndo` producer — root-routed or sheet-routed — is tracked in the
 * one shared [UndoHistoryStore], and the snackbar tap and the Undo Center's tap
 * share one consumed guard (never the raw [FeedbackEvent.WithUndo.undoAction]
 * lambda directly).
 *
 * Order matters: [UndoHistoryStore.append] runs BEFORE [showSnackbar] so the entry
 * exists (and is visible in the Undo Center) for the entire lifetime the snackbar is
 * shown, not just after it resolves.
 */
suspend fun SnackbarHostState.emitTrackedWithUndo(
    store: UndoHistoryStore,
    event: FeedbackEvent.WithUndo
) {
    val entryId = store.append(
        message = event.message,
        preview = event.preview,
        target = event.target,
        undoAction = event.undoAction
    )
    val result = showSnackbar(
        message = event.message,
        actionLabel = "Undo",
        duration = event.duration
    )
    // D-04: only act on explicit user tap — never on Dismissed/timeout. Routed through
    // the store's atomic guard, never event.undoAction() directly, so first-consumer-wins
    // holds against a concurrent Undo Center tap on the same entry.
    if (result == SnackbarResult.ActionPerformed) {
        store.attemptUndo(entryId)
    }
}

/**
 * Tracked-append sequence for [FeedbackEvent.WithUndo] on the feedback card stack (FSTK-07,
 * FTGT-02). The stack twin of the Material3 [SnackbarHostState.emitTrackedWithUndo] above, with
 * the same store-guard rule: the card's Undo and an Undo Center tap share one consumed guard, and
 * the event's raw [FeedbackEvent.WithUndo.undoAction] is never called directly.
 *
 * Order matters: [UndoHistoryStore.append] runs BEFORE the card is shown so the entry exists (and
 * is visible in the Undo Center) for the entire lifetime of the card, not just after it resolves.
 * The entry carries the event's [FeedbackEvent.WithUndo.target], and the visible card carries the
 * entry id.
 *
 * Only [FeedbackResult.Undone] runs the undo, through [UndoHistoryStore.attemptUndo]. A tap that
 * loses the race to the Undo Center still returns [FeedbackResult.Undone]; the store call is then
 * a no-op. [FeedbackResult.Opened], [FeedbackResult.Dismissed] and [FeedbackResult.TimedOut]
 * never undo and leave the entry Available.
 *
 * Cancellation: if the caller is cancelled before a result, no undo runs, the card is removed and
 * the entry stays Available, so it can still be undone from the Undo Center. If the caller is
 * cancelled while the store undo is running, or after the Undo tap resolved the card but before
 * the caller resumed, the undo still finishes (it runs under `NonCancellable`) and the entry ends
 * Undone, never Available but inert. This guard exists on this stack path only; the Material3
 * path is unchanged.
 *
 * A card whose entry is undone, fails or is removed elsewhere is kept on screen marked spent; it
 * is not auto-dismissed.
 *
 * @return the single [FeedbackResult] the card resolved to.
 */
suspend fun FeedbackStackState.emitTrackedWithUndo(
    store: UndoHistoryStore,
    event: FeedbackEvent.WithUndo
): FeedbackResult {
    val entryId = store.append(
        message = event.message,
        preview = event.preview,
        target = event.target,
        undoAction = event.undoAction
    )
    var shown: FeedbackStackItem? = null
    val result = try {
        showInternal(event, entryId, watch = { item ->
            // Spent elsewhere (undone, failed or removed): keep the card, flag it for the host.
            store.entries.first { entries ->
                entries.none { it.id == entryId && it.status == UndoStatus.Available }
            }
            item.spent = true
        }, onEnqueued = { shown = it })
    } catch (cancelled: CancellationException) {
        // The Undo tap may have won resolution before the cancel reached the awaiting caller.
        // The card already vanished as if honored, so honor it (see the Cancellation note above).
        if (shown?.outcome == FeedbackResult.Undone) {
            withContext(NonCancellable) { store.attemptUndo(entryId) }
        }
        throw cancelled
    }
    if (result == FeedbackResult.Undone) {
        withContext(NonCancellable) { store.attemptUndo(entryId) }
    }
    return result
}
