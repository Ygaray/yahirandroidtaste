package com.example.secondbrain.yahirandroidtaste.explorer

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.example.secondbrain.yahirandroidtaste.theme.AmberOnTertiary
import com.example.secondbrain.yahirandroidtaste.theme.AmberOnTertiaryContainer
import com.example.secondbrain.yahirandroidtaste.theme.AmberOnTertiaryContainerDark
import com.example.secondbrain.yahirandroidtaste.theme.AmberOnTertiaryDark
import com.example.secondbrain.yahirandroidtaste.theme.AmberTertiary
import com.example.secondbrain.yahirandroidtaste.theme.AmberTertiaryContainer
import com.example.secondbrain.yahirandroidtaste.theme.AmberTertiaryContainerDark
import com.example.secondbrain.yahirandroidtaste.theme.AmberTertiaryDark
import com.example.secondbrain.yahirandroidtaste.theme.Dimens
import com.example.secondbrain.yahirandroidtaste.theme.ErrorRed
import com.example.secondbrain.yahirandroidtaste.theme.ErrorRedContainer
import com.example.secondbrain.yahirandroidtaste.theme.ErrorRedContainerDark
import com.example.secondbrain.yahirandroidtaste.theme.ErrorRedDark
import com.example.secondbrain.yahirandroidtaste.theme.InkBackground
import com.example.secondbrain.yahirandroidtaste.theme.InkOnBackground
import com.example.secondbrain.yahirandroidtaste.theme.InverseOnSurfaceDark
import com.example.secondbrain.yahirandroidtaste.theme.InverseOnSurfaceLight
import com.example.secondbrain.yahirandroidtaste.theme.InversePrimaryDark
import com.example.secondbrain.yahirandroidtaste.theme.InversePrimaryLight
import com.example.secondbrain.yahirandroidtaste.theme.InverseSurfaceDark
import com.example.secondbrain.yahirandroidtaste.theme.InverseSurfaceLight
import com.example.secondbrain.yahirandroidtaste.theme.NeutralBgLight
import com.example.secondbrain.yahirandroidtaste.theme.NeutralOnBgLight
import com.example.secondbrain.yahirandroidtaste.theme.NeutralOnSurfaceVarDark
import com.example.secondbrain.yahirandroidtaste.theme.NeutralOnSurfaceVarLight
import com.example.secondbrain.yahirandroidtaste.theme.NeutralSurfaceVarDark
import com.example.secondbrain.yahirandroidtaste.theme.NeutralSurfaceVarLight
import com.example.secondbrain.yahirandroidtaste.theme.OnErrorRed
import com.example.secondbrain.yahirandroidtaste.theme.OnErrorRedContainer
import com.example.secondbrain.yahirandroidtaste.theme.OnErrorRedContainerDark
import com.example.secondbrain.yahirandroidtaste.theme.OnErrorRedDark
import com.example.secondbrain.yahirandroidtaste.theme.OutlineDark
import com.example.secondbrain.yahirandroidtaste.theme.OutlineLight
import com.example.secondbrain.yahirandroidtaste.theme.OutlineVarDark
import com.example.secondbrain.yahirandroidtaste.theme.OutlineVarLight
import com.example.secondbrain.yahirandroidtaste.theme.ScrimColor
import com.example.secondbrain.yahirandroidtaste.theme.SlateOnSecondary
import com.example.secondbrain.yahirandroidtaste.theme.SlateOnSecondaryContainer
import com.example.secondbrain.yahirandroidtaste.theme.SlateOnSecondaryContainerDark
import com.example.secondbrain.yahirandroidtaste.theme.SlateOnSecondaryDark
import com.example.secondbrain.yahirandroidtaste.theme.SlateSecondary
import com.example.secondbrain.yahirandroidtaste.theme.SlateSecondaryContainer
import com.example.secondbrain.yahirandroidtaste.theme.SlateSecondaryContainerDark
import com.example.secondbrain.yahirandroidtaste.theme.SlateSecondaryDark
import com.example.secondbrain.yahirandroidtaste.theme.SurfaceBrightDark
import com.example.secondbrain.yahirandroidtaste.theme.SurfaceBrightLight
import com.example.secondbrain.yahirandroidtaste.theme.SurfaceContainerDark
import com.example.secondbrain.yahirandroidtaste.theme.SurfaceContainerHighDark
import com.example.secondbrain.yahirandroidtaste.theme.SurfaceContainerHighLight
import com.example.secondbrain.yahirandroidtaste.theme.SurfaceContainerHighestDark
import com.example.secondbrain.yahirandroidtaste.theme.SurfaceContainerHighestLight
import com.example.secondbrain.yahirandroidtaste.theme.SurfaceContainerLight
import com.example.secondbrain.yahirandroidtaste.theme.SurfaceContainerLowDark
import com.example.secondbrain.yahirandroidtaste.theme.SurfaceContainerLowLight
import com.example.secondbrain.yahirandroidtaste.theme.SurfaceContainerLowestDark
import com.example.secondbrain.yahirandroidtaste.theme.SurfaceContainerLowestLight
import com.example.secondbrain.yahirandroidtaste.theme.SurfaceDimDark
import com.example.secondbrain.yahirandroidtaste.theme.SurfaceDimLight
import com.example.secondbrain.yahirandroidtaste.theme.TealOnPrimary
import com.example.secondbrain.yahirandroidtaste.theme.TealOnPrimaryContainer
import com.example.secondbrain.yahirandroidtaste.theme.TealOnPrimaryContainerDark
import com.example.secondbrain.yahirandroidtaste.theme.TealOnPrimaryDark
import com.example.secondbrain.yahirandroidtaste.theme.TealPrimary
import com.example.secondbrain.yahirandroidtaste.theme.TealPrimaryContainer
import com.example.secondbrain.yahirandroidtaste.theme.TealPrimaryContainerDark
import com.example.secondbrain.yahirandroidtaste.theme.TealPrimaryDark
import com.example.secondbrain.yahirandroidtaste.theme.Typography

