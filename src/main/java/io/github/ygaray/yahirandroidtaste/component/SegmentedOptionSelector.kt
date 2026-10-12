package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * A generic two-option segmented toggle (Material3 [SingleChoiceSegmentedButtonRow]) with an
 * always-visible disabled+reason affordance — the caller supplies both option labels and never
 * has its choice hidden, even when disabled.
 *
 * Contrast discipline: the UNSELECTED segment keeps Material3's own default border color from
 * [SegmentedButtonDefaults.colors] (which draws `outlineVariant`). Do NOT override it — the
 * opposite approach from this library's `MetricBar`, whose track color needs an explicit
 * override; here the M3 default is already visibly distinct against a plain background, and
 * overriding it risks re-introducing a dynamic-color contrast collapse. The selected segment
 * reads M3's own `primary`-tinted container.
 *
 * When [enabled] is false, both segments dim via the Material3 default disabled treatment so a
 * tap cannot fire — the row itself, and a [disabledReason] caption beneath it (when supplied),
 * both stay fully visible rather than being conditionally hidden. The caption is never tinted as
 * a warning or failure color: the values already shown are still valid, only the toggle itself is
 * temporarily unavailable.
 *
 * @param options Exactly two labels for the two segments. A label too long for its half of the row
 *   wraps to at most two lines and then ellipsizes, never running past the segment edge or under
 *   the selected segment's check icon; both segments keep equal height.
 */
@Composable
fun SegmentedOptionSelector(
    selectedIndex: Int,
    options: List<String>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    disabledReason: String? = null,
) {
    // WR-04: enforce the documented "exactly two options" contract at runtime -- a 1- or 3+
    // element list previously compiled and rendered silently with the wrong segment count.
    require(options.size == 2) {
        "SegmentedOptionSelector requires exactly 2 options, got ${options.size}"
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // height(IntrinsicSize.Min) + each segment's fillMaxHeight(): when only one label wraps to
        // two lines, both segments take the taller height (M3 does not equalize them itself).
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                SegmentedButton(
                    selected = selected,
                    // enabled=false dims the segment (Material3 default) so a tap cannot fire.
                    enabled = enabled,
                    onClick = { onSelect(index) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    // Default colors draw the unselected segment's outlineVariant border — leave them.
                    colors = SegmentedButtonDefaults.colors(),
                    modifier = Modifier.fillMaxHeight().semantics {
                        contentDescription = "$label, ${if (selected) "selected" else "not selected"}"
                    },
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.reserveSegmentIconSlot(),
                    )
                }
            }
        }
        // Soft inline reason when the toggle is unavailable — a caption, NOT error red (the
        // values shown are still valid; only the toggle itself is unavailable).
        if (!enabled && disabledReason != null) {
            Text(
                text = disabledReason,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Keeps a segment label inside its segment (G-15-1). Material3 1.4.0's
 * `SegmentedButtonContentMeasurePolicy` measures the label at the FULL content width, then places
 * it after the icon slot (`SegmentedButtonDefaults.IconSize` + `ButtonDefaults.IconSpacing`, at
 * most 26dp; shifted back by half that when the check icon is hidden), so a label that fills its
 * segment ran past the segment's right edge and was clipped under the selected check icon's shift.
 * Shrinking the label's bounded max width by that slot keeps it inside the segment, check icon or
 * not. Unbounded constraints pass through; the label reports its own size and is placed with
 * `placeRelative` so RTL mirrors.
 */
private fun Modifier.reserveSegmentIconSlot(): Modifier = layout { measurable, constraints ->
    val childConstraints = if (constraints.hasBoundedWidth) {
        val slot = (SegmentedButtonDefaults.IconSize + ButtonDefaults.IconSpacing).roundToPx()
        constraints.copy(minWidth = 0, maxWidth = (constraints.maxWidth - slot).coerceAtLeast(0))
    } else {
        constraints
    }
    val placeable = measurable.measure(childConstraints)
    layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
}
