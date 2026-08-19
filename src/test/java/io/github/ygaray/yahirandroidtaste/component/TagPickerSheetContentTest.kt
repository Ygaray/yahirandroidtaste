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

    // --- Task 2: blank-confirm branch + pre-existing picker-path regression coverage ---

    // De-risking probe (REVIEWS.md cycle 1, MEDIUM finding) — written and run ALONE first, before
    // any of the other Task 2 tests. BulkCreatePopup wraps its content in a Compose `Dialog`
    // (BulkCreatePopup.kt lines ~59-70), so `bulk_create_popup_name_field` lives in a second
    // window. No test in this hub or in SecondBrain has ever asserted a node inside this Dialog
    // (the only precedent, the app-side BulkCreatePopupContentTest, renders the wrapper-free
    // BulkCreatePopupContent directly). Compose's semantics finders are expected to traverse every
    // registered composition root, including extra Dialog/Popup windows, so the plain default
    // finder below is expected to work unmodified — but that is unproven in this codebase, hence
    // this standalone checked probe rather than an assumption baked into a larger test.
    @Test
    fun blankConfirm_showsBulkCreatePopup() {
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

        composeTestRule.onNodeWithTag("tag_search_field").performImeAction()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("bulk_create_popup_name_field").assertExists()
        assertEquals(emptyList<String>(), createdNames)
    }

    @Test
    fun `whitespace-only confirm shows the bulk-create popup and creates nothing`() {
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

        composeTestRule.onNodeWithTag("tag_search_field").performTextInput("   ")
        composeTestRule.onNodeWithTag("tag_search_field").performImeAction()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("bulk_create_popup_name_field").assertExists()
        assertEquals(emptyList<String>(), createdNames)
    }

    @Test
    fun `the pre-existing create-tag chip still opens the bulk-create popup`() {
        composeTestRule.setContent {
            TagPickerSheetContent(
                allTags = emptyList(),
                onDone = {},
                onCreate = {},
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("create_tag_chip").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("bulk_create_popup_name_field").assertExists()
    }

    @Test
    fun `the pre-existing tag-list selection path is unregressed`() {
        var doneIds: List<String>? = null
        composeTestRule.setContent {
            TagPickerSheetContent(
                allTags = listOf(TagChipUiModel(id = "w1", name = "Work", occurrenceCount = 0)),
                onDone = { doneIds = it },
                onCreate = {},
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Done").performClick()
        composeTestRule.waitForIdle()

        assertEquals(listOf("w1"), doneIds)
    }

    @Test
    fun `the pre-existing live filter path is unregressed`() {
        composeTestRule.setContent {
            TagPickerSheetContent(
                allTags = listOf(
                    TagChipUiModel(id = "w1", name = "Work", occurrenceCount = 0),
                    TagChipUiModel(id = "p1", name = "Personal", occurrenceCount = 0)
                ),
                onDone = {},
                onCreate = {},
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tag_search_field").performTextInput("wor")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").assertExists()
        composeTestRule.onNodeWithText("Personal").assertDoesNotExist()
    }
}
