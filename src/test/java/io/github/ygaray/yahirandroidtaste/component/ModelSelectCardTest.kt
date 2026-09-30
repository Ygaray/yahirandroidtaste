package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.ygaray.yahirandroidtaste.model.ModelOptionUiModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [ModelSelectCard] (Phase 10 Plan 02, VSET-02).
 *
 * Infra mirrors this module's established Robolectric+Compose harness ([CountBadgeTest],
 * [ProviderKeyCardTest]): `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`,
 * `createComposeRule()`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ModelSelectCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val models = listOf(
        ModelOptionUiModel(id = "gpt-4", label = "GPT-4"),
        ModelOptionUiModel(id = "claude", label = "Claude")
    )

    @Test
    fun `renders the currently-selected model label from props`() {
        composeTestRule.setContent {
            ModelSelectCard(
                models = models,
                selectedModelId = "claude",
                onModelSelected = {},
                emptyReason = "Set a provider and key first"
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Claude").assertExists()
    }

    @Test
    fun `choosing a model from the dropdown emits onModelSelected with the tapped id`() {
        var selected: String? = null
        composeTestRule.setContent {
            ModelSelectCard(
                models = models,
                selectedModelId = null,
                onModelSelected = { selected = it },
                emptyReason = "Set a provider and key first"
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("model_select_card_dropdown").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("GPT-4").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "Tapping the GPT-4 dropdown row must emit onModelSelected(\"gpt-4\")",
            "gpt-4",
            selected
        )
    }

    @Test
    fun `when models is empty renders a disabled state with the reason string, not a blank control`() {
        composeTestRule.setContent {
            ModelSelectCard(
                models = emptyList(),
                selectedModelId = null,
                onModelSelected = {},
                emptyReason = "Set a provider and key first"
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Set a provider and key first").assertExists()
        composeTestRule.onNodeWithTag("model_select_card_dropdown").assertDoesNotExist()
    }
}
