package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/**
 * Content alpha applied to a whole Phase 20 atom ([PillChip], [RoundIconButton]) while it is
 * disabled (Material disabled emphasis).
 */
internal const val DISABLED_CONTENT_ALPHA = 0.38f

/**
 * A fully rounded, outlined chip with a single-line label and optional leading and trailing icons
 * (sketch Y-12): a transparent pill with a 1dp `outlineVariant` border, a `bodySmall` `onSurface`
 * label and 18dp icons.
 *
 * The chip is 32dp tall at the default font scale. Its height is a **minimum**, so at large font
 * scales it grows instead of clipping (the same rationale [PresetChip] documents). The label is
 * always one line and ellipsizes when the chip is narrower than the text; the label takes the
 * leftover width after the icons are measured, so a trailing icon stays visible when the label is
 * truncated. The touch region and the layout reserve are at least 48dp
 * ([minimumInteractiveComponentSize]).
 *
 * Horizontal padding is 12dp per side when the chip is label-only, and 8dp on a side that carries
 * an icon, with a 4dp gap between an icon and the label.
 *
 * The chip is **one** clickable node: the node that carries [modifier], the stable test tag
 * `pill_chip`, the click action and the optional [contentDescription] is the same node. The tag is
 * applied after [modifier], so a caller `Modifier.testTag(...)` wins. The library never exposes
 * tags as resource ids.
 *
 * [AppChip] and [PresetChip] keep their 8dp corner shape; this is a separate, additive atom.
 *
 * @param label Chip text. Must be static UI copy (for example "Today"), never sensitive text.
 *   Rendered on a single line and ellipsized.
 * @param onClick Invoked on tap. Never invoked while [enabled] is false.
 * @param modifier Applied first on the chip's single clickable node.
 * @param leadingIcon Optional decorative icon before the label.
 * @param trailingIcon Optional decorative icon after the label.
 * @param enabled When `false`, the node is disabled, ignores taps and renders at 38% alpha.
 * @param contentDescription Optional accessible name for the chip. `null` keeps the visible
 *   [label] text as the accessible name.
 */
@Composable
fun PillChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    val startPadding = if (leadingIcon != null) {
        Dimens.PillChip.IconSideHorizontalPadding
    } else {
        Dimens.PillChip.LabelOnlyHorizontalPadding
    }
    val endPadding = if (trailingIcon != null) {
        Dimens.PillChip.IconSideHorizontalPadding
    } else {
        Dimens.PillChip.LabelOnlyHorizontalPadding
    }
    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .heightIn(min = Dimens.PillChip.Height)
            .testTag("pill_chip")
            .alpha(if (enabled) 1f else DISABLED_CONTENT_ALPHA)
            .clip(CircleShape)
            .border(Dimens.HairlineBorder, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { if (contentDescription != null) this.contentDescription = contentDescription }
            .padding(start = startPadding, end = endPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(Dimens.Icons.ChipIcon),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(Dimens.ContentSpacing))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (trailingIcon != null) {
            Spacer(Modifier.width(Dimens.ContentSpacing))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                modifier = Modifier.size(Dimens.Icons.ChipIcon),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
