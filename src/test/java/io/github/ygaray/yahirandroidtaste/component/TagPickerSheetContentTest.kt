package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import io.github.ygaray.yahirandroidtaste.model.TagChipUiModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [TagPickerSheetContent]'s dynamic inline create-and-select control (TAG-01,
 * D-01). Renders [TagPickerSheetContent] directly (never the [TagPickerSheet] ModalBottomSheet
 * wrapper) per this module's established Content-split testing convention, mirroring
 * [TagListItemTest]'s Robolectric + Compose harness shape.
 *
 * Task 1 covers the single end-to-end inline create-and-select path. Task 2 adds the
 * blank-confirm/regression tests; Task 3 adds the validation-error tests.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TagPickerSheetContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `confirming a non-blank name invokes onCreate exactly once with the trimmed name`() {
        val createdNames = mutableListOf<String>()
        composeTestRule.setContent {
            TagPickerSheetContent(
                allTags = emptyList(),
                onDone = {},
                onCreate = { createdNames.add(it) },
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tag_search_field").performTextInput("Groceries")
        composeTestRule.onNodeWithTag("tag_search_field").performImeAction()
        composeTestRule.waitForIdle()

        assertEquals(listOf("Groceries"), createdNames)
    }

    @Test
    fun `confirming clears the field`() {
        composeTestRule.setContent {
            TagPickerSheetContent(
                allTags = emptyList(),
                onDone = {},
                onCreate = {},
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tag_search_field").performTextInput("Groceries")
        composeTestRule.onNodeWithTag("tag_search_field").performImeAction()
        composeTestRule.waitForIdle()

        composeTestRule
            .onNode(hasTestTag("tag_search_field") and hasText("Groceries", substring = true))
            .assertDoesNotExist()
    }

    @Test
    fun `create-and-select round trip auto-selects the newly created tag`() {
        var tags by mutableStateOf(emptyList<TagChipUiModel>())
        var doneIds: List<String>? = null
        composeTestRule.setContent {
            TagPickerSheetContent(
                allTags = tags,
                onDone = { doneIds = it },
                onCreate = {},
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tag_search_field").performTextInput("Groceries")
        composeTestRule.onNodeWithTag("tag_search_field").performImeAction()
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            tags = listOf(TagChipUiModel(id = "t1", name = "Groceries", occurrenceCount = 0))
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Done").performClick()
        composeTestRule.waitForIdle()

        assertEquals(listOf("t1"), doneIds)
    }

    @Test
    fun `leading and trailing whitespace is trimmed before onCreate`() {
        val createdNames = mutableListOf<String>()
        composeTestRule.setContent {
            TagPickerSheetContent(
                allTags = emptyList(),
                onDone = {},
                onCreate = { createdNames.add(it) },
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tag_search_field").performTextInput("  Groceries  ")
        composeTestRule.onNodeWithTag("tag_search_field").performImeAction()
        composeTestRule.waitForIdle()

        assertEquals(listOf("Groceries"), createdNames)
    }
}
