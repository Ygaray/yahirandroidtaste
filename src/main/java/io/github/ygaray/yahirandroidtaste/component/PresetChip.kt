package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/**
 * The `ChipBar` `itemContent` chip for one-tap preset rows (D-03) — durations, relative dates,
 * or any other preset a caller wants to expose as a flat row of tappable chips. `PresetChip`
 * carries no domain meaning of its own: [label] and the optional [supportingLabel] are whatever
 * text the caller supplies (e.g. "30 min" / "7:40 PM").
 *
 * Deliberate, documented deviation from [AppChip] (review 163-04 MEDIUM, responsive coverage):
 * [AppChip]'s inner `Surface` is a fixed 32dp tall and truncates its label with an ellipsis when
 * it doesn't fit. `PresetChip` instead uses a **minimum** height ([heightIn] `min = 32.dp`) and
 * lays its content out in a wrapping [FlowRow] (requires `@OptIn(ExperimentalLayoutApi::class)`,
 * same opt-in [ChipBar] already carries), so at large font scales — or inside a narrow `ChipBar`
 * — the chip grows taller and its text wraps instead of clipping. At the default font scale the
 * chip still draws at AppChip's 32dp height (labelLarge's 20sp line plus 12dp vertical padding),
 * so the two chips look identical in the common case.
 *
 * Visual invariants mirrored from [AppChip] at the default font size: 32dp drawn height, 48dp
 * touch target (outer [Box] with [minimumInteractiveComponentSize]), 8dp corner radius,
 * `colorScheme.outline`/`colorScheme.secondaryContainer` roles, `typography.labelLarge` label —
 * theme roles only, never a hard-coded color.
 *
 * This file imports nothing outside AndroidX/Compose and this hub (HUBW-01 / T-163-02: no app
 * type ever appears in a hub public signature).
 *
 * @param label Required chip text (e.g. "30 min"). Always rendered.
 * @param onClick Invoked on tap. Not invoked while [enabled] is false.
 * @param modifier Applied to the outer 48dp touch-target [Box].
 * @param supportingLabel Optional secondary text rendered after [label] (e.g. a resolved
 *   timestamp such as "7:40 PM"). A `null` or blank value renders no second child and no gap
 *   (conditional-render-no-dead-space) — [FlowRow]'s `spacedBy` arrangement never inserts a
 *   spacing for a single child.
 * @param enabled When `false`, the chip exposes a disabled semantics node, ignores taps, and
 *   renders its content at 38% alpha (Material disabled-content emphasis).
 * @param isSelected When `true`, the chip fills with `colorScheme.secondaryContainer` and drops
 *   its outline border; the semantics node reports `selected = true`.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingLabel: String? = null,
    enabled: Boolean = true,
    isSelected: Boolean = false
) {
    val shape = RoundedCornerShape(8.dp)
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderStroke = if (isSelected) {
        null
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }
    val contentAlpha = if (enabled) 1f else 0.38f

    // Outer Box: reserves >= 48dp touch target without growing the visible chip (mirrors AppChip).
    Box(
        modifier = modifier.minimumInteractiveComponentSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .heightIn(min = 32.dp)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .semantics(mergeDescendants = true) { selected = isSelected },
            shape = shape,
            color = containerColor,
            border = borderStroke
        ) {
            FlowRow(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .alpha(contentAlpha),
                horizontalArrangement = Arrangement.spacedBy(Dimens.ContentSpacing),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                if (!supportingLabel.isNullOrBlank()) {
                    Text(
                        text = supportingLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}
