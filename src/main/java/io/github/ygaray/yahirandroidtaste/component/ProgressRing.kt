package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.ExpressiveMotion
import io.github.ygaray.yahirandroidtaste.theme.expressive

/**
 * A caller-supplied circular determinate-progress primitive (Progress / Metrics family). This
 * composable does NO formatting/rounding arithmetic itself — every fraction and color is
 * supplied by the caller (mirrors [MetricBar]'s own "zero primitive-authored copy" litmus).
 *
 * [fraction] is clamped via `coerceIn(0f, 1f)` rather than `require()`-crashing — a deliberate
 * divergence from [MetricBar]'s `require()` precedent: an out-of-range caller value (e.g. 1.5f,
 * "150% of some target") is an expected real state, not a caller bug, so this primitive renders
 * a sane clamped fill instead of crashing (mirrors [MetricBar.trackColor]'s own documented
 * deliberate-deviation convention).
 *
 * The animated fill value is read strictly inside the [Canvas] draw lambda's `drawArc` calls,
 * never hoisted into this composable's own top-level body — reading it only in the draw phase
 * skips recomposition entirely on each animation tick (perf discipline, load-bearing for a
 * future no-jank frame-timing budget).
 *
 * @param fraction the ring's fill fraction, caller-computed, clamped to `0f..1f` on render (e.g.
 *   a generic progress/step-count scenario such as "6 of 8 steps complete" -> `0.75f`).
 * @param modifier applied to the outer [Box]; determines the ring's bounds.
 * @param strokeWidth the ring stroke's thickness.
 * @param trackColor the empty-track color. Defaults to [MaterialTheme.expressive]'s
 *   [io.github.ygaray.yahirandroidtaste.theme.ExpressiveTokens.ringTrack] — deliberately NOT
 *   `outlineVariant`/`surfaceVariant`/`surface`, mirroring [MetricBar.trackColor]'s own
 *   contrast-safe default (`outline`-derived) rather than re-deriving it.
 * @param progressColor the filled-progress color. Defaults to [MaterialTheme.expressive]'s
 *   [io.github.ygaray.yahirandroidtaste.theme.ExpressiveTokens.onTrack].
 * @param animationSpec the fill animation's spec. Defaults to
 *   [ExpressiveMotion.emphasizedSpatialSpec] (the hub's plain-Kotlin motion-token set, DS-02).
 * @param content optional center content (e.g. a value label), laid out over the ring.
 */
@Composable
fun ProgressRing(
    fraction: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    trackColor: Color = MaterialTheme.expressive.ringTrack,
    progressColor: Color = MaterialTheme.expressive.onTrack,
    animationSpec: FiniteAnimationSpec<Float> = ExpressiveMotion.emphasizedSpatialSpec,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val clampedFraction = fraction.coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = clampedFraction,
        animationSpec = animationSpec,
        label = "ProgressRing"
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // animatedFraction read HERE (draw phase) — never hoisted above into composition.
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx())
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedFraction,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        content()
    }
}
