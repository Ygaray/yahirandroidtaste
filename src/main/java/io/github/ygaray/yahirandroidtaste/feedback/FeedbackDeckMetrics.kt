package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

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

/** Share of the card row an Undo label may take before it wraps. */
internal const val FEEDBACK_UNDO_MAX_WIDTH_FRACTION: Float = 0.4f

/** Hard ceiling of the Undo width; only a word longer than this breaks mid-word. */
internal const val FEEDBACK_UNDO_CEILING_FRACTION: Float = 0.6f

/**
 * Widest the Undo action may measure in a row [rowWidth] px wide: its [naturalWidth] when that fits,
 * otherwise 40 percent of the row, widened to [minIntrinsicWidth] (the longest unbreakable word) so
 * a normal word is never split, and never beyond 60 percent of the row.
 */
internal fun feedbackUndoMaxWidth(rowWidth: Int, naturalWidth: Int, minIntrinsicWidth: Int): Int {
    val cap = (rowWidth * FEEDBACK_UNDO_MAX_WIDTH_FRACTION).toInt()
    val ceiling = (rowWidth * FEEDBACK_UNDO_CEILING_FRACTION).toInt()
    return minOf(naturalWidth, minOf(maxOf(cap, minIntrinsicWidth), ceiling))
}

/** Gap between cards of a fanned deck (the mock's 8px). */
internal val FEEDBACK_FANNED_GAP: Dp = 8.dp

/** A fanned deck takes at most this share of the available height; the cards scroll inside. */
internal const val FEEDBACK_FANNED_MAX_HEIGHT_FRACTION: Float = 0.6f

/** Height of a fanned deck: the card [heights] stacked with [gapPx] between neighbours. */
internal fun feedbackFannedHeight(heights: List<Int>, gapPx: Int): Int =
    if (heights.isEmpty()) 0 else heights.sum() + (heights.size - 1) * gapPx

/**
 * Least height a fanned deck keeps when the on-screen keyboard leaves it little room, so the fanned
 * column can never collapse to nothing while its cards are still in state and the timers are paused.
 */
internal val FEEDBACK_FANNED_MIN_CAP: Dp = 96.dp

/**
 * Cap on the fanned deck's height: the smaller of 60 percent of [available] and what the keyboard
 * leaves of it, where [imeOverlap] px of [available] are covered by the keyboard (the part of the
 * keyboard that actually overlaps the host, not the whole window inset). The keyboard term never
 * pushes the cap below [floorPx], itself limited to [available]; the 60 percent share is the only
 * term that can go lower. Never negative. An unbounded [available] (`Constraints.Infinity`) stays
 * unbounded as `Int.MAX_VALUE`.
 */
internal fun feedbackFannedCap(available: Int, imeOverlap: Int, floorPx: Int = 0): Int {
    if (available == Constraints.Infinity) return Int.MAX_VALUE
    val share = (available * FEEDBACK_FANNED_MAX_HEIGHT_FRACTION).toInt()
    val keyboardRoom = maxOf(available - imeOverlap, minOf(floorPx, available))
    return minOf(share, keyboardRoom).coerceAtLeast(0)
}

/** Linear interpolation of two pixel values by [fraction] (0 gives [start], 1 gives [stop]). */
internal fun feedbackLerp(start: Int, stop: Int, fraction: Float): Int =
    (start + (stop - start) * fraction).roundToInt()

/** Linear interpolation of two floats by [fraction]. */
internal fun feedbackLerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction

/** How far a card must be dragged sideways before releasing it dismisses it. */
internal val FEEDBACK_SWIPE_THRESHOLD: Dp = 72.dp

/** Alpha a swiped card fades to when it has travelled a full width. */
private const val FEEDBACK_SWIPE_MIN_ALPHA = 0.2f

/**
 * Alpha of a card displaced by [offsetPx] sideways in a [widthPx] wide row: 1 at rest, falling
 * linearly to 0.2 at a full width and never below it (the mock's `max(.2, 1 - |dx|/width)`). A zero,
 * negative or NaN width, or a NaN offset, gives 1 so a card is never hidden by a bad measurement.
 */
internal fun feedbackSwipeAlpha(offsetPx: Float, widthPx: Float): Float {
    if (offsetPx.isNaN() || widthPx.isNaN() || widthPx <= 0f) return 1f
    val travelled = (abs(offsetPx) / widthPx).coerceIn(0f, 1f)
    return 1f - (1f - FEEDBACK_SWIPE_MIN_ALPHA) * travelled
}
