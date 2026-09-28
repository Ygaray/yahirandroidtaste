package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import io.github.ygaray.yahirandroidtaste.model.TagChipUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Task 2 of Phase 169-05 (TAGUI-01, Option A, `tagui01-editor-picker-color-gap`): flag-on gesture
 * parity and forwarding source contracts for [TagChipEditorContent]'s current-tag strip and
 * `RecordingBottomSheetContent`'s reuse of it. Mirrors this module's established Robolectric +
 * Compose harness ([TagChipEditorDoubleTapRemovalTest]).
 *
 * `showTagColors = true` must NOT change any gesture: tap stays a no-op, long-press still opens
 * the Edit / Remove from this card / Delete tag everywhere menu, and double-tap still invokes
 * [TagChipEditorContent]'s `onRemoveTag` exactly once and never `onRemoveTagNoUndo`. Only the
 * *visual* container fill and the `isSelected` flag change (proven at the value level by
 * [TagChipColorOptInTest]'s `editorTagChipIsSelected` cases, not re-proven here).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TagChipEditorShowTagColorsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val workColor = Color(0xFF6750A4)

    private fun coloredTags() = listOf(
        TagChipUiModel(id = "id-1", name = "Work", occurrenceCount = 0).apply { color = workColor },
        TagChipUiModel(id = "id-2", name = "Home", occurrenceCount = 0)
    )

    @Test
    fun `both current-tag chips render with showTagColors true`() {
        composeTestRule.setContent {
            TagChipEditorContent(
                currentTags = coloredTags(),
                isLastTag = false,
                allTags = emptyList(),
                onRemoveTag = {},
                onAddTags = {},
                onRemoveTagNoUndo = {},
                onCreateTag = {},
                showTagColors = true
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Work tag").assertExists()
        composeTestRule.onNodeWithContentDescription("Home tag").assertExists()
    }

    @Test
    fun `double-tap on a colored chip still invokes onRemoveTag exactly once with its own id, never onRemoveTagNoUndo`() {
        val removed = mutableListOf<String>()
        var noUndoCount = 0
        composeTestRule.setContent {
            TagChipEditorContent(
                currentTags = coloredTags(),
                isLastTag = false,
                allTags = emptyList(),
                onRemoveTag = { removed.add(it) },
                onAddTags = {},
                onRemoveTagNoUndo = { noUndoCount++ },
                onCreateTag = {},
                showTagColors = true
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Work tag").performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        assertEquals(listOf("id-1"), removed)
        assertEquals(0, noUndoCount)
    }

    @Test
    fun `long-press on a colored chip still opens the Remove from this card menu`() {
        composeTestRule.setContent {
            TagChipEditorContent(
                currentTags = coloredTags(),
                isLastTag = false,
                allTags = emptyList(),
                onRemoveTag = {},
                onAddTags = {},
                onRemoveTagNoUndo = {},
                onCreateTag = {},
                showTagColors = true
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Work tag").performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Remove from this card").assertExists()
    }

    @Test
    fun `tapping Add tag composes the forwarded picker with showTagColors on`() {
        composeTestRule.setContent {
            TagChipEditorContent(
                currentTags = coloredTags(),
                isLastTag = false,
                allTags = emptyList(),
                onRemoveTag = {},
                onAddTags = {},
                onRemoveTagNoUndo = {},
                onCreateTag = {},
                showTagColors = true
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Add tag").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Search/filter tags").assertExists()
    }

    @Test
    fun `source contracts wire the editor strip, its picker launch, and RecordingBottomSheetContent's forward`() {
        val editorSrc = SourceContractTestSupport.stripComments(
            SourceContractTestSupport.source("TagChipEditorContent.kt")
        )
        val rawEditorSrc = SourceContractTestSupport.source("TagChipEditorContent.kt")
        val stripItemRegion = rawEditorSrc.substringAfter("// region:tag-chip-item")
            .substringBefore("// endregion:tag-chip-item")
        val strippedStripItemRegion = SourceContractTestSupport.stripComments(stripItemRegion)

        assertTrue(
            "region:tag-chip-item must wire isSelected = editorTagChipIsSelected(showTagColors, tag.color)",
            strippedStripItemRegion.contains("isSelected = editorTagChipIsSelected(showTagColors, tag.color)")
        )
        assertTrue(
            "region:tag-chip-item must wire containerColorOverride = optedInTagColor(showTagColors, tag.color)",
            strippedStripItemRegion.contains("containerColorOverride = optedInTagColor(showTagColors, tag.color)")
        )

        val pickerCallBody = SourceContractTestSupport.functionBody(editorSrc, "fun TagChipEditorContent(")
        assertTrue(
            "the launched TagPickerSheet(...) call must forward showTagColors = showTagColors",
            pickerCallBody.contains("showTagColors = showTagColors")
        )

        val recordingSrc = SourceContractTestSupport.stripComments(
            SourceContractTestSupport.source("RecordingBottomSheetContent.kt")
        )
        val titleStateBody = SourceContractTestSupport.functionBody(recordingSrc, "fun TitleStateContent(")
        assertTrue(
            "the TagChipEditorContent(...) call inside RecordingBottomSheetContent.kt must forward showTagColors = showTagColors",
            titleStateBody.contains("showTagColors = showTagColors")
        )
    }
}
