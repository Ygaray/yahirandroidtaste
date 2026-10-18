package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.ButtonDefaults
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
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/**
 * The cap of the corner radius in pixels for a text button: the larger of the handled-by pill's cap
 * ([handledByPillCapPx], half the height of a one-line button made of [lineHeight] plus two
 * [verticalPadding]) and half of [minHeight] (the Material text-button minimum height). A one-line
 * button is as tall as the larger of those two heights, so its half height never exceeds this cap and
 * it stays a capsule at every font scale, also at 1.0 where the minimum, not the line height, sets
 * the height. A [lineHeight] that is not in sp falls back to 16sp exactly as the pill does.
 */
internal fun cappedPillCapPx(density: Density, lineHeight: TextUnit, verticalPadding: Dp, minHeight: Dp): Float =
    maxOf(
        handledByPillCapPx(density, lineHeight, verticalPadding),
        with(density) { minHeight.toPx() / 2f }
    )

/**
 * A button background that is a capsule while the label is one line and a rounded rectangle with the
 * radius capped at the half height of a ONE-line button once the label wraps, so no corner of a
 * multi-line label is cut by its own rounded ends (Gate-1 SC3: the stacked card's Spanish Undo at
 * font scale 2.0). It reuses the handled-by pill rule ([handledByPillCornerRadius]); the only extra
 * term is the minimum height (see [cappedPillCapPx]).
 *
 * Everything is computed in [createOutline] from the final size and the draw [Density], never from
 * text layout state: an `onTextLayout` callback would need an extra frame and flicker. It is a data
 * class so a background or clip does not invalidate for equal inputs.
 */
internal data class CappedPillShape(
    val lineHeight: TextUnit,
    val verticalPadding: Dp,
    val minHeight: Dp
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cap = cappedPillCapPx(density, lineHeight, verticalPadding, minHeight)
        val radius = handledByPillCornerRadius(size.height, cap)
        return Outline.Rounded(RoundRect(0f, 0f, size.width, size.height, CornerRadius(radius)))
    }
}

/**
 * The capped shape for a Text-emphasis button of the current theme: the label-large line height, the
 * button's vertical content padding and the Material text-button minimum height.
 */
@Composable
internal fun rememberTextButtonCappedShape(): Shape {
    val lineHeight = MaterialTheme.typography.labelLarge.lineHeight
    return remember(lineHeight) {
        CappedPillShape(lineHeight, Dimens.ActionButton.ContentVerticalPadding, ButtonDefaults.MinHeight)
    }
}
