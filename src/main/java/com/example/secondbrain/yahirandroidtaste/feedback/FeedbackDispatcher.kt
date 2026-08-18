package com.example.secondbrain.yahirandroidtaste.feedback

import androidx.compose.runtime.compositionLocalOf

/**
 * Nav/DI-free port for dispatching [FeedbackEvent]s (MOD-02, D-05-fb).
 *
 * The concrete, Nav-aware adapter (`FeedbackController`) lives in `:app` and
 * implements this interface — this module never sees `NavHostController`.
 */
interface FeedbackDispatcher {

    /**
     * Dispatch a [FeedbackEvent] to the root snackbar host.
     *
     * Suspend — the caller must invoke this from a coroutine scope
     * (e.g. a screen-level `LaunchedEffect`).
     */
    suspend fun emit(event: FeedbackEvent)
}

/**
 * CompositionLocal that provides the single app-root [FeedbackDispatcher].
 *
 * Provided at `AppNavGraph` via `CompositionLocalProvider`:
 *
 * ```
 * CompositionLocalProvider(LocalFeedbackController provides feedbackController) { ... }
 * ```
 *
 * Consumed in any composable that needs to forward feedback events:
 *
 * ```
 * val feedbackController = LocalFeedbackController.current
 * LaunchedEffect(viewModel) {
 *     viewModel.feedbackEvents.collect { event -> feedbackController.emit(event) }
 * }
 * ```
 *
 * Throws an [IllegalStateException] if accessed outside a `CompositionLocalProvider`
 * scope that provides a [FeedbackDispatcher] — this is intentional to catch
 * mis-configured callers at runtime.
 */
val LocalFeedbackController = compositionLocalOf<FeedbackDispatcher> {
    error("No FeedbackDispatcher provided — wrap the NavHost in CompositionLocalProvider(LocalFeedbackController provides feedbackController)")
}
