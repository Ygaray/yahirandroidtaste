package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.ygaray.yahirandroidtaste.model.TagManagementUiModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [TagListItem] (GADGET-03, Phase 86).
 *
 * Covers a plain tag (default icon tint) and a home tag with a non-null color (the
 * `tag.isHome && tag.color != null` custom-tint branch).
 *
 * Infra mirrors this module's established Robolectric+Compose harness
 * ([DynamicActionButtonTest]): `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`,
 * `createComposeRule()`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TagListItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `renders a plain tag's name and card count`() {
        composeTestRule.setContent {
            TagListItem(
                tag = TagManagementUiModel(
                    id = "tag-plain",
                    name = "Groceries",
                    cardCount = 3,
                    isHome = false,
                    iconName = null,
                    color = null
                ),
                onClick = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Groceries").assertExists()
        composeTestRule.onNodeWithText("3 cards").assertExists()
    }

    @Test
    fun `renders a home tag with a custom color`() {
        composeTestRule.setContent {
            TagListItem(
                tag = TagManagementUiModel(
                    id = "tag-home",
                    name = "Home",
                    cardCount = 1,
                    isHome = true,
                    iconName = "star",
                    color = 0xFF6750A4L
                ),
                onClick = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Home").assertExists()
        composeTestRule.onNodeWithText("1 card").assertExists()
    }
}
