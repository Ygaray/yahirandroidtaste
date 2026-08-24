package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import io.github.ygaray.yahirandroidtaste.model.TagChipUiModel
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for `TagChipEditorContent`'s double-tap-to-remove binding (TAG-03, Phase 114-03).
 *
 * **De-risking probes (established hub convention, mirrors [TagChipWithContextMenuTest]'s own
 * KDoc):** [probeA_composesAndChipIsFindable] and [probeB_longPressOpensMenu_throughFullComposition]
 * are written and run ALONE first, before the production `onDoubleClick` binding exists, to prove
 * (not assume) that this specific `TagChipEditorContent` -> `ChipBar` -> `FlowRow` ->
 * `TagChipWithContextMenu` -> `AppChip` composition delivers synthesized pointer input all the way
 * down to `AppChip`'s single `combinedClickable`, rather than inheriting that guarantee from
 * [TagChipWithContextMenuTest]'s isolated one-layer harness (`114-REVIEWS.md` cycle-1 finding 5).
 * Probe A proves plain findability; Probe B drives an already-shipped gesture (long-press opening
 * the context menu) end to end through the real composition — this is runnable and expected to
 * pass before any change in this plan, which is what makes it a valid de-risking probe rather than
 * a test of new behavior.
 *
 * **Residual limit (`<proof_scope>`, restated per-file):** even with both probes green and Tests
 * 1-8b passing, this suite exercises a Robolectric-synthesized gesture — real double-tap timing
 * thresholds, touch slop, and view-system interception are only exercised on-device. The definitive
 * behavioral confirmation of TAG-03 is Phase 115's SecondBrain Gate-1; nothing in this file is
 * reported as that confirmation.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TagChipEditorDoubleTapRemovalTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun tag(id: String, name: String) =
        TagChipUiModel(id = id, name = name, occurrenceCount = 0)

    private fun source(file: String): String =
        File("src/main/java/io/github/ygaray/yahirandroidtaste/component/$file").readText()

    private fun countOccurrences(haystack: String, needle: String): Int =
        haystack.split(needle).size - 1

    /**
     * Plan 01's extended `stripComments` contract, reused verbatim here (`<proof_scope>`'s
     * comment-stripping contract): drops full-line `//` comments, `*`-prefixed KDoc continuation
     * lines, and string-literal-aware trailing inline `//` comments on code lines. The third
     * clause is load-bearing for the marker-scoped negative grep below — without it, a trailing
     * explanatory tail could carry a forbidden identifier past the gate.
     */
    private fun stripComments(src: String): String =
        src.lineSequence()
            .filterNot { line ->
                val trimmed = line.trimStart()
                trimmed.startsWith("//") || trimmed.startsWith("*")
            }
            .map { line -> stripTrailingInlineComment(line) }
            .joinToString("\n")

    private fun stripTrailingInlineComment(line: String): String {
        var inString = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '\\' && inString -> i++ // skip escaped char inside string
                c == '"' -> inString = !inString
                c == '/' && !inString && i + 1 < line.length && line[i + 1] == '/' ->
                    return line.substring(0, i)
            }
            i++
        }
        return line
    }

    /**
     * Isolates the substring between the `// region:tag-chip-item` / `// endregion:tag-chip-item`
     * marker comments added in this plan. Located on the RAW source first (the markers are
     * themselves comments), before [stripComments] is applied by the caller. Fails loudly by name
     * if either marker is missing, rather than returning an empty region that would let a negative
     * grep pass vacuously (cycle-2 review finding 2).
     */
    private fun tagChipItemRegion(src: String): String {
        val start = src.indexOf("// region:tag-chip-item")
        val end = src.indexOf("// endregion:tag-chip-item")
        require(start >= 0 && end > start) {
            "114-03: could not locate the // region:tag-chip-item / // endregion:tag-chip-item " +
                "markers — they are load-bearing anchors for " +
                "TagChipEditorDoubleTapRemovalTest, not decorative comments. Restore them around " +
                "the TagChipWithContextMenu(...) call site in TagChipEditorContent.kt."
        }
        return src.substring(start, end)
    }

    @Test
    fun probeA_composesAndChipIsFindable() {
        composeTestRule.setContent {
            TagChipEditorContent(
                currentTags = listOf(tag("id-1", "Work"), tag("id-2", "Home")),
                isLastTag = false,
                allTags = emptyList(),
                onRemoveTag = {},
                onAddTags = {},
                onRemoveTagNoUndo = {},
                onCreateTag = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Work tag").assertExists()
    }

    @Test
    fun probeB_longPressOpensMenu_throughFullComposition() {
        composeTestRule.setContent {
            TagChipEditorContent(
                currentTags = listOf(tag("id-1", "Work")),
                isLastTag = false,
                allTags = emptyList(),
                onRemoveTag = {},
                onAddTags = {},
                onRemoveTagNoUndo = {},
                onCreateTag = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Work tag").performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Remove from this card").assertExists()
    }

    @Test
    fun test1_doubleTap_invokesOnRemoveTagOnce_withCorrectId() {
        val removed = mutableListOf<String>()

        composeTestRule.setContent {
            TagChipEditorContent(
                currentTags = listOf(tag("id-1", "Work")),
                isLastTag = false,
                allTags = emptyList(),
                onRemoveTag = { removed.add(it) },
                onAddTags = {},
                onRemoveTagNoUndo = {},
                onCreateTag = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Work tag").performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "A double-tap must invoke onRemoveTag exactly once, with this chip's own tag id",
            listOf("id-1"),
            removed
        )
    }

    @Test
    fun test2_doubleTap_middleOfThreeChips_removesMiddleChipsIdOnly() {
        val removed = mutableListOf<String>()

        composeTestRule.setContent {
            TagChipEditorContent(
                currentTags = listOf(
                    tag("id-1", "Work"),
                    tag("id-2", "Home"),
                    tag("id-3", "Travel")
                ),
                isLastTag = false,
                allTags = emptyList(),
                onRemoveTag = { removed.add(it) },
                onAddTags = {},
                onRemoveTagNoUndo = {},
                onCreateTag = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Home tag").performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "Double-tapping the second of three chips must remove only the second chip's id " +
                "(stale-capture guard)",
            listOf("id-2"),
            removed
        )
    }

    @Test
    fun test3_doubleTap_neverInvokesOnRemoveTagNoUndo() {
        var removeTagCount = 0
        var removeTagNoUndoCount = 0

        composeTestRule.setContent {
            TagChipEditorContent(
                currentTags = listOf(tag("id-1", "Work")),
                isLastTag = false,
                allTags = emptyList(),
                onRemoveTag = { removeTagCount++ },
                onAddTags = {},
                onRemoveTagNoUndo = { removeTagNoUndoCount++ },
                onCreateTag = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Work tag").performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "The undo-backed onRemoveTag must fire exactly once",
            1,
            removeTagCount
        )
        assertEquals(
            "The picker-only no-undo path must stay unreachable from a chip double-tap " +
                "(PITFALLS Pitfall 8)",
            0,
            removeTagNoUndoCount
        )
    }

    // --- Structural drift guards (`<proof_scope>`): prove source shape, not runtime behavior.
    // Test 3 above is the behavioral, load-bearing gate; these are the cheap, permanent
    // tripwires against the wiring quietly drifting back onto the wrong callback or a second
    // gesture detector creeping in. ---

    @Test
    fun sourceGuard_markerScopedRegion_hasExactlyOneOnDoubleClick_andZeroOnRemoveTagNoUndo() {
        val src = source("TagChipEditorContent.kt")
        val region = stripComments(tagChipItemRegion(src))

        assertEquals(
            "The // region:tag-chip-item marker-scoped region must bind onDoubleClick exactly " +
                "once",
            1,
            countOccurrences(region, "onDoubleClick")
        )
        assertEquals(
            "The // region:tag-chip-item marker-scoped region must never reference the " +
                "picker-only onRemoveTagNoUndo callback (PITFALLS Pitfall 8)",
            0,
            countOccurrences(region, "onRemoveTagNoUndo")
        )
    }

    @Test
    fun sourceGuard_noSecondGestureDetector_pointerInputOrDetectTapGestures() {
        val src = stripComments(source("TagChipEditorContent.kt"))

        assertEquals(
            "TagChipEditorContent.kt must not add a pointerInput gesture detector — the " +
                "existing AppChip combinedClickable already routes tap/long-press/double-tap " +
                "(PITFALLS Pitfall 9)",
            0,
            countOccurrences(src, "pointerInput")
        )
        assertEquals(
            "TagChipEditorContent.kt must not add a detectTapGestures gesture detector",
            0,
            countOccurrences(src, "detectTapGestures")
        )
    }
}
