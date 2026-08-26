package io.github.ygaray.yahirandroidtaste.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Additive expressive design-token surface (DS-01), read via [LocalExpressive] /
 * [MaterialTheme.expressive]. This tracer slice (Plan 01 Task 1) exposes only the two color
 * roles [ProgressRing] needs; Plan 01 Task 2 expands this to the full 9-field DS-01 surface
 * (extra color roles, a hero gradient/brush, expressive type, and a large-card shape) without
 * changing this file's own public shape contract — only additive fields.
 *
 * Every field is derived from a resolved [ColorScheme]'s own roles (see [expressiveTokensFor])
 * rather than duplicating raw color constants — under the hub's fixed (non-dynamic) palette this
 * resolves to the module's existing `theme/Color.kt` values with zero new entries there.
 */
@Immutable
data class ExpressiveTokens(
    /** Empty-track color for ring/bar-style progress primitives. Mirrors [ColorScheme.outline]. */
    val ringTrack: Color,
    /** Filled-progress color for ring/bar-style progress primitives. Mirrors [ColorScheme.primary]. */
    val onTrack: Color,
)

/**
 * Derives [ExpressiveTokens] from a resolved [ColorScheme] — never duplicates raw color
 * constants, always reuses the [ColorScheme]'s own role assignments.
 */
fun expressiveTokensFor(colorScheme: ColorScheme): ExpressiveTokens = ExpressiveTokens(
    ringTrack = colorScheme.outline,
    onTrack = colorScheme.primary,
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
