package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import io.github.ygaray.yahirandroidtaste.theme.expressive

/**
 * A caller-supplied generic hero/stat card face (Progress / Metrics family). This composable does
 * NO formatting/rounding arithmetic itself — [label] and [value] are both plain, fully
 * caller-formatted `String`s (mirrors [MetricBar]'s own "zero primitive-authored copy" litmus).
 * Owning both as plain strings (rather than an opaque value-rendering slot) is exactly what lets
 * this composable apply `maxLines = 1` + [TextOverflow.Ellipsis] to both, mirroring [MetricBar]'s
 * existing header-row precedent.
 *
 * [accentBrush] renders as a thin leading-edge accent stripe only, never a full-card fill — it
 * MUST stay visually subordinate to [containerColor]. Do not "fix" this later to a full-card
 * gradient fill; that would blow past the 10%-accent-usage cap this default layering satisfies.
 *
 * @param label the card's supporting label text (e.g. a generic stat name such as "This week").
 * @param value the card's headline value text, fully caller-formatted (e.g. `"1,204"`).
 * @param modifier applied to the outer layout (accent stripe + card surface together).
 * @param onClick optional tap callback. When null (the default), the card renders with no
 *   clickable semantics node at all — not a disabled clickable one (there is no `enabled` param).
 * @param shape the card surface's shape. Defaults to [MaterialTheme.expressive]'s
 *   [io.github.ygaray.yahirandroidtaste.theme.ExpressiveTokens.cardShapeLarge].
 * @param containerColor the card surface's background color. Defaults to
 *   [MaterialTheme.colorScheme.surfaceContainer].
 * @param accentBrush the thin leading-edge accent's brush. Defaults to [MaterialTheme.expressive]'s
 *   [io.github.ygaray.yahirandroidtaste.theme.ExpressiveTokens.heroGradient].
 * @param content optional additional content appended below [label] (e.g. an embedded
 *   [ProgressRing] or [AnimatedStatValue], demonstrating primitive composition).
 */
@Composable
fun HeroStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = MaterialTheme.expressive.cardShapeLarge,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    accentBrush: Brush = MaterialTheme.expressive.heroGradient,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    // Modifier.height(IntrinsicSize.Min) makes the Row measure its intrinsic content height first,
    // so the accent Box's fillMaxHeight() resolves against that intrinsic height (matching the
    // Surface's actual content-driven height) instead of an unbounded/unrelated ambient
    // constraint. Without this, the stripe collapses to 0dp in unbounded-height parents (e.g. a
    // LazyColumn) or overshoots the Surface's bounds in bounded-height parents.
    Row(modifier = modifier.height(IntrinsicSize.Min)) {
        Box(
            modifier = Modifier
                .testTag("hero_stat_card_accent_stripe")
                .width(4.dp)
                .fillMaxHeight()
                .background(accentBrush)
        )
        Surface(
            shape = shape,
            color = containerColor,
            modifier = Modifier
                .testTag("hero_stat_card_surface")
                .weight(1f)
                .fillMaxHeight()
                .then(
                    if (onClick != null) {
                        Modifier
                            .defaultMinSize(minHeight = Dimens.TouchTarget)
                            .clickable(onClick = onClick)
                    } else {
                        Modifier
                    }
                )
        ) {
            Column(modifier = Modifier.padding(Dimens.HorizontalPadding)) {
                Text(
                    text = value,
                    style = MaterialTheme.expressive.heroValueStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    style = MaterialTheme.expressive.heroLabelStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                content?.invoke(this)
            }
        }
    }
}
