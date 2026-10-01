package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.ygaray.yahirandroidtaste.model.ClarificationOptionUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [ClarificationBar] (Phase 11 Plan 02, VCLAR-01).
 *
 * Harness mirrors this module's established Robolectric+Compose harness
 * ([OutcomeSheetTest]): `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`,
 * `createComposeRule()`, direct instantiation inside `setContent`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ClarificationBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `renders the question text and one AppChip per option`() {
        composeTestRule.setContent {
            ClarificationBar(
                question = "Which list?",
                options = listOf(
                    ClarificationOptionUiModel(id = "groceries", label = "Groceries"),
                    ClarificationOptionUiModel(id = "work", label = "Work")
                ),
                onSelect = {},
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Which list?").assertExists()
        composeTestRule.onAllNodesWithTag("clarification_bar_option").assertCountEquals(2)
    }

    @Test
    fun `tapping the Nth chip invokes onSelect with exactly that option's id`() {
        var selected: String? = null
        composeTestRule.setContent {
            ClarificationBar(
                question = "Which list?",
                options = listOf(
                    ClarificationOptionUiModel(id = "groceries", label = "Groceries"),
                    ClarificationOptionUiModel(id = "work", label = "Work"),
                    ClarificationOptionUiModel(id = "home", label = "Home")
                ),
                onSelect = { selected = it },
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        val chips = composeTestRule.onAllNodesWithTag("clarification_bar_option")
        chips[1].performClick()
        composeTestRule.waitForIdle()

        assertEquals("work", selected)
    }

    @Test
    fun `tapping dismiss invokes onDismiss and never onSelect`() {
        var dismissed = false
        var selected: String? = null
        composeTestRule.setContent {
            ClarificationBar(
                question = "Which list?",
                options = listOf(ClarificationOptionUiModel(id = "groceries", label = "Groceries")),
                onSelect = { selected = it },
                onDismiss = { dismissed = true }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("clarification_bar_dismiss").performClick()
        composeTestRule.waitForIdle()

        assertEquals(true, dismissed)
        assertNull(selected)
    }
}
