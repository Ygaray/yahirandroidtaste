package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * A caller-supplied 5-band progress bar (Progress / Metrics family). This composable does NO
 * formatting/rounding arithmetic itself — every string, fraction, and band is supplied by the
 * caller. Generalized from CalTracker's own device-verified `MetricBar` (v1.6 give-leg,
 * GIVE-01) — the hub version owns its own [MetricBand] enum rather than importing any
 * consumer's domain-shaped progress-state type (impossible cross-repo anyway, and would violate
 * the hub's domain-agnostic litmus).
 *
 * @param label the leading text in the header row (e.g. a metric name).
 * @param valueText the trailing text in the header row, fully caller-formatted (e.g.
 *   `"142 / 180 kcal"` or `"42 / 100 pts"`) — this composable performs no number formatting.
 * @param fraction the progress bar's fill fraction, `0f..1f`, caller-computed. Required (non-null)
 *   when [remainingText] is non-null; unused and safely omittable for a header-only call
 *   (`remainingText == null`, IN-02).
 * @param band the 5-band state driving [barColor]'s default. Required (non-null) when
 *   [remainingText] is non-null; unused and safely omittable for a header-only call.
 * @param modifier applied to the outer [Column].
 * @param remainingText when non-null, renders the progress bar + a trailing remaining-amount
 *   line below the header; when null, only the header row is shown (mirrors a no-goal metric) and
 *   [fraction]/[band] are never read.
 * @param barColor the progress fill color; defaults to [MetricBarDefaults.colorFor] for [band]
 *   (or [MetricBarDefaults.Good] when [band] is null — only reachable in header-only mode, where
 *   this default is never actually rendered).
 * @param trackColor the empty-track color. Defaults to [MaterialTheme.colorScheme.outline] —
 *   deliberately NOT `outlineVariant`, `surfaceVariant`, or `surface`. This is CalTracker's most
 *   recently fixed value (its own Phase 40-04 / D-03 contrast fix): on a FIXED (non-dynamic)
 *   palette, `outlineVariant` collapses to visually-identical-to-`surfaceVariant` in dark mode,
 *   and `surface` collides with `surfaceVariant` in light mode. Only `outline` stays visibly
 *   distinct from `surfaceVariant` in BOTH color schemes. Do not "fix" this back to
 *   `outlineVariant` — that is the exact contrast regression this default guards against.
 */
@Composable
fun MetricBar(
    label: String,
    valueText: String,
    fraction: Float? = null,
    band: MetricBand? = null,
    modifier: Modifier = Modifier,
    remainingText: String? = null,
    barColor: Color = band?.let(MetricBarDefaults::colorFor) ?: MetricBarDefaults.Good,
    trackColor: Color = MaterialTheme.colorScheme.outline,
) {
    // IN-02: fraction/band are optional so a header-only call site (remainingText == null) isn't
    // forced to fabricate meaningless values, but they're still required once remainingText makes
    // the progress bar actually render.
    require(remainingText == null || (fraction != null && band != null)) {
        "MetricBar: fraction and band are required when remainingText is non-null"
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (remainingText != null) {
            LinearProgressIndicator(
                // Guarded by the require() above -- fraction is non-null whenever this branch runs.
                progress = { fraction ?: 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp),
                color = barColor,
                trackColor = trackColor,
                strokeCap = StrokeCap.Round,
            )
            Text(
                text = remainingText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The hub's own 5-band progress state — independent of any consumer's domain type. */
enum class MetricBand { VeryUnder, Under, Good, OnTarget, Over }

/**
 * Default band->color mapping. Every value is a caller-overridable [Color] default carried over
 * from CalTracker's own device-verified, dual-theme-legible palette (its Phase 12 D-02a/UIX-05).
 */
object MetricBarDefaults {
    val VeryUnder: Color = Color(0xFFD32F2F)
    val Under: Color = Color(0xFFE6A700)
    val Good: Color = Color(0xFF7CB342)
    val OnTarget: Color = Color(0xFF2E7D32)
    val Over: Color = Color(0xFF8E1B3D)

    fun colorFor(band: MetricBand): Color = when (band) {
        MetricBand.VeryUnder -> VeryUnder
        MetricBand.Under -> Under
        MetricBand.Good -> Good
        MetricBand.OnTarget -> OnTarget
        MetricBand.Over -> Over
    }
}
