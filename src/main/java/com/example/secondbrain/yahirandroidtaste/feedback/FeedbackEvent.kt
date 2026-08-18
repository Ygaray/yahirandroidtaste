package com.example.secondbrain.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration

/**
 * Sealed interface representing UI feedback events — D-10 (SWIPE-02 / Phase 31).
 *
 * Three variants:
 *  - [Notify]        — show a plain snackbar message
 *  - [WithUndo]      — show a snackbar with an undo action (suspend lambda)
 *  - [DismissScreen] — show a message and dismiss/navigate back
 *
 * This is a PURE ADDITION in Phase 31 (D-11): zero call sites, zero consumers.
 * Emitters and collectors are wired in Phase 32 (FEED-01/02/03).
 *
 * The legacy per-module event types (formerly in CardUiModels.kt)
 * were removed in Phase 32, Plan 08 after all consumers migrated.
 */
sealed interface FeedbackEvent {

    /**
     * Show a plain informational snackbar.
     *
     * @param message  Text to display.
     * @param duration How long to show the snackbar; defaults to [SnackbarDuration.Short].
     */
    data class Notify(
        val message: String,
        val duration: SnackbarDuration = SnackbarDuration.Short
    ) : FeedbackEvent

    /**
     * Show a snackbar with an undo affordance.
     *
     * @param message    Text to display.
     * @param undoAction Suspend lambda invoked when the user taps Undo.
     * @param duration   How long to show the snackbar; defaults to [SnackbarDuration.Long]
     *                   to give the user time to react.
     * @param preview    Optional snapshot preview payload (UNDO-04, D-03) — captured at
     *                   emit time by the producer, carried verbatim into the Undo Center's
     *                   [UndoHistoryEntry]. Defaulted null so existing producers compile
     *                   unchanged.
     */
    data class WithUndo(
        val message: String,
        val undoAction: suspend () -> Unit,
        val duration: SnackbarDuration = SnackbarDuration.Long,
        val preview: UndoPreview? = null
    ) : FeedbackEvent

    /**
     * Show a message and signal the current screen to dismiss / navigate back.
     *
     * @param message Text to display (e.g. "Saved", "Deleted").
     */
    data class DismissScreen(
        val message: String
    ) : FeedbackEvent
}
