package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/**
 * Showcase composable for [heatTier] / [heatVisual] / [hubNodeVisual] (Phase 123 DS-01, D-03,
 * `135-UI-SPEC.md` § "Heat Tier Contract"). Renders a small mindmap fragment left to right for
 * six sample jaccard values — one per [HeatTier] — connected by sample edges that thicken
 * toward the hotter node, plus one distinct-hub example, so all six tiers and the hub ring are
 * visible in one glance. Reads [MaterialTheme.colorScheme] live so both theme variants of the
 * ramp render correctly in the Explorer's light and dark modes. The sample row scrolls
 * horizontally (Phase 135) since six nodes plus the hub example no longer fit a narrow viewport.
 *
 * A standalone registered composable (D-03) rather than an extension of an existing chip
 * showcase: [heatTier]/[heatVisual] are plain non-`@Composable` functions the drift guard cannot
 * see, and Heat targets mindmap nodes/edges — a different consumer archetype than a chip.
 */
@Composable
fun HeatSwatch(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val samples = listOf(0.04f, 0.12f, 0.24f, 0.37f, 0.55f, 0.80f)
    val visuals = samples.map { heatVisual(it, colorScheme) }
    val hubVisual = visuals.last() // The BLAZING sample's node, per the distinct-hub example spec.

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.HairlineSpacing),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(
                    horizontal = Dimens.HorizontalPadding,
                    vertical = Dimens.TopPadding
                )
        ) {
            visuals.forEachIndexed { index, visual ->
                if (index > 0) {
                    // Connecting edge uses the higher-jaccard (current) node's edge values, so
                    // the edge thickens toward the hotter node.
                    Box(
                        modifier = Modifier
                            .width(Dimens.HorizontalPadding)
                            .height(visual.edgeStrokeWidth)
                            .background(visual.edgeColor)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(visual.nodeRadius * 2f)
                        .background(visual.nodeFillColor, CircleShape)
                )
            }

            // Distinct-hub example — the HOT sample's node, additionally ringed. Orthogonal to
            // tier color: the hub keeps its tier fill and simply gains a ring, so this is not a
            // fifth tier.
            Box(
                modifier = Modifier
                    .size(hubVisual.nodeRadius * 2f)
                    .background(hubVisual.nodeFillColor, CircleShape)
                    .border(hubNodeVisual(colorScheme), CircleShape)
            )
        }

        Column(
            modifier = Modifier.padding(
                horizontal = Dimens.HorizontalPadding,
                vertical = Dimens.ContentSpacing
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.HairlineSpacing)
        ) {
            samples.zip(visuals).forEach { (jaccard, visual) ->
                Text(
                    text = "${heatTier(jaccard)} — j=${"%.2f".format(jaccard)}, " +
                        "r=${visual.nodeRadius.trimmedLabel()}, " +
                        "edge ${visual.edgeStrokeWidth.trimmedLabel()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Hub — BLAZING fill + 2dp primary ring",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Formats a [Dp] value as a caption-friendly label, dropping a trailing ".0" (e.g. "16dp"). */
private fun Dp.trimmedLabel(): String {
    val v = value
    return if (v == v.toInt().toFloat()) "${v.toInt()}dp" else "${v}dp"
}
