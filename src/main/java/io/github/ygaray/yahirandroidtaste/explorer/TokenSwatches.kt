package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import io.github.ygaray.yahirandroidtaste.theme.AmberOnTertiary
import io.github.ygaray.yahirandroidtaste.theme.AmberOnTertiaryContainer
import io.github.ygaray.yahirandroidtaste.theme.AmberOnTertiaryContainerDark
import io.github.ygaray.yahirandroidtaste.theme.AmberOnTertiaryDark
import io.github.ygaray.yahirandroidtaste.theme.AmberTertiary
import io.github.ygaray.yahirandroidtaste.theme.AmberTertiaryContainer
import io.github.ygaray.yahirandroidtaste.theme.AmberTertiaryContainerDark
import io.github.ygaray.yahirandroidtaste.theme.AmberTertiaryDark
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import io.github.ygaray.yahirandroidtaste.theme.ErrorRed
import io.github.ygaray.yahirandroidtaste.theme.ErrorRedContainer
import io.github.ygaray.yahirandroidtaste.theme.ErrorRedContainerDark
import io.github.ygaray.yahirandroidtaste.theme.ErrorRedDark
import io.github.ygaray.yahirandroidtaste.theme.InkBackground
import io.github.ygaray.yahirandroidtaste.theme.InkOnBackground
import io.github.ygaray.yahirandroidtaste.theme.InverseOnSurfaceDark
import io.github.ygaray.yahirandroidtaste.theme.InverseOnSurfaceLight
import io.github.ygaray.yahirandroidtaste.theme.InversePrimaryDark
import io.github.ygaray.yahirandroidtaste.theme.InversePrimaryLight
import io.github.ygaray.yahirandroidtaste.theme.InverseSurfaceDark
import io.github.ygaray.yahirandroidtaste.theme.InverseSurfaceLight
import io.github.ygaray.yahirandroidtaste.theme.NeutralBgLight
import io.github.ygaray.yahirandroidtaste.theme.NeutralOnBgLight
import io.github.ygaray.yahirandroidtaste.theme.NeutralOnSurfaceVarDark
import io.github.ygaray.yahirandroidtaste.theme.NeutralOnSurfaceVarLight
import io.github.ygaray.yahirandroidtaste.theme.NeutralSurfaceVarDark
import io.github.ygaray.yahirandroidtaste.theme.NeutralSurfaceVarLight
import io.github.ygaray.yahirandroidtaste.theme.OnErrorRed
import io.github.ygaray.yahirandroidtaste.theme.OnErrorRedContainer
import io.github.ygaray.yahirandroidtaste.theme.OnErrorRedContainerDark
import io.github.ygaray.yahirandroidtaste.theme.OnErrorRedDark
import io.github.ygaray.yahirandroidtaste.theme.OutlineDark
import io.github.ygaray.yahirandroidtaste.theme.OutlineLight
import io.github.ygaray.yahirandroidtaste.theme.OutlineVarDark
import io.github.ygaray.yahirandroidtaste.theme.OutlineVarLight
import io.github.ygaray.yahirandroidtaste.theme.ScrimColor
import io.github.ygaray.yahirandroidtaste.theme.SlateOnSecondary
import io.github.ygaray.yahirandroidtaste.theme.SlateOnSecondaryContainer
import io.github.ygaray.yahirandroidtaste.theme.SlateOnSecondaryContainerDark
import io.github.ygaray.yahirandroidtaste.theme.SlateOnSecondaryDark
import io.github.ygaray.yahirandroidtaste.theme.SlateSecondary
import io.github.ygaray.yahirandroidtaste.theme.SlateSecondaryContainer
import io.github.ygaray.yahirandroidtaste.theme.SlateSecondaryContainerDark
import io.github.ygaray.yahirandroidtaste.theme.SlateSecondaryDark
import io.github.ygaray.yahirandroidtaste.theme.SurfaceBrightDark
import io.github.ygaray.yahirandroidtaste.theme.SurfaceBrightLight
import io.github.ygaray.yahirandroidtaste.theme.SurfaceContainerDark
import io.github.ygaray.yahirandroidtaste.theme.SurfaceContainerHighDark
import io.github.ygaray.yahirandroidtaste.theme.SurfaceContainerHighLight
import io.github.ygaray.yahirandroidtaste.theme.SurfaceContainerHighestDark
import io.github.ygaray.yahirandroidtaste.theme.SurfaceContainerHighestLight
import io.github.ygaray.yahirandroidtaste.theme.SurfaceContainerLight
import io.github.ygaray.yahirandroidtaste.theme.SurfaceContainerLowDark
import io.github.ygaray.yahirandroidtaste.theme.SurfaceContainerLowLight
import io.github.ygaray.yahirandroidtaste.theme.SurfaceContainerLowestDark
import io.github.ygaray.yahirandroidtaste.theme.SurfaceContainerLowestLight
import io.github.ygaray.yahirandroidtaste.theme.SurfaceDimDark
import io.github.ygaray.yahirandroidtaste.theme.SurfaceDimLight
import io.github.ygaray.yahirandroidtaste.theme.TealOnPrimary
import io.github.ygaray.yahirandroidtaste.theme.TealOnPrimaryContainer
import io.github.ygaray.yahirandroidtaste.theme.TealOnPrimaryContainerDark
import io.github.ygaray.yahirandroidtaste.theme.TealOnPrimaryDark
import io.github.ygaray.yahirandroidtaste.theme.TealPrimary
import io.github.ygaray.yahirandroidtaste.theme.TealPrimaryContainer
import io.github.ygaray.yahirandroidtaste.theme.TealPrimaryContainerDark
import io.github.ygaray.yahirandroidtaste.theme.TealPrimaryDark
import io.github.ygaray.yahirandroidtaste.theme.Typography

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
