package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyActions
import io.github.ygaray.yahirandroidtaste.model.ProviderOptionUiModel
import io.github.ygaray.yahirandroidtaste.model.SavedKeyUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Per-provider behavior of [ProviderKeyCard] (VKEY-02, ROADMAP SC3): three providers where A and C
 * have a saved key and B does not. Labels differ from ids so an id leaking into text is caught.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProviderKeyCardPerProviderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val providers = listOf(
        ProviderOptionUiModel(id = "a", label = "Alpha"),
        ProviderOptionUiModel(id = "b", label = "Beta"),
        ProviderOptionUiModel(id = "c", label = "Gamma")
    )
    private val saved = mapOf(
        "a" to SavedKeyUiModel("fp-A-1111"),
        "c" to SavedKeyUiModel("fp-C-3333")
    )

    private var selected by mutableStateOf<String?>("a")
    private var picked: String? = null

    private fun setCard() {
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = providers,
                selectedProviderId = selected,
                onProviderSelected = { picked = it },
                keyValue = "",
                onKeyChange = {},
                keyState = KeyFieldState.Empty,
                keyLabel = "API key",
                savedKeys = saved,
                actions = ProviderKeyActions(onSave = {}, onUpdate = {}, onClear = {}, onCancelEdit = {})
            )
        }
        composeTestRule.waitForIdle()
    }

    private fun select(id: String) {
        composeTestRule.runOnIdle { selected = id }
        composeTestRule.waitForIdle()
    }

    private fun tagExists(tag: String): Boolean =
        composeTestRule.onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun `switching A to B to C shows each provider's own summary or field`() {
        setCard()

        // A: saved -> summary with A's fingerprint, no field, Update and Clear.
        composeTestRule.onNodeWithTag("provider_key_saved_summary").assertTextEquals("Saved · fp-A-1111")
        assertTrue(!tagExists("provider_key_card_key_field"))
        assertTrue(tagExists("provider_key_action_update"))
        assertTrue(tagExists("provider_key_action_clear"))

        // B: unsaved -> field with Save and Cancel, no summary.
        select("b")
        assertTrue(!tagExists("provider_key_saved_summary"))
        assertTrue(tagExists("provider_key_card_key_field"))
        assertTrue(tagExists("provider_key_action_save"))
        assertTrue(tagExists("provider_key_action_cancel"))
        assertTrue(!tagExists("provider_key_action_update"))

        // C: saved -> summary with C's own fingerprint.
        select("c")
        composeTestRule.onNodeWithTag("provider_key_saved_summary").assertTextEquals("Saved · fp-C-3333")
        assertTrue(!tagExists("provider_key_card_key_field"))
        assertTrue(!tagExists("provider_key_action_save"))
    }

    @Test
    fun `dropdown marks saved providers on the menu rows and the anchor keeps the plain label`() {
        selected = "a"
        setCard()

        // The anchor shows the plain label of the selection (an EditableText), never the marker.
        composeTestRule.onNodeWithTag("provider_key_card_dropdown").performClick()
        composeTestRule.waitForIdle()

        // A and C carry the marker on their menu rows, B stays plain.
        assertEquals(1, composeTestRule.onAllNodesWithText("Alpha · saved").fetchSemanticsNodes().size)
        assertEquals(1, composeTestRule.onAllNodesWithText("Gamma · saved").fetchSemanticsNodes().size)
        assertEquals(1, composeTestRule.onAllNodesWithText("Beta").fetchSemanticsNodes().size)
        // The plain "Alpha" appears exactly once: the anchor (the menu row is the marked one).
        assertEquals(1, composeTestRule.onAllNodesWithText("Alpha").fetchSemanticsNodes().size)
        // An id never shows as text.
        listOf("a", "b", "c").forEach {
            assertEquals("id \"$it\" must not render", 0, composeTestRule.onAllNodesWithText(it).fetchSemanticsNodes().size)
        }
    }

    @Test
    fun `choosing a marked row emits the plain provider id`() {
        selected = "b"
        setCard()

        composeTestRule.onNodeWithTag("provider_key_card_dropdown").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Gamma · saved").performClick()
        composeTestRule.waitForIdle()

        assertEquals("c", picked)
    }

    @Test
    fun `the provider dropdown does not open while a save is in flight`() {
        // The enabled control is `dropdown marks saved providers...` above: the same tap opens the menu.
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = providers,
                selectedProviderId = "a",
                onProviderSelected = { picked = it },
                keyValue = "",
                onKeyChange = {},
                keyState = KeyFieldState.Empty,
                keyLabel = "API key",
                savedKeys = saved,
                actions = ProviderKeyActions(onSave = {}, onUpdate = {}, onClear = {}, inFlight = true)
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("provider_key_card_dropdown").performClick()
        composeTestRule.waitForIdle()
        assertEquals(0, composeTestRule.onAllNodesWithText("Gamma · saved").fetchSemanticsNodes().size)
        assertEquals(1, composeTestRule.onAllNodesWithText("Alpha").fetchSemanticsNodes().size)
    }

    @Test
    fun `the reveal flag survives a summary and field flip`() {
        selected = "b"
        setCard()

        composeTestRule.onNodeWithContentDescription("Show key").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Hide key").assertExists()

        select("a")
        assertTrue(!tagExists("provider_key_card_key_field"))

        select("b")
        composeTestRule.onNodeWithContentDescription("Hide key").assertExists()
    }
}
