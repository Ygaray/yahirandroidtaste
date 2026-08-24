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

    // --- Non-regression matrix (Task 2): single tap, long press, gesture mutual exclusion, and
    // the isLastTag warning row. `isLastTag` is a plain, caller-supplied Boolean parameter on
    // TagChipEditorContent (TagChipEditorContent.kt:92) — it is fixed for the composition, not
    // derived from any live selection/list state inside this composable. That means Tests 8a/8b's
    // split is belt-and-braces here (the warning provably cannot vanish mid-test because the test
    // controls the value directly), not a load-bearing race guard — but the split is written
    // anyway per the plan's uniform Tests-8a/8b shape. The warning row is inside an
    // AnimatedVisibility (TagChipEditorContent.kt:113-137), so no absence assertion is made
    // anywhere in this file — only presence, and only pre-gesture (Test 8a). ---

    @Test
    fun test4_singleTap_invokesNeitherRemovalCallback_doesNotOpenMenu() {
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

        composeTestRule.onNodeWithContentDescription("Work tag").performClick()
        composeTestRule.waitForIdle()

        assertEquals("A single tap must not invoke onRemoveTag", 0, removeTagCount)
        assertEquals("A single tap must not invoke onRemoveTagNoUndo", 0, removeTagNoUndoCount)
        composeTestRule.onNodeWithText("Remove from this card").assertDoesNotExist()
    }

    @Test
    fun test5_longPress_stillOpensMenu_withRemoveFromThisCardItem() {
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
    fun test6_longPress_tappingRemoveItem_invokesOnRemoveTagOnce_withCorrectId() {
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

        composeTestRule.onNodeWithContentDescription("Work tag").performTouchInput { longClick() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Remove from this card").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "Tapping 'Remove from this card' must invoke onRemoveTag exactly once, with this " +
                "chip's own tag id — the long-press path is unchanged by the double-tap binding",
            listOf("id-1"),
            removed
        )
    }

    @Test
    fun test7_doubleTap_doesNotOpenContextMenu() {
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

        composeTestRule.onNodeWithContentDescription("Work tag").performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Remove from this card").assertDoesNotExist()
        composeTestRule.onAllNodesWithText("Delete tag everywhere").assertCountEquals(0)
    }

    @Test
    fun test8a_lastTag_warningDisplayed_beforeAnyGesture() {
        composeTestRule.setContent {
            TagChipEditorContent(
                currentTags = listOf(tag("id-1", "Work")),
                isLastTag = true,
                allTags = emptyList(),
                onRemoveTag = {},
                onAddTags = {},
                onRemoveTagNoUndo = {},
                onCreateTag = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Removing this tag will leave the card untagged")
            .assertExists()
    }

    @Test
    fun test8b_lastTag_doubleTap_stillInvokesOnRemoveTagOnce_noNewBranch() {
        val removed = mutableListOf<String>()

        composeTestRule.setContent {
            TagChipEditorContent(
                currentTags = listOf(tag("id-1", "Work")),
                isLastTag = true,
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
            "A double-tap on the last remaining tag must still route through the same " +
                "onRemoveTag callback exactly once — no new last-tag-specific double-tap " +
                "branch exists",
            listOf("id-1"),
            removed
        )
    }

    // --- Scope carve-out guard (Task 3, D-02): the read-only card-face CardTagRow is
    // deliberately excluded from this phase's double-tap binding — deferred as a fast
    // follow-up, not an oversight. This test is a drift tripwire, not behavioral evidence
    // (`<proof_scope>`): it exists to catch a future contributor silently lifting the carve-out
    // without updating 114-CONTEXT.md's Deferred Ideas entry. ---

    @Test
    fun cardTagRow_bindsNoDoubleTapGesture_D02ScopeCarveOut() {
        val src = stripComments(source("CardTagRow.kt"))

        assertEquals(
            "CardTagRow must bind no onDoubleClick gesture — this is a deliberate scope " +
                "carve-out (D-02, 114-CONTEXT.md Deferred Ideas), not an oversight. If this " +
                "carve-out is intentionally lifted, update the decision record before deleting " +
                "this guard.",
            0,
            countOccurrences(src, "onDoubleClick")
        )
    }
}
