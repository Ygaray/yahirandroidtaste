package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Proves FACE-04's hub half (Phase 133, Task 2): [AlbumCard] threads `accent`/`tactileDepth`
 * verbatim into its single [CardBase] call and leads its header with a [CardTypeChip] ahead of a
 * [io.github.ygaray.yahirandroidtaste.theme.TactileType.CardTitle]-styled title — the identical
 * Phase-132/133-Task-1 pattern applied to the Album face — plus [AdaptiveMediaPreview]'s new
 * per-cell mosaic framing (D-03: the tier dispatch and overflow arithmetic themselves stay
 * byte-identical).
 *
 * ## Why the active assertions are source-structural, not render-based
 * Full CardBase-based card composables are **unrenderable under this module's Robolectric
 * harness**: [CardBase]'s unconditional `SwipeableActionRow` throws
 * `IllegalStateException: The offset was read before being initialized` on the very first frame —
 * unconditionally, independent of `accent`/`tactileDepth`/`openRowState`, inside a pre-existing
 * file this plan does not touch. This is the identical, already-documented blocker confirmed
 * independently multiple times before: [CardBaseTest] (Phase 129), [TextCardImageIndicatorTest]
 * (Phase 107), [VoiceAlbumEditMenuTest] (Phase 112), and [TextCardTest] (Phase 132, this exact
 * change shape). Every `<behavior>` bullet is therefore locked by an ACTIVE source-structural
 * assertion instead (parsing the real, committed `AlbumCard.kt` and `AdaptiveMediaPreview.kt`),
 * following [TextCardTest]'s exact precedent: active source guards run green in the suite as
 * permanent regression guards; the rendered proof of the new chrome is discharged visually at
 * Gate-1 on the SM-S908U (133-01-PLAN.md `<verify>`), not here.
 *
 * The source-scan idiom (vacuous-pass guard, `resolveModuleSourceRoot` upward walk,
 * `countOccurrences`) copies [TextCardTest] exactly, generalised to read two source files.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AlbumCardTest {

    // --- accent / tactileDepth signature and forwarding (AlbumCard.kt) ----------------------

    @Test
    fun `AlbumCard declares a nullable defaulted accent param`() {
        val src = readAlbumCardSource()
        assertEquals(
            "AlbumCard must declare 'accent: Color? = null' exactly once — a nullable, defaulted " +
                "param, so no existing call site breaks and the untagged-card neutral case is " +
                "representable (FACE-04).",
            1,
            countOccurrences(src, "accent: Color? = null")
        )
    }

    @Test
    fun `AlbumCard declares a defaulted-off tactileDepth param`() {
        val src = readAlbumCardSource()
        assertEquals(
            "AlbumCard must declare 'tactileDepth: Boolean = false' exactly once, defaulted to " +
                "today's behavior so every pre-existing call site renders byte-identically until " +
                "a consumer opts in (FACE-04).",
            1,
            countOccurrences(src, "tactileDepth: Boolean = false")
        )
    }

    @Test
    fun `AlbumCard still contains exactly one CardBase call — no wrapper introduced`() {
        val src = readAlbumCardSource()
        assertEquals(
            "AlbumCard must contain exactly one 'CardBase(' call. A second card container or a " +
                "wrapper around CardBase would shadow its single combinedClickable, silently " +
                "killing tap-to-open and swipe-to-edit/delete — the shipped SWIPE-02 defect class.",
            1,
            countOccurrences(src, "CardBase(")
        )
    }

    @Test
    fun `both accent and tactileDepth are forwarded verbatim inside the CardBase call region`() {
        val src = readAlbumCardSource()
        val cardBaseRegionStart = src.indexOf("CardBase(")
        assertTrue("Could not locate 'CardBase(' in AlbumCard.kt", cardBaseRegionStart >= 0)
        val cardBaseRegion = src.substring(cardBaseRegionStart)

        assertTrue(
            "'accent = accent' must appear inside the CardBase(...) call region — a forward must " +
                "be verbatim (no coalesced fallback colour) so CardBase's designed null-accent " +
                "branch receives a genuine null, not a hub-side default.",
            cardBaseRegion.contains("accent = accent")
        )
        assertTrue(
            "'tactileDepth = tactileDepth' must appear inside the CardBase(...) call region — " +
                "never hardcoded to true, so the param genuinely controls the depth chrome.",
            cardBaseRegion.contains("tactileDepth = tactileDepth")
        )
    }

    @Test
    fun `AlbumCard never applies the not-null assertion operator to accent`() {
        val src = readAlbumCardSource()
        assertEquals(
            "AlbumCard.kt must never force-unwrap 'accent' — an untagged card must reach " +
                "CardBase/CardTypeChip as a genuine null, never a crash risk.",
            0,
            countOccurrences(src, "accent!!")
        )
    }

    // --- header chip / title restyle (AlbumCard.kt) -------------------------------------------

    @Test
    fun `AlbumCard composes exactly one CardTypeChip`() {
        val src = readAlbumCardSource()
        assertEquals(
            "AlbumCard must compose exactly one 'CardTypeChip(' — the 32dp accent badge FACE-04 " +
                "leads the header with.",
            1,
            countOccurrences(src, "CardTypeChip(")
        )
    }

    @Test
    fun `the CardTypeChip icon resolves through cardTypeIcon(ALBUM), never a hand-picked literal`() {
        val src = readAlbumCardSource()
        assertEquals(
            "AlbumCard's chip icon must resolve via 'cardTypeIcon(\"ALBUM\")' — the app-wide " +
                "single source of truth for card-type glyphs — never a hand-picked Icons.Default.* " +
                "literal at this call site, or Text/List/Voice/Album glyphs would drift apart.",
            1,
            countOccurrences(src, "cardTypeIcon(\"ALBUM\")")
        )
    }

    @Test
    fun `the CardTypeChip is composed ahead of the title text in the header region`() {
        val src = readAlbumCardSource()
        val chipIndex = src.indexOf("CardTypeChip(")
        val titleStyleIndex = src.indexOf("TactileType.CardTitle")
        assertTrue("Could not locate 'CardTypeChip(' in AlbumCard.kt", chipIndex >= 0)
        assertTrue("Could not locate 'TactileType.CardTitle' in AlbumCard.kt", titleStyleIndex >= 0)
        assertTrue(
            "The type chip must sit textually ahead of the title Text in the headerContent " +
                "lambda — a chip composed after the title would render trailing, not leading " +
                "(FACE-04's typography-forward identity contract).",
            chipIndex < titleStyleIndex
        )
    }

    @Test
    fun `the title uses TactileType_CardTitle and no longer the general titleMedium tier`() {
        val commentStrippedSrc = SourceContractTestSupport.stripComments(readAlbumCardSource())
        assertEquals(
            "AlbumCard's title Text must use 'TactileType.CardTitle' exactly once — the Space " +
                "Grotesk card-title tier FACE-04 locks in.",
            1,
            countOccurrences(commentStrippedSrc, "TactileType.CardTitle")
        )
        assertEquals(
            "AlbumCard.kt must no longer reference the Material3 'titleMedium' typography token " +
                "anywhere (comment-stripped scan, so KDoc prose cannot pollute this assertion) — " +
                "the general tier the new TactileType.CardTitle tier replaces for this card face.",
            0,
            countOccurrences(commentStrippedSrc, "titleMedium")
        )
    }

    // --- mosaic block height + inset (AlbumCard.kt call site) ---------------------------------

    @Test
    fun `AlbumCard declares MOSAIC_BLOCK_HEIGHT at 220dp and applies it at the call site`() {
        val src = readAlbumCardSource()
        assertTrue(
            "AlbumCard.kt must reference 'MOSAIC_BLOCK_HEIGHT' at least twice — once for its " +
                "declaration and once at the AdaptiveMediaPreview( call site.",
            countOccurrences(src, "MOSAIC_BLOCK_HEIGHT") >= 2
        )
        assertEquals(
            "AlbumCard.kt must declare the mosaic block height as exactly 220.dp — the " +
                "pre-restyle 196dp visible photo area plus two 12dp CompactPadding insets.",
            1,
            countOccurrences(src, "220.dp")
        )
    }

    @Test
    fun `the AdaptiveMediaPreview call site applies height before padding — 220dp block, 196dp content`() {
        val src = readAlbumCardSource()
        val callSiteStart = src.indexOf("AdaptiveMediaPreview(")
        assertTrue("Could not locate 'AdaptiveMediaPreview(' in AlbumCard.kt", callSiteStart >= 0)
        // Bound the region to the call's own modifier chain — up to the next top-level ')' that
        // closes this call — via brace/paren depth tracking so a later unrelated call site can
        // never leak into this region.
        var depth = 0
        var callSiteEnd = -1
        for (i in callSiteStart until src.length) {
            when (src[i]) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) {
                        callSiteEnd = i
                        break
                    }
                }
            }
        }
        assertTrue("Could not find the closing paren of the AdaptiveMediaPreview( call", callSiteEnd > callSiteStart)
        val callSiteRegion = src.substring(callSiteStart, callSiteEnd + 1)

        val heightIndex = callSiteRegion.indexOf(".height(MOSAIC_BLOCK_HEIGHT)")
        val paddingIndex = callSiteRegion.indexOf(".padding(Dimens.CompactPadding)")
        assertTrue(
            "Could not find '.height(MOSAIC_BLOCK_HEIGHT)' in the AdaptiveMediaPreview( call-site region",
            heightIndex >= 0
        )
        assertTrue(
            "Could not find '.padding(Dimens.CompactPadding)' in the AdaptiveMediaPreview( call-site region",
            paddingIndex >= 0
        )
        assertTrue(
            "'.height(MOSAIC_BLOCK_HEIGHT)' must appear textually BEFORE '.padding(Dimens." +
                "CompactPadding)' in the modifier chain — height before padding means the node " +
                "is 220dp tall and its content measures 196dp; the reverse order would silently " +
                "produce a 244dp block (PD-2/D-03).",
            heightIndex < paddingIndex
        )
    }

    @Test
    fun `the AdaptiveMediaPreview call site leaves cells, totalImageCount, onCellTap, onOverflowTap untouched`() {
        val src = readAlbumCardSource()
        assertEquals(1, countOccurrences(src, "cells = thumbnailItems"))
        assertEquals(1, countOccurrences(src, "totalImageCount = totalImageCount"))
        assertEquals(1, countOccurrences(src, "onCellTap = { onTap() }"))
        assertEquals(1, countOccurrences(src, "onOverflowTap = onTap"))
    }

    // --- mosaic per-cell framing (AdaptiveMediaPreview.kt) -------------------------------------

    @Test
    fun `mosaicCellFraming is declared once and applied at exactly three call sites`() {
        val src = readAdaptiveMediaPreviewSource()
        assertTrue(
            "'mosaicCellFraming' must appear at least 4 times in AdaptiveMediaPreview.kt — the " +
                "declaration plus the three application sites (inside ThumbnailCell, on the " +
                "tier-0 placeholder, and on the '+N' overflow cell).",
            countOccurrences(src, "mosaicCellFraming") >= 4
        )
        assertEquals(
            "The private 'fun Modifier.mosaicCellFraming()' extension must be declared exactly once.",
            1,
            countOccurrences(src, "private fun Modifier.mosaicCellFraming()")
        )
    }

    @Test
    fun `mosaicCellFraming is applied inside ThumbnailCell's own Box, not hand-enumerated per tier`() {
        val src = readAdaptiveMediaPreviewSource()
        assertEquals(
            "ThumbnailCell's own Box must apply 'modifier.mosaicCellFraming()' so all six " +
                "image-cell call sites across every tier inherit the framing structurally (PD-2) " +
                "— never hand-enumerated at each ThumbnailCell( call site.",
            1,
            countOccurrences(src, "Box(modifier = modifier.mosaicCellFraming())")
        )
    }

    @Test
    fun `spacedBy ContentSpacing appears exactly 5 times — 2-cell, 3-cell, 4+ Column, and its two Rows`() {
        val src = readAdaptiveMediaPreviewSource()
        assertEquals(
            "'Arrangement.spacedBy(Dimens.ContentSpacing)' must appear exactly 5 times: the " +
                "2-cell Row, the 3-cell Row, the 4+ tier's Column, and the 4+ tier's two Rows.",
            5,
            countOccurrences(src, "Arrangement.spacedBy(Dimens.ContentSpacing)")
        )
    }

    @Test
    fun `the tier-dispatch expression and the overflow-count arithmetic each appear exactly once`() {
        val src = readAdaptiveMediaPreviewSource()
        assertEquals(
            "The tier-dispatch 'when (cells.size)' expression must appear exactly once — D-03 " +
                "forbids restructuring the dispatch.",
            1,
            countOccurrences(src, "when (cells.size)")
        )
        assertEquals(
            "The overflow-count arithmetic '(totalImageCount - 3).coerceAtLeast(1)' must appear " +
                "exactly once, byte-identical to the pre-plan formula (D-03).",
            1,
            countOccurrences(src, "(totalImageCount - 3).coerceAtLeast(1)")
        )
    }

    @Test
    fun `the count-badge gate is untouched — totalImageCount greater than one, exactly once`() {
        val src = readAdaptiveMediaPreviewSource()
        assertEquals(
            "The top-right count badge's gate 'totalImageCount > 1' must appear exactly once, " +
                "untouched by this plan's framing-only edit.",
            1,
            countOccurrences(src, "totalImageCount > 1")
        )
    }

    // --- Source-reading helpers -------------------------------------------------------------

    private fun readAlbumCardSource(): String = readComponentSource("AlbumCard.kt")

    private fun readAdaptiveMediaPreviewSource(): String = readComponentSource("AdaptiveMediaPreview.kt")

    private fun readComponentSource(fileName: String): String {
        val sourceRoot = resolveModuleSourceRoot()
        val sourceFile = File(sourceRoot, "component/$fileName")
        val text = sourceFile.readText()
        assertTrue(
            "Source scan found an empty/missing $fileName at $sourceFile — the working-" +
                "directory/source-root assumption broke. Failing loudly instead of vacuously " +
                "passing (mirrors CardBaseTest's own vacuous-pass guard).",
            text.isNotBlank()
        )
        return text
    }

    private fun countOccurrences(haystack: String, needle: String): Int =
        haystack.split(needle).size - 1

    /**
     * Resolves the `yahirandroidtaste` module's source root robustly (mirrors
     * `CardBaseTest.resolveModuleSourceRoot`/`TextCardTest.resolveModuleSourceRoot` exactly) — do
     * not rely solely on the test process's current working directory.
     */
    private fun resolveModuleSourceRoot(): File {
        val relativeSourceRoot = "src/main/java/io/github/ygaray/yahirandroidtaste"

        var dir: File? = File(".").absoluteFile
        var depth = 0
        while (dir != null && depth < 8) {
            if (dir.name == "yahirandroidtaste") {
                val candidateBuildFile = File(dir, "build.gradle.kts")
                val candidateSourceRoot = File(dir, relativeSourceRoot)
                if (candidateBuildFile.isFile && candidateSourceRoot.isDirectory) {
                    return candidateSourceRoot
                }
            }
            dir = dir.parentFile
            depth++
        }

        val fallback = File(relativeSourceRoot)
        check(fallback.isDirectory) {
            "Could not resolve the yahirandroidtaste source root by walking up from the process " +
                "CWD (${File(".").absoluteFile}) looking for a yahirandroidtaste module " +
                "directory, nor via the Gradle-default relative path " +
                "($relativeSourceRoot, resolved absolute: ${fallback.absoluteFile})."
        }
        return fallback
    }
}
