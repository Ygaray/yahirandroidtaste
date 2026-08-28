package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Proves FACE-01's hub half (Phase 132, Task 1 tracer): [TextCard] threads `accent`/`tactileDepth`
 * verbatim into its single [CardBase] call and leads its header with a [CardTypeChip] ahead of a
 * [io.github.ygaray.yahirandroidtaste.theme.TactileType.CardTitle]-styled title.
 *
 * ## Why the active assertions are source-structural, not render-based
 * Full CardBase-based card composables are **unrenderable under this module's Robolectric
 * harness**: [CardBase]'s unconditional `SwipeableActionRow` throws
 * `IllegalStateException: The offset was read before being initialized` on the very first frame —
 * unconditionally, independent of `accent`/`tactileDepth`/`openRowState`, inside a pre-existing
 * file ([CardBase]'s own `SwipeableActionRow.kt`) this plan does not touch. This is the identical,
 * already-documented blocker confirmed independently three times before: [CardBaseTest]
 * (Phase 129), [TextCardImageIndicatorTest] (Phase 107), and [VoiceAlbumEditMenuTest] (Phase 112).
 * Every `<behavior>` bullet is therefore locked by an ACTIVE source-structural assertion instead
 * (parsing the real, committed `TextCard.kt`), following [CardBaseTest]'s exact precedent: active
 * source guards run green in the suite as permanent regression guards; the rendered proof of the
 * new chrome is discharged visually at Gate-1 on the SM-S908U (132-01-PLAN.md `<verify>`), not
 * here.
 *
 * The source-scan idiom (vacuous-pass guard, `resolveModuleSourceRoot` upward walk,
 * `countOccurrences`) copies [CardBaseTest] exactly.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TextCardTest {

    // --- accent / tactileDepth signature and forwarding ------------------------------------

    @Test
    fun `TextCard declares a nullable defaulted accent param`() {
        val src = readTextCardSource()
        assertEquals(
            "TextCard must declare 'accent: Color? = null' exactly once — a nullable, defaulted " +
                "param, so no existing call site breaks and the untagged-card neutral case is " +
                "representable (FACE-01).",
            1,
            countOccurrences(src, "accent: Color? = null")
        )
    }

    @Test
    fun `TextCard declares a defaulted-off tactileDepth param`() {
        val src = readTextCardSource()
        assertEquals(
            "TextCard must declare 'tactileDepth: Boolean = false' exactly once, defaulted to " +
                "today's behavior so every pre-existing call site renders byte-identically " +
                "until a consumer opts in (FACE-01).",
            1,
            countOccurrences(src, "tactileDepth: Boolean = false")
        )
    }

    @Test
    fun `TextCard still contains exactly one CardBase call — no wrapper introduced`() {
        val src = readTextCardSource()
        assertEquals(
            "TextCard must contain exactly one 'CardBase(' call. A second card container or a " +
                "wrapper around CardBase would shadow its single combinedClickable, silently " +
                "killing tap-to-open and swipe-to-edit/delete — the shipped SWIPE-02 defect " +
                "class D-03 forbids.",
            1,
            countOccurrences(src, "CardBase(")
        )
    }

    @Test
    fun `both accent and tactileDepth are forwarded verbatim inside the CardBase call region`() {
        val src = readTextCardSource()
        val cardBaseRegionStart = src.indexOf("CardBase(")
        assertTrue("Could not locate 'CardBase(' in TextCard.kt", cardBaseRegionStart >= 0)
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

    // --- header chip / title restyle ---------------------------------------------------------

    @Test
    fun `TextCard composes exactly one CardTypeChip`() {
        val src = readTextCardSource()
        assertEquals(
            "TextCard must compose exactly one 'CardTypeChip(' — the 32dp accent badge FACE-01 " +
                "leads the header with.",
            1,
            countOccurrences(src, "CardTypeChip(")
        )
    }

    @Test
    fun `the CardTypeChip icon resolves through cardTypeIcon(TEXT), never a hand-picked literal`() {
        val src = readTextCardSource()
        assertEquals(
            "TextCard's chip icon must resolve via 'cardTypeIcon(\"TEXT\")' — the app-wide single " +
                "source of truth for card-type glyphs — never a hand-picked Icons.Default.* " +
                "literal at this call site, or Text/List/Voice/Album glyphs would drift apart.",
            1,
            countOccurrences(src, "cardTypeIcon(\"TEXT\")")
        )
    }

    @Test
    fun `the CardTypeChip is composed ahead of the title text in the header region`() {
        val src = readTextCardSource()
        val chipIndex = src.indexOf("CardTypeChip(")
        val titleStyleIndex = src.indexOf("TactileType.CardTitle")
        assertTrue("Could not locate 'CardTypeChip(' in TextCard.kt", chipIndex >= 0)
        assertTrue("Could not locate 'TactileType.CardTitle' in TextCard.kt", titleStyleIndex >= 0)
        assertTrue(
            "The type chip must sit textually ahead of the title Text in the headerContent " +
                "lambda — a chip composed after the title would render trailing, not leading " +
                "(FACE-01's typography-forward identity contract).",
            chipIndex < titleStyleIndex
        )
    }

    @Test
    fun `the title uses TactileType_CardTitle and no longer the general titleMedium tier`() {
        val src = readTextCardSource()
        assertEquals(
            "TextCard's title Text must use 'TactileType.CardTitle' exactly once — the Space " +
                "Grotesk card-title tier FACE-01 locks in.",
            1,
            countOccurrences(src, "TactileType.CardTitle")
        )
        assertEquals(
            "TextCard's header region must no longer reference 'MaterialTheme.typography." +
                "titleMedium' — the general tier the new TactileType.CardTitle tier replaces for " +
                "this card face.",
            0,
            countOccurrences(src, "MaterialTheme.typography.titleMedium")
        )
    }

    // --- Dimens.ChipToTitleGap ----------------------------------------------------------------

    @Test
    fun `Dimens exposes ChipToTitleGap at 8dp`() {
        val src = readDimensSource()
        assertTrue(
            "Dimens.kt must declare 'ChipToTitleGap' as the named 8dp spacing token between a " +
                "card face's leading CardTypeChip and its title (132-UI-SPEC.md Spacing Scale).",
            src.contains("ChipToTitleGap")
        )
        assertTrue(
            "Dimens.ChipToTitleGap must be declared at exactly 8.dp per 132-UI-SPEC.md's locked " +
                "Spacing Scale value.",
            src.contains("val ChipToTitleGap: Dp = 8.dp")
        )
    }

    // --- Source-reading helpers -------------------------------------------------------------

    private fun readTextCardSource(): String {
        val sourceRoot = resolveModuleSourceRoot()
        val textCardFile = File(sourceRoot, "component/TextCard.kt")
        val text = textCardFile.readText()
        assertTrue(
            "Source scan found an empty/missing TextCard.kt at $textCardFile — the working-" +
                "directory/source-root assumption broke. Failing loudly instead of vacuously " +
                "passing (mirrors CardBaseTest's own vacuous-pass guard).",
            text.isNotBlank()
        )
        return text
    }

    private fun readDimensSource(): String {
        val sourceRoot = resolveModuleSourceRoot()
        val dimensFile = File(sourceRoot, "theme/Dimens.kt")
        val text = dimensFile.readText()
        assertTrue(
            "Source scan found an empty/missing Dimens.kt at $dimensFile — the working-directory" +
                "/source-root assumption broke. Failing loudly instead of vacuously passing.",
            text.isNotBlank()
        )
        return text
    }

    private fun countOccurrences(haystack: String, needle: String): Int =
        haystack.split(needle).size - 1

    /**
     * Resolves the `yahirandroidtaste` module's source root robustly (mirrors
     * `CardBaseTest.resolveModuleSourceRoot`/`ComponentRegistryDriftGuardTest` exactly) — do not
     * rely solely on the test process's current working directory.
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
