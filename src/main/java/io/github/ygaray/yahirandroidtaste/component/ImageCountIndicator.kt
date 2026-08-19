package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/**
 * Shared render cluster for IMG-02's inline-image-count indicator.
 *
 * The count is caller-supplied only — this composable never computes it from card content, body
 * text, or any storage. It is deliberately `internal` (not `public`), so
 * `ComponentRegistryDriftGuardTest`'s public-top-level scan does not require a
 * `ComponentRegistry` entry: the guard's `isPrivateOrInternal` check exempts it entirely.
 *
 * Renders nothing and reserves no space when [imageCount] is not positive
 * ([[conditional-render-no-dead-space]]). When positive, renders a 16dp filled image icon tinted
 * with the theme's surface-variant foreground role, a [Dimens.HairlineSpacing] gap, then the
 * exact integer count in the theme's small-label typography role and the same foreground color
 * role. The count is never capped, abbreviated, rounded, or truncated. The icon carries the
 * accessibility description (singular wording at exactly 1, plural otherwise); the count text
 * carries no separate description.
 *
 * @param imageCount Caller-supplied number of inline images the card body contains. Not positive
 *   (zero or negative) composes nothing.
 * @param modifier Applied to the outer row, so call sites can supply their own padding/spacing.
 */
@Composable
internal fun ImageCountIndicator(imageCount: Int, modifier: Modifier = Modifier) {
    if (imageCount <= 0) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.semantics(mergeDescendants = true) {}
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "$imageCount inline image${if (imageCount != 1) "s" else ""}",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(Dimens.HairlineSpacing))
        Text(
            text = "$imageCount",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
