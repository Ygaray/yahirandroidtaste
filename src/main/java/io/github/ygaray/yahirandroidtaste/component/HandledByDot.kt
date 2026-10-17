package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The minimum WCAG 1.4.11 non-text contrast the handled-by dot must have against the pill (POL-02, D-07). */
internal const val HANDLED_BY_DOT_MIN_CONTRAST = 3f

/**
 * The width of the contrast ring drawn around a failing dot (PD-18). An internal constant, not a
 * `Dimens` token, so the public token surface and its test stay untouched.
 */
internal val HANDLED_BY_DOT_RING_WIDTH: Dp = 1.5.dp

/**
 * The WCAG contrast ratio of two colors, `(lighter + 0.05) / (darker + 0.05)` over their relative
 * luminance. A translucent color is treated as opaque because luminance ignores alpha (FA-7), so
 * callers composite a translucent fill over its backdrop first ([handledByDotRing] does).
 */
internal fun wcagContrast(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
}

/**
 * Whether the handled-by dot needs a contrast ring, and in which color (POL-02, D-07): `null` when the
 * [fill] already has at least [HANDLED_BY_DOT_MIN_CONTRAST] against the [pill], so a passing caller sees
 * no change at all. Otherwise the ring is `scheme.onSurface` when that reaches the minimum against the
 * pill, else black or white, whichever has the larger contrast. A translucent [fill] is composited over
 * the [pill] first, so the contrast is measured on the color that is actually painted.
 *
 * The repo's luminance-threshold black-or-white helper is deliberately not used: its 0.5 threshold can
 * return a color below the minimum on a mid-tone pill (PD-17). The larger of black and white always
 * reaches at least 4.58:1 against any color, so the result is total.
 */
internal fun handledByDotRing(fill: Color, pill: Color, scheme: ColorScheme): Color? = when {
    wcagContrast(fill.compositeOver(pill), pill) >= HANDLED_BY_DOT_MIN_CONTRAST -> null
    wcagContrast(scheme.onSurface, pill) >= HANDLED_BY_DOT_MIN_CONTRAST -> scheme.onSurface
    wcagContrast(Color.Black, pill) >= wcagContrast(Color.White, pill) -> Color.Black
    else -> Color.White
}

/**
 * Draws a [ring] behind the node, OUTSIDE its bounds: a circle whose radius is the smaller half-dimension
 * plus [width]. Returns the receiver unchanged for a `null` ring. A drawn ring and not an inner border,
 * because an inner border would shrink the 6dp dot fill to 4dp and change its size (PD-18); the node's
 * layout footprint, tag and position are untouched. Apply it BEFORE the node's own background.
 */
internal fun Modifier.handledByDotRingBehind(ring: Color?, width: Dp = HANDLED_BY_DOT_RING_WIDTH): Modifier =
    if (ring == null) {
        this
    } else {
        drawBehind {
            drawCircle(color = ring, radius = minOf(size.width, size.height) / 2f + width.toPx())
        }
    }
