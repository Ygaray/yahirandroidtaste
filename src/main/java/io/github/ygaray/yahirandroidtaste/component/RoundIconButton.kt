package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/**
 * A round, outlined icon button: a transparent circle with a 1dp `outlineVariant` border and a
 * 20dp `onSurface` icon (sketch Y-8). It is built by hand rather than from the Material outlined
 * icon button because that component draws `colorScheme.outline`, not the quieter `outlineVariant`
 * the Receipt surfaces use.
 *
 * The drawn circle is [size] (default 40dp), but the touch region and the layout reserve are never
 * smaller than 48dp ([minimumInteractiveComponentSize]), so neighbouring buttons keep an
 * accessible 48dp pitch even when [size] is smaller.
 *
 * The node carries the stable test tag `round_icon_button`. The tag is applied after [modifier],
 * so a caller `Modifier.testTag(...)` wins. The library never exposes tags as resource ids.
 *
 * @param icon The glyph drawn inside the circle (decorative; the accessible name is
 *   [contentDescription]).
 * @param contentDescription Required accessible name of the button. Must be static UI copy (for
 *   example "Add"), never user content or sensitive text.
 * @param onClick Invoked on tap. Never invoked while [enabled] is false.
 * @param modifier Applied first on the node, before the touch-target and size modifiers.
 * @param enabled When `false`, the node is disabled, ignores taps and renders at 38% alpha.
 * @param size Diameter of the drawn circle. Defaults to [Dimens.RoundIconButton.Size].
 */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = Dimens.RoundIconButton.Size
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(size)
            .testTag("round_icon_button")
            .alpha(if (enabled) 1f else DISABLED_CONTENT_ALPHA)
            .clip(CircleShape)
            .border(Dimens.HairlineBorder, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimens.RoundIconButton.IconSize),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
