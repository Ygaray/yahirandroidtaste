package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Pure geometry for the feedback deck (D-01 Direction A). Composition-free so the numbers can be
 * pinned by plain JVM tests, modelled on `receiptActionSlotWidths`. Everything here is internal.
 */

/** Visible strip of an older card above the next newer card: the approved mock's 14dp step. */
internal val FEEDBACK_PEEK_HEIGHT: Dp = 14.dp

/** Scale lost per depth step: 1.0 for the newest card, then 0.94 and 0.88. */
internal const val FEEDBACK_DEPTH_SCALE_STEP: Float = 0.06f

/** Widest the deck's cards grow on a wide host; they stay centered. */
internal val FEEDBACK_DECK_MAX_WIDTH: Dp = 560.dp

/** The mock's alpha for the first and second older card. */
private const val FEEDBACK_DEPTH_ALPHA_FIRST = 0.9f
private const val FEEDBACK_DEPTH_ALPHA_SECOND = 0.75f

/** Scale of the card at [depth] (0 is the newest card). */
internal fun feedbackDepthScale(depth: Int): Float =
    1f - FEEDBACK_DEPTH_SCALE_STEP * depth.coerceAtLeast(0)

/** Alpha of the card at [depth]: 1.0, 0.9, 0.75; deeper cards clamp to 0.75. */
internal fun feedbackDepthAlpha(depth: Int): Float = when {
    depth <= 0 -> 1f
    depth == 1 -> FEEDBACK_DEPTH_ALPHA_FIRST
    else -> FEEDBACK_DEPTH_ALPHA_SECOND
}

/**
 * Height of a collapsed deck: nothing for no cards, the front card's height for one, otherwise the
 * front card plus one [peekPx] strip per older card.
 */
internal fun feedbackCollapsedHeight(frontHeight: Int, count: Int, peekPx: Int): Int = when {
    count <= 0 -> 0
    else -> frontHeight + (count - 1) * peekPx
}
