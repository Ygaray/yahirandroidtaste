package io.github.ygaray.yahirandroidtaste.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

private data class TactileTypeSample(val label: String, val style: TextStyle, val weightName: String)

private val tactileTypeSamples: List<TactileTypeSample> = listOf(
    TactileTypeSample("DisplayLarge", TactileType.DisplayLarge, "Bold"),
    TactileTypeSample("DisplayMedium", TactileType.DisplayMedium, "SemiBold"),
    TactileTypeSample("DisplaySmall", TactileType.DisplaySmall, "Medium"),
    TactileTypeSample("DisplayXSmall", TactileType.DisplayXSmall, "Normal")
)

/**
 * Explorer showcase for [TactileType] (Phase 123 DS-01/D-01/D-05). Renders all four Space
 * Grotesk display ramp tiers — a caption, a short real sample, and a deliberately long real
 * sample per tier (the `123-UI-SPEC.md` long-text/overflow backstop, since the hub has no
 * pixel-diff harness) — followed by a same-text [FontFamily.Default] comparison row so a missing
 * or wrong weight is visually obvious side by side (`123-RESEARCH.md` Pitfall 4's mitigation).
 */
@Composable
fun TactileTypeShowcase(modifier: Modifier = Modifier) {
    val longSample = "The quick brown fox jumps over the lazy dog near the riverbank at dawn"
    val shortSample = "Tactile Design"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.HorizontalPadding, vertical = Dimens.TopPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.ContentSpacing)
    ) {
        tactileTypeSamples.forEach { sample ->
            Text(
                text = "${sample.label} — ${sample.style.fontSize.value.toInt()}sp / ${sample.weightName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = shortSample, style = sample.style)
            Text(text = longSample, style = sample.style)
        }

        Text(
            text = "Default face comparison (same size/weight/spacing, FontFamily.Default)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        tactileTypeSamples.forEach { sample ->
            Text(
                text = shortSample,
                style = sample.style.copy(fontFamily = FontFamily.Default)
            )
        }
    }
}
