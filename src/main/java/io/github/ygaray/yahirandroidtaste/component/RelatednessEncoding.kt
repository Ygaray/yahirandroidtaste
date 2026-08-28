package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The four discrete relatedness tiers a normalized Jaccard score buckets into (VISUAL-03, D-01).
 * Single source of truth reused unmodified by both Phase 91 ([AppChip]) and Phase 94's mindmap
 * (D-04).
 */
enum class RelatednessTier { MINIMAL, WEAK, RELATED, STRONG }

/**
 * The shared visual encoding for a relatedness tier — fill color, content color, border stroke,
 * and label font weight (VISUAL-01/02).
 */
data class RelatednessVisual(
    val containerColor: Color,
    val contentColor: Color,
    val borderStroke: BorderStroke,
    val fontWeight: FontWeight
)

/**
 * Buckets a normalized Jaccard relatedness score into one of four [RelatednessTier] values —
 * single source of truth for both Phase 91 ([AppChip]) and Phase 94's mindmap (D-04). The input
 * is clamped to `[0f, 1f]` first, so an out-of-range value is never thrown, only coerced.
 * `Float.NaN` is explicitly mapped to [RelatednessTier.MINIMAL] (the "no signal" tier) — IEEE-754
 * NaN comparisons are always `false`, so `NaN.coerceIn(...)` alone would NOT clamp it.
 *
 * Cut points (0.12 / 0.35 / 0.65) are pinned verbatim by `91-UI-SPEC.md §1` — deliberately
 * non-uniform, weighted for this app's real on-device jaccard distribution (D-01).
 */
fun relatednessTier(jaccard: Float): RelatednessTier {
    val j = if (jaccard.isNaN()) 0f else jaccard.coerceIn(0f, 1f)
    return when {
        j < 0.12f -> RelatednessTier.MINIMAL
        j < 0.35f -> RelatednessTier.WEAK
        j < 0.65f -> RelatednessTier.RELATED
        else -> RelatednessTier.STRONG
    }
}

/**
 * Maps a normalized Jaccard relatedness score to the shared visual encoding (VISUAL-01/02/03,
 * `91-UI-SPEC.md §2/§4/§5`). `colorScheme` is read live by the caller (never imported from
 * `theme/Color.kt` directly) so the result is correct under both static and Material You dynamic
 * color. Binds only to `jaccard: Float` — never `sharedCount` — so two rows with equal jaccard
 * but unequal sharedCount always produce identical output (VISUAL-03).
 *
 * Never returns the `secondaryContainer`/`onSecondaryContainer` pair at any tier — that pairing
 * stays reserved exclusively for a chip's `isSelected` state (D-02 non-negotiable).
 */
