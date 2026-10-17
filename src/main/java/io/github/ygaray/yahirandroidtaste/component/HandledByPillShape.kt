package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/** The line height used for the cap when the theme's body-small line height is Unspecified or in em (PD-16). */
private val FallbackLineHeight: TextUnit = 16.sp

/**
 * The corner radius of the handled-by pill for a painted [heightPx]: half the height (a full capsule)
 * while that is at most [capPx], else [capPx] (POL-01, D-06). Never negative.
 */
internal fun handledByPillCornerRadius(heightPx: Float, capPx: Float): Float =
    minOf(heightPx / 2f, capPx).coerceAtLeast(0f)

/**
 * The cap of the pill's corner radius in pixels: half the height of a ONE-line pill at [density] (which
 * carries the font scale), that is (line height + 2 x [verticalPadding]) / 2. A [lineHeight] that is not
 * in sp (Unspecified or em) cannot be converted to pixels in a draw pass without throwing, so it is
 * replaced by a fixed 16sp (PD-16).
 */
internal fun handledByPillCapPx(density: Density, lineHeight: TextUnit, verticalPadding: Dp): Float {
    val resolved = if (lineHeight.isSp) lineHeight else FallbackLineHeight
    return with(density) { (resolved.toPx() + 2f * verticalPadding.toPx()) / 2f }
}

/**
 * The Receipt handled-by pill's background shape: a rounded rectangle whose uniform corner radius is
 * `min(height / 2, cap)`, the cap being half the height of a one-line pill ([handledByPillCapPx]).
 *
 * A one-line pill therefore has radius `height / 2`, exactly the old `CircleShape` capsule at every
 * font scale. A pill that wraps to several lines keeps the one-line corner geometry instead of growing
 * a half-height arc that would cut through the text near the corners.
 *
 * It is computed in [createOutline] from the final size and the draw [Density], never from text layout
 * state: an `onTextLayout` callback would need an extra frame and flicker. It is a data class so
 * `Modifier.background` does not invalidate for equal inputs.
 */
internal data class HandledByPillShape(
    val lineHeight: TextUnit,
    val verticalPadding: Dp
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radius = handledByPillCornerRadius(size.height, handledByPillCapPx(density, lineHeight, verticalPadding))
        return Outline.Rounded(RoundRect(0f, 0f, size.width, size.height, CornerRadius(radius)))
    }
}

/** The pill shape for the current theme: the body-small line height and the pill's vertical padding. */
@Composable
internal fun rememberHandledByPillShape(): Shape {
    val lineHeight = MaterialTheme.typography.bodySmall.lineHeight
    return remember(lineHeight) {
        HandledByPillShape(lineHeight, Dimens.OutcomeReceipt.PillVerticalPadding)
    }
}
