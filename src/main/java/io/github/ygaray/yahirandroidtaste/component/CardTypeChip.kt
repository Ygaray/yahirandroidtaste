package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/**
 * Tactile card-face type-indicator badge (Phase 129 DS-02 D-01) — a 32dp square, accent-tinted
 * background carrying an 18dp icon slot. Sibling to [CardBase]'s accent spine: both are driven by
 * the same generic per-card accent [Color] the caller supplies. The hub performs NO tag-resolution
 * logic here — Phase 131's app-side resolver supplies the actual [Color]; this composable only
 * renders whatever it is handed.
 *
 * `accent == null` is a designed neutral state (not an omission or a broken/unpainted look): the
 * background falls back to `colorScheme.surfaceVariant` and the icon tints to
 * `colorScheme.onSurfaceVariant`.
 *
 * ⚠ Deliberate deviation from [ColorUtils.kt]'s general foreground contract: the icon is drawn at
 * FULL accent strength when [accent] is non-null — never resolved through [contrastingForeground]
 * — per 129-UI-SPEC.md's "Accent reserved for" item 3 and the approved canvas (Voice chip renders
 * a `#F5E9DA` light-tint background with a `#B4690E` accent-hue icon, not a black/white icon).
 * This is a reviewed exception scoped to this one element because the chip's own background is
 * always a light tint of that same accent (via [accentTint]), so the accent hue reads directly
 * without a separate contrast pass. [contrastingForeground] itself is unchanged and still governs
 * every other accent-tinted surface in this module.
 *
 * @param accent Optional per-card accent [Color]. Drives the badge background (via [accentTint])
 *   and the icon tint. `null` renders the neutral branch described above.
 * @param modifier Modifier applied to the outer 32dp badge [Box].
 * @param icon The icon slot — typically a Material3 `Icon(...)` call with no explicit `tint`, so
 *   it inherits both the 18dp size and the resolved tint color from this composable via
 *   [LocalContentColor].
 */
@Composable
fun CardTypeChip(
    accent: Color?,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = if (accent != null) {
        accentTint(accent, colorScheme)
    } else {
        colorScheme.surfaceVariant
    }
    // Full accent strength, NOT contrastingForeground — see class KDoc's deliberate deviation note.
    val tintColor = accent ?: colorScheme.onSurfaceVariant

    Box(
        // 32dp is 129-UI-SPEC.md's Spacing Scale value for this badge — a literal, not
        // Dimens.Icons.MenuButton, because that token names a semantically different concept
        // (the MoreVert IconButton container) that merely happens to share the same dp value.
        modifier = modifier
            .size(32.dp)
            .testTag("card_type_chip")
            .clip(RoundedCornerShape(Dimens.CornerRadius.Small))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides tintColor) {
            Box(modifier = Modifier.size(Dimens.Icons.ChipIcon), contentAlignment = Alignment.Center) {
                icon()
            }
        }
    }
}
