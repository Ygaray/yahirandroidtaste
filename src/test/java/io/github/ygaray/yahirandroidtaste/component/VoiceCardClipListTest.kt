package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import io.github.ygaray.yahirandroidtaste.model.VoiceClipUiModel
import io.github.ygaray.yahirandroidtaste.modifier.SwipeAnchor
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Compose tests for [VoiceCard]'s clip-list API (Phase 129 DS-03 D-02) — the `clips` param, the
 * aggregate clip-count-and-total-duration header pill (task 1), and (task 2) the capped per-clip
 * mini-rows and overflow line.
 *
 * ## Why the active assertions are source-structural / pure-function, not render-based
 * Full `CardBase`-based card composables are **unrenderable under this module's Robolectric
 * harness**: `CardBase`'s unconditional `SwipeableActionRow` runs
 * `LaunchedEffect(state) { snapshotFlow { state.requireOffset() } ... }`, which collects before the
 * sibling `SideEffect` installs the drag anchors, so `AnchoredDraggableState.requireOffset()`
 * throws `IllegalStateException: The offset was read before being initialized` on the very first
 * frame — unconditionally, independent of `openRowState`, `title`, or `clips`, inside a
 * pre-existing file this plan does not touch. This is the identical, already-documented blocker
 * as `TextCardImageIndicatorTest` (Phase 107), `VoiceAlbumEditMenuTest` (Phase 112), and
 * `CardBaseTest`/`129-01-SUMMARY.md` (this same phase, Plan 01) — reconfirmed here by direct
 * probe against every one of this file's six original render-based cases.
 *
 * Following the established repo remedy exactly: every `<behavior>` fact this task needs to prove
 * is instead locked by (a) a directly unit-tested pure helper — [voiceClipPillCopy], extracted
 * specifically so the pill's singular/plural/summed-total copy logic is testable without
 * composing `VoiceCard` at all — and (b) source-structural assertions against the real committed
 * `VoiceCard.kt`. The rendered proof is preserved as `@Ignore`'d cases (with the driving
 * [VoiceCardFixture] intact) for Phase 130's Gate-1 Explorer render check to discharge.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class VoiceCardClipListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun source(file: String = "VoiceCard.kt"): String {
        val sourceRoot = resolveModuleSourceRoot()
        val sourceFile = File(sourceRoot, "component/$file")
        val text = sourceFile.readText()
        assertTrue(
            "Source scan found an empty/missing $file at $sourceFile — the working-directory/" +
                "source-root assumption broke. Failing loudly instead of vacuously passing " +
                "(mirrors ComponentRegistryDriftGuardTest's own vacuous-pass guard).",
            text.isNotBlank()
        )
        return text
    }

    /**
     * Resolves the `yahirandroidtaste` module's source root robustly (mirrors
     * `CardBaseTest.resolveModuleSourceRoot`/`ComponentRegistryDriftGuardTest.resolveModuleSourceRoot`
     * exactly, 129-REVIEW.md WR-01) — do not rely solely on the test process's current working
     * directory, which the previous bare-relative-path `source()` implementation did.
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

    private fun countOccurrences(haystack: String, needle: String): Int =
        haystack.split(needle).size - 1

    // --- Active pure-function assertions: voiceClipPillCopy (the renderable-proof-free path) ---

    @Test
    fun `voiceClipPillCopy renders the singular form for exactly one clip`() {
        // 80_000ms -> "1:20"
        assertEquals("1 clip · 1:20", voiceClipPillCopy(clipCount = 1, totalDurationMs = 80_000L))
    }

    @Test
    fun `voiceClipPillCopy renders the plural form for two clips`() {
        assertEquals("2 clips · 0:30", voiceClipPillCopy(clipCount = 2, totalDurationMs = 30_000L))
    }

    @Test
    fun `voiceClipPillCopy renders the plural form and sums three clip durations`() {
        // 30_000 + 45_000 + 20_000 = 95_000ms -> "1:35"
        assertEquals("3 clips · 1:35", voiceClipPillCopy(clipCount = 3, totalDurationMs = 95_000L))
    }

    @Test
    fun `voiceClipPillCopy stays plural and correct at a fifty-clip count`() {
        assertEquals("50 clips · 8:20", voiceClipPillCopy(clipCount = 50, totalDurationMs = 500_000L))
    }

    @Test
    fun `voiceClipPillCopy total reflects the caller-supplied sum, proving hidden clips still count`() {
        // The pill never re-derives the total itself — it renders whatever totalDurationMs the
        // caller passes. VoiceCard's own call site sums clips.sumOf { it.durationMs } (asserted
        // structurally below) over the FULL list, not a capped/visible subset, so a fourth hidden
        // clip's duration is included by construction.
        val fourClipsTotal = 10_000L + 10_000L + 10_000L + 10_000L
        assertEquals("4 clips · 0:40", voiceClipPillCopy(clipCount = 4, totalDurationMs = fourClipsTotal))
    }

    // --- Active source-structural assertions (the renderable proof) ---

    @Test
    fun `VoiceCard declares a clips param defaulting to an empty list`() {
        val src = source()
        assertEquals(
            "VoiceCard must declare exactly one 'clips: List<VoiceClipUiModel> = emptyList()' param",
            1,
            countOccurrences(src, "clips: List<VoiceClipUiModel> = emptyList()")
        )
    }

    @Test
    fun `headerContent guard renders when the title is visible OR clips is non-empty`() {
        val src = source()
        assertEquals(
            "headerContent's null-guard must be conditioned on clips.isEmpty() alongside " +
                "titleSlotVisible(title) so the slot survives a blank title when clips is present " +
                "(Planner Decision 4)",
            1,
            countOccurrences(src, "if (!titleSlotVisible(title) && clips.isEmpty()) null else {")
        )
    }

    @Test
    fun `the title Text node inside the header is independently conditional on titleSlotVisible`() {
        val src = source()
        assertEquals(
            "The title Text must stay independently gated on titleSlotVisible(title) so a blank " +
                "title renders no title node even when clips forces the header slot open",
            1,
            countOccurrences(src, "if (titleSlotVisible(title)) {")
        )
    }

    @Test
    fun `the clip-count pill is declared once and invoked once, gated on clips being non-empty`() {
        val src = source()
        assertEquals(
            "VoiceClipCountPill must be declared exactly once and invoked exactly once " +
                "(the private fun declaration + its one call site in headerContent)",
            2,
            countOccurrences(src, "VoiceClipCountPill(")
        )
        assertEquals(
            "The pill call site must be gated on clips.isNotEmpty()",
            1,
            countOccurrences(src, "if (clips.isNotEmpty()) {")
        )
    }

    @Test
    fun `the pill's total is driven by summing every clip's own duration, not a capped subset`() {
        val src = source()
        assertEquals(
            "The header pill's totalDurationMs must be clips.sumOf { it.durationMs } over the " +
                "FULL clips list — never a pre-cap/pre-take subset",
            1,
            countOccurrences(src, "clips.sumOf { it.durationMs }")
        )
    }

    @Test
    fun `pin and favorite indicators remain unconditional, independent of the clips param`() {
        val src = source()
        assertEquals(1, countOccurrences(src, "contentDescription = \"Pinned\""))
        assertEquals(1, countOccurrences(src, "contentDescription = \"Favourite\""))
    }

    // --- Active pure-function assertions: voiceClipOverflowCopy ---

    @Test
    fun `voiceClipOverflowCopy renders the singular form when exactly one clip is hidden`() {
        assertEquals("+1 more clip", voiceClipOverflowCopy(hiddenCount = 1))
    }

    @Test
    fun `voiceClipOverflowCopy renders the plural form when more than one clip is hidden`() {
        assertEquals("+48 more clips", voiceClipOverflowCopy(hiddenCount = 48))
    }

    // --- Active direct-render assertions on VoiceClipRow / VoiceClipRowsSection ---
    //
    // Unlike the full VoiceCard, these two composables do NOT go through CardBase's
    // SwipeableActionRow — they were deliberately made `internal` (not `private`) specifically so
    // this real-bytes waveform-renderability proof (129-REVIEWS.md cycle-1 MEDIUM) could compose
    // and settle under this harness, not merely compile. Verified by direct probe: composing
    // VoiceClipRowsSection alone (no CardBase in the tree) does not throw.

    private fun rowNodes() = composeTestRule.onAllNodesWithTag("voice_clip_row")

    @Test
    fun `one clip renders exactly one row and no overflow line`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme {
                VoiceClipRowsSection(
                    clips = listOf(VoiceClipUiModel(id = "c1", sortOrder = 0, durationMs = 80_000L))
                )
            }
        }
        composeTestRule.waitForIdle()

        rowNodes().assertCountEquals(1)
        composeTestRule.onNodeWithText("1", substring = false).assertExists()
        composeTestRule.onNodeWithText("1:20").assertExists()
        composeTestRule.onNode(hasText("more clip", substring = true)).assertDoesNotExist()
    }

    @Test
    fun `two clips render exactly two rows and no overflow line`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme {
                VoiceClipRowsSection(
                    clips = listOf(
                        VoiceClipUiModel(id = "c1", sortOrder = 0, durationMs = 10_000L),
                        VoiceClipUiModel(id = "c2", sortOrder = 1, durationMs = 20_000L)
                    )
                )
            }
        }
        composeTestRule.waitForIdle()

        rowNodes().assertCountEquals(2)
        composeTestRule.onNode(hasText("more clip", substring = true)).assertDoesNotExist()
    }

    @Test
    fun `three clips render exactly three rows and no overflow line`() {
        // D-02: cap raised to 3 (was 2) for cross-face consistency with LIST_PREVIEW_ITEM_LIMIT.
        // At the new cap, 3 clips is exactly at the boundary — all three rows render, no overflow.
        composeTestRule.setContent {
            YahirAndroidTasteTheme {
                VoiceClipRowsSection(
                    clips = listOf(
                        VoiceClipUiModel(id = "c1", sortOrder = 0, durationMs = 10_000L),
                        VoiceClipUiModel(id = "c2", sortOrder = 1, durationMs = 20_000L),
                        VoiceClipUiModel(id = "c3", sortOrder = 2, durationMs = 30_000L)
                    )
                )
            }
        }
        composeTestRule.waitForIdle()

        rowNodes().assertCountEquals(3)
        composeTestRule.onNode(hasText("more clip", substring = true)).assertDoesNotExist()
    }

    @Test
    fun `four clips render exactly three rows plus a singular overflow line`() {
        // D-02 cap boundary case (`[[overflow-affordance-reserves-width]]`): the first clip past
        // the raised cap of 3 must produce the singular "+1 more clip" branch, not the plural.
        composeTestRule.setContent {
            YahirAndroidTasteTheme {
                VoiceClipRowsSection(
                    clips = listOf(
                        VoiceClipUiModel(id = "c1", sortOrder = 0, durationMs = 10_000L),
                        VoiceClipUiModel(id = "c2", sortOrder = 1, durationMs = 20_000L),
                        VoiceClipUiModel(id = "c3", sortOrder = 2, durationMs = 30_000L),
                        VoiceClipUiModel(id = "c4", sortOrder = 3, durationMs = 15_000L)
                    )
                )
            }
        }
        composeTestRule.waitForIdle()

        rowNodes().assertCountEquals(3)
        composeTestRule.onNodeWithText("+1 more clip").assertExists()
    }

    @Test
    fun `five clips render exactly three rows plus a plural overflow line`() {
        // Proves the plural branch at the new cap of 3 (two hidden clips).
        composeTestRule.setContent {
            YahirAndroidTasteTheme {
                VoiceClipRowsSection(
                    clips = listOf(
                        VoiceClipUiModel(id = "c1", sortOrder = 0, durationMs = 10_000L),
                        VoiceClipUiModel(id = "c2", sortOrder = 1, durationMs = 20_000L),
                        VoiceClipUiModel(id = "c3", sortOrder = 2, durationMs = 30_000L),
                        VoiceClipUiModel(id = "c4", sortOrder = 3, durationMs = 15_000L),
                        VoiceClipUiModel(id = "c5", sortOrder = 4, durationMs = 25_000L)
                    )
                )
            }
        }
        composeTestRule.waitForIdle()

        rowNodes().assertCountEquals(3)
        composeTestRule.onNodeWithText("+2 more clips").assertExists()
    }

    @Test
    fun `fifty clips still render exactly three rows plus a plural overflow line for the remainder`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme {
                VoiceClipRowsSection(
                    clips = List(50) { i -> VoiceClipUiModel(id = "c$i", sortOrder = i, durationMs = 5_000L) }
                )
            }
        }
        composeTestRule.waitForIdle()

        rowNodes().assertCountEquals(3)
        composeTestRule.onNodeWithText("+47 more clips").assertExists()
    }

    @Test
    fun `a clip with a null samplesPath still renders its own row with no exception`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme {
                VoiceClipRowsSection(
                    clips = listOf(VoiceClipUiModel(id = "c1", sortOrder = 0, durationMs = 10_000L, samplesPath = null))
                )
            }
        }
        composeTestRule.waitForIdle()

        rowNodes().assertCountEquals(1)
        composeTestRule.onNodeWithText("0:10").assertExists()
    }

    @Test
    fun `a clip whose samplesPath points at a nonexistent file still renders with no exception`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme {
                VoiceClipRowsSection(
                    clips = listOf(
                        VoiceClipUiModel(
                            id = "c1",
                            sortOrder = 0,
                            durationMs = 10_000L,
                            samplesPath = "/tmp/does-not-exist-${System.nanoTime()}.bin"
                        )
                    )
                )
            }
        }
        composeTestRule.waitForIdle()

        rowNodes().assertCountEquals(1)
        composeTestRule.onNodeWithText("0:10").assertExists()
    }

    @Test
    fun `a clip pointing at a real well-formed bin file renders without exception`() {
        // Real generated bytes through the identical production decode path — the closure of
        // 129-REVIEWS.md cycle-1 MEDIUM: the shared WaveformCanvas actually receives non-empty
        // bars for this row (proven directly against readAmplitudeBars in
        // AmplitudeBarsDecodeTest; the paint itself is not semantics-queryable, which is exactly
        // why the decode is asserted separately there).
        val samplesFile = writeAmplitudeSamplesFile(List(120) { i -> (i % 10) / 10f })
        composeTestRule.setContent {
            YahirAndroidTasteTheme {
                VoiceClipRowsSection(
                    clips = listOf(
                        VoiceClipUiModel(
                            id = "c1",
                            sortOrder = 0,
                            durationMs = 10_000L,
                            samplesPath = samplesFile.absolutePath
                        )
                    )
                )
            }
        }
        composeTestRule.waitForIdle()

        rowNodes().assertCountEquals(1)
        composeTestRule.onNodeWithText("0:10").assertExists()
    }

    @Test
    fun `two clips with identical durations and sort orders still render as two distinct rows`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme {
                VoiceClipRowsSection(
                    clips = listOf(
                        VoiceClipUiModel(id = "c1", sortOrder = 0, durationMs = 10_000L),
                        VoiceClipUiModel(id = "c2", sortOrder = 0, durationMs = 10_000L)
                    )
                )
            }
        }
        composeTestRule.waitForIdle()

        rowNodes().assertCountEquals(2)
    }

    @Test
    fun `no node in the clip-row subtree exposes a click or long-click semantics action`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme {
                VoiceClipRowsSection(
                    clips = listOf(
                        VoiceClipUiModel(id = "c1", sortOrder = 0, durationMs = 10_000L),
                        VoiceClipUiModel(id = "c2", sortOrder = 1, durationMs = 20_000L),
                        VoiceClipUiModel(id = "c3", sortOrder = 2, durationMs = 30_000L)
                    )
                )
            }
        }
        composeTestRule.waitForIdle()

        assertNoClickActionsInSubtree(composeTestRule.onRoot().fetchSemanticsNode())
    }

    private fun assertNoClickActionsInSubtree(node: SemanticsNode) {
        assertNull(
            "Node (tags=${node.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.TestTag)}) " +
                "must not expose an OnClick semantics action",
            node.config.getOrNull(SemanticsActions.OnClick)
        )
        assertNull(
            "Node (tags=${node.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.TestTag)}) " +
                "must not expose an OnLongClick semantics action",
            node.config.getOrNull(SemanticsActions.OnLongClick)
        )
        node.children.forEach { assertNoClickActionsInSubtree(it) }
    }

    // --- Active region-scoped source guard (backs up the semantics assertion above) ---

    @Test
    fun `the VoiceClipRow function body contains no gesture-modifier token`() {
        val src = source()
        assertTrue("Source read must be non-empty for the region scan to be meaningful", src.isNotEmpty())

        val startMarker = "internal fun VoiceClipRow("
        val startIndex = src.indexOf(startMarker)
        assertTrue("VoiceClipRow declaration must be found in VoiceCard.kt", startIndex >= 0)

        // Walk braces character-by-character from the function's opening brace to find its exact
        // closing brace — a naive line-based/first-match scan would be ambiguous in a file this
        // size (129-01's precedent: CardBaseTest's brace-depth-aware idiom).
        val bodyStart = src.indexOf('{', startIndex)
        assertTrue(bodyStart >= 0)
        var depth = 0
        var bodyEnd = -1
        for (i in bodyStart until src.length) {
            when (src[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        bodyEnd = i
                        break
                    }
                }
            }
        }
        assertTrue("VoiceClipRow's closing brace must be found", bodyEnd > bodyStart)

        val functionBody = src.substring(bodyStart, bodyEnd + 1)
        for (gestureToken in listOf("clickable(", "combinedClickable(", "pointerInput(", "detectTapGestures")) {
            assertTrue(
                "VoiceClipRow's body must not contain the gesture-modifier token '$gestureToken'",
                !functionBody.contains(gestureToken)
            )
        }
    }

    // --- Active source-structural assertions: the cap is applied before any row composes ---

    @Test
    fun `clips are capped via take before any row is composed, and the total is never re-sorted or filtered`() {
        val src = source()
        assertEquals(
            "VoiceClipRowsSection must apply clips.take(CLIP_ROW_CAP) before composing any row",
            1,
            countOccurrences(src, "clips.take(CLIP_ROW_CAP)")
        )
        assertEquals(
            "CLIP_ROW_CAP must appear in its declaration, the take() call, and the overflow-count arithmetic",
            true,
            countOccurrences(src, "CLIP_ROW_CAP") >= 3
        )
        assertEquals(0, countOccurrences(src, ".sortedBy("))
        assertEquals(0, countOccurrences(src, ".sortedWith("))
        assertEquals(0, countOccurrences(src, ".filter("))
        assertEquals(0, countOccurrences(src, ".distinct"))
    }

    // --- Active source-structural assertions: accent / tactileDepth (FACE-03, Phase 133) ------
    //
    // Full CardBase-based card composables are unrenderable under this module's Robolectric
    // harness (see class KDoc) — these lock every FACE-03 <behavior> bullet against the real,
    // committed VoiceCard.kt, mirroring TextCardTest's exact precedent for the Phase 132 tracer.

    @Test
    fun `VoiceCard declares a nullable defaulted accent param`() {
        val src = source()
        assertEquals(
            "VoiceCard must declare 'accent: Color? = null' exactly once — a nullable, defaulted " +
                "param, so no existing call site breaks and the untagged-card neutral case is " +
                "representable (FACE-03).",
            1,
            countOccurrences(src, "accent: Color? = null")
        )
    }

    @Test
    fun `VoiceCard declares a defaulted-off tactileDepth param`() {
        val src = source()
        assertEquals(
            "VoiceCard must declare 'tactileDepth: Boolean = false' exactly once, defaulted to " +
                "today's behavior so every pre-existing call site renders byte-identically until " +
                "a consumer opts in (FACE-03).",
            1,
            countOccurrences(src, "tactileDepth: Boolean = false")
        )
    }

    @Test
    fun `VoiceCard still contains exactly one CardBase call — no wrapper introduced`() {
        val src = source()
        assertEquals(
            "VoiceCard must contain exactly one 'CardBase(' call. A second card container or a " +
                "wrapper around CardBase would shadow its single combinedClickable, silently " +
                "killing tap-to-open and swipe-to-edit/delete — the shipped SWIPE-02 defect class.",
            1,
            countOccurrences(src, "CardBase(")
        )
    }

    @Test
    fun `both accent and tactileDepth are forwarded verbatim inside the CardBase call region`() {
        val src = source()
        val cardBaseRegionStart = src.indexOf("CardBase(")
        assertTrue("Could not locate 'CardBase(' in VoiceCard.kt", cardBaseRegionStart >= 0)
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
    fun `VoiceCard never applies the not-null assertion operator to accent`() {
        val src = source()
        assertEquals(
            "VoiceCard.kt must never force-unwrap 'accent' — an untagged card must reach " +
                "CardBase/CardTypeChip as a genuine null, never a crash risk.",
            0,
            countOccurrences(src, "accent!!")
        )
    }

    // --- header chip / title restyle (FACE-03) -------------------------------------------------

    @Test
    fun `VoiceCard composes exactly one CardTypeChip`() {
        val src = source()
        assertEquals(
            "VoiceCard must compose exactly one 'CardTypeChip(' — the 32dp accent badge FACE-03 " +
                "leads the header with.",
            1,
            countOccurrences(src, "CardTypeChip(")
        )
    }

    @Test
    fun `the CardTypeChip icon resolves through cardTypeIcon(VOICE), never a hand-picked literal`() {
        val src = source()
        assertEquals(
            "VoiceCard's chip icon must resolve via 'cardTypeIcon(\"VOICE\")' — the app-wide " +
                "single source of truth for card-type glyphs — never a hand-picked Icons.Default.* " +
                "literal at this call site, or Text/List/Voice/Album glyphs would drift apart.",
            1,
            countOccurrences(src, "cardTypeIcon(\"VOICE\")")
        )
    }

    @Test
    fun `the CardTypeChip is composed ahead of the header's title sub-gate, on the combined gate`() {
        val src = source()
        val chipIndex = src.indexOf("CardTypeChip(")
        val titleGateIndex = src.indexOf("if (titleSlotVisible(title)) {")
        assertTrue("Could not locate 'CardTypeChip(' in VoiceCard.kt", chipIndex >= 0)
        assertTrue(
            "Could not locate the header's 'if (titleSlotVisible(title)) {' sub-gate in VoiceCard.kt",
            titleGateIndex >= 0
        )
        assertTrue(
            "The type chip must sit textually ahead of the header's own title sub-gate — proving " +
                "the chip renders whenever the header Row renders (the combined title-OR-clips " +
                "gate), never only when a title exists.",
            chipIndex < titleGateIndex
        )
    }

    @Test
    fun `the title uses TactileType_CardTitle and no longer the general titleMedium tier`() {
        val commentStrippedSrc = SourceContractTestSupport.stripComments(source())
        assertEquals(
            "VoiceCard's title Text must use 'TactileType.CardTitle' exactly once — the Space " +
                "Grotesk card-title tier FACE-03 locks in.",
            1,
            countOccurrences(commentStrippedSrc, "TactileType.CardTitle")
        )
        assertEquals(
            "VoiceCard.kt must no longer reference the Material3 'titleMedium' typography token " +
                "anywhere (comment-stripped scan, so KDoc prose cannot pollute this assertion) — " +
                "the general tier the new TactileType.CardTitle tier replaces for this card face.",
            0,
            countOccurrences(commentStrippedSrc, "titleMedium")
        )
    }

    @Test
    fun `VoiceCard source never calls the accent-tint helper — no new accent-tinted foreground`() {
        // UI-SPEC locked Color decision: Voice introduces ZERO new accent-tinted foreground this
        // phase (RESEARCH Pitfall 4; the exact defect class Phase 132's Gate-1 caught on-device).
        // Comment-stripped so KDoc prose mentioning the helper by name cannot invalidate this.
        val commentStrippedSrc = SourceContractTestSupport.stripComments(source())
        assertEquals(
            "VoiceCard.kt must contain zero calls to the accent-tint helper — the pill, clip " +
                "rows, and overflow line stay in their neutral surfaceVariant/onSurfaceVariant " +
                "roles.",
            0,
            countOccurrences(commentStrippedSrc, "accentTint(")
        )
    }

    @Test
    fun `VoiceClipCountPill still renders in its neutral surfaceVariant onSurfaceVariant roles`() {
        val src = source()
        assertTrue(
            "VoiceClipCountPill's background must still be driven by " +
                "MaterialTheme.colorScheme.surfaceVariant (neutral role preserved) — asserted at " +
                "least twice: once for the pill background and once for the clip-row waveform's " +
                "inactive colour.",
            countOccurrences(src, "MaterialTheme.colorScheme.surfaceVariant") >= 2
        )
        assertTrue(
            "VoiceClipCountPill's text colour must still be driven by " +
                "MaterialTheme.colorScheme.onSurfaceVariant.",
            src.contains("color = MaterialTheme.colorScheme.onSurfaceVariant")
        )
    }

    // --- Shared fixture for the rendered-proof (quarantined) cases below ---

    /**
     * Shared fixture — composes [VoiceCard] inside [YahirAndroidTasteTheme] with the same
     * argument shape `CardsFamilyScreen.kt`'s private `VoiceCardContent` fixture uses, exposing
     * only the params these tests vary. Retained (not deleted) so Phase 130's Gate-1 Explorer
     * render check has a ready reference matching this test's exact intended coverage.
     */
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun VoiceCardFixture(
        title: String,
        clips: List<VoiceClipUiModel> = emptyList(),
        isPinned: Boolean = false,
        isFavorite: Boolean = false,
        durationMs: Long = 65_000L,
        categoryPath: String? = null
    ) {
        val openRowState = remember { mutableStateOf<AnchoredDraggableState<SwipeAnchor>?>(null) }
        YahirAndroidTasteTheme {
            VoiceCard(
                id = "fixture",
                title = title,
                durationMs = durationMs,
                samplesPath = null,
                categoryPath = categoryPath,
                isPinned = isPinned,
                isFavorite = isFavorite,
                onTap = {},
                onDelete = {},
                onTogglePin = {},
                onToggleFavorite = {},
                onRenameOrTagsRequest = {},
                openRowState = openRowState,
                clips = clips
            )
        }
    }

    // --- Rendered proofs (quarantined: harness cannot render CardBase cards; see class KDoc) ---

    @Test
    @Ignore(
        "VoiceCard is unrenderable under this Robolectric harness — CardBase's SwipeableActionRow " +
            "throws IllegalStateException (requireOffset read before layout) on the first frame, in " +
            "a pre-existing file this plan does not touch (same blocker as VoiceAlbumEditMenuTest, " +
            "Phase 112, and CardBaseTest, this phase's Plan 01). Rendered proof discharged at " +
            "Phase 130 Gate-1. [Blocking]"
    )
    fun `rendered - no clips argument keeps the existing overview strip and duration unchanged`() {
        composeTestRule.setContent {
            VoiceCardFixture(title = "Fixture Title", categoryPath = "Cat/Path")
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("1:05").assertExists()
        composeTestRule.onNodeWithText("Cat/Path").assertExists()
        composeTestRule.onNode(hasText("clip", substring = true, ignoreCase = true)).assertDoesNotExist()
    }

    @Test
    @Ignore(
        "VoiceCard is unrenderable under this Robolectric harness — see the no-clips case above. " +
            "Rendered proof discharged at Phase 130 Gate-1. [Blocking]"
    )
    fun `rendered - one clip renders the singular pill copy`() {
        composeTestRule.setContent {
            VoiceCardFixture(
                title = "Fixture Title",
                clips = listOf(VoiceClipUiModel(id = "c1", sortOrder = 0, durationMs = 80_000L))
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("1 clip · 1:20").assertExists()
    }

    @Test
    @Ignore(
        "VoiceCard is unrenderable under this Robolectric harness — see the no-clips case above. " +
            "Rendered proof discharged at Phase 130 Gate-1. [Blocking]"
    )
    fun `rendered - three clips render the plural pill copy with the summed duration`() {
        composeTestRule.setContent {
            VoiceCardFixture(
                title = "Fixture Title",
                clips = listOf(
                    VoiceClipUiModel(id = "c1", sortOrder = 0, durationMs = 30_000L),
                    VoiceClipUiModel(id = "c2", sortOrder = 1, durationMs = 45_000L),
                    VoiceClipUiModel(id = "c3", sortOrder = 2, durationMs = 20_000L)
                )
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("3 clips · 1:35").assertExists()
    }

    @Test
    @Ignore(
        "VoiceCard is unrenderable under this Robolectric harness — see the no-clips case above. " +
            "Rendered proof discharged at Phase 130 Gate-1. [Blocking]"
    )
    fun `rendered - pill total sums every clip's duration including a fourth hidden clip`() {
        composeTestRule.setContent {
            VoiceCardFixture(
                title = "Fixture Title",
                clips = List(4) { i -> VoiceClipUiModel(id = "c$i", sortOrder = i, durationMs = 10_000L) }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("4 clips · 0:40").assertExists()
    }

    @Test
    @Ignore(
        "VoiceCard is unrenderable under this Robolectric harness — see the no-clips case above. " +
            "Rendered proof discharged at Phase 130 Gate-1. [Blocking]"
    )
    fun `rendered - blank title with non-empty clips still renders the pill and no title text node`() {
        composeTestRule.setContent {
            VoiceCardFixture(
                title = "   ",
                clips = listOf(
                    VoiceClipUiModel(id = "c1", sortOrder = 0, durationMs = 10_000L),
                    VoiceClipUiModel(id = "c2", sortOrder = 1, durationMs = 20_000L)
                )
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("2 clips · 0:30").assertExists()
        composeTestRule.onNodeWithTag("voice_card_title").assertDoesNotExist()
    }

    @Test
    @Ignore(
        "VoiceCard is unrenderable under this Robolectric harness — see the no-clips case above. " +
            "Rendered proof discharged at Phase 130 Gate-1. [Blocking]"
    )
    fun `rendered - non-blank title with clips renders title, pill, and pin plus favorite`() {
        composeTestRule.setContent {
            VoiceCardFixture(
                title = "Fixture Title",
                isPinned = true,
                isFavorite = true,
                clips = listOf(VoiceClipUiModel(id = "c1", sortOrder = 0, durationMs = 10_000L))
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("voice_card_title").assertExists()
        composeTestRule.onNodeWithText("1 clip · 0:10").assertExists()
        composeTestRule.onNodeWithContentDescription("Pinned").assertExists()
        composeTestRule.onNodeWithContentDescription("Favourite").assertExists()
    }
}
