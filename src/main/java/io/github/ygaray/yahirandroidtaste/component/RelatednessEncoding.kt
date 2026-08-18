package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
