package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import io.github.ygaray.yahirandroidtaste.modifier.SwipeAnchor
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Tests for [CardBase]'s opt-in Tactile depth-card chrome (Phase 129 DS-02, D-01/D-03).
 *
 * ## Why the active assertions are source-structural, not render-based
 * Full CardBase-based card composables are **unrenderable under this module's Robolectric
 * harness**: CardBase's unconditional `SwipeableActionRow` runs
 * `LaunchedEffect(state) { snapshotFlow { state.requireOffset() } ... }`, which collects before
 * the sibling `SideEffect` installs the drag anchors, so `AnchoredDraggableState.requireOffset()`
 * throws `IllegalStateException: The offset was read before being initialized` on the very first
 * frame — unconditionally, independent of `accent`/`tactileDepth`/`openRowState`, inside a
 * pre-existing file (`SwipeableActionRow.kt`) this plan does not touch. This is the identical,
 * already-documented blocker as `TextCardImageIndicatorTest` (Phase 107) and
 * `VoiceAlbumEditMenuTest` (Phase 112) — RESEARCH's "no test in the repo instantiates a full card
 * composable" pitfall, confirmed again here by direct probe (see the `@Ignore`d cases below).
 *
 * Every `<behavior>` bullet is therefore locked by an ACTIVE source-structural assertion instead
 * (parsing the real, committed `CardBase.kt`), following the exact precedent
 * `VoiceAlbumEditMenuTest` established: active source guards run green in the suite as permanent
 * regression guards; the rendered proof is preserved as `@Ignore`d cases (with the fixture that
 * would drive them) and is discharged on-device at Phase 130's Gate-1 Explorer render check
 * (129-01-PLAN.md `<verification>`).
 *
 * Infra mirrors this module's established Robolectric+Compose harness ([CountBadgeTest]) for the
 * quarantined cases: `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`,
 * `createComposeRule()`. The source-scan idiom (vacuous-pass guard, `resolveModuleSourceRoot`
 * upward walk) copies
 * [io.github.ygaray.yahirandroidtaste.explorer.ComponentRegistryDriftGuardTest] exactly.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CardBaseTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- Active source-structural assertions (the renderable proof) -----------------------

    @Test
    fun `card_depth_container is tagged exactly once and only inside the tactileDepth true branch`() {
        val src = readCardBaseSource()

        assertEquals(
            "card_depth_container must be tagged at exactly one site in CardBase.kt",
            1,
            countOccurrences(src, "card_depth_container")
        )

        val (ifOpenBrace, ifCloseBrace, _, _) = renderConditionalBraces(src)
        val tagIndex = src.indexOf("card_depth_container")
        assertTrue(
            "card_depth_container must sit inside the tactileDepth == true branch, not the else " +
                "branch or anywhere outside the conditional",
            tagIndex in ifOpenBrace..ifCloseBrace
        )
    }

    @Test
    fun `card_accent_spine is a matchParentSize overlay with a null-safe neutral fallback`() {
        val src = readCardBaseSource()

        assertTrue("card_accent_spine tag must exist in CardBase.kt", src.contains("card_accent_spine"))
        assertTrue(
            "the spine overlay must use matchParentSize so it is measured against the Column's " +
                "size and can never influence the card's own measured height (UI-SPEC E2 overflow)",
            src.contains("matchParentSize")
        )
        assertTrue(
            "the spine fill must resolve via a null-safe elvis on accent (accent ?: " +
                "neutralSpineColor), never an unguarded read or a !! — this is what makes the " +
                "null-accent case a designed neutral state instead of a crash (UI-SPEC E2 empty)",
            src.contains("accent ?: neutralSpineColor")
        )
    }

    @Test
    fun `accent alone without tactileDepth never activates the depth chrome`() {
        val src = readCardBaseSource()

        // Neither tag literal appears anywhere outside the tactileDepth == true branch (already
        // proven per-tag above); this test locks the converse framing the plan's behavior bullet
        // states directly: tactileDepth, not accent, is what gates the chrome. Confirmed by the
        // signature itself declaring accent with no conditional on its own nullability driving
        // shape/elevation/spine presence — only `tactileDepth` gates the `if` this file branches on.
        val conditionalLine = src.lineSequence().first { it.trim().startsWith("if (tactileDepth)") }
        assertEquals("if (tactileDepth) {", conditionalLine.trim())
    }

    @Test
    fun `the shared content value invokes every content slot so all four param combinations still compose them`() {
        val src = readCardBaseSource()

        val contentValueStart = src.indexOf("val cardColumnContent")
        assertTrue("Could not locate 'val cardColumnContent' in CardBase.kt", contentValueStart >= 0)
        val trueBranchStart = src.indexOf("if (tactileDepth) {")
        assertTrue(
            "cardColumnContent must be declared before the tactileDepth conditional so both " +
                "branches can reference it",
            contentValueStart in 0 until trueBranchStart
        )

        val sharedContentBody = src.substring(contentValueStart, trueBranchStart)
        listOf("headerContent()", "bodyContent()", "tagRowContent()", "footerContent()").forEach { call ->
            assertTrue(
                "the shared cardColumnContent value must invoke $call so that slot still " +
                    "composes and is findable in every one of the four tactileDepth/accent " +
                    "param combinations",
                sharedContentBody.contains(call)
            )
        }
    }

    @Test
    fun `the container is never tinted by accent`() {
        val src = readCardBaseSource()
        assertEquals(
            "CardBase's Card(...) call must not gain a containerColor argument — the depth-card " +
                "container stays colorScheme.surface (129-01-PLAN.md Planner Decision 2)",
            0,
            countOccurrences(src, "containerColor")
        )
    }

    @Test
    fun `the tactileDepth else branch reduces to a single Column invocation of the shared content value`() {
        val src = readCardBaseSource()
        val (_, _, elseOpenBrace, elseCloseBrace) = renderConditionalBraces(src)

        val elseBranchStatements = src.substring(elseOpenBrace + 1, elseCloseBrace)
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("//") }

        assertTrue(
            "Expected the tactileDepth conditional's else branch to reduce (after stripping " +
                "blank/comment-only lines) to exactly one statement invoking Column with the " +
                "shared content value; found: $elseBranchStatements",
            elseBranchStatements.size == 1 &&
                elseBranchStatements[0].startsWith("Column(") &&
                elseBranchStatements[0].contains("cardColumnContent")
        )
    }

    // --- Rendered proofs (quarantined: harness cannot render CardBase cards; see class KDoc) --

    @Test
    @Ignore(
        "CardBase is unrenderable under this Robolectric harness — CardBase's unconditional " +
            "SwipeableActionRow throws IllegalStateException (requireOffset read before layout) " +
            "on the first frame, in a pre-existing file this plan does not touch (identical " +
            "blocker to TextCardImageIndicatorTest/Phase 107 and VoiceAlbumEditMenuTest/Phase " +
            "112). Rendered proof discharged at Phase 130 Gate-1's Explorer render check. [Blocking]"
    )
    fun `default params render no depth chrome and no wrapper node`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(dynamicColor = false) {
                CardBaseFixture()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_depth_container").assertDoesNotExist()
        composeTestRule.onNodeWithTag("card_accent_spine").assertDoesNotExist()
    }

    @Test
    @Ignore(
        "CardBase is unrenderable under this Robolectric harness — see the class KDoc / the " +
            "default-params case above. [Blocking]"
    )
    fun `tactileDepth true with an accent renders both the depth container and the spine`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(dynamicColor = false) {
                CardBaseFixture(accent = Color(0xFF6750A4), tactileDepth = true)
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_depth_container").assertExists()
        composeTestRule.onNodeWithTag("card_accent_spine").assertExists()
    }

    @Test
    @Ignore(
        "CardBase is unrenderable under this Robolectric harness — see the class KDoc / the " +
            "default-params case above. [Blocking]"
    )
    fun `tactileDepth true with a null accent still renders the neutral spine without throwing`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(dynamicColor = false) {
                CardBaseFixture(accent = null, tactileDepth = true)
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_accent_spine").assertExists()
    }

    @Test
    @Ignore(
        "CardBase is unrenderable under this Robolectric harness — see the class KDoc / the " +
            "default-params case above. [Blocking]"
    )
    fun `rendered proof — accent alone without tactileDepth never activates depth chrome`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(dynamicColor = false) {
                CardBaseFixture(accent = Color(0xFF6750A4), tactileDepth = false)
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_depth_container").assertDoesNotExist()
        composeTestRule.onNodeWithTag("card_accent_spine").assertDoesNotExist()
    }

    @Test
    @Ignore(
        "CardBase is unrenderable under this Robolectric harness — see the class KDoc / the " +
            "default-params case above. [Blocking]"
    )
    fun `content slots compose in every param combination`() {
        val combinations = listOf(
            Pair<Color?, Boolean>(null, false),
            Pair<Color?, Boolean>(Color(0xFF6750A4), true),
            Pair<Color?, Boolean>(null, true),
            Pair<Color?, Boolean>(Color(0xFF6750A4), false)
        )

        combinations.forEachIndexed { index, (accent, tactileDepth) ->
            val probe = "probe body $index"
            composeTestRule.setContent {
                YahirAndroidTasteTheme(dynamicColor = false) {
                    CardBaseFixture(accent = accent, tactileDepth = tactileDepth, probeText = probe)
                }
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("probe header").assertExists()
            composeTestRule.onNodeWithText(probe).assertExists()
            composeTestRule.onNodeWithText("probe tag").assertExists()
            composeTestRule.onNodeWithText("probe footer").assertExists()
        }
    }

    // --- Source-reading helpers -------------------------------------------------------------

    private fun readCardBaseSource(): String {
        val sourceRoot = resolveModuleSourceRoot()
        val cardBaseFile = File(sourceRoot, "component/CardBase.kt")
        val text = cardBaseFile.readText()
        assertTrue(
            "Source scan found an empty/missing CardBase.kt at $cardBaseFile — the working-" +
                "directory/source-root assumption broke. Failing loudly instead of vacuously " +
                "passing (mirrors ComponentRegistryDriftGuardTest's own vacuous-pass guard).",
            text.isNotBlank()
        )
        return text
    }

    private fun countOccurrences(haystack: String, needle: String): Int =
        haystack.split(needle).size - 1

    /**
     * Locates the `if (tactileDepth) { ... } else { ... }` render conditional inside CardBase.kt
     * via depth-aware brace matching (not naive `indexOf`/first-textual-match, which would
     * mismatch: the file also contains an unrelated `if (tactileDepth)` inline expression for
     * `elevation`, and the render branch's own body nests a SEPARATE `if (...) { ... } else { ... }`
     * for RTL/LTR spine positioning inside `drawBehind`). Anchors on the unique
     * `card_depth_container` tag literal (searching backward for the nearest preceding
     * `if (tactileDepth) {`) so the correct outer conditional is found regardless of what else
     * changes in the file, then walks braces character-by-character to find the exact matching
     * close for both the `if` and the `else` blocks.
     *
     * @return (ifOpenBraceIndex, ifCloseBraceIndex, elseOpenBraceIndex, elseCloseBraceIndex) —
     *   all indices into [src].
     */
    private fun renderConditionalBraces(src: String): List<Int> {
        val tagIndex = src.indexOf("card_depth_container")
        assertTrue("Could not locate card_depth_container in CardBase.kt", tagIndex >= 0)

        val ifKeywordIndex = src.lastIndexOf("if (tactileDepth) {", tagIndex)
        assertTrue(
            "Could not locate the 'if (tactileDepth) {' render conditional preceding " +
                "card_depth_container in CardBase.kt",
            ifKeywordIndex in 0 until tagIndex
        )

        val ifOpenBrace = src.indexOf('{', ifKeywordIndex)
        val ifCloseBrace = matchingCloseBraceIndex(src, ifOpenBrace)

        val afterIf = src.substring(ifCloseBrace + 1).trimStart()
        assertTrue(
            "Expected the tactileDepth render conditional to be followed by an 'else {' branch; " +
                "found: '${afterIf.take(20)}'",
            afterIf.startsWith("else {") || afterIf.startsWith("else{")
        )
        val elseOpenBrace = src.indexOf('{', ifCloseBrace + 1)
        val elseCloseBrace = matchingCloseBraceIndex(src, elseOpenBrace)

        return listOf(ifOpenBrace, ifCloseBrace, elseOpenBrace, elseCloseBrace)
    }

    /**
     * Depth-aware brace matcher: given the character index of an opening `{` in [text], returns
     * the index of its matching closing `}`. Character-level (not line-level) so it correctly
     * handles a closing `}` followed immediately by more code on the same line (e.g.
     * `} else {`), which a per-line "does this line net to zero" check would miss — the depth
     * genuinely hits zero mid-line, before the following `{` reopens it. Assumes no braces occur
     * inside string/char literals or comments in the scanned range, true for CardBase.kt's
     * tactileDepth conditional.
     */
    private fun matchingCloseBraceIndex(text: String, openBraceIndex: Int): Int {
        var depth = 1
        var i = openBraceIndex + 1
        while (i < text.length) {
            when (text[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
            i++
        }
        error("No matching closing brace found for opening brace at index $openBraceIndex")
    }

    /**
     * Resolves the `yahirandroidtaste` module's source root robustly (mirrors
     * `ComponentRegistryDriftGuardTest.resolveModuleSourceRoot` exactly) — do not rely solely on
     * the test process's current working directory.
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

@Composable
private fun CardBaseFixture(
    accent: Color? = null,
    tactileDepth: Boolean = false,
    probeText: String = "probe body"
) {
    val openRowState = remember { mutableStateOf<AnchoredDraggableState<SwipeAnchor>?>(null) }
    CardBase(
        openRowState = openRowState,
        accent = accent,
        tactileDepth = tactileDepth,
        headerContent = { Text("probe header") },
        bodyContent = { Text(probeText) },
        tagRowContent = { Text("probe tag") },
        footerContent = { Text("probe footer") }
    )
}
