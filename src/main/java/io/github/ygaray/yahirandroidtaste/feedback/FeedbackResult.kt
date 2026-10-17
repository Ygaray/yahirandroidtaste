package io.github.ygaray.yahirandroidtaste.feedback

/**
 * How a single feedback card ended — exactly one of these is returned by
 * [FeedbackStackState.show] (FSTK-01), or a `CancellationException` is thrown instead.
 *
 * - [Opened]: the card's body was tapped and the card carries a [FeedbackTarget].
 * - [Undone]: the Undo action was tapped. The stack only reports this; the consumer (or the
 *   tracked path in `UndoTracking.kt`) performs the undo.
 * - [Dismissed]: swipe, [FeedbackStackState.dismissAll], early eviction by the visible-card
 *   cap, or a Dismiss action.
 * - [TimedOut]: the card's timer elapsed.
 *
 * An enum, not a sealed type: adding a fifth value later would break exhaustive `when`
 * callers, so the four values are fixed.
 */
enum class FeedbackResult { Opened, Undone, Dismissed, TimedOut }
