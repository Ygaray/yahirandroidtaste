package io.github.ygaray.yahirandroidtaste.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.ygaray.yahirandroidtaste.R

/**
 * The bundled Space Grotesk variable font ([R.font.space_grotesk_variable]), instanced at four
 * distinct weights for [TactileType] (Phase 123 DS-01/D-04/D-05).
 *
 * The bundled `.ttf`'s `wght` variation axis spans 300-700 but DEFAULTS to 300 (Light) when no
 * variation setting is supplied. Each [Font] entry below therefore passes an EXPLICIT
 * `variationSettings` value pinning its own numeric weight — deliberately redundant
 * with the `Font(...)` overload's own default derivation of variation settings from its
 * `weight`/`style` parameters — so that if variation-settings application ever silently failed,
 * every [TactileType] tier would render visibly Light rather than shipping as a silent
 * nearest-weight substitution (the failure mode D-05 exists to prevent, `123-RESEARCH.md`
 * Pitfall 4). This makes that failure mode structurally impossible to miss rather than merely
 * something to remember to check.
 */
@OptIn(ExperimentalTextApi::class)
val SpaceGroteskFamily: FontFamily = FontFamily(
    Font(
        resId = R.font.space_grotesk_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = R.font.space_grotesk_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.space_grotesk_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resId = R.font.space_grotesk_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
)

/**
 * Space Grotesk display type ramp (Phase 123 DS-01) — an ADDITIVE SIBLING to Material3's
 * [Typography] (D-01). `theme/Type.kt`'s [Typography] val is deliberately left untouched by this
 * object, so no existing consumer's rendering changes: every pre-existing screen keeps resolving
 * through the shared Material3 scale exactly as before.
 *
 * A plain top-level `object` — not a `ColorScheme`-parametrized `data class` with a
 * `staticCompositionLocalOf`, unlike [ExpressiveTokens]'s precedent shape — because font-backed
 * [TextStyle]s need no scheme-derived colors; the `CompositionLocal` ceremony in that precedent
 * exists only to support scheme-derived color fields, which this token surface has none of.
 *
 * The four tiers intentionally declare four DISTINCT font weights (Normal/Medium/SemiBold/Bold),
 * doubling the app-wide two-weight-per-screen typography convention. That is a reviewed,
 * documented exception (`123-UI-SPEC.md` § "Primitive Family 2"): this is a foundational,
 * multi-tier DISPLAY scale for the hub design-system library, not a single-screen composition,
 * and each tier uses weight as a primary differentiator alongside size — mirroring the existing
 * hub precedent in [ExpressiveTokens]/[Typography] where display-tier scales already vary weight
 * per tier.
 *
 * ⚠ Sizes and weights are flagged in `123-UI-SPEC.md` as pending a design-canvas cross-check
 * before the Phase 123 tag cut — not independently confirmed against the canvas as of this
 * commit.
 */
object TactileType {
    /** Hero display tier — e.g. Home's "All Cards" hero. 34sp / Bold / 40sp / -0.25sp. */
    val DisplayLarge: TextStyle = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.25).sp
    )

    /** Section/dashboard heading tier, card-editor accent-header titles. 28sp / SemiBold / 34sp / 0sp. */
    val DisplayMedium: TextStyle = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    )

    /** Mindmap hub-node label, list/voice/album accent-header titles. 22sp / Medium / 28sp / 0.15sp. */
    val DisplaySmall: TextStyle = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.15.sp
    )

    /** Compact accent-header metadata line (e.g. Voice's duration + clip count). 18sp / Normal / 24sp / 0.15sp. */
    val DisplayXSmall: TextStyle = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )
}