fun relatednessVisual(jaccard: Float, colorScheme: ColorScheme): RelatednessVisual {
    return when (relatednessTier(jaccard)) {
        RelatednessTier.MINIMAL -> RelatednessVisual(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurfaceVariant,
            borderStroke = BorderStroke(1.dp, colorScheme.outline),
            fontWeight = FontWeight.Normal
        )
        RelatednessTier.WEAK -> RelatednessVisual(
            containerColor = colorScheme.surfaceVariant,
            contentColor = colorScheme.onSurfaceVariant,
            borderStroke = BorderStroke(1.5.dp, colorScheme.outline),
            fontWeight = FontWeight.Normal
        )
        RelatednessTier.RELATED -> RelatednessVisual(
            containerColor = colorScheme.primaryContainer,
            contentColor = colorScheme.onPrimaryContainer,
            borderStroke = BorderStroke(2.dp, colorScheme.outline),
            fontWeight = FontWeight.SemiBold
        )
        RelatednessTier.STRONG -> RelatednessVisual(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary,
            borderStroke = BorderStroke(2.5.dp, colorScheme.outline),
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * The four discrete relatedness-heat tiers a normalized Jaccard score buckets into for mindmap
 * nodes and edges (Phase 123 DS-01, D-03). Wholly independent from [RelatednessTier] above — a
 * separate type deliberately not shared with the chip-facing Jaccard ramp, because the two serve
 * different consumer archetypes (mindmap nodes/edges here vs. [AppChip]'s chip bars there). Two
 * independent siblings is the locked decision; a `mode` parameter threaded through the existing
 * ramp is the named anti-pattern this shape avoids.
 */
enum class HeatTier { COOL, BRISK, MILD, WARM, HOT, BLAZING }

/**
 * The visual encoding for a [HeatTier] — node fill color, edge color, edge stroke width, and node
 * radius (Phase 123 DS-01). Mirrors how [RelatednessVisual] pairs color with a redundant
 * non-color channel (border width / font weight) so the encoding survives color-vision
 * deficiency; here the redundant channels are edge stroke width and node radius, both scaling
 * with tier alongside color.
 */
data class HeatVisual(
    val nodeFillColor: Color,
    val edgeColor: Color,
    val edgeStrokeWidth: Dp,
    val nodeRadius: Dp
)

/**
 * Buckets a normalized Jaccard relatedness score into one of four [HeatTier] values for the
 * mindmap's node/edge heat ramp (Phase 123 DS-01, `123-UI-SPEC.md` § "Primitive Family 4"). A
 * wholly independent copy of [relatednessTier]'s guard shape above — never a shared helper — so a
 * future Heat-only retune can change these cut points without touching the Jaccard ramp.
 *
 * The input is clamped to `[0f, 1f]` first, so an out-of-range value is never thrown, only
 * coerced. `Float.NaN` is explicitly mapped to [HeatTier.COOL] (the "no signal" tier) — IEEE-754
 * NaN comparisons are always `false`, so `NaN.coerceIn(...)` alone would NOT clamp it — the
 * identical guard discipline [relatednessTier] documents above.
 *
 * Cut points (0.12 / 0.35 / 0.65) intentionally match the Jaccard ramp's own cut points for
 * cross-ramp consistency (both ramps bucket the same underlying jaccard distribution), while
 * remaining an independent copy a future Heat-only retune can change on its own.
 *
 * ⚠ ASSUMED — the cut points and per-tier colors ([heatVisual]) are flagged `ASSUMED` in
 * `123-UI-SPEC.md`, pending a design-canvas cross-check before the Phase 123 tag cut (Plan 05).
 */
fun heatTier(jaccard: Float): HeatTier {
    val j = if (jaccard.isNaN()) 0f else jaccard.coerceIn(0f, 1f)
    return when {
        j < 0.08f -> HeatTier.COOL
        j < 0.18f -> HeatTier.BRISK
        j < 0.30f -> HeatTier.MILD
        j < 0.45f -> HeatTier.WARM
        j < 0.65f -> HeatTier.HOT
        else -> HeatTier.BLAZING
    }
}

/**
 * Maps a normalized Jaccard relatedness score to the shared Heat visual encoding for mindmap
 * nodes/edges (Phase 123 DS-01, `123-UI-SPEC.md` § "Primitive Family 4"). Dispatches on
 * [heatTier] so the cut points stay single-sourced, exactly as [relatednessVisual] dispatches on
 * [relatednessTier] above. `colorScheme` is read live by the caller so the result is correct under
 * both static and Material You dynamic color.
 *
 * Per-tier node fill / edge stroke width / node radius are pinned verbatim by
 * `123-UI-SPEC.md`'s Heat table.
 *
 * Dark-theme edge color derivation (Claude's Discretion, flagged for design-canvas cross-check):
 * `123-UI-SPEC.md`'s table pins no dark-theme edge column. Resolved as each tier's LIGHT node-fill
 * hex — one step brighter than its dark node fill, preserving the same "edge is a paler echo of
 * the node" relation the light table expresses. Theme is resolved via
 * `colorScheme.surface.luminance() < 0.5f`, the same W3C relative-luminance discipline
 * [contrastingForeground] uses.
 */
fun heatVisual(jaccard: Float, colorScheme: ColorScheme): HeatVisual {
    val isDark = colorScheme.surface.luminance() < 0.5f
    return when (heatTier(jaccard)) {
        HeatTier.COOL -> HeatVisual(
            nodeFillColor = if (isDark) Color(0xFF3B82F6) else Color(0xFF60A5FA),
            edgeColor = if (isDark) Color(0xFF60A5FA) else Color(0xFFBFDBFE),
            edgeStrokeWidth = 1.dp,
            nodeRadius = 8.dp
        )
        HeatTier.BRISK -> HeatVisual(
            nodeFillColor = if (isDark) Color(0xFF0EA5E9) else Color(0xFF38BDF8),
            edgeColor = if (isDark) Color(0xFF38BDF8) else Color(0xFFBAE6FD),
            edgeStrokeWidth = 1.3.dp,
            nodeRadius = 10.dp
        )
        HeatTier.MILD -> HeatVisual(
            nodeFillColor = if (isDark) Color(0xFFF59E0B) else Color(0xFFFBBF24),
            edgeColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFFDE68A),
            edgeStrokeWidth = 1.6.dp,
            nodeRadius = 13.dp
        )
        HeatTier.WARM -> HeatVisual(
            nodeFillColor = if (isDark) Color(0xFFF97316) else Color(0xFFFB923C),
            edgeColor = if (isDark) Color(0xFFFB923C) else Color(0xFFFED7AA),
            edgeStrokeWidth = 1.9.dp,
            nodeRadius = 15.dp
        )
        HeatTier.HOT -> HeatVisual(
            nodeFillColor = if (isDark) Color(0xFFDC2626) else Color(0xFFEF4444),
            edgeColor = if (isDark) Color(0xFFEF4444) else Color(0xFFFCA5A5),
            edgeStrokeWidth = 2.2.dp,
            nodeRadius = 18.dp
        )
        HeatTier.BLAZING -> HeatVisual(
            nodeFillColor = if (isDark) Color(0xFFB91C1C) else Color(0xFFDC2626),
            edgeColor = if (isDark) Color(0xFFDC2626) else Color(0xFFFECACA),
            edgeStrokeWidth = 2.5.dp,
            nodeRadius = 20.dp
        )
    }
}

/**
 * Returns the distinct-hub-node ring treatment (MIND-08) — a fixed 2dp [ColorScheme.primary]
 * border drawn around whatever tier-derived fill color the hub node's own [heatVisual] result
 * would otherwise resolve to. Orthogonal to tier color: the hub keeps its tier fill and simply
 * gains a ring, so it is not a fifth [HeatTier] and no caller has to special-case the tier enum.
 * A standalone function (rather than an `isHub: Boolean` param threaded through [heatVisual]) is
 * Claude's Discretion per `123-UI-SPEC.md` — it keeps [heatVisual] a clean tier-to-visual mapping
 * and avoids a boolean flag on a function every mindmap node calls. Its color is a live
 * [ColorScheme] role, never a hardcoded value.
 */
fun hubNodeVisual(colorScheme: ColorScheme): BorderStroke = BorderStroke(2.dp, colorScheme.primary)
