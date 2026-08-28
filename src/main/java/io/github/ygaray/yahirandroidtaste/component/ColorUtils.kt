package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/**
 * Returns Color.Black or Color.White depending on which provides better contrast
 * against [background]. Uses W3C relative luminance threshold of 0.5.
 */
fun contrastingForeground(background: Color): Color =
    if (background.luminance() > 0.5f) Color.Black else Color.White

/**
 * Returns the two-stop color ramp behind [accentGradient] (Phase 123 DS-01, D-02) — exposed as
 * its own pure function because a [Brush]'s color stops are not publicly readable, and the
 * "sheen is never darker than the base" contract needs a directly assertable return value to be
 * unit-testable.
 *
 * Stop 0 is [accentColor] unchanged, full strength. Stop 1 blends [accentColor] ~40% toward a
 * `sheenTarget` role read live from [colorScheme] — never a hardcoded constant. The UI-SPEC's
 * contract is "blend toward `colorScheme.surface` for a lightened sheen, never darkened," but in
 * a dark theme `surface` is near-black, so blending toward it would darken the result. This
 * function instead blends toward `colorScheme.onSurface` (a light color scheme role in dark
 * theme) whenever `colorScheme.surface` is itself dark — determined with the same W3C relative-
 * luminance threshold [contrastingForeground] already uses in this file — which keeps the mix
 * partner a live theme role while still preserving the "always lighter" guarantee.
 *
 * ⚠ ASSUMED — the 0.4f blend ratio is marked `ASSUMED` in `123-UI-SPEC.md`, pending a
 * design-canvas cross-check before the Phase 123 tag cut.
 */
fun accentGradientStops(accentColor: Color, colorScheme: ColorScheme): List<Color> {
    val isDark = colorScheme.surface.luminance() < 0.5f
    val sheenTarget = if (isDark) colorScheme.onSurface else colorScheme.surface
    return listOf(accentColor, lerp(accentColor, sheenTarget, 0.4f))
}

/**
 * Parametrized pure-function gradient helper (Phase 123 DS-01, D-02) — takes a runtime accent
 * [Color] rather than a fixed `heroGradient`-style field, so per-tag-tinted surfaces (HOME-08)
 * can drive it with any accent at call time. The stop math lives in [accentGradientStops] because
 * a [Brush]'s color stops are not publicly readable, so exposing them separately is what keeps
 * the never-darkened contract unit-testable — the same "assert on a pure function, not on a
 * rendered artifact" discipline [io.github.ygaray.yahirandroidtaste.component.relatednessVisual]
 * follows.
 *
 * `start`/`end` are left at [Brush.linearGradient]'s defaults, which run top-left to
 * bottom-right — matching the UI-SPEC's 135-degree diagonal contract.
 *
 * Any text/icon drawn over this gradient MUST resolve its foreground via
 * [contrastingForeground] in this same file, never a hardcoded black/white choice.
 */
fun accentGradient(accentColor: Color, colorScheme: ColorScheme): Brush =
    Brush.linearGradient(accentGradientStops(accentColor, colorScheme))

/**
 * Flat-[Color] companion to [accentGradient] (Phase 123 DS-01, D-02) — a card's solid
 * `CardDefaults.containerColor` needs a [Color], and a [Brush] cannot fill one. Returns
 * [accentColor] composited over `colorScheme.surface` at [alpha] via [lerp].
 *
 * The default [alpha] is theme-aware: a dark surface needs a stronger tint to read, so it
 * defaults to a stronger value when `colorScheme.surface` is dark (same luminance-threshold
 * determination as [accentGradientStops]) than in light theme.
 *
 * **Light default (0.13f) — canvas-derived (Phase 129 DS-02, D-03).** Each of the four v2.1
 * card-faces artboards (`.planning/design/v2.1-card-faces/{Text,List,Album,Voice}Face.dc.html`)
 * renders its type-chip box as its accent tinted over the card's `#FFFFFF` surface, i.e. exactly
 * `lerp(white, accent, alpha)`. Solving `alpha` per RGB channel and averaging per face gives:
 * TextFace (`#4A6267` over `#E7ECEC`) ≈ 0.126, ListFace (`#006875` over `#DCEBED`) ≈ 0.133,
 * AlbumFace (`#8E4585` over `#F0E7EF`) ≈ 0.131, VoiceFace (`#B4690E` over `#F5E9DA`) ≈ 0.145. The
 * four faces agree to within ~0.02, and their mean (≈0.134) rounds to **0.13**, replacing the
 * prior 0.08f placeholder.
 *
 * **Dark default (0.26f) — extrapolated, not canvas-derived.** The v2.1 card-faces canvas
 * contains only light-theme artboards, so there is no dark-theme specimen to solve against. To
 * preserve the documented "a dark surface needs a stronger tint to read" relationship, the dark
 * default is scaled by the same factor the light default moved (0.13f / 0.08f ≈ 1.625×), applied
 * to the prior 0.16f dark placeholder (0.16f × 1.625 ≈ 0.26f) and rounded to two decimals. This
 * keeps the dark value strictly greater than the light value, but it is an extrapolation, not a
 * measurement — ⚠ ASSUMED, pending a future dark-theme card-faces canvas to cross-check against.
 *
 * [alpha] is coerced into `0f..1f` so an out-of-range input is clamped, never thrown.
 *
 * Any text/icon drawn over this result MUST resolve its foreground via [contrastingForeground]
 * in this same file, never a hardcoded black/white choice.
 */
fun accentTint(
    accentColor: Color,
    colorScheme: ColorScheme,
    alpha: Float = if (colorScheme.surface.luminance() < 0.5f) 0.26f else 0.13f
): Color = lerp(colorScheme.surface, accentColor, alpha.coerceIn(0f, 1f))
