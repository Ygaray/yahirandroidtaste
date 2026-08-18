package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Semi-transparent pill badge displayed on a home tag tile showing the card count.
 *
 * Container is a low-opacity overlay of the tile foreground color so it reads
 * against any accent background without a dedicated colorScheme token.
 *
 * Phase 86 (GADGET-03): relocated verbatim from `:app`'s
 * `feature/dashboard/components/CountBadge.kt` into `:yahirandroidtaste/component/` — a
 * pure-presentational move, no logic change (D-02). [contrastingForeground] is same-package
 * now, so its import line is dropped rather than rewritten.
 *
 * @param count The number to display. Values > 999 are shown as "999+".
 * @param tileAccentColor The tile's accent background color — used to derive
 *   foreground and badge container colors so the badge is always legible.
 */
@Composable
fun CountBadge(
    count: Int,
    tileAccentColor: Color,
    modifier: Modifier = Modifier
) {
    val foreground = contrastingForeground(tileAccentColor)
    val badgeContainer = foreground.copy(alpha = 0.18f)
    val displayText = if (count > 999) "999+" else count.toString()

    Surface(
        shape = CircleShape,
        color = badgeContainer,
        modifier = modifier
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall,
            color = foreground,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
