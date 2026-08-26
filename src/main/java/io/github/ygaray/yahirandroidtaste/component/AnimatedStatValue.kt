package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import io.github.ygaray.yahirandroidtaste.theme.ExpressiveMotion
import io.github.ygaray.yahirandroidtaste.theme.expressive

/**
 * A caller-supplied count-up numeric primitive (Progress / Metrics family). Tweens its rendered
 * text from the previous [targetValue] to the new one on every recomposition where [targetValue]
 * changes — in either direction (an increase OR a decrease, e.g. a running total that goes back
 * down to zero) — using [animationSpec].
 *
 * The default [format] renders `targetValue.toInt().toString()` — **truncate-toward-zero, NOT
 * round-half-up/round-half-even**. An in-flight animated value of `41.9f` therefore displays as
 * `"41"`, never `"42"`. Callers who need rounding (or any other numeral formatting — currency,
 * grouping separators, a unit suffix) must supply their own [format] override; this composable
 * performs no formatting/rounding arithmetic of its own beyond that documented default (mirrors
 * [MetricBar]'s "zero primitive-authored copy" litmus).
 *
 * Unlike [ProgressRing]'s [androidx.compose.foundation.Canvas]-drawn arc, this composable's
 * [Text] necessarily recomposes on every animation tick, since its own string content changes
 * each frame — that recomposition is scoped to this single leaf composable and does not cascade
 * to its caller (Compose's own smart recomposition). This is the achievable perf discipline for a
 * text-based counter; [ProgressRing]'s "read only in the draw phase" guidance applies literally to
 * a [androidx.compose.foundation.Canvas], not to a [Text] whose rendered content itself changes
 * every frame.
 *
 * Deliberately renders with no `maxLines`/`overflow` restriction — a caller-supplied long (5+
 * digit) formatted numeral is never truncated or clipped by this composable; the caller's own
 * layout container bounds the width.
 *
 * @param targetValue the value to count up (or down) to, caller-computed (e.g. a generic
 *   point-total or step-count scenario such as "6,240 points" -> `6240f`).
 * @param modifier applied to the rendered [Text].
 * @param style the rendered text's [TextStyle]. Defaults to [MaterialTheme.expressive]'s
 *   [io.github.ygaray.yahirandroidtaste.theme.ExpressiveTokens.statValueStyle].
 * @param color the rendered text's color. Defaults to [LocalContentColor.current].
 * @param animationSpec the count-up/count-down animation's spec. Defaults to
 *   [ExpressiveMotion.emphasizedEffectsSpec] (the hub's plain-Kotlin motion-token set, DS-02).
 * @param format converts the in-flight animated float to display text. Defaults to
 *   `{ it.toInt().toString() }` — truncate-toward-zero (see the class doc above); override for
 *   rounding or any other numeral formatting.
 */
@Composable
fun AnimatedStatValue(
    targetValue: Float,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.expressive.statValueStyle,
    color: Color = LocalContentColor.current,
    animationSpec: FiniteAnimationSpec<Float> = ExpressiveMotion.emphasizedEffectsSpec,
    format: (Float) -> String = { it.toInt().toString() },
) {
    val animated by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        label = "AnimatedStatValue"
    )
    Text(
        text = format(animated),
        modifier = modifier,
        style = style,
        color = color
    )
}
