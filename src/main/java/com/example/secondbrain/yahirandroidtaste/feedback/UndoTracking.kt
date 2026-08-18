package com.example.secondbrain.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

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
    val entryId = store.append(message = event.message, preview = event.preview, undoAction = event.undoAction)
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
