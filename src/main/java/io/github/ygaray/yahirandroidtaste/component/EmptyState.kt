package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Shared empty state composable for all modules.
 *
 * Replaces per-module empty state implementations with a single generic composable
 * driven by parameters. Layout: 48dp icon, headlineSmall title, bodyMedium body,
 * optional CTA button — all tinted onSurfaceVariant.
 *
 * @param icon The icon to display at the top of the empty state.
 * @param title The headline text shown below the icon.
 * @param body Optional body text shown below the title. Omitted when null.
 * @param ctaLabel Optional label for the call-to-action button. Omitted when null or when [onCta] is null.
 * @param onCta Optional callback invoked when the CTA button is tapped. Both [ctaLabel] and [onCta]
 *   must be non-null for the button to appear.
 * @param modifier Modifier for the container Column.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    ctaLabel: String? = null,
    onCta: (() -> Unit)? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (body != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (ctaLabel != null && onCta != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onCta) {
                Text(ctaLabel)
            }
        }
    }
}
