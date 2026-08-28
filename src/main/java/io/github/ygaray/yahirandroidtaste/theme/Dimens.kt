package io.github.ygaray.yahirandroidtaste.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Single source of truth for all shared dimension constants (spacing, sizing, corner radius).
 *
 * Broadened from the former card-scoped `CardDimens` (D-01) to be the module's canonical
 * dimension-token home — captures repeated `.dp` literals surfaced by the Phase 43
 * descriptive sweep (`43-FINDINGS.md`), not just card-specific values.
 *
 * All card composables reference these values — no hardcoded dp literals for padding,
 * spacing, or icon sizes that are shared across card families.
 *
 * ## Base dimensions (apply to all list-style cards)
 * - [HorizontalPadding]: horizontal padding for header/body/footer content rows (16.dp per UI-SPEC)
 * - [TopPadding]: top padding for the first content row
 * - [BottomPadding]: bottom padding for the last content row
 * - [ContentSpacing]: spacing between title and body, body and footer
 *
 * ## Nested groups
 * - [Icons]: icon sizes for the standard icon strip and inline controls
 * - [SwipeReveal]: dimension tokens for the SwipeableActionRow reveal slots
 * - [CornerRadius]: consolidated corner-radius tokens (D-01)
 * - [Elevation]: shadow-elevation scale (Phase 123 DS-01)
 */
object Dimens {

    /** Horizontal padding applied to card content rows. Normalized to 16.dp (UI-SPEC). */
    val HorizontalPadding: Dp = 16.dp

    /** Top padding for the first content row of a card. Normalized to 8.dp. */
    val TopPadding: Dp = 8.dp

    /** Bottom padding for the last content row of a card. Normalized to 4.dp. */
    val BottomPadding: Dp = 4.dp

    /** Spacing between content sections (title→body, body→footer). Normalized to 4.dp. */
    val ContentSpacing: Dp = 4.dp

    /**
     * Hairline spacing — smallest spacing value in active use app-wide (D-01 consolidation;
     * `43-FINDINGS.md` § "Named clusters of repeated non-token values": 25 occurrences / 12 files,
     * no prior named token). Additive only — not yet swapped into any call site this phase.
     */
    val HairlineSpacing: Dp = 2.dp

    /**
     * Minimum accessible touch-target size (D-01 consolidation; matches the app's documented
     * 48dp accessibility touch-target size, UIQ-01 — `43-FINDINGS.md`: 24 occurrences / 16 files).
     * Additive only — not yet swapped into any call site this phase.
     */
    val TouchTarget: Dp = 48.dp

    /**
     * Compact padding value used where the standard [HorizontalPadding]/[TopPadding] rhythm is
     * too generous (D-01 consolidation; `43-FINDINGS.md`: 15 occurrences / 11 files, no prior
     * named token). Additive only — not yet swapped into any call site this phase.
     */
    val CompactPadding: Dp = 12.dp

    /**
     * Hairline border/divider width (D-01 consolidation; `43-FINDINGS.md`: 14 occurrences / 11
     * files, no prior named token). Additive only — not yet swapped into any call site this phase.
     */
    val HairlineBorder: Dp = 1.dp

    /**
     * Width of [CardBase]'s opt-in Tactile depth-card accent spine (Phase 129 DS-02 D-03) — the
     * full-height leading-edge stripe rendered only when `tactileDepth = true`. Canvas-derived:
     * `.planning/design/v2.1-card-faces/VoiceFace.dc.html`'s spine div is `width: 4px`.
     */
    val AccentSpineWidth: Dp = 4.dp

    /** Icon sizes shared across the standard icon strip and inline card controls. */
    object Icons {
        /** MoreVert IconButton container size. Also used for inline rename confirm/cancel buttons. */
        val MenuButton: Dp = 32.dp

        /** MoreVert icon size inside its IconButton. */
        val MenuIcon: Dp = 20.dp

        /** DragHandle icon size. */
        val DragHandle: Dp = 24.dp

        /**
         * [CardTypeChip]'s icon slot size (Phase 129 DS-02 D-01). Canvas shows a 19px icon; this
         * plan's 129-UI-SPEC.md snaps it to 18dp for design-system token conformance (see
         * 129-01-PLAN.md Planner Decision 6).
         */
        val ChipIcon: Dp = 18.dp
    }

    /** Dimension tokens for the SwipeableActionRow reveal slots (D-07). */
    object SwipeReveal {
        /** Fixed width of each swipe action button slot (Delete or Edit). Per D-07 and UI-SPEC. */
        val ButtonWidth: Dp = 72.dp

        /** Icon size inside the swipe action button slot. */
        val IconSize: Dp = 24.dp
    }

    /**
     * Consolidated corner-radius tokens (D-01) — no shape tokens existed in the former
     * `CardDimens` at all (`43-FINDINGS.md` § "RoundedCornerShape construction": 7 ad-hoc sites,
     * 4/7 using 8.dp). Additive only — not yet swapped into any call site this phase.
     */
    object CornerRadius {
        /** Dominant repeated corner-radius value (AppChip, IconPickerGrid, BreadcrumbBar, AlbumCameraScreen). */
        val Small: Dp = 8.dp

        /** Secondary corner-radius value (AlbumTitleConfirmSheet, DrawToolPalette). */
        val Medium: Dp = 12.dp

        /**
         * Large corner-radius value for hero/feature card surfaces (Phase 44 DS-01,
         * [ExpressiveTokens.cardShapeLarge]). Mirrors Material 3's own `Shapes.extraLarge` token,
         * signaling "hero/feature surface, not a list row" per the v1.7 UI-SPEC's Spacing Scale
         * section. Additive only — not yet swapped into any call site this phase.
         */
        val Large: Dp = 28.dp

        /**
         * [CardBase]'s opt-in Tactile depth-card corner radius (Phase 129 DS-02 D-03),
         * canvas-derived: `.planning/design/v2.1-card-faces/VoiceFace.dc.html`'s card container
         * is `border-radius: 16px`. Applied only when `tactileDepth = true`; the default path
         * keeps [CardDefaults.shape].
         */
        val Card: Dp = 16.dp
    }

    /**
     * Shadow-elevation scale (Phase 123 DS-01, Tactile Design System foundation) — six
     * strictly-increasing `Dp` levels for real `shadowElevation` (via `Modifier.shadow(...)`),
     * mirroring Material Design 3's own published elevation scale. Additive sibling to
     * [CornerRadius] — not yet swapped into any call site this phase.
     *
     * ⚠ Values are a well-known Material 3 default, flagged in `123-UI-SPEC.md` as pending a
     * design-canvas cross-check before the Phase 123 tag cut (not independently confirmed
     * against the canvas as of this commit).
     */
    object Elevation {
        /** Flush/flat surfaces — no depth cue (e.g. a full-bleed background). */
        val Level0: Dp = 0.dp

        /** Minimal separation — hairline-adjacent surfaces. */
        val Level1: Dp = 1.dp

        /** Standard list/card row rest state. */
        val Level2: Dp = 3.dp

        /** Raised depth card (the Tactile "white depth card" default). */
        val Level3: Dp = 6.dp

        /** Emphasized/hero surface at rest (e.g. Home's "All Cards" hero). */
        val Level4: Dp = 8.dp

        /** Pressed/active or highest-priority overlay surface. */
        val Level5: Dp = 12.dp
    }
}
