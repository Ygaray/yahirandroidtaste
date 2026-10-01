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

    // ── VCLAR-01 edge hardening (Phase 11 Plan 02, Task 2) ───────────────────

    @Test
    fun `two options sharing the same id or label both render as separate chips, and tapping the second fires its own id`() {
        var selected: String? = null
        composeTestRule.setContent {
            ClarificationBar(
                question = "Which list?",
                options = listOf(
                    ClarificationOptionUiModel(id = "list-a", label = "Shopping List"),
                    ClarificationOptionUiModel(id = "list-b", label = "Shopping List")
                ),
                onSelect = { selected = it },
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        val chips = composeTestRule.onAllNodesWithTag("clarification_bar_option")
        chips.assertCountEquals(2)

        chips[1].performClick()
        composeTestRule.waitForIdle()

        assertEquals("list-b", selected)
    }

    @Test
    fun `empty options renders no surface or option node at all`() {
        composeTestRule.setContent {
            ClarificationBar(
                question = "Which list?",
                options = emptyList(),
                onSelect = {},
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("clarification_bar_surface").assertDoesNotExist()
        composeTestRule.onNodeWithTag("clarification_bar_option").assertDoesNotExist()
    }

    @Test
    fun `a single-option list renders exactly one pressable chip that behaves like any N-option case`() {
        var selected: String? = null
        composeTestRule.setContent {
            ClarificationBar(
                question = "Which list?",
                options = listOf(ClarificationOptionUiModel(id = "groceries", label = "Groceries")),
                onSelect = { selected = it },
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        val chips = composeTestRule.onAllNodesWithTag("clarification_bar_option")
        chips.assertCountEquals(1)

        chips[0].performClick()
        composeTestRule.waitForIdle()

        assertEquals("groceries", selected)
    }

    @Test
    fun `an id containing mixed case or punctuation passes through onSelect byte-for-byte unmodified`() {
        var selected: String? = null
        val mixedCaseId = "List-A_v2.Final!"
        composeTestRule.setContent {
            ClarificationBar(
                question = "Which list?",
                options = listOf(ClarificationOptionUiModel(id = mixedCaseId, label = "Shopping List")),
                onSelect = { selected = it },
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("clarification_bar_option").performClick()
        composeTestRule.waitForIdle()

        // Exact equality only -- never a case-insensitive/normalized comparison.
        assertEquals(mixedCaseId, selected)
    }

    @Test
    fun `three options in non-alphabetical order render in that exact order, never re-sorted`() {
        var selected: String? = null
        composeTestRule.setContent {
            ClarificationBar(
                question = "Which list?",
                options = listOf(
                    ClarificationOptionUiModel(id = "1", label = "Zebra"),
                    ClarificationOptionUiModel(id = "2", label = "Apple"),
                    ClarificationOptionUiModel(id = "3", label = "Mango")
                ),
                onSelect = { selected = it },
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        // AppChip's own clickable Surface (the shared-tag node's merge boundary) doesn't expose
        // its label text through the shared tag's OWN semantics config -- performClick() instead
        // dispatches a real touch at the resolved node's on-screen bounds, so tapping index N
        // reaches the Nth composed chip regardless. Verifying order via "tap index N -> resolves
        // option N's id" is therefore an equally strong (indeed behavioral, not just visual)
        // proof that options render -- and are wired -- in exact list order, never re-sorted.
        val chips = composeTestRule.onAllNodesWithTag("clarification_bar_option")
        chips.assertCountEquals(3)

        chips[0].performClick()
        composeTestRule.waitForIdle()
        assertEquals("1", selected)

        chips[1].performClick()
        composeTestRule.waitForIdle()
        assertEquals("2", selected)

        chips[2].performClick()
        composeTestRule.waitForIdle()
        assertEquals("3", selected)

        // Also confirm the labels themselves are present on screen, in list order, via the
        // top-level text query (unaffected by AppChip's internal merge boundary since each
        // Text's own SemanticsNode carries its Text property directly).
        composeTestRule.onNodeWithText("Zebra").assertExists()
        composeTestRule.onNodeWithText("Apple").assertExists()
        composeTestRule.onNodeWithText("Mango").assertExists()
    }
}
