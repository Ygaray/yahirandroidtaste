package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.Dimens

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
 * @param disabledOptions Indices of options that cannot be chosen. A listed segment reports
 *   `enabled = false` and never fires [onSelect]; its sibling still does. Indices outside the option
 *   range are ignored. In [SegmentedOptionStyle.Material] a disabled option takes the same Material3
 *   disabled treatment a whole-control `enabled = false` takes; in [SegmentedOptionStyle.Pill] it is
 *   drawn at 38% alpha. An empty set (the default) changes nothing.
 * @param style [SegmentedOptionStyle.Material] (the default) keeps the layout of earlier releases;
 *   [SegmentedOptionStyle.Pill] draws one outlined, fully rounded container with a `primary` selected
 *   segment and no check icon.
 *
 * Long localized labels truncate to a single ellipsized line in [SegmentedOptionStyle.Pill], while
 * [SegmentedOptionStyle.Material] wraps them to two lines first, so choose short labels for Pill.
 */
@Composable
fun SegmentedOptionSelector(
    selectedIndex: Int,
    options: List<String>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    disabledReason: String? = null,
    disabledOptions: Set<Int> = emptySet(),
    style: SegmentedOptionStyle = SegmentedOptionStyle.Material,
) {
    // WR-04: enforce the documented "exactly two options" contract at runtime -- a 1- or 3+
    // element list previously compiled and rendered silently with the wrong segment count.
    require(options.size == 2) {
        "SegmentedOptionSelector requires exactly 2 options, got ${options.size}"
    }
    // Pill is a separate Row-based branch: M3 SegmentedButton forces the check icon and its shape (D-07).
    if (style == SegmentedOptionStyle.Pill) {
        PillSegmentedOptionSelector(selectedIndex, options, onSelect, modifier, enabled, disabledReason, disabledOptions)
        return
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // height(IntrinsicSize.Min) + each segment's fillMaxHeight(): when only one label wraps to
        // two lines, both segments take the taller height (M3 does not equalize them itself).
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                // Shadows the whole-control flag on purpose so the per-segment call below stays identical.
                val enabled = enabled && index !in disabledOptions
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
 * `SegmentedButtonContentMeasurePolicy` measures the label at the FULL content width (the segment
 * minus `SegmentedButtonDefaults.ContentPadding` on each side), then lays the icon slot
 * (`SegmentedButtonDefaults.IconSize` + M3's private 8dp icon spacing, 26dp) in front of it, and
 * the content `Box` centers that wider row inside the padded segment. A full-width label therefore
 * overflows each side by half of `slot - horizontal padding`, so only that difference (about 2dp)
 * has to come off the label's max width to keep it inside the segment, check icon or not (WR-03).
 * Reserving the whole 26dp slot instead wrapped short labels at large font scales that fit on one
 * line in v2.5.0. The trade-off: a label that fills its segment lets the selected check icon reach
 * the segment's edge, as in v2.5.0. Unbounded constraints pass through. The wrapper reports exactly
 * the label's size and places it at (0, 0), so it mirrors nothing itself: RTL placement comes from
 * M3's own segment layout.
 */
private fun Modifier.reserveSegmentIconSlot(): Modifier = layout { measurable, constraints ->
    val childConstraints = if (constraints.hasBoundedWidth) {
        val slot = SegmentedButtonDefaults.IconSize.roundToPx() + SegmentedButtonIconSpacing.roundToPx()
        val padding = SegmentedButtonDefaults.ContentPadding.let {
            it.calculateLeftPadding(layoutDirection).roundToPx() +
                it.calculateRightPadding(layoutDirection).roundToPx()
        }
        val reserve = (slot - padding).coerceAtLeast(0)
        constraints.copy(minWidth = 0, maxWidth = (constraints.maxWidth - reserve).coerceAtLeast(0))
    } else {
        constraints
    }
    val placeable = measurable.measure(childConstraints)
    layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
}

/**
 * The icon-to-label spacing M3 1.4.0's `SegmentedButtonContentMeasurePolicy` actually uses: a
 * PRIVATE `SegmentedButton.kt` literal (`private val IconSpacing = 8.dp`), NOT
 * `ButtonDefaults.IconSpacing` (a different button token that is only coincidentally 8dp today and
 * could move on its own in a Material token update). Pinned here; the G-15-1 segment-overflow tests
 * are the tripwire if an M3 bump changes it.
 */
private val SegmentedButtonIconSpacing = 8.dp

/**
 * Visual style of a [SegmentedOptionSelector].
 *
 * - [Material]: the Material3 `SingleChoiceSegmentedButtonRow` -- check icon on the selected
 *   segment, labels wrap to two lines.
 * - [Pill]: one full-radius outlined container, a `primary`/`onPrimary` selected segment, no check
 *   icon, single-line ellipsized labels, 40dp tall.
 */
enum class SegmentedOptionStyle { Material, Pill }

/**
 * The [SegmentedOptionStyle.Pill] branch of [SegmentedOptionSelector] (D-07, D-08): one clipped,
 * 1dp `outlineVariant` outlined full-radius container; the selected segment's `primary` fill is
 * full-bleed inside it. A segment in [disabledOptions] (or every segment when [enabled] is false)
 * renders at 38% alpha and never fires [onSelect]. Segment semantics keep the Material strings.
 */
@Composable
private fun PillSegmentedOptionSelector(
    selectedIndex: Int,
    options: List<String>,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    disabledReason: String?,
    disabledOptions: Set<Int>,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Dimens.ContentSpacing)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(CircleShape)
                .border(Dimens.HairlineBorder, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .selectableGroup(),
        ) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val segmentEnabled = enabled && index !in disabledOptions
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .heightIn(min = Dimens.SegmentedPill.Height)
                        .alpha(if (segmentEnabled) 1f else DISABLED_CONTENT_ALPHA)
                        .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .selectable(
                            selected = selected,
                            enabled = segmentEnabled,
                            role = Role.RadioButton,
                            onClick = { onSelect(index) },
                        )
                        .semantics {
                            contentDescription = "$label, ${if (selected) "selected" else "not selected"}"
                        }
                        .padding(horizontal = Dimens.SegmentedPill.LabelHorizontalPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
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