/**
 * Hand-authored, verbatim-from-`theme/` reference tables backing the token-browser screen
 * (EXPLORE-03, D-02). Every value here is read directly from `theme/Color.kt`, `theme/Type.kt`,
 * and `theme/Dimens.kt` — no reflection, no derived/computed pairing (RESEARCH.md Pattern 6-8,
 * Pitfall 1). This file is data-only; the Scaffold/screen that renders these tables is
 * `TokenBrowserScreen` (Plan 03).
 *
 * The light/dark color pairing below is hand-authored, NEVER derived by string manipulation on
 * val names — the neutral-surface dark counterparts use the semantically-named `Ink*` prefix
 * (not a mechanical `*Dark` suffix), and `Scrim` has no distinct dark variant at all (Pitfall 1).
 */

/** A single named (light, dark) color-token pair rendered as a swatch or elevation ramp row. */
data class TokenSwatch(val name: String, val light: Color, val dark: Color)

/** All named color tokens from `theme/Color.kt`, in fixed authored-list order (Color group). */
val colorSwatches: List<TokenSwatch> = listOf(
    // Primary
    TokenSwatch("Primary", TealPrimary, TealPrimaryDark),
    TokenSwatch("On Primary", TealOnPrimary, TealOnPrimaryDark),
    TokenSwatch("Primary Container", TealPrimaryContainer, TealPrimaryContainerDark),
    TokenSwatch("On Primary Container", TealOnPrimaryContainer, TealOnPrimaryContainerDark),
    // Secondary
    TokenSwatch("Secondary", SlateSecondary, SlateSecondaryDark),
    TokenSwatch("On Secondary", SlateOnSecondary, SlateOnSecondaryDark),
    TokenSwatch("Secondary Container", SlateSecondaryContainer, SlateSecondaryContainerDark),
    TokenSwatch("On Secondary Container", SlateOnSecondaryContainer, SlateOnSecondaryContainerDark),
    // Tertiary
    TokenSwatch("Tertiary", AmberTertiary, AmberTertiaryDark),
    TokenSwatch("On Tertiary", AmberOnTertiary, AmberOnTertiaryDark),
    TokenSwatch("Tertiary Container", AmberTertiaryContainer, AmberTertiaryContainerDark),
    TokenSwatch("On Tertiary Container", AmberOnTertiaryContainer, AmberOnTertiaryContainerDark),
    // Error
    TokenSwatch("Error", ErrorRed, ErrorRedDark),
    TokenSwatch("On Error", OnErrorRed, OnErrorRedDark),
    TokenSwatch("Error Container", ErrorRedContainer, ErrorRedContainerDark),
    TokenSwatch("On Error Container", OnErrorRedContainer, OnErrorRedContainerDark),
    // Neutral surfaces — dark counterpart is "Ink*"-prefixed, NOT "*Dark"-suffixed (Pitfall 1)
    TokenSwatch("Background", NeutralBgLight, InkBackground),
    TokenSwatch("On Background", NeutralOnBgLight, InkOnBackground),
    TokenSwatch("Surface Variant", NeutralSurfaceVarLight, NeutralSurfaceVarDark),
    TokenSwatch("On Surface Variant", NeutralOnSurfaceVarLight, NeutralOnSurfaceVarDark),
    TokenSwatch("Outline", OutlineLight, OutlineDark),
    TokenSwatch("Outline Variant", OutlineVarLight, OutlineVarDark),
    // Scrim has no distinct dark variant — same value used for both (Pitfall 1)
    TokenSwatch("Scrim", ScrimColor, ScrimColor),
    TokenSwatch("Inverse Surface", InverseSurfaceLight, InverseSurfaceDark),
    TokenSwatch("Inverse On Surface", InverseOnSurfaceLight, InverseOnSurfaceDark),
    TokenSwatch("Inverse Primary", InversePrimaryLight, InversePrimaryDark)
)

/** A single named M3 typography style, read directly from the `Typography` instance. */
data class TypeScaleEntry(val name: String, val style: TextStyle)

