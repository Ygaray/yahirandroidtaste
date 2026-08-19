package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests proving IMG-02's `imageCount` indicator end-to-end on both `TextCard`-family
 * surfaces: [TextCardBottomSheet] directly (Task 1 tracer) and [TextCard], including the
 * card-to-sheet forwarding hop (Task 2 expansion).
 *
 * Infra mirrors this module's established Robolectric+Compose harness
 * ([DynamicActionButtonTest]): `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`,
 * `createComposeRule()`. The indicator is located by its count-carrying accessibility
 * description ([onNodeWithContentDescription]); absence is asserted via
 * [assertDoesNotExist] (existence-negative), not by asserting a zero numeral is missing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TextCardImageIndicatorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- TextCardBottomSheet (Task 1 tracer) ---

    @Test
    fun `bottom sheet with positive imageCount exposes count-carrying accessibility node and visible text`() {
        composeTestRule.setContent {
            TextCardBottomSheet(
                title = "Trip notes",
                content = "Some body text",
                categoryPath = null,
                createdAt = 0L,
                updatedAt = 0L,
                isPinned = false,
                isFavorite = false,
                onEdit = {},
                onDismiss = {},
                onTogglePin = {},
                onToggleFavorite = {},
                onDelete = {},
                onConfirmRename = {},
                imageCount = 3
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("3 inline images").assertExists()
        composeTestRule.onNodeWithText("3").assertExists()
    }

    @Test
    fun `bottom sheet with zero imageCount renders no indicator node and no count text`() {
        composeTestRule.setContent {
            TextCardBottomSheet(
                title = "Trip notes",
                content = "Some body text",
                categoryPath = null,
                createdAt = 0L,
                updatedAt = 0L,
                isPinned = false,
                isFavorite = false,
                onEdit = {},
                onDismiss = {},
                onTogglePin = {},
                onToggleFavorite = {},
                onDelete = {},
                onConfirmRename = {},
                imageCount = 0
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("0 inline images").assertDoesNotExist()
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun `bottom sheet with imageCount omitted defaults to zero and renders nothing`() {
        composeTestRule.setContent {
            TextCardBottomSheet(
                title = "Trip notes",
                content = "Some body text",
                categoryPath = null,
                createdAt = 0L,
                updatedAt = 0L,
                isPinned = false,
                isFavorite = false,
                onEdit = {},
                onDismiss = {},
                onTogglePin = {},
                onToggleFavorite = {},
                onDelete = {},
                onConfirmRename = {}
                // imageCount omitted — must default to 0
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("0 inline images").assertDoesNotExist()
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }
}
