package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/**
 * One segment of a [StackedProgressBar]: how much of the track it covers and what color it is.
 *
 * @property fraction Share of the track width, nominally `0f..1f`. Out-of-range values are clamped
 *   by the bar (see [StackedProgressBar]); this class stores whatever the caller passes.
 * @property color Fill color of the segment. Pass a theme role or a caller token; the bar owns no
 *   color of its own.
 */
@Immutable
data class BarSegment(
    val fraction: Float,
    val color: Color
)

/**
 * Clamps raw segment fractions into a safe layout input (D-09).
 *
 * Walks [raw] in order with `1f` of room: `NaN` counts as `0f` (`coerceAtLeast` would leave a
 * `NaN` as `NaN`), every other value is coerced into `0f..remaining`, and the accepted value is
 * subtracted from the room. The segment that crosses the end of the track is therefore
 * **truncated**, not rescaled, and any segment after a full track gets `0f`. The result always has
 * the same size as [raw] and never sums to more than `1f`.
 */
internal fun clampSegmentFractions(raw: List<Float>): List<Float> {
    var remaining = 1f
    return raw.map { value ->
        val clamped = if (value.isNaN()) 0f else value.coerceIn(0f, remaining)
        remaining -= clamped
        clamped
    }
}

/**
 * A horizontal progress bar made of colored [segments] drawn start to end inside a fully rounded
 * track (sketch Y-9). Example: segments of `0.4f` and `0.18f` fill 58 percent of the track, the
 * first segment at the start edge (the left in LTR, the right in RTL); the rest of the track shows
 * [trackColor].
 *
 * The track clips to a pill shape before it paints [trackColor], so the track and the segments keep
 * rounded ends at any width.
 *
 * Hostile input is safe. Fractions are clamped by an internal rule: a negative or `NaN` fraction
 * draws as zero, fractions accumulate in order, and the running total is capped at `1f` by
 * truncating the segment that crosses it. A zero-width segment draws nothing. An empty list draws
 * the bare track.
 *
 * [trackColor] is required and has no default on purpose: each consumer owns its contrast rule
 * (a track that disappears in one color scheme is the failure this avoids), so pass a role such as
 * `MaterialTheme.colorScheme.outline`.
 *
 * Accessibility: the bar is decorative and exposes no semantics of its own. The caller describes the
 * values in adjacent text.
 *
 * Test tags: the track node carries the stable tag `stacked_progress_bar`; a `Modifier.testTag(...)`
 * in [modifier] wins. Each drawn segment carries `stacked_progress_bar_segment`, which is a
 * **test-only** hook and not stable API. The library never exposes tags as resource ids itself; a
 * consumer that turns on `testTagsAsResourceId` for its own tree would expose both tags.
 *
 * Width: the bar fills the width it is given, so it needs a bounded width. Inside an unbounded
 * parent (such as a horizontal scroller) pass an explicit width in [modifier], or it collapses.
 *
 * @param segments Segments in drawing order, start to end.
 * @param trackColor Color of the unfilled track. Required.
 * @param modifier Applied first on the track node. Pass a width (or a weight) here; the bar fills
 *   the width it is given.
 * @param height Track height. Defaults to 6dp.
 */
@Composable
fun StackedProgressBar(
    segments: List<BarSegment>,
    trackColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = Dimens.StackedProgressBar.Height
) {
    // Not cached with remember(segments): a caller may mutate the list in place (SnapshotStateList,
    // ArrayList), which would leave a stale cache keyed on the same instance. The clamp is a single
    // pass over a few floats. The colors are read from the same snapshot so the two never disagree.
    val snapshot = segments.toList()
    val fractions = clampSegmentFractions(snapshot.map { it.fraction })
    val rest = 1f - fractions.sum()
    Box(
        modifier = modifier
            .testTag("stacked_progress_bar")
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(trackColor)
    ) {
        Row(Modifier.matchParentSize()) {
            fractions.forEachIndexed { index, fraction ->
                // Modifier.weight throws on a non-positive weight, so zero-width segments are skipped.
                if (fraction > 0f) {
                    Box(
                        Modifier
                            .weight(fraction)
                            .fillMaxHeight()
                            .background(snapshot[index].color)
                            .testTag("stacked_progress_bar_segment")
                    )
                }
            }
            if (rest > 0f) {
                Spacer(Modifier.weight(rest))
            }
        }
    }
}
