package io.github.ygaray.yahirandroidtaste.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp

/**
 * Public showcase composable for [Dimens.Elevation] (Phase 123 DS-01, Tactile Design System
 * foundation) — renders one sample band per level (`Level0` -> `Level5`) at a consistent
 * size/shape so only the elevation changes are perceptible, each labeled with its dp value.
 *
 * Uses real `shadowElevation` (`Modifier.shadow(...)`) rather than `tonalElevation` alone — a
 * tonal-only scale is barely perceptible against a near-white light-theme background
 * (`123-UI-SPEC.md` Pitfall 5). `.shadow(...)` is applied BEFORE `.background(...)` in each
 * band's modifier chain — reversing that order clips the shadow away invisibly (Pitfall 6).
 *
 * Reads [MaterialTheme.colorScheme] live so it renders correctly in both the Explorer's light
 * and dark theme modes (Pitfall 5). Uses only existing [Dimens] tokens plus [Dimens.Elevation]
 * — no new raw dp literal for padding, spacing, or height.
 */
@Composable
fun ElevationLadder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.HorizontalPadding, vertical = Dimens.TopPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.ContentSpacing)
    ) {
        ElevationBand("Level0 — 0dp", Dimens.Elevation.Level0)
        ElevationBand("Level1 — 1dp", Dimens.Elevation.Level1)
        ElevationBand("Level2 — 3dp", Dimens.Elevation.Level2)
        ElevationBand("Level3 — 6dp", Dimens.Elevation.Level3)
        ElevationBand("Level4 — 8dp", Dimens.Elevation.Level4)
        ElevationBand("Level5 — 12dp", Dimens.Elevation.Level5)
    }
}

@Composable
private fun ElevationBand(label: String, elevation: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.TouchTarget)
            .shadow(elevation = elevation, shape = RoundedCornerShape(Dimens.CornerRadius.Medium), clip = true)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Dimens.HorizontalPadding)
        )
    }
}
