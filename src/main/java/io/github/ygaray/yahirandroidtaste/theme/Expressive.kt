package io.github.ygaray.yahirandroidtaste.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Additive expressive design-token surface (DS-01), read via [LocalExpressive] /
 * [MaterialTheme.expressive]. Every color field is derived from a resolved [ColorScheme]'s own
 * roles (see [expressiveTokensFor]) rather than duplicating raw color constants — under the
 * hub's fixed (non-dynamic) palette this resolves to the module's existing `theme/Color.kt`
 * values with zero new entries there.
 */
@Immutable
data class ExpressiveTokens(
    /** Empty-track color for ring/bar-style progress primitives. Mirrors [ColorScheme.outline]. */
    val ringTrack: Color,
    /** Filled-progress color for ring/bar-style progress primitives. Mirrors [ColorScheme.primary]. */
    val onTrack: Color,
    /** Over-budget/negative-state color. Mirrors [ColorScheme.error]. */
    val overBudget: Color,
    /** Near-goal/positive-highlight color. Mirrors [ColorScheme.tertiary]. */
    val nearGoal: Color,
    /** Hero surface gradient. Linear gradient from [ColorScheme.primary] to [ColorScheme.primaryContainer]. */
    val heroGradient: Brush,
    /** Large numeric hero-value text style (e.g. a headline stat). */
    val heroValueStyle: TextStyle,
    /** Hero-value's supporting label text style. */
    val heroLabelStyle: TextStyle,
    /** Mid-size stat-value text style (e.g. a secondary metric). */
    val statValueStyle: TextStyle,
    /** Large corner-radius shape for hero/feature card surfaces. Backed by [Dimens.CornerRadius.Large]. */
    val cardShapeLarge: Shape,
)

/**
 * Derives [ExpressiveTokens] from a resolved [ColorScheme] — never duplicates raw color
 * constants, always reuses the [ColorScheme]'s own role assignments.
 */
fun expressiveTokensFor(colorScheme: ColorScheme): ExpressiveTokens = ExpressiveTokens(
    ringTrack = colorScheme.outline,
    onTrack = colorScheme.primary,
    overBudget = colorScheme.error,
    nearGoal = colorScheme.tertiary,
    heroGradient = Brush.linearGradient(listOf(colorScheme.primary, colorScheme.primaryContainer)),
    heroValueStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),
    heroLabelStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    statValueStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    cardShapeLarge = RoundedCornerShape(Dimens.CornerRadius.Large),
)

/**
 * Non-throwing [ExpressiveTokens] CompositionLocal (DS-01's literal contract). The default value
 * is a REAL [ExpressiveTokens] instance derived from the hub's own [LightColorScheme] — never
 * `error(...)` — so reading [LocalExpressive] outside a [YahirAndroidTasteTheme] wrapper (e.g. an
 * unwrapped Preview, or a not-yet-migrated composition) never throws.
 */
val LocalExpressive = staticCompositionLocalOf<ExpressiveTokens> { expressiveTokensFor(LightColorScheme) }

/** Reads the current [ExpressiveTokens] from [LocalExpressive]. */
val MaterialTheme.expressive: ExpressiveTokens
    @Composable
    @ReadOnlyComposable
    get() = LocalExpressive.current
