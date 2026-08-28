package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Proves FACE-02's hub half (Phase 132, Task 2): [ListCard] threads `accent`/`tactileDepth`
 * verbatim into its single [CardBase] call, leads its header with a [CardTypeChip], renders a
 * trailing `"N / M"` completion pill and a 4dp completion progress bar — both driven by the one
 * [listCompletionVisible] predicate that also widens the header gate — and sheds its now-duplicate
 * footer completion text.
 *
 * ## Why the composable-shaped assertions are source-structural, not render-based
 * Full CardBase-based card composables are **unrenderable under this module's Robolectric
 * harness**: [CardBase]'s unconditional `SwipeableActionRow` throws
 * `IllegalStateException: The offset was read before being initialized` on the very first frame —
 * unconditionally, independent of `accent`/`tactileDepth`/`openRowState`, inside a pre-existing
 * file this plan does not touch. This is the identical, already-documented blocker confirmed
 * independently three times before: [CardBaseTest] (Phase 129), [TextCardImageIndicatorTest]
 * (Phase 107), and [VoiceAlbumEditMenuTest] (Phase 112) — and again by [TextCardTest] (Phase 132
 * Task 1). Every composable-shaped `<behavior>` bullet is therefore locked by an ACTIVE
 * source-structural assertion instead (parsing the real, committed `ListCard.kt`).
 *
 * Unlike the composables, [listCompletionVisible], [listCompletionPillCopy], and
 * [listCompletionFraction] are pure `internal` functions that compose nothing — they are called
 * directly from the JVM below and get real BEHAVIORAL assertions, not source scans.
 *
 * The source-scan idiom (vacuous-pass guard, `resolveModuleSourceRoot` upward walk,
 * `countOccurrences`) copies [CardBaseTest]/[TextCardTest] exactly.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ListCardTest {

    // =========================================================================================
    // BEHAVIORAL — the three pure helpers, called directly (T-132-01-02, T-132-01-03)
    // =========================================================================================

    @Test
    fun `listCompletionVisible is true for a non-empty CHECKBOX list`() {
        assertTrue(
            "A non-empty CHECKBOX list must show completion UI.",
            listCompletionVisible("CHECKBOX", 5)
        )
    }

    @Test
    fun `listCompletionVisible is false for a zero-item CHECKBOX list`() {
        assertTrue(
            "A zero-item CHECKBOX list must reserve no completion-UI space " +
                "(conditional-render-no-dead-space).",
            !listCompletionVisible("CHECKBOX", 0)
        )
    }

    @Test
    fun `listCompletionVisible is false for BULLETED and ORDERED lists`() {
        assertTrue(
            "A BULLETED list must never show completion UI — markers only, no completion state.",
            !listCompletionVisible("BULLETED", 5)
        )
        assertTrue(
            "An ORDERED list must never show completion UI — markers only, no completion state.",
            !listCompletionVisible("ORDERED", 5)
        )
    }

    @Test
    fun `listCompletionVisible does not widen to a case-insensitive match`() {
        assertTrue(
            "listCompletionVisible must match the stored uppercase 'CHECKBOX' token exactly, " +
                "mirroring the pre-existing footer gate it replaces — a lowercase 'checkbox' " +
                "must NOT trigger completion UI, or the gate silently widened beyond its " +
                "predecessor's contract.",
            !listCompletionVisible("checkbox", 5)
        )
    }

    @Test
    fun `listCompletionPillCopy renders the fixed N over M template at zero, mid, and full completion`() {
        assertEquals(
            "listCompletionPillCopy(0, 8) must render '0 / 8' with no special-case branch.",
            "0 / 8",
            listCompletionPillCopy(0, 8)
        )
        assertEquals(
            "listCompletionPillCopy(3, 8) must render '3 / 8'.",
            "3 / 8",
            listCompletionPillCopy(3, 8)
        )
        assertEquals(
            "listCompletionPillCopy(8, 8) must render '8 / 8' with no special-case branch at " +
                "full completion.",
            "8 / 8",
            listCompletionPillCopy(8, 8)
        )
    }

    @Test
    fun `listCompletionFraction computes zero, half, and full fractions`() {
        assertEquals(
            "listCompletionFraction(0, 8) must be 0f.",
            0f,
            listCompletionFraction(0, 8)
        )
        assertEquals(
            "listCompletionFraction(4, 8) must be 0.5f.",
            0.5f,
            listCompletionFraction(4, 8)
        )
        assertEquals(
            "listCompletionFraction(8, 8) must be 1f.",
            1f,
            listCompletionFraction(8, 8)
        )
    }

    @Test
    fun `listCompletionFraction guards against divide-by-zero on a zero total`() {
        assertEquals(
            "listCompletionFraction(0, 0) must return 0f, never NaN — an unguarded division " +
                "would feed NaN to LinearProgressIndicator (T-132-01-02, a Denial of Service " +
                "threat this pure guard mitigates).",
            0f,
            listCompletionFraction(0, 0)
        )
    }

    // =========================================================================================
    // SOURCE-STRUCTURAL — accent/tactileDepth signature, forwarding, and composition
    // =========================================================================================

    @Test
    fun `ListCard declares a nullable defaulted accent param`() {
        val src = readListCardSource()
        assertEquals(
            "ListCard must declare 'accent: Color? = null' exactly once — a nullable, defaulted " +
                "param, so no existing call site breaks and the untagged-card neutral case is " +
                "representable (FACE-02).",
            1,
            countOccurrences(src, "accent: Color? = null")
        )
    }

    @Test
    fun `ListCard declares a defaulted-off tactileDepth param`() {
        val src = readListCardSource()
        assertEquals(
            "ListCard must declare 'tactileDepth: Boolean = false' exactly once, defaulted to " +
                "today's behavior so every pre-existing call site renders byte-identically " +
                "until a consumer opts in (FACE-02).",
            1,
            countOccurrences(src, "tactileDepth: Boolean = false")
        )
    }

    @Test
    fun `ListCard still contains exactly one CardBase call — no wrapper introduced`() {
        val src = readListCardSource()
        assertEquals(
            "ListCard must contain exactly one 'CardBase(' call. A second card container or a " +
                "wrapper around CardBase would shadow its single combinedClickable, silently " +
                "killing tap-to-open and swipe-to-edit/delete — the shipped SWIPE-02 defect " +
                "class D-03 forbids.",
            1,
            countOccurrences(src, "CardBase(")
        )
    }

    @Test
    fun `both accent and tactileDepth are forwarded verbatim inside the CardBase call region`() {
        val src = readListCardSource()
        val cardBaseRegionStart = src.indexOf("CardBase(")
        assertTrue("Could not locate 'CardBase(' in ListCard.kt", cardBaseRegionStart >= 0)
        val cardBaseRegion = src.substring(cardBaseRegionStart)

        assertTrue(
            "'accent = accent' must appear inside the CardBase(...) call region — a forward " +
                "must be verbatim (no coalesced fallback colour) so CardBase's designed " +
                "null-accent branch receives a genuine null, not a hub-side default.",
            cardBaseRegion.contains("accent = accent")
        )
        assertTrue(
            "'tactileDepth = tactileDepth' must appear inside the CardBase(...) call region — " +
                "never hardcoded to true, so the param genuinely controls the depth chrome.",
            cardBaseRegion.contains("tactileDepth = tactileDepth")
        )
    }

    @Test
    fun `ListCard composes exactly one CardTypeChip resolving its icon through cardTypeIcon(LIST)`() {
        val src = readListCardSource()
        assertEquals(
            "ListCard must compose exactly one 'CardTypeChip(' — the 32dp accent badge FACE-02 " +
                "leads the header with.",
            1,
            countOccurrences(src, "CardTypeChip(")
        )
        assertEquals(
            "ListCard's chip icon must resolve via 'cardTypeIcon(\"LIST\")' — the app-wide " +
                "single source of truth for card-type glyphs — never a hand-picked literal.",
            1,
            countOccurrences(src, "cardTypeIcon(\"LIST\")")
        )
    }

    @Test
    fun `ListCompletionPill and ListCompletionProgressBar are each private and each called exactly once`() {
        val src = readListCardSource()
        assertEquals(
            "ListCompletionPill must be declared 'private' exactly once — it is not a public " +
                "hub component, so ComponentRegistryDriftGuardTest has nothing to register.",
            1,
            countOccurrences(src, "private fun ListCompletionPill")
        )
        assertEquals(
            "ListCompletionProgressBar must be declared 'private' exactly once.",
            1,
            countOccurrences(src, "private fun ListCompletionProgressBar")
        )
        // Each declaration site plus exactly one call site.
        assertEquals(
            "ListCompletionPill must be referenced exactly twice — its declaration plus one " +
                "call site (the header). A second call site would risk the pill and the bar " +
                "disagreeing about what they render.",
            2,
            countOccurrences(src, "ListCompletionPill(")
        )
        assertEquals(
            "ListCompletionProgressBar must be referenced exactly twice — its declaration plus " +
                "one call site (the body).",
            2,
            countOccurrences(src, "ListCompletionProgressBar(")
        )
    }

    @Test
    fun `the pill background resolves through accentTint and neither surface force-unwraps accent`() {
        val src = readListCardSource()
        assertTrue(
            "ListCompletionPill's background must resolve via 'accentTint(' when accent is " +
                "non-null — never a hardcoded colour or a force-unwrap.",
            src.contains("accentTint(accent, colorScheme)")
        )
        assertEquals(
            "ListCard.kt must contain zero 'accent!!' force-unwraps — the null-accent path is " +
                "a designed neutral branch, never an error path (T-132-01-03).",
            0,
            countOccurrences(src, "accent!!")
        )
    }

    @Test
    fun `the pill foreground resolves through contrastingForeground against its own background, not the raw accent`() {
        // Gate-1 gap-closure (SC2/IN-01): a pale seeded accent (0xFFFFF9C4) measured a WCAG
        // contrast ratio of ~1.01:1 when the foreground was 'accent' at full strength drawn on
        // 'accentTint(accent, colorScheme)' — both converge toward the same pale hue. The fix
        // must derive the foreground from contrastingForeground(backgroundColor) so it always
        // resolves to a readable Black/White pairing, regardless of accent lightness.
        val src = readListCardSource()
        val pillRegionStart = src.indexOf("private fun ListCompletionPill")
        assertTrue(
            "Could not locate 'private fun ListCompletionPill' in ListCard.kt",
            pillRegionStart >= 0
        )
        val nextPrivateFun = src.indexOf(
            "private fun ",
            pillRegionStart + "private fun ".length
        )
        val pillRegion = if (nextPrivateFun >= 0) {
            src.substring(pillRegionStart, nextPrivateFun)
        } else {
            src.substring(pillRegionStart)
        }

        assertTrue(
            "ListCompletionPill's foreground must resolve via " +
                "'contrastingForeground(backgroundColor)' when accent is non-null — computing " +
                "contrast against the pill's own actual rendered background, not a hardcoded " +
                "colour and not the raw accent value, so it stays legible against any accent " +
                "lightness (pale or dark).",
            pillRegion.contains("contrastingForeground(backgroundColor)")
        )
        assertTrue(
            "ListCompletionPill must no longer assign 'foregroundColor = accent ?: ...' — that " +
                "was the exact formula Gate-1 measured at ~1.01:1 contrast for a pale accent " +
                "(SC2/IN-01), and reintroducing it would regress the fix.",
            !pillRegion.contains("foregroundColor = accent ?:")
        )
    }

    @Test
    fun `the progress bar track resolves through colorScheme_outline, not surfaceVariant or outlineVariant`() {
        val src = readListCardSource()
        assertEquals(
            "ListCompletionProgressBar's trackColor must be 'MaterialTheme.colorScheme.outline' " +
                "exactly once — the only value MetricBar's own recorded finding shows stays " +
                "visibly distinct from surfaceVariant/surface in BOTH color schemes on this " +
                "fixed palette.",
            1,
            countOccurrences(src, "trackColor = MaterialTheme.colorScheme.outline")
        )
    }

    @Test
    fun `listCompletionVisible is referenced at least four times — one shared predicate, not copies`() {
        val src = readListCardSource()
        assertTrue(
            "listCompletionVisible must be referenced at least 4 times in ListCard.kt: its " +
                "declaration, the widened header gate, the header pill's gate, and the body " +
                "bar's gate — one shared predicate so they can never drift apart.",
            countOccurrences(src, "listCompletionVisible(") >= 4
        )
    }

    @Test
    fun `the item-preview cap literals are each still present exactly once — untouched by this phase`() {
        val src = readListCardSource()
        assertEquals(
            "'items.take(3)' must still appear exactly once — the hub-internal preview cap is " +
                "not this phase's to change (D-01, RESEARCH Pitfall 5).",
            1,
            countOccurrences(src, "items.take(3)")
        )
        assertEquals(
            "'items.take(10)' must still appear exactly once.",
            1,
            countOccurrences(src, "items.take(10)")
        )
    }

    @Test
    fun `the footer region contains no completed-item count expression and no subType parameter`() {
        val src = readListCardSource()
        val footerStart = src.indexOf("private fun ListCardFooterContent")
        assertTrue(
            "Could not locate 'private fun ListCardFooterContent' in ListCard.kt",
            footerStart >= 0
        )
        // Anchor the region from the footer's declaration to the NEXT top-level `private fun`
        // (or end of file) so this scan cannot false-match ListItemPreviewRow's own legitimate
        // completed-item logic elsewhere in the file, which must survive untouched.
        val nextPrivateFun = src.indexOf("private fun ", footerStart + "private fun ".length)
        val footerRegion = if (nextPrivateFun >= 0) {
            src.substring(footerStart, nextPrivateFun)
        } else {
            src.substring(footerStart)
        }

        assertTrue(
            "The ListCardFooterContent region must not declare a 'subType' parameter — its " +
                "only reader (the completion text) was deleted, so keeping the parameter would " +
                "be an unused-parameter finding this project's detekt policy forbids banking.",
            !footerRegion.contains("subType")
        )
        assertTrue(
            "The ListCardFooterContent region must not contain a completed-item count " +
                "expression ('it.isCompleted') — the header pill is now the sole completion " +
                "display (RESEARCH Pitfall 4).",
            !footerRegion.contains("it.isCompleted")
        )
        assertTrue(
            "The ListCardFooterContent region must not render the old '\"N/M done\"' template " +
                "any more.",
            !footerRegion.contains("done\"")
        )
    }

    // --- Source-reading helpers -------------------------------------------------------------

    private fun readListCardSource(): String {
        val sourceRoot = resolveModuleSourceRoot()
        val listCardFile = File(sourceRoot, "component/ListCard.kt")
        val text = listCardFile.readText()
        assertTrue(
            "Source scan found an empty/missing ListCard.kt at $listCardFile — the working-" +
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
     * `CardBaseTest.resolveModuleSourceRoot`/`TextCardTest.resolveModuleSourceRoot` exactly) —
     * do not rely solely on the test process's current working directory.
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
