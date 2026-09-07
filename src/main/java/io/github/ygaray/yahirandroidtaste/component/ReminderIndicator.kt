package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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
 * Shared render cluster for REMIND-09's card-face reminder presence indicator.
 *
 * The count is caller-supplied only — this composable never queries reminder storage itself; it
 * simply renders whatever [reminderCount] the caller passes. It is deliberately `internal` (not
 * `public`), so `ComponentRegistryDriftGuardTest`'s public-top-level scan does not require a
 * `ComponentRegistry` entry: the guard's `isPrivateOrInternal` check exempts it entirely — the
 * exact precedent [ImageCountIndicator] already established.
 *
 * Renders nothing and reserves no space when [reminderCount] is not positive
 * ([[conditional-render-no-dead-space]]). When positive, renders a 16dp bell icon tinted with the
 * theme's surface-variant foreground role, a [Dimens.ContentSpacing] gap, then the exact integer
 * count in the theme's small-label typography role and the same foreground color role. The count
 * is never capped, abbreviated, rounded, or truncated. The icon carries the accessibility
 * description (singular wording at exactly 1, plural otherwise); the count text carries no
 * separate description.
 *
 * Deliberately uses [Dimens.ContentSpacing] (4dp) rather than [ImageCountIndicator]'s
 * [Dimens.HairlineSpacing] (2dp) — the UI-SPEC's one deliberate departure from pixel-parity with
 * the shipped image-count indicator, keeping the icon-to-count gap on the 8-point grid.
 *
 * @param reminderCount Caller-supplied number of active reminders attached to the card. Not
 *   positive (zero or negative) composes nothing.
 * @param modifier Applied to the outer row, so call sites can supply their own padding/spacing.
 */
@Composable
internal fun ReminderIndicator(reminderCount: Int, modifier: Modifier = Modifier) {
    if (reminderCount <= 0) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.semantics(mergeDescendants = true) {}
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "$reminderCount reminder${if (reminderCount != 1) "s" else ""}",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(Dimens.ContentSpacing))
        Text(
            text = "$reminderCount",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
