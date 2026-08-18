package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [CardEditorShellContent]'s Phase 65 generalization (65-02-PLAN.md, EDIT-01):
 * the three new params — `title`, `showSaveAction`, `onRelatedClick` — and the intentional
 * D-01/D-05 action-row reorder (`trailingContent()` -> Related -> Save).
 *
 * Renders [CardEditorShellContent] directly with local fake state (Content-split, no-Hilt
 * pattern, mirrors [ListCardEditorScreenTest] in `:app`) — no ViewModel/Hilt wiring needed since
 * the component under test is a pure presentation primitive.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CardEditorShellContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun related_rendersWhenOnRelatedClickNonNull() {
        composeTestRule.setContent {
            CardEditorShellContent(
                accentColor = null,
                onSave = {},
                onNavigateBack = {},
                onRelatedClick = {}
            ) { }
        }

        composeTestRule.onNodeWithContentDescription("Related").assertExists()
    }

    @Test
    fun related_absentWhenOnRelatedClickNull() {
        composeTestRule.setContent {
            CardEditorShellContent(
                accentColor = null,
                onSave = {},
                onNavigateBack = {},
                onRelatedClick = null
            ) { }
        }

        composeTestRule.onNodeWithContentDescription("Related").assertDoesNotExist()
    }

    @Test
    fun title_rendersTextWhenNonNull() {
        composeTestRule.setContent {
            CardEditorShellContent(
                accentColor = null,
                onSave = {},
                onNavigateBack = {},
                title = "Some Album Title"
            ) { }
        }

        composeTestRule.onNodeWithText("Some Album Title").assertExists()
    }

    @Test
    fun title_noTitleTextWhenNull() {
        composeTestRule.setContent {
            CardEditorShellContent(
                accentColor = null,
                onSave = {},
                onNavigateBack = {},
                title = null
            ) { }
        }

        composeTestRule.onNodeWithText("Some Album Title").assertDoesNotExist()
    }

    @Test
    fun save_absentWhenShowSaveActionFalse() {
        composeTestRule.setContent {
            CardEditorShellContent(
                accentColor = null,
                onSave = {},
                onNavigateBack = {},
                showSaveAction = false
            ) { }
        }

        composeTestRule.onNodeWithContentDescription("Save").assertDoesNotExist()
    }

    @Test
    fun save_presentWhenShowSaveActionTrueAndNotReadOnly() {
        composeTestRule.setContent {
            CardEditorShellContent(
                accentColor = null,
                onSave = {},
                onNavigateBack = {},
                showSaveAction = true,
                readOnly = false
            ) { }
        }

        composeTestRule.onNodeWithContentDescription("Save").assertExists()
    }

    @Test
    fun actionsRow_trailingContentCoexistsWithRelated() {
        composeTestRule.setContent {
            CardEditorShellContent(
                accentColor = null,
                onSave = {},
                onNavigateBack = {},
                onRelatedClick = {},
                trailingContent = {
                    Text("TrailingMarker")
                }
            ) { }
        }

        // Presence of both the trailing-content marker and Related proves trailingContent()
        // renders alongside Related in the actions row (order is source-verified in the
        // implementation; this test proves coexistence, not pixel position).
        composeTestRule.onNodeWithText("TrailingMarker", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithContentDescription("Related").assertExists()
    }

    @Test
    fun actionsRow_orderIsTrailingContentThenRelatedThenSave() {
        // WR-04 (65-REVIEW.md): CardEditorShellContent.kt:117-122 carries an explicit
        // "Do NOT 'fix' this order back — see 65-UI-SPEC.md" comment protecting the intentional
        // D-01/D-05 reorder (trailingContent() -> Related -> Save). The existing coexistence
        // test above only proves both nodes exist, not their relative order. This test asserts
        // relative horizontal position within the actions row (a Row lays out left-to-right in
        // LTR, so x-position order is call order) so an accidental revert of the reorder fails
        // the suite.
        composeTestRule.setContent {
            CardEditorShellContent(
                accentColor = null,
                onSave = {},
                onNavigateBack = {},
                showSaveAction = true,
                readOnly = false,
                onRelatedClick = {},
                trailingContent = {
                    Text("TrailingMarker")
                }
            ) { }
        }

        val trailingX = composeTestRule
            .onNodeWithText("TrailingMarker", useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
            .left
        val relatedX = composeTestRule
            .onNodeWithContentDescription("Related", useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
            .left
        val saveX = composeTestRule
            .onNodeWithContentDescription("Save", useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
            .left

        assert(trailingX < relatedX) {
            "Expected trailingContent (x=$trailingX) to render before Related (x=$relatedX)"
        }
        assert(relatedX < saveX) {
            "Expected Related (x=$relatedX) to render before Save (x=$saveX)"
        }
    }
}