/** All 15 named M3 Typography styles (`Typography.displayLarge` .. `Typography.labelSmall`). */
val typeScaleEntries: List<TypeScaleEntry> = listOf(
    TypeScaleEntry("Display Large", Typography.displayLarge),
    TypeScaleEntry("Display Medium", Typography.displayMedium),
    TypeScaleEntry("Display Small", Typography.displaySmall),
    TypeScaleEntry("Headline Large", Typography.headlineLarge),
    TypeScaleEntry("Headline Medium", Typography.headlineMedium),
    TypeScaleEntry("Headline Small", Typography.headlineSmall),
    TypeScaleEntry("Title Large", Typography.titleLarge),
    TypeScaleEntry("Title Medium", Typography.titleMedium),
    TypeScaleEntry("Title Small", Typography.titleSmall),
    TypeScaleEntry("Body Large", Typography.bodyLarge),
    TypeScaleEntry("Body Medium", Typography.bodyMedium),
    TypeScaleEntry("Body Small", Typography.bodySmall),
    TypeScaleEntry("Label Large", Typography.labelLarge),
    TypeScaleEntry("Label Medium", Typography.labelMedium),
    TypeScaleEntry("Label Small", Typography.labelSmall)
)

/** A single named spacing/size dimension token, read directly from `theme/Dimens.kt`. */
data class DimensEntry(val name: String, val value: Dp)

/** All named `Dimens` spacing/size tokens, in fixed authored-list order (Spacing group). */
val dimensEntries: List<DimensEntry> = listOf(
    DimensEntry("Horizontal Padding", Dimens.HorizontalPadding),
    DimensEntry("Top Padding", Dimens.TopPadding),
    DimensEntry("Bottom Padding", Dimens.BottomPadding),
    DimensEntry("Content Spacing", Dimens.ContentSpacing),
    DimensEntry("Hairline Spacing", Dimens.HairlineSpacing),
    DimensEntry("Touch Target", Dimens.TouchTarget),
    DimensEntry("Compact Padding", Dimens.CompactPadding),
    DimensEntry("Hairline Border", Dimens.HairlineBorder),
    DimensEntry("Icon — Menu Button", Dimens.Icons.MenuButton),
    DimensEntry("Icon — Menu Icon", Dimens.Icons.MenuIcon),
    DimensEntry("Icon — Drag Handle", Dimens.Icons.DragHandle),
    DimensEntry("Swipe Reveal — Button Width", Dimens.SwipeReveal.ButtonWidth),
    DimensEntry("Swipe Reveal — Icon Size", Dimens.SwipeReveal.IconSize),
    DimensEntry("Corner Radius — Small", Dimens.CornerRadius.Small),
    DimensEntry("Corner Radius — Medium", Dimens.CornerRadius.Medium)
)

/**
 * The 7 `SurfaceContainer*`/`SurfaceDim*`/`SurfaceBright*` elevation-ramp levels, as (name,
 * light, dark) pairs. Presented as rendered surfaces (not flat swatches) by the Elevation
 * section — a presentation difference from [colorSwatches], not duplicated data (RESEARCH.md
 * Pattern 7).
 */
val elevationLevels: List<TokenSwatch> = listOf(
    TokenSwatch("Surface Dim", SurfaceDimLight, SurfaceDimDark),
    TokenSwatch("Surface Container Lowest", SurfaceContainerLowestLight, SurfaceContainerLowestDark),
    TokenSwatch("Surface Container Low", SurfaceContainerLowLight, SurfaceContainerLowDark),
    TokenSwatch("Surface Container", SurfaceContainerLight, SurfaceContainerDark),
    TokenSwatch("Surface Container High", SurfaceContainerHighLight, SurfaceContainerHighDark),
    TokenSwatch("Surface Container Highest", SurfaceContainerHighestLight, SurfaceContainerHighestDark),
    TokenSwatch("Surface Bright", SurfaceBrightLight, SurfaceBrightDark)
)

/** A single named shape token, either an M3 default shape or a `Dimens.CornerRadius` tile. */
data class ShapeEntry(val name: String, val shape: Shape)

/**
 * The M3 default `Shapes` (read live from `MaterialTheme.shapes`, never hardcoded dp values —
 * RESEARCH.md Pattern 8) plus the two `Dimens.CornerRadius` tokens rendered as `RoundedCornerShape`
 * tiles, in fixed authored-list order (Shape group).
 */
@Composable
fun shapeEntries(): List<ShapeEntry> {
    val shapes = MaterialTheme.shapes
    return listOf(
        ShapeEntry("Extra Small", shapes.extraSmall),
        ShapeEntry("Small", shapes.small),
        ShapeEntry("Medium", shapes.medium),
        ShapeEntry("Large", shapes.large),
        ShapeEntry("Extra Large", shapes.extraLarge),
        ShapeEntry("Corner Radius — Small (${Dimens.CornerRadius.Small})", RoundedCornerShape(Dimens.CornerRadius.Small)),
        ShapeEntry("Corner Radius — Medium (${Dimens.CornerRadius.Medium})", RoundedCornerShape(Dimens.CornerRadius.Medium))
    )
}
